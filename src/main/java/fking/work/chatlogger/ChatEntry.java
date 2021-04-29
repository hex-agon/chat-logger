package fking.work.chatlogger;

import lombok.ToString;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.util.Text;

import java.time.Clock;
import java.time.ZonedDateTime;

@ToString
public class ChatEntry {

    private final ZonedDateTime timestamp;
    private final long id;
    private final String friendChat;
    private final String sender;
    private final String message;

    private ChatEntry(long id, String friendChat, String sender, String message) {
        this.id = id;
        this.timestamp = ZonedDateTime.now(Clock.systemUTC());
        this.friendChat = friendChat;
        this.sender = sender;
        this.message = message;
    }

    public static ChatEntry from(long messageId, String friendsChatOwner, ChatMessage chatMessage) {
        return new ChatEntry(messageId, Text.standardize(friendsChatOwner), Text.standardize(chatMessage.getName()), chatMessage.getMessage());
    }
}
