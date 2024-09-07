package fking.work.chatlogger;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provides;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import fking.work.chatlogger.ChatEntry.ChatType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import okhttp3.OkHttpClient;

@Slf4j
@PluginDescriptor(name = "Chat Logger", description = "Logs chat messages to a file")
public class ChatLoggerPlugin extends Plugin {

    private static final String BASE_DIRECTORY = RuneLite.RUNELITE_DIR + "/chatlogs/";
    private static final int CHANNEL_UNRANKED = -2;

    @Inject
    private ChatLoggerConfig config;

    @Inject
    private Client client;

    @Inject
    private OkHttpClient httpClient;

    @Inject
    private Gson gson;

    private RemoteSubmitter remoteSubmitter;
    private Logger publicChatLogger;
    private Logger privateChatLogger;
    private Logger friendsChatLogger;
    private Logger clanChatLogger;
    private Logger groupChatLogger;
    private Logger gameChatLogger;

    private boolean can_load = false;

    @Provides
    ChatLoggerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ChatLoggerConfig.class);
    }

    @Override
    protected void startUp() {
        startRemoteSubmitter();
        // If plugin enabled while logged in
        if(client.getGameState().equals(GameState.LOGGED_IN)){
            triggerInit();
        }
    }

    @Override
    protected void shutDown() {
        shutdownRemoteSubmitter();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState().equals(GameState.LOGGED_IN)) {
            // SO this actually fires BEFORE the player is fully logged in.. sooo we know it
            // is about to happen
            triggerInit();
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        // If we are logging per player, wait until we have the player login name
        if (can_load && (!config.logChatPerUser() || client.getLocalPlayer().getName() != null)) {
            initLoggers();
            can_load = false;
        }
    }

    private void triggerInit() {
        can_load = true;
    }

    private void initLoggers() {
        publicChatLogger = setupLogger("PublicChatLogger", "public");
        privateChatLogger = setupLogger("PrivateChatLogger", "private");
        friendsChatLogger = setupLogger("FriendsChatLogger", "friends");
        clanChatLogger = setupLogger("ClanChatLogger", "clan");
        groupChatLogger = setupLogger("GroupChatLogger", "group");
        gameChatLogger = setupLogger("GameChatLogger", "game");
    }

    private void startRemoteSubmitter() {
        if (config.remoteSubmitLogFriendsChat() || config.remoteSubmitLogClanChat()) {

            if (remoteSubmitter != null) {
                log.debug("Shutting down previous remoteSubmitter...");
                shutdownRemoteSubmitter();
            }
            log.debug("Starting a new remoteSubmitter...");
            remoteSubmitter = RemoteSubmitter.create(config, httpClient, gson);
            remoteSubmitter.initialize();
        }
    }

    private void shutdownRemoteSubmitter() {
        if (remoteSubmitter != null) {
            remoteSubmitter.shutdown();
            remoteSubmitter = null;
        }
    }

    private int friendsChatMemberRank(String name) {
        FriendsChatManager friendsChatManager = client.getFriendsChatManager();
        if (friendsChatManager != null) {
            FriendsChatMember member = friendsChatManager.findByName(Text.removeTags(name));
            return member != null ? member.getRank().getValue() : CHANNEL_UNRANKED;
        }
        return CHANNEL_UNRANKED;
    }

    private int clanChannelMemberRank(String name, String clanName) {
        String cleanName = Text.removeTags(name);
        ClanChannel clanChannel = client.getClanChannel();

        if (clanChannel != null) {
            ClanChannelMember member = clanChannel.findMember(cleanName);
            if (member != null && clanChannel.getName().equals(clanName)) {
                return member.getRank().getRank();
            }
        }
        clanChannel = client.getGuestClanChannel();

        if (clanChannel != null) {
            ClanChannelMember member = clanChannel.findMember(cleanName);
            if (member != null && clanChannel.getName().equals(clanName)) {
                return member.getRank().getRank();
            }
        }
        return CHANNEL_UNRANKED;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!ChatLoggerConfig.GROUP_NAME.equals(event.getGroup())) {
            return;
        }
        // If we need to reload loggers
        if (event.getKey().equals("per_user") || event.getKey().equals("archive_count")) {
            triggerInit();
        }
        startRemoteSubmitter();
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        switch (event.getType()) {
            case CLAN_GIM_CHAT:
            case CLAN_GIM_MESSAGE:
            case CLAN_GIM_FORM_GROUP:
            case CLAN_GIM_GROUP_WITH:
                if (config.logGroupChat()) {
                    if (event.getType() == ChatMessageType.CLAN_GIM_MESSAGE) {
                        groupChatLogger.info("{}", event.getMessage());
                    } else {
                        groupChatLogger.info("{}: {}", event.getName(), event.getMessage());
                    }
                }
                
                if (config.remoteSubmitLogGroupChat() && remoteSubmitter != null) {
                    submitToRemote("groupiron", event, CHANNEL_UNRANKED);
                }

            case FRIENDSCHAT:
                if (config.logFriendsChat()) {
                    friendsChatLogger.info("[{}] {}: {}", event.getSender(),nameFormatting(event.getName()), event.getMessage());
                }

                if (config.remoteSubmitLogFriendsChat() && remoteSubmitter != null) {
                    FriendsChatManager friendsChatManager = client.getFriendsChatManager();

                    if (friendsChatManager == null) {
                        return;
                    }
                    String owner = friendsChatManager.getOwner();
                    submitToRemote(owner, event, friendsChatMemberRank(event.getName()));
                }
                break;

            case GAMEMESSAGE:
                if (config.logGameChat()) {
                    gameChatLogger.info(event.getMessage());
                }

                break;
            case CLAN_CHAT:
            case CLAN_GUEST_CHAT:
            case CLAN_MESSAGE:
                if (config.logClanChat()) {
                    if (event.getType() == ChatMessageType.CLAN_MESSAGE) {
                        clanChatLogger.info("{}", event.getMessage());
                    } else {
                        clanChatLogger.info("{}: {}", nameFormatting(event.getName()), event.getMessage());
                    }
                }

                if (config.remoteSubmitLogClanChat() && remoteSubmitter != null) {
                    ClanChannel clanChannel = event.getType() == ChatMessageType.CLAN_CHAT || event.getType() == ChatMessageType.CLAN_MESSAGE ? client.getClanChannel() : client.getGuestClanChannel();

                    if (clanChannel == null) {
                        return;
                    }
                    String chatName = clanChannel.getName();
                    submitToRemote(chatName, event, clanChannelMemberRank(event.getName(), chatName));
                }
                break;
            case PRIVATECHAT:
            case MODPRIVATECHAT:
            case PRIVATECHATOUT:
                if (config.logPrivateChat()) {
                    String predicate = event.getType() == ChatMessageType.PRIVATECHATOUT ? "To" : "From";
                    privateChatLogger.info("{} {}: {}", predicate, nameFormatting(event.getName()), event.getMessage());
                }
                break;
            case MODCHAT:
            case PUBLICCHAT:
                if (config.logPublicChat()) {
                    publicChatLogger.info("{}: {}", nameFormatting(event.getName()), event.getMessage());
                }
                break;
        }
    }

    private void submitToRemote(String channelName, ChatMessage event, int rank) {
        long messageId = CrossWorldMessages.latestId(client);
        remoteSubmitter.queue(ChatEntry.from(messageId, ChatType.CLAN, channelName, rank, event));
    }

    private Logger setupLogger(String loggerName, String subFolder) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss} %msg%n");
        encoder.start();

        String directory = BASE_DIRECTORY;

        if (config.logChatPerUser()) {
            directory += client.getLocalPlayer().getName() + "/";
        }

        directory += subFolder + "/";

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setFile(directory + "latest.log");
        appender.setAppend(true);
        appender.setEncoder(encoder);
        appender.setContext(context);

        TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
        logFilePolicy.setContext(context);
        logFilePolicy.setParent(appender);
        logFilePolicy.setFileNamePattern(directory + "chatlog_%d{yyyy-MM-dd}.log");
        logFilePolicy.setMaxHistory(config.archiveCount());
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

    private String nameFormatting(String name){
        return Text.removeFormattingTags(Text.toJagexName(name));
    }
}
