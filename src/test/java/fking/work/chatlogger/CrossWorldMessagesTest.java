package fking.work.chatlogger;

import net.runelite.api.Client;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.mockito.Mockito.*;

@TestInstance(Lifecycle.PER_CLASS)
public class CrossWorldMessagesTest {

    private static final int ORIGINAL_ARRAY_SIZE = 100;

    @Test
    public void latest_id_test() {
        Client clientMock = mock(Client.class);
        long expected = 123456;
        long[] messageIds = new long[ORIGINAL_ARRAY_SIZE];
        messageIds[0] = expected;

        when(clientMock.getCrossWorldMessageIds()).thenReturn(messageIds);
        when(clientMock.getCrossWorldMessageIdsIndex()).thenReturn(1);

        long latestId = CrossWorldMessages.latestId(clientMock);

        Assertions.assertEquals(expected, latestId);
    }

    @Test
    public void latest_id_rollover_test() {
        Client clientMock = mock(Client.class);
        long expected = 123456;
        long[] messageIds = new long[ORIGINAL_ARRAY_SIZE];
        messageIds[ORIGINAL_ARRAY_SIZE - 1] = expected;

        when(clientMock.getCrossWorldMessageIds()).thenReturn(messageIds);
        when(clientMock.getCrossWorldMessageIdsIndex()).thenReturn(0);

        long latestId = CrossWorldMessages.latestId(clientMock);

        Assertions.assertEquals(expected, latestId);
    }
}
