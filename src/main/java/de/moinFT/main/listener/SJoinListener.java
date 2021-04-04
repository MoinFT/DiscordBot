package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.listener.server.ServerJoinListener;

import static de.moinFT.main.Main.*;
import static de.moinFT.main.Functions.*;


public class SJoinListener implements ServerJoinListener {

    private Server Server = null;
    private long ServerID = 0;
    private String DiscordServerName = "";

    @Override
    public void onServerJoin(ServerJoinEvent event){
        Server = event.getServer();
        ServerID = Server.getId();
        DiscordServerName = Server.getName();

        DatabaseConnection.DBAddItem("server", "(`serverID`, `discordServerName`, `commandTimeout`, `prefix`, `musicBotPrefix`)", "('" + ServerID + "', '" + DiscordServerName + "', '0', '!', '-')");

        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + ServerID + "_Channel` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `channelID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `discordChannelName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `channelType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `channelName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " KEY `channelName` (`channelName`)," +
                " KEY `channelID` (`channelID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + ServerID + "_Role` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `roleID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `discordRoleName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `roleType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `roleName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " UNIQUE KEY `roleID` (`roleID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + ServerID + "_User` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `userID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `discordUserName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `isAdmin` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `botPermission` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " UNIQUE KEY `userID` (`userID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");

        int DB_ID = DatabaseConnection.DBGetDB_ID("server", "serverID", String.valueOf(ServerID));

        DBServer.setData(DB_ID, ServerID, DiscordServerName, 0, 0, "!", "-");

        for (int i = 0; i < DBServer.count(); i++) {
            if(DBServer.getServer(i).getServerID() == ServerID){
                compareDBUser_WithGuildUser(i);
                compareDBRole_WithGuildRole(i);
                compareDBChannel_WithGuildChannel(i);
            }
        }
    }
}
