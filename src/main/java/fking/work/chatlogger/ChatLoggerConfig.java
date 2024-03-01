package fking.work.chatlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import static fking.work.chatlogger.ChatLoggerConfig.*;

@ConfigGroup(GROUP_NAME)
public interface ChatLoggerConfig extends Config {

    String GROUP_NAME = "chatlogger";

    @ConfigSection(
        name = "Channel Select", 
        description = "Enable/Disable chat channels", 
        position = 0
    )
    String CHANNEL_SECT = "Channel Select";

    @ConfigSection(
        name = "Logging Options", 
        description = "Change Logging behaviour", 
        position = 10
    )
    String LOGGING_SECT = "Logging Options";

    @ConfigSection(
            name = "Remote Submission",
            description = "Settings for remote submission of chat messages",
            position = 20,
            closedByDefault = true
    )
    String REMOTE_SECT = "remote";

    // Channel Select
    @ConfigItem(
        section = CHANNEL_SECT, 
        keyName = "game", 
        name = "Game Chat", 
        description = "Enable game chat logging"
    )
    default boolean logGameChat() {
        return false;
    }

    @ConfigItem(
        section = CHANNEL_SECT, 
        keyName = "public",
        name = "Public Chat",
        description = "Enables logging of the public chat"
    )
    default boolean logPublicChat() {
        return false;
    }

    @ConfigItem(
        section = CHANNEL_SECT, 
        keyName = "private",
        name = "Private Chat",
        description = "Enables logging of the private chat"
    )
    default boolean logPrivateChat() {
        return true;
    }

    @ConfigItem(
        section = CHANNEL_SECT, 
        keyName = "friends",
        name = "Friends Chat (Channel Chat)",
        description = "Enables logging of the friends chat"
    )
    default boolean logFriendsChat() {
        return true;
    }

    @ConfigItem(
        section = CHANNEL_SECT, 
        keyName = "clan",
        name = "Clan Chat",
        description = "Enables logging of the clan chat"
    )
    default boolean logClanChat() {
        return true;
    }
    
    @ConfigItem(
        section = CHANNEL_SECT, 
        keyName = "group_iron",
        name = "Group Iron Chat",
        description = "Enables logging of the group iron-man chat"
    )
    default boolean logGroupChat() {
        return true;
    }


    // Logging Config
    
    @ConfigItem(
        section = LOGGING_SECT, 
        keyName = "per_user", 
        name = "Folder Per User", 
        description = "Splits chats up into folders per logged in user"
    )
    default boolean logChatPerUser() {
        return true;
    }

    @ConfigItem(
        section = LOGGING_SECT, 
        keyName = "archive_count", 
        name = "Archive Count", 
        description = "Number of archived days of chat to save (0 for infinite)"
    )
    default int archiveCount() {
        return 30;
    }

    // Remote Submission
    @ConfigItem(
            keyName = "remotelogfriends",
            name = "Remote Friends Chat (Channel Chat)",
            description = "Enables remote submission of the friends chat",
            section = REMOTE_SECT
    )
    default boolean remoteSubmitLogFriendsChat() {
        return false;
    }

    @ConfigItem(
        section = REMOTE_SECT,
        keyName = "remoteloggroup",
        name = "Remote Group Chat",
        description = "Enables remote submission of the group iron-man chat"
    )
    default boolean remoteSubmitLogGroupChat() {
        return false;
    }

    @ConfigItem(
        section = REMOTE_SECT,
        keyName = "remotelogclan",
        name = "Remote Clan Chat",
        description = "Enables remote submission of the clan chat"
    )
    default boolean remoteSubmitLogClanChat() {
        return false;
    }

    @ConfigItem(
        section = REMOTE_SECT,
        keyName = "remoteendpoint",
        name = "Endpoint",
        description = "The endpoint that messages will be submitted to"
    )
    default String remoteEndpoint() {
        return null;
    }

    @ConfigItem(
        section = REMOTE_SECT,
        keyName = "remoteauthorization",
        name = "Authorization",
        description = "The Authorization header value"
    )
    default String remoteEndpointAuthorization() {
        return null;
    }
}
