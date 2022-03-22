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
    private final ChatType chatType;
    private final String chatName;
    private final String sender;
    private final String message;

    private ChatEntry(long id, ChatType chatType, String chatName, String sender, String message) {
        this.id = id;
        this.chatType = chatType;
        this.timestamp = ZonedDateTime.now(Clock.systemUTC());
        this.chatName = chatName;
        this.sender = sender;
        this.message = message;
    }

    public static ChatEntry from(long messageId, ChatType chatType, String chatName, ChatMessage chatMessage) {
        String sender = chatMessage.getName().isEmpty() ? chatName : Text.removeFormattingTags(chatMessage.getName());
        return new ChatEntry(messageId, chatType, Text.standardize(chatName), sender, chatMessage.getMessage());
    }

    public enum ChatType {
        FRIENDS,
        CLAN
    }
}
