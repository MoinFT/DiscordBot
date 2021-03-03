package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.listener.server.ServerLeaveListener;

import static de.moinFT.main.Main.*;

public class SLeaveListener implements ServerLeaveListener {

    private Server Server = null;
    private long ServerID = 0;

    @Override
    public void onServerLeave(ServerLeaveEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();

        DatabaseConnection.DBDeleteItem("server", DBServer.getDB_ID(ServerID));
        DatabaseConnection.DB_SQL_Execute("DROP TABLE " + ServerID +  "_Channel");
        DatabaseConnection.DB_SQL_Execute("DROP TABLE " + ServerID +  "_Role");
        DatabaseConnection.DB_SQL_Execute("DROP TABLE " + ServerID +  "_User");
        DBServer.delete(DBServer.getID(ServerID));
    }
}
