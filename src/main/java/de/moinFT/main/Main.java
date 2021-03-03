package de.moinFT.main;

import de.moinFT.main.listener.*;
import de.moinFT.utils.DBServerArray;
import de.moinFT.utils.Privates;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.Iterator;

public class Main {

    public static DBServerArray DBServer;
    public static DiscordApi client;

    public static void main(String[] args) {
        DBServer = new DBServerArray();
        DatabaseConnection.DBGetAllData();

        client = new DiscordApiBuilder()
                .setToken(Privates.botToken)
                .setIntents(Intent.GUILDS, Intent.GUILD_MESSAGES, Intent.GUILDS, Intent.GUILD_MEMBERS, Intent.DIRECT_MESSAGES)
                .login()
                .join();

        System.out.println("Bot logged in as: " + client.getYourself().getDiscriminatedName());
        System.out.println("Invite the Bot using following Link: " + client.createBotInvite(Permissions.fromBitmask(2147479295)));

        client.updateActivity(ActivityType.LISTENING, "!help");

        compareDBServer_WithServer();

        for (int i = 0; i < DBServer.count(); i++) {
            compareDBUser_WithGuildUser(i);
            compareDBRole_WithGuildRole(i);
            compareDBChannel_WithGuildChannel(i);
        }

        client.addServerJoinListener(new SJoinListener());
        client.addServerLeaveListener(new SLeaveListener());

        client.addServerMemberJoinListener(new MemberJoinListener());
        client.addServerMemberLeaveListener(new MemberLeaveListener());

        client.addRoleCreateListener(new RCreateListener());
        client.addRoleDeleteListener(new RDeleteListener());

        client.addServerChannelCreateListener(new ChannelCreateListener());
        client.addServerChannelDeleteListener(new ChannelDeleteListener());

        client.addMessageCreateListener(new MessageListener());
    }

    private static void compareDBServer_WithServer() {
        int DBServerCount = DBServer.count();

        for (int i_Server = 0; i_Server < DBServerCount; i_Server++) {
            try {
                System.out.println(client.getServerById(DBServer.getServerID(i_Server)).get());
            } catch (Exception e) {
                System.out.println("Remove Server from DB: " + DBServer.getServerID(i_Server));
                DatabaseConnection.DBDeleteItem("server", DBServer.getDB_ID(i_Server));
                DatabaseConnection.DB_SQL_Execute("DROP TABLE " + DBServer.getServerID(i_Server) +  "_Channel");
                DatabaseConnection.DB_SQL_Execute("DROP TABLE " + DBServer.getServerID(i_Server) +  "_Role");
                DatabaseConnection.DB_SQL_Execute("DROP TABLE " + DBServer.getServerID(i_Server) +  "_User");
                DBServer.delete(i_Server);
            }
        }

        int serverCount = client.getServers().size();
        DBServerCount = DBServer.count();

        Iterator<Server> servers = client.getServers().iterator();

        if (serverCount > DBServerCount) {
            while (servers.hasNext()) {
                long serverID = servers.next().getId();

                boolean existServerDB = DBServer.getID(serverID) > -1;

                if (!existServerDB) {
                    System.out.println("Add Server to DB: " + serverID);
                    DatabaseConnection.DBAddItem("server", "(`serverID`, `prefix`)", "('" + serverID + "', '!')");
                    DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + serverID + "_Channel` (" +
                            " `id` int(11) NOT NULL AUTO_INCREMENT," +
                            " `channelID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " `channelType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " `channelName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " PRIMARY KEY (`id`)," +
                            " UNIQUE `channelID` (`channelID`)" +
                            " KEY `channelName` (`channelName`)," +
                            ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
                    DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + serverID + "_Role` (" +
                            " `id` int(11) NOT NULL AUTO_INCREMENT," +
                            " `roleID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " `roleType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " `roleName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " PRIMARY KEY (`id`)," +
                            " UNIQUE KEY `roleID` (`roleID`)" +
                            ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
                    DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + serverID + "_User` (" +
                            " `id` int(11) NOT NULL AUTO_INCREMENT," +
                            " `userID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " `botPermission` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " PRIMARY KEY (`id`)," +
                            " UNIQUE KEY `userID` (`userID`)" +
                            ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
                }
            }
        }
    }

    public static void compareDBUser_WithGuildUser(int i) {
        int DBUserCount = DBServer.getServer(i).getUsers().count();

        for (int i_User = 0; i_User < DBUserCount; i_User++) {
            try {
                System.out.println(client.getServerById(DBServer.getServerID(i)).get().getMemberById(DBServer.getServer(i).getUsers().getUserID(i_User)).get());
            } catch (Exception e) {
                System.out.println("Remove User from DB: " + DBServer.getServer(i).getUsers().getDB_ID(i_User));
                DatabaseConnection.DBDeleteItem(DBServer.getServerID(i) + "_User", DBServer.getServer(i).getUsers().getDB_ID(i_User));
                DBServer.getServer(DBServer.getServerID(i)).getUsers().delete(i_User);
            }
        }

        int userCount = client.getServerById(DBServer.getServerID(i)).get().getMemberCount();
        DBUserCount = DBServer.getServer(i).getUsers().count();

        Iterator<User> users = client.getServerById(DBServer.getServerID(i)).get().getMembers().iterator();

        if (userCount > DBUserCount) {
            while (users.hasNext()) {
                long userID = users.next().getId();

                boolean existUserDB = DBServer.getServer(i).getUsers().getID(userID) > -1;

                if (!existUserDB) {
                    System.out.println("Add User to DB: " + userID);
                    if (userID == Privates.MyUserID || userID == client.getServerById(DBServer.getServerID(i)).get().getOwnerId()) {
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_User", "(`userID`, `botPermission`)", "('" + userID + "', 'true')");
                    } else {
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_User", "(`userID`, `botPermission`)", "('" + userID + "', 'false')");
                    }

                    int DB_ID = DatabaseConnection.DBGetDB_ID(DBServer.getServerID(i) + "_User", "userID", String.valueOf(userID));

                    boolean botPermission = userID == Privates.MyUserID || userID == client.getServerById(DBServer.getServerID(i)).get().getOwnerId();
                    DBServer.getServer(DBServer.getServerID(i)).getUsers().setData(DB_ID, userID, botPermission);
                }
            }
        }
    }

