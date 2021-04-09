package de.moinFT.main.listener;

import de.moinFT.main.Functions;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.ServerChannelCreateEvent;
import org.javacord.api.listener.channel.server.ServerChannelCreateListener;

public class ChannelCreateListener implements ServerChannelCreateListener {

    private Server Server = null;
    private long ServerID = 0;
    private ServerChannel Channel = null;

    @Override
    public void onServerChannelCreate(ServerChannelCreateEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        Channel = event.getChannel();

        Functions.addChannelToDB(ServerID, Channel);
    }
}
