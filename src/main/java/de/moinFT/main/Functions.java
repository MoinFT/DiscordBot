package de.moinFT.main;

import de.moinFT.utils.DBUserArray;
import de.moinFT.utils.Privates;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static de.moinFT.main.Main.DBServer;
import static de.moinFT.main.Main.client;

public class Functions {
    public static Message replyMessage(Message message, String messageContent) {
        try {
            return message.getChannel().sendMessage(message.getUserAuthor().get().getMentionTag() + messageContent).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void messageDelete(Message message, int timeout) {
        Server server = message.getServer().get();

        int infoChannelID = DBServer.getServer(server.getId()).getChannels().getID("info");

        Thread newThread = new Thread(() -> {
            try {
                if (message.getChannel().getId() != DBServer.getServer(server.getId()).getChannels().getChannelID(infoChannelID)) {
                    Thread.sleep(timeout);
                    message.delete();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        newThread.start();
    }

    public static int membersOnlineCount(Server server) {
        Iterator<User> users = server.getMembers().iterator();
        int usersOnline = 0;

        while (users.hasNext()) {
            User user = users.next();
            if (user.getDesktopStatus() != UserStatus.OFFLINE || user.getMobileStatus() != UserStatus.OFFLINE) {
                usersOnline++;
            }
        }

        return usersOnline;
    }

    public static Role getUserHighestRole(Server server, User user) {
        Iterator<Role> userRoles = user.getRoles(server).listIterator();

        Role highestRole = null;
        while (userRoles.hasNext()) {
            Role role = userRoles.next();
            if (highestRole != null) {
                if (highestRole.getPosition() < role.getPosition()) {
                    highestRole = role;
                }
            } else {
                highestRole = role;
            }
        }

        return highestRole;
    }

    public static void compareDBServer_WithServer() {
        int DBServerCount = DBServer.count();

        for (int i_Server = 0; i_Server < DBServerCount; i_Server++) {
            try {
                client.getServerById(DBServer.getServer(i_Server).getServerID()).get();
            } catch (Exception e) {
                System.out.println("Remove Server from DB: " + DBServer.getServer(i_Server).getServerID());
                DatabaseConnection.DBDeleteItem("server", DBServer.getServer(i_Server).getDB_ID());
                DatabaseConnection.DB_SQL_Execute("DROP TABLE " + DBServer.getServer(i_Server).getServerID() + "_Channel");
                DatabaseConnection.DB_SQL_Execute("DROP TABLE " + DBServer.getServer(i_Server).getServerID() + "_Role");
                DatabaseConnection.DB_SQL_Execute("DROP TABLE " + DBServer.getServer(i_Server).getServerID() + "_User");
                DBServer.delete(i_Server);
            }
        }

        int serverCount = client.getServers().size();
        DBServerCount = DBServer.count();

        Iterator<Server> servers = client.getServers().iterator();

        if (serverCount > DBServerCount) {
            while (servers.hasNext()) {
                long serverID = servers.next().getId();

                boolean existServerDB = DBServer.getServer(serverID) != null;

                if (!existServerDB) {
                    System.out.println("Add Server to DB: " + serverID);
                    DatabaseConnection.DBAddItem("server", "(`serverID`, `commandTimeout`, `prefix`, `musicBotPrefix`)", "('" + serverID + "', '0', '!', '-')");
                    DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + serverID + "_Channel` (" +
                            " `id` int(11) NOT NULL AUTO_INCREMENT," +
                            " `channelID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " `channelType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " `channelName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " PRIMARY KEY (`id`)," +
                            " UNIQUE `channelID` (`channelID`)" +
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
                            " `isAdmin` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " `botPermission` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                            " PRIMARY KEY (`id`)," +
                            " UNIQUE KEY `userID` (`userID`)" +
                            ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
                }
            }
        }
    }

    public static void compareDBUser_WithGuildUser(int i) {
        long serverID = DBServer.getServer(i).getServerID();
        int DBUserCount = DBServer.getServer(i).getUsers().count();

        for (int i_User = 0; i_User < DBUserCount; i_User++) {
            try {
                client.getServerById(serverID).get().getMemberById(DBServer.getServer(i).getUsers().getUser(i_User).getUserID()).get();
            } catch (Exception e) {
                System.out.println("Remove User from DB: " + DBServer.getServer(i).getUsers().getUser(i_User).getDB_ID());
                DatabaseConnection.DBDeleteItem(serverID + "_User", DBServer.getServer(i).getUsers().getUser(i_User).getDB_ID());
                DBServer.getServer(serverID).getUsers().delete(i_User);
            }
        }

        int userCount = client.getServerById(serverID).get().getMemberCount();
        DBUserCount = DBServer.getServer(i).getUsers().count();

        Iterator<User> users = client.getServerById(serverID).get().getMembers().iterator();

        if (userCount > DBUserCount) {
            while (users.hasNext()) {
                User user = users.next();
                long userID = user.getId();

                boolean existUserDB = DBServer.getServer(i).getUsers().getUser(userID) != null;

                if (!existUserDB) {
                    System.out.println("Add User to DB: " + userID);
                    boolean isAdmin = client.getServerById(serverID).get().isAdmin(user) || client.getServerById(serverID).get().isOwner(user);
                    boolean botPermission = client.getServerById(serverID).get().isAdmin(user) || client.getServerById(serverID).get().isOwner(user) || userID == Privates.MyUserID;

                    if (isAdmin) {
                        DatabaseConnection.DBAddItem(serverID + "_User", "(`userID`, `isAdmin`, `botPermission`)", "('" + userID + "', 'true', 'true')");
                    } else if (userID == Privates.MyUserID) {
                        DatabaseConnection.DBAddItem(serverID + "_User", "(`userID`, `isAdmin`, `botPermission`)", "('" + userID + "', 'false', 'true')");
                    } else {
                        DatabaseConnection.DBAddItem(serverID + "_User", "(`userID`, `isAdmin`, `botPermission`)", "('" + userID + "', 'false', 'false')");
                    }

                    int DB_ID = DatabaseConnection.DBGetDB_ID(serverID + "_User", "userID", String.valueOf(userID));
                    DBServer.getServer(serverID).getUsers().setData(DB_ID, userID, isAdmin, botPermission);
                }
            }
        }
    }

    public static void compareDBRole_WithGuildRole(int i) {
        long serverID = DBServer.getServer(i).getServerID();
        int DBRoleCount = DBServer.getServer(i).getRoles().count();

        for (int i_Role = 0; i_Role < DBRoleCount; i_Role++) {
            try {
                client.getServerById(serverID).get().getRoleById(DBServer.getServer(i).getRoles().getRole(i_Role).getRoleID()).get();
            } catch (Exception e) {
                System.out.println("Remove Role from DB: " + DBServer.getServer(i).getRoles().getRole(i_Role).getDB_ID());
                DatabaseConnection.DBDeleteItem(serverID + "_Role", DBServer.getServer(i).getRoles().getRole(i_Role).getDB_ID());
                DBServer.getServer(serverID).getRoles().delete(i_Role);
            }
        }

        int roleCount = client.getServerById(serverID).get().getRoles().size();
        DBRoleCount = DBServer.getServer(i).getRoles().count();

        Iterator<Role> roles = client.getServerById(serverID).get().getRoles().iterator();

        if (roleCount > DBRoleCount) {
            while (roles.hasNext()) {
                Role role = roles.next();
                long roleID = role.getId();

                boolean existRoleDB = DBServer.getServer(i).getRoles().getRole(roleID) != null;

                if (!existRoleDB) {
                    System.out.println("Add Role to DB: " + roleID);
                    if (role.isEveryoneRole()) {
                        DatabaseConnection.DBAddItem(serverID + "_Role", "(`roleID`, `roleName`, `roleType`)", "('" + roleID + "', 'everyone', 'everyone')");
                    } else {
                        DatabaseConnection.DBAddItem(serverID + "_Role", "(`roleID`, `roleName`, `roleType`)", "('" + roleID + "', '', '')");
                    }

                    int DB_ID = DatabaseConnection.DBGetDB_ID(serverID + "_Role", "roleID", String.valueOf(roleID));

                    DBServer.getServer(serverID).getRoles().setData(DB_ID, roleID, "", "");
                }
            }
        }
    }

    public static void compareDBChannel_WithGuildChannel(int i) {
        long serverID = DBServer.getServer(i).getServerID();
        int DBChannelCount = DBServer.getServer(i).getChannels().count();

        for (int i_Channel = 0; i_Channel < DBChannelCount; i_Channel++) {
            try {
                client.getServerById(serverID).get().getChannelById(DBServer.getServer(i).getChannels().getChannelID(i_Channel)).get();
            } catch (Exception e) {
                System.out.println("Remove Channel from DB: " + DBServer.getServer(i).getChannels().getDB_ID(i_Channel));
                DatabaseConnection.DBDeleteItem(serverID + "_Channel", DBServer.getServer(i).getChannels().getDB_ID(i_Channel));
                DBServer.getServer(serverID).getChannels().delete(i_Channel);
            }
        }

        int channelCount = client.getServerById(serverID).get().getChannels().size();
        DBChannelCount = DBServer.getServer(i).getChannels().count();

        Iterator<ServerChannel> channels = client.getServerById(serverID).get().getChannels().iterator();

        if (channelCount > DBChannelCount) {
            while (channels.hasNext()) {
                ServerChannel channel = channels.next();

                boolean existChannelDB = DBServer.getServer(i).getChannels().getID(channel.getId()) > -1;

                if (!existChannelDB) {
                    String channelType;
                    String channelName = "";
                    if (channel.getType().isTextChannelType()) {
                        channelType = "textChannel";
                    } else if (channel.getType().isVoiceChannelType()) {
                        if (client.getServerById(serverID).get().getAfkChannel().isPresent()) {
                            if (client.getServerById(serverID).get().getAfkChannel().get().getId() == channel.getId()) {
                                channelName = "afk";
                            }
                        }
                        channelType = "voiceChannel";
                    } else {
                        channelType = "categorieChannel";
                    }

                    System.out.println("Add Channel to DB: " + channel.getId());
                    DatabaseConnection.DBAddItem(serverID + "_Channel", "(`channelID`, `channelType`, `channelName`)", "('" + channel.getId() + "', '" + channelType + "', '" + channelName + "')");

                    int DB_ID = DatabaseConnection.DBGetDB_ID(serverID + "_Channel", "channelID", String.valueOf(channel.getId()));

                    DBServer.getServer(serverID).getChannels().setData(DB_ID, channel.getId(), channelType, channelName);
                }
            }
        }
    }
}
