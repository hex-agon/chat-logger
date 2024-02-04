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
            name = "Remote Submission",
            description = "Settings for remote submission of chat messages",
            position = 0,
            closedByDefault = true
    )
    String sectionRemote = "remote";

    @ConfigItem(
            keyName = "public",
            name = "Public Chat",
            description = "Enables logging of the public chat"
    )
    default boolean logPublicChat() {
        return false;
    }

    @ConfigItem(
            keyName = "private",
            name = "Private Chat",
            description = "Enables logging of the private chat"
    )
    default boolean logPrivateChat() {
        return false;
    }

    @ConfigItem(
            keyName = "friends",
            name = "Friends Chat (Channel Chat)",
            description = "Enables logging of the friends chat"
    )
    default boolean logFriendsChat() {
        return true;
    }

    @ConfigItem(
            keyName = "remotelogfriends",
            name = "Remote Friends Chat (Channel Chat)",
            description = "Enables remote submission of the friends chat",
            section = sectionRemote
    )
    default boolean remoteSubmitLogFriendsChat() {
        return false;
    }

    @ConfigItem(
            keyName = "clan",
            name = "Clan Chat",
            description = "Enables logging of the clan chat"
    )
    default boolean logClanChat() {
        return true;
    }
    
    @ConfigItem(
            keyName = "group_iron",
            name = "Group Iron Chat",
            description = "Enables logging of the group iron-man chat"
    )
    default boolean logGroupChat() {
        return true;
    }

    @ConfigItem(
            keyName = "remoteloggroup",
            name = "Remote Group Chat",
            description = "Enables remote submission of the group iron-man chat",
            section = sectionRemote
    )
    default boolean remoteSubmitLogGroupChat() {
        return false;
    }
    
    @ConfigItem(
            keyName = "archive_count",
            name = "Archive Count",
            description = "Number of archived days of chat to save (0 for infinite)"
    )
    default int archiveCount() {
        return 30;
    }

    @ConfigItem(
            keyName = "remotelogclan",
            name = "Remote Clan Chat",
            description = "Enables remote submission of the clan chat",
            section = sectionRemote
    )
    default boolean remoteSubmitLogClanChat() {
        return false;
    }

    @ConfigItem(
            keyName = "remoteendpoint",
            name = "Endpoint",
            description = "The endpoint that messages will be submitted to",
            section = sectionRemote
    )
    default String remoteEndpoint() {
        return null;
    }

    @ConfigItem(
            keyName = "remoteauthorization",
            name = "Authorization",
            description = "The Authorization header value",
            section = sectionRemote
    )
    default String remoteEndpointAuthorization() {
        return null;
    }
}
