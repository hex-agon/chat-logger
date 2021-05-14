package fking.work.chatlogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.LoggerFactory;

@Slf4j
@PluginDescriptor(name = "Chat Logger", description = "Logs chat messages to a file")
public class ChatLoggerPlugin extends Plugin {

    private static final String BASE_DIRECTORY = System.getProperty("user.home") + "/.runelite/chatlogs/";

    @Inject
    private ChatLoggerConfig config;

    @Inject
    private Client client;

    private RemoteSubmitter remoteSubmitter;
    private Logger publicChatLogger;
    private Logger privateChatLogger;
    private Logger friendsChatLogger;

    @Provides
    ChatLoggerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ChatLoggerConfig.class);
    }

    @Override
    protected void startUp() {
        publicChatLogger = setupLogger("PublicChatLogger", "public");
        privateChatLogger = setupLogger("PrivateChatLogger", "private");
        friendsChatLogger = setupLogger("FriendsChatLogger", "friends");
        startRemoteSubmitter();
    }

    @Override
    protected void shutDown() {
        shutdownRemoteSubmitter();
    }

    private void startRemoteSubmitter() {
        if (config.remoteSubmitLogFriendsChat()) {

            if (remoteSubmitter != null) {
                log.debug("Shutting down previous remoteSubmitter...");
                shutdownRemoteSubmitter();
            }
            log.debug("Starting a new remoteSubmitter...");
            remoteSubmitter = RemoteSubmitter.create(config);
            remoteSubmitter.initialize();
        }
    }

    private void shutdownRemoteSubmitter() {
        if (remoteSubmitter != null) {
            remoteSubmitter.shutdown();
            remoteSubmitter = null;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!ChatLoggerConfig.GROUP_NAME.equals(event.getGroup())) {
            return;
        }
        startRemoteSubmitter();
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        switch (event.getType()) {

            case FRIENDSCHAT:
                long messageId = CrossWorldMessages.latestId(client);
                log.info("[{}] {}: {}, world={}, sequence={}", event.getSender(), event.getName(), event.getMessage(), messageId >> 32, messageId & 0xFFFFFFFFL);
                if (config.logFriendsChat()) {
                    friendsChatLogger.info("[{}] {}: {}", event.getSender(), event.getName(), event.getMessage());
                }

                if (config.remoteSubmitLogFriendsChat() && remoteSubmitter != null) {
                    FriendsChatManager friendsChatManager = client.getFriendsChatManager();

                    if (friendsChatManager == null) {
                        return;
                    }
                    String owner = friendsChatManager.getOwner();
                    remoteSubmitter.queue(ChatEntry.from(messageId, owner, event));
                }
                break;
            case PRIVATECHAT:
            case MODPRIVATECHAT:
            case PRIVATECHATOUT:
                if (config.logPrivateChat()) {
                    privateChatLogger.info("{}: {}", event.getName(), event.getMessage());
                }
                break;
            case MODCHAT:
            case PUBLICCHAT:
                if (config.logPublicChat()) {
                    publicChatLogger.info("{}: {}", event.getName(), event.getMessage());
                }
                break;
        }
    }

    private Logger setupLogger(String loggerName, String subFolder) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss} %msg%n");
        encoder.start();

        String directory = BASE_DIRECTORY + subFolder + "/";

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setFile(directory + "latest.log");
        appender.setAppend(true);
        appender.setEncoder(encoder);
        appender.setContext(context);

        TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
        logFilePolicy.setContext(context);
        logFilePolicy.setParent(appender);
        logFilePolicy.setFileNamePattern(directory + "chatlog_%d{yyyy-MM-dd}.log");
        logFilePolicy.setMaxHistory(30);
        logFilePolicy.start();

        appender.setRollingPolicy(logFilePolicy);
        appender.start();

        Logger logger = context.getLogger(loggerName);
        logger.detachAndStopAllAppenders();
        logger.setAdditive(false);
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);

        return logger;
    }
}
