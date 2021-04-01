package fking.work.chatlogger;

import lombok.ToString;
import net.runelite.api.events.ChatMessage;

import java.time.Clock;
import java.time.ZonedDateTime;

@ToString
public class ChatEntry {

    private final ZonedDateTime timestamp;
    private final String friendChat;
    private final String sender;
    private final String message;

    private ChatEntry(String friendChat, String sender, String message) {
        this.timestamp = ZonedDateTime.now(Clock.systemUTC());
        this.friendChat = friendChat;
        this.sender = sender;
        this.message = message;
    }

    public static ChatEntry from(String friendsChatOwner, ChatMessage chatMessage) {
        return new ChatEntry(friendsChatOwner, chatMessage.getName(), chatMessage.getMessage());
    }
}
