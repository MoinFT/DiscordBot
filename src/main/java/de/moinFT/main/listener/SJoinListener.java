package de.moinFT.main.listener;

import de.moinFT.main.Functions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.listener.server.ServerJoinListener;

import static de.moinFT.main.Main.*;
import static de.moinFT.main.Functions.*;


public class SJoinListener implements ServerJoinListener {

    private Server Server = null;
    private long ServerID = 0;

    @Override
    public void onServerJoin(ServerJoinEvent event){
        Server = event.getServer();
        ServerID = Server.getId();

        Functions.addServerToDB(Server);

        for (int i = 0; i < DBServer.count(); i++) {
            if(DBServer.getServer(i).getServerID() == ServerID){
                compareDBUser_WithGuildUser(i);
                compareDBRole_WithGuildRole(i);
                compareDBChannel_WithGuildChannel(i);
            }
        }
    }
}
