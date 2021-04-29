package fking.work.chatlogger;

import lombok.experimental.UtilityClass;
import net.runelite.api.Client;

@UtilityClass
public class CrossWorldMessages {

    long latestId(Client client) {
        long[] crossWorldMessageIds = client.getCrossWorldMessageIds();
        int index = client.getCrossWorldMessageIdsIndex() - 1;
        if (index == -1) {
            index = crossWorldMessageIds.length - 1;
        }
        return crossWorldMessageIds[index];
    }
}
