package de.moinFT.main.listener.channel;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.ServerChannelDeleteEvent;
import org.javacord.api.listener.channel.server.ServerChannelDeleteListener;

import static de.moinFT.main.Main.DBServer;

public class ChannelDeleteListener implements ServerChannelDeleteListener {

    @Override
    public void onServerChannelDelete(ServerChannelDeleteEvent event) {
        Server Server = event.getServer();
        long ServerID = Server.getId();
        Channel Channel = event.getChannel();
        long ChannelID = Channel.getId();

        DatabaseConnection.SQL_Execute("DELETE FROM channel WHERE serverID = '" + ServerID + "' AND channelID = '" + ChannelID + "'");
        DBServer.getServer(ServerID).getChannels().delete(ChannelID);
    }
}
