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
            name = "Friends Chat (Clan Chat)",
            description = "Enables logging of the friends chat"
    )
    default boolean logFriendsChat() {
        return true;
    }

    @ConfigItem(
            keyName = "remotelogfriends",
            name = "Remote Friends Chat (Clan Chat)",
            description = "Enables remote submission of the friends chat",
            section = sectionRemote
    )
    default boolean remoteSubmitLogFriendsChat() {
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
