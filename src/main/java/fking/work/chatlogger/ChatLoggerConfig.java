package fking.work.chatlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("chatlogger")
public interface ChatLoggerConfig extends Config {

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
}
