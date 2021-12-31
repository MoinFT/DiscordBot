package de.moinFT.main.listener.channel;

import de.moinFT.main.Functions;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.ServerChannelCreateEvent;
import org.javacord.api.listener.channel.server.ServerChannelCreateListener;

public class ChannelCreateListener implements ServerChannelCreateListener {

    @Override
    public void onServerChannelCreate(ServerChannelCreateEvent event) {
        Server Server = event.getServer();
        long ServerID = Server.getId();
        ServerChannel Channel = event.getChannel();

        Functions.addChannelToDB(ServerID, Channel);
    }
}