    public static void compareDBRole_WithGuildRole(int i) {
        int DBRoleCount = DBServer.getServer(i).getRoles().count();

        for (int i_Role = 0; i_Role < DBRoleCount; i_Role++) {
            try {
                System.out.println(client.getServerById(DBServer.getServerID(i)).get().getRoleById(DBServer.getServer(i).getRoles().getRoleID(i_Role)).get());
            } catch (Exception e) {
                System.out.println("Remove Role from DB: " + DBServer.getServer(i).getRoles().getDB_ID(i_Role));
                DatabaseConnection.DBDeleteItem(DBServer.getServerID(i) + "_Role", DBServer.getServer(i).getRoles().getDB_ID(i_Role));
                DBServer.getServer(DBServer.getServerID(i)).getRoles().delete(i_Role);
            }
        }

        int roleCount = client.getServerById(DBServer.getServerID(i)).get().getRoles().size();
        DBRoleCount = DBServer.getServer(i).getRoles().count();

        Iterator<Role> roles = client.getServerById(DBServer.getServerID(i)).get().getRoles().iterator();

        if (roleCount > DBRoleCount) {
            while (roles.hasNext()) {
                Role role = roles.next();

                boolean existRoleDB = DBServer.getServer(i).getRoles().getID(role.getId()) > -1;

                if (!existRoleDB) {
                    System.out.println("Add Role to DB: " + role.getId());
                    if (role.isEveryoneRole()) {
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_Role", "(`roleID`, `roleName`, `roleType`)", "('" + role.getId() + "', 'everyone', 'everyone')");
                    } else {
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_Role", "(`roleID`, `roleName`, `roleType`)", "('" + role.getId() + "', '', '')");
                    }

                    int DB_ID = DatabaseConnection.DBGetDB_ID(DBServer.getServerID(i) + "_Role", "roleID", String.valueOf(role.getId()));

                    DBServer.getServer(DBServer.getServerID(i)).getRoles().setData(DB_ID, role.getId(), "", "");
                }
            }
        }
    }

    public static void compareDBChannel_WithGuildChannel(int i) {
        int DBChannelCount = DBServer.getServer(i).getChannels().count();

        for (int i_Channel = 0; i_Channel < DBChannelCount; i_Channel++) {
            try {
                System.out.println(client.getServerById(DBServer.getServerID(i)).get().getChannelById(DBServer.getServer(i).getChannels().getChannelID(i_Channel)).get());
            } catch (Exception e) {
                System.out.println("Remove Channel from DB: " + DBServer.getServer(i).getChannels().getDB_ID(i_Channel));
                DatabaseConnection.DBDeleteItem(DBServer.getServerID(i) + "_Channel", DBServer.getServer(i).getChannels().getDB_ID(i_Channel));
                DBServer.getServer(DBServer.getServerID(i)).getChannels().delete(i_Channel);
            }
        }

        int channelCount = client.getServerById(DBServer.getServerID(i)).get().getChannels().size();
        DBChannelCount = DBServer.getServer(i).getChannels().count();

        Iterator<ServerChannel> channels = client.getServerById(DBServer.getServerID(i)).get().getChannels().iterator();

        if (channelCount > DBChannelCount) {
            while (channels.hasNext()) {
                ServerChannel channel = channels.next();

                boolean existChannelDB = DBServer.getServer(i).getChannels().getID(channel.getId()) > -1;

                if (!existChannelDB) {
                    String channelType = " ";
                    String channelName = " ";
                    if (channel.getType().isTextChannelType()) {
                        channelType = "textChannel";
                    } else if (channel.getType().isVoiceChannelType()) {
                        if (client.getServerById(DBServer.getServerID(i)).get().getAfkChannel().isPresent()){
                            if (client.getServerById(DBServer.getServerID(i)).get().getAfkChannel().get().getId() == channel.getId()) {
                                channelName = "afk";
                            }
                        }
                        channelType = "voiceChannel";
                    }

                    if (channel.getType().isTextChannelType() || channel.getType().isVoiceChannelType()) {
                        System.out.println("Add Channel to DB: " + channel.getId());
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_Channel", "(`channelID`, `channelType`, `channelName`)", "('" + channel.getId() + "', '" + channelType + "', '" + channelName + "'");

                        int DB_ID = DatabaseConnection.DBGetDB_ID(DBServer.getServerID(i) + "_Channel", "channelID", String.valueOf(channel.getId()));

                        DBServer.getServer(DBServer.getServerID(i)).getChannels().setData(DB_ID, channel.getId(), channelType, channelName);
                    }
                }
            }
        }
    }
}
