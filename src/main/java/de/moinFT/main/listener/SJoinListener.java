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

    @Override
    public void onServerJoin(ServerJoinEvent event){
        Server = event.getServer();
        ServerID = Server.getId();

        DatabaseConnection.DBAddItem("server", "(`serverID`, `prefix`)", "('" + ServerID + "', '!')");

        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + ServerID + "_Channel` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `channelID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `channelType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `channelName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " KEY `channelName` (`channelName`)," +
                " KEY `channelID` (`channelID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + ServerID + "_Role` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `roleID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `roleType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `roleName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " UNIQUE KEY `roleID` (`roleID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + ServerID + "_User` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `userID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `botPermission` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " UNIQUE KEY `userID` (`userID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");

        int DB_ID = DatabaseConnection.DBGetDB_ID("server", "serverID", String.valueOf(ServerID));

        DBServer.setData(DB_ID, ServerID, "!");

        for (int i = 0; i < DBServer.count(); i++) {
            if(DBServer.getServerID(i) == ServerID){
                compareDBUser_WithGuildUser(i);
                compareDBRole_WithGuildRole(i);
                compareDBChannel_WithGuildChannel(i);
            }
        }
    }
}
