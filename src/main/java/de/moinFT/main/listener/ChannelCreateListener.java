package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.ServerChannelCreateEvent;
import org.javacord.api.listener.channel.server.ServerChannelCreateListener;

import static de.moinFT.main.Main.DBServer;

public class ChannelCreateListener implements ServerChannelCreateListener {

    private Server Server = null;
    private long ServerID = 0;
    private Channel Channel = null;
    private long ChannelID = 0;
    private ChannelType ChannelType = null;

    @Override
    public void onServerChannelCreate(ServerChannelCreateEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        Channel = event.getChannel();
        ChannelID = Channel.getId();
        ChannelType = Channel.getType();

        if (ChannelType.isServerChannelType() && (ChannelType.isTextChannelType() || ChannelType.isVoiceChannelType())) {
            String channelType = "";
            if (ChannelType.isTextChannelType()){
                channelType = "textChannel";
            } else if (ChannelType.isVoiceChannelType()) {
                channelType = "voiceChannel";
            }
            DatabaseConnection.DBAddItem(ServerID + "_Channel", "(`channelID`, `channelType`, `channelName`)", "('" + ChannelID + "', '" + channelType + "', '')");

            int DB_ID = DatabaseConnection.DBGetDB_ID(ServerID + "_Channel", "channelID", String.valueOf(ChannelID));

            DBServer.getServer(ServerID).getChannels().setData(DB_ID, ChannelID, channelType, "");
        }
    }
}
