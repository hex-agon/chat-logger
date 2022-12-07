package fking.work.chatlogger;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RemoteSubmitter {

    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json");
    private static final int MAX_ENTRIES_PER_TICK = 30;
    private static final int TICK_INTERVAL = 5;

    private static final CircuitBreaker<Object> BREAKER = new CircuitBreaker<>()
            .handle(IOException.class)
            .withDelay(Duration.ofMinutes(5))
            .withFailureThreshold(3, Duration.ofSeconds(30))
            .onHalfOpen(RemoteSubmitter::onHalfOpen);

    private final ConcurrentLinkedDeque<ChatEntry> queuedEntries = new ConcurrentLinkedDeque<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final ChatLoggerConfig config;
    private final OkHttpClient okHttpClient;
    private final Gson gson;

    private RemoteSubmitter(ChatLoggerConfig config, OkHttpClient okHttpClient, Gson gson) {
        this.config = config;
        this.okHttpClient = okHttpClient;
        this.gson = gson.newBuilder()
                        .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
                        .create();
    }

    public static RemoteSubmitter create(ChatLoggerConfig config, OkHttpClient okHttpClient, Gson gson) {
        return new RemoteSubmitter(config, okHttpClient, gson);
    }

    private static void onHalfOpen() {
        log.info("Checking if remote host is answering properly again (HALF_OPEN)");
    }

    public void initialize() {
        executorService.scheduleAtFixedRate(this::processQueue, TICK_INTERVAL, TICK_INTERVAL, TimeUnit.SECONDS);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void queue(ChatEntry entry) {
        queuedEntries.add(entry);
    }

    private void processQueue() {

        if (queuedEntries.isEmpty()) {
            return;
        }
        RequestBody payload = buildPayload();

        try {
            Failsafe.with(BREAKER).run(() -> {
                String authorization = config.remoteEndpointAuthorization();

                if (authorization == null || authorization.trim().isEmpty()) {
                    authorization = "none";
                }
                Request request = new Builder()
                        .url(config.remoteEndpoint())
                        .addHeader("Authorization", authorization)
                        .post(payload)
                        .build();

                try (Response response = okHttpClient.newCall(request).execute()) {

                    if (!response.isSuccessful()) {
                        log.warn("Remote endpoint returned non successful response, responseCode={}", response.code());
                    }
                }
            });
        } catch (Exception e) {
            if (!BREAKER.isOpen()) {
                log.warn("Failed to submit chat entries: {}", e.getMessage());
            }
        }
    }

    private RequestBody buildPayload() {
        List<ChatEntry> entries = new ArrayList<>();
        int count = 0;

        while (!queuedEntries.isEmpty() && count < MAX_ENTRIES_PER_TICK) {
            entries.add(queuedEntries.poll());
            count++;
        }
        return RequestBody.create(APPLICATION_JSON, gson.toJson(entries));
    }
}
