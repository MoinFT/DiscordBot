package de.moinFT.main.listener.client;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.listener.server.ServerLeaveListener;

import static de.moinFT.main.Main.*;

public class ClientLeaveListener implements ServerLeaveListener {

    @Override
    public void onServerLeave(ServerLeaveEvent event) {
        Server server = event.getServer();
        long serverID = server.getId();

        DatabaseConnection.SQL_Execute("DELETE FROM server WHERE serverID = " + serverID);
        DatabaseConnection.SQL_Execute("DELETE FROM channel WHERE serverID = " + serverID);
        DatabaseConnection.SQL_Execute("DELETE FROM role WHERE serverID = " + serverID);
        DatabaseConnection.SQL_Execute("DELETE FROM user WHERE serverID = " + serverID);
        DBServer.delete(DBServer.getServer(serverID).getID());
    }
}
