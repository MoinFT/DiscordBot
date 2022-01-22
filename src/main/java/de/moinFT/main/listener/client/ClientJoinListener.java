package de.moinFT.main.listener.client;

import de.moinFT.main.Functions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.listener.server.ServerJoinListener;

import static de.moinFT.main.Main.*;
import static de.moinFT.main.Functions.*;

public class ClientJoinListener implements ServerJoinListener {

    @Override
    public void onServerJoin(ServerJoinEvent event){
        Server Server = event.getServer();
        long ServerID = Server.getId();

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
