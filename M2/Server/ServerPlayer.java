package M2.Server;

import M2.Common.Constants;
import M2.Common.Phase;
import M2.Common.Player;
import M2.Common.TextFX;
import M2.Common.TextFX.Color;

public class ServerPlayer extends Player {
    private ServerThread client;

    public ServerPlayer(ServerThread t) {
        client = t;
        System.out.println(TextFX.colorize("Wrapped ServerThread " + t.getClientName(), Color.CYAN));
    }

    public long getClientId() {
        if (client == null) {
            return Constants.DEFAULT_CLIENT_ID;
        }
        return client.getClientId();
    }

    public String getClientName() {
        if (client == null) {
            return "";
        }
        return client.getClientName();
    }

    public void sendPhase(Phase phase) {
        if (client == null) {
            return;
        }
        client.sendPhase(phase.name());
    }

    public void sendReadyState(long clientId, boolean isReady) {
        if (client == null) {
            return;
        }
        client.sendReadyState(clientId, isReady);
    }
}
