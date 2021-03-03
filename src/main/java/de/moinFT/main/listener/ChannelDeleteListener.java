package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.ServerChannelDeleteEvent;
import org.javacord.api.listener.channel.server.ServerChannelDeleteListener;

import static de.moinFT.main.Main.DBServer;

public class ChannelDeleteListener implements ServerChannelDeleteListener {

    private Server Server = null;
    private long ServerID = 0;
    private Channel Channel = null;
    private long ChannelID = 0;

    @Override
    public void onServerChannelDelete(ServerChannelDeleteEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        Channel = event.getChannel();
        ChannelID = Channel.getId();

        DatabaseConnection.DBDeleteItem(ServerID + "_Channel", DBServer.getServer(ServerID).getChannels().getDB_ID(ChannelID));
        DBServer.getServer(ServerID).getChannels().delete(ChannelID);
    }
}
