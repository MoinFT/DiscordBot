package de.moinFT.main;

import de.moinFT.utils.Privates;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import java.nio.charset.StandardCharsets;
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

    public static String createSpaces(int count) {
        StringBuilder spaces = new StringBuilder();

        for (int i = 0; i < count; i++) {
            spaces.append(" ");
        }

        return spaces.toString();
    }

    public static void messageDelete(Message message, int timeout) {
        Server server = message.getServer().get();

        Thread newThread = new Thread(() -> {
            try {
                if (DBServer.getServer(server.getId()).getChannels().getChannel("info") != null) {
                    if (message.getChannel().getId() != DBServer.getServer(server.getId()).getChannels().getChannel("info").getChannelID()) {
                        Thread.sleep(timeout);
                        message.delete();
                    } else if (!message.getAuthor().isBotUser()) {
                        Thread.sleep(timeout);
                        message.delete();
                    }
                } else {
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
                Server server = servers.next();
                long serverID = server.getId();

                boolean existServerDB = DBServer.getServer(serverID) != null;

                if (!existServerDB) {
                    addServerToDB(server);
                }
            }
        }
    }

    public static void compareDBChannel_WithGuildChannel(int i) {
        long serverID = DBServer.getServer(i).getServerID();
        int DBChannelCount = DBServer.getServer(i).getChannels().count();

        for (int i_Channel = 0; i_Channel < DBChannelCount; i_Channel++) {
            try {
                client.getServerById(serverID).get().getChannelById(DBServer.getServer(i).getChannels().getChannel(i_Channel).getChannelID()).get();
            } catch (Exception e) {
                System.out.println("Remove Channel from DB: " + DBServer.getServer(i).getChannels().getChannel(i_Channel).getDB_ID());
                DatabaseConnection.DBDeleteItem(serverID + "_Channel", DBServer.getServer(i).getChannels().getChannel(i_Channel).getDB_ID());
                DBServer.getServer(serverID).getChannels().delete(i_Channel);
            }
        }

        int channelCount = client.getServerById(serverID).get().getChannels().size();
        DBChannelCount = DBServer.getServer(i).getChannels().count();

        Iterator<ServerChannel> channels = client.getServerById(serverID).get().getChannels().iterator();

        if (channelCount > DBChannelCount) {
            while (channels.hasNext()) {
                ServerChannel channel = channels.next();
                long channelID = channel.getId();

                boolean existChannelDB = DBServer.getServer(i).getChannels().getChannel(channelID) != null;

                if (!existChannelDB) {
                    addChannelToDB(serverID, channel);
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
                    addRoleToDB(serverID, role);
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
                    addUserToDB(serverID, user);
                }
            }
        }
    }

    public static void addServerToDB(Server Server) {
        long serverID = Server.getId();
        String discordServerName = new String(Server.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        System.out.println("Add Server to DB: " + serverID);
        DatabaseConnection.DBAddItem("server", "(`serverID`, `discordServerName`, `commandTimeoutTimestamp`, `commandTimeout`, `prefix`, `musicBotPrefix`)", "('" + serverID + "', '" + discordServerName.replace("'", "~") + "', '0', '5000', '!', '-')");

        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + serverID + "_Channel` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `channelID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `discordChannelName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `channelType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `channelName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " UNIQUE `channelID` (`channelID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + serverID + "_Role` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `roleID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `discordRoleName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `roleType` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `roleName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " UNIQUE KEY `roleID` (`roleID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
        DatabaseConnection.DB_SQL_Execute("CREATE TABLE `discordBot`.`" + serverID + "_User` (" +
                " `id` int(11) NOT NULL AUTO_INCREMENT," +
                " `userID` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `discordUserName` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `isAdmin` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " `botPermission` varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
                " PRIMARY KEY (`id`)," +
                " UNIQUE KEY `userID` (`userID`)" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");

        int DB_ID = DatabaseConnection.DBGetDB_ID("server", "serverID", String.valueOf(serverID));
        DBServer.setData(DB_ID, serverID, discordServerName, 0, 0, "!", "-");
    }

    public static void addChannelToDB(long ServerID, ServerChannel Channel) {
        long channelID = Channel.getId();
        String discordChannelName = new String(Channel.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        String channelType;
        String channelName = "";
        if (Channel.getType().isTextChannelType()) {
            channelType = "textChannel";
        } else if (Channel.getType().isVoiceChannelType()) {
            if (client.getServerById(ServerID).get().getAfkChannel().isPresent()) {
                if (client.getServerById(ServerID).get().getAfkChannel().get().getId() == channelID) {
                    channelName = "afk";
                }
            }
            channelType = "voiceChannel";
        } else {
            channelType = "categorieChannel";
        }

        System.out.println("Add Channel to DB: " + channelID);
        DatabaseConnection.DBAddItem(ServerID + "_Channel", "(`channelID`, `discordChannelName`, `channelType`, `channelName`)", "('" + channelID + "', '" + discordChannelName.replace("'", "~") + "', '" + channelType + "', '" + channelName + "')");

        int DB_ID = DatabaseConnection.DBGetDB_ID(ServerID + "_Channel", "channelID", String.valueOf(channelID));
        DBServer.getServer(ServerID).getChannels().setData(DB_ID, channelID, discordChannelName, channelType, channelName);
    }

    public static void addRoleToDB(long ServerID, Role Role) {
        long roleID = Role.getId();
        String discordRoleName = new String(Role.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        System.out.println("Add Role to DB: " + roleID);
        if (Role.isEveryoneRole()) {
            DatabaseConnection.DBAddItem(ServerID + "_Role", "(`roleID`, `discordRoleName`, `roleName`, `roleType`)", "('" + roleID + "', '" + discordRoleName.replace("'", "~") + "', 'everyone', 'everyone')");
        } else {
            DatabaseConnection.DBAddItem(ServerID + "_Role", "(`roleID`, `discordRoleName`, `roleName`, `roleType`)", "('" + roleID + "', '" + discordRoleName.replace("'", "~") + "', '', '')");
        }

        int DB_ID = DatabaseConnection.DBGetDB_ID(ServerID + "_Role", "roleID", String.valueOf(roleID));
        DBServer.getServer(ServerID).getRoles().setData(DB_ID, roleID, discordRoleName, "", "");
    }

    public static void addUserToDB(long ServerID, User User) {
        long userID = User.getId();
        String discordUsername = new String(User.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        System.out.println("Add User to DB: " + userID);
        boolean isAdmin = client.getServerById(ServerID).get().isAdmin(User) || client.getServerById(ServerID).get().isOwner(User);
        boolean botPermission = client.getServerById(ServerID).get().isAdmin(User) || client.getServerById(ServerID).get().isOwner(User) || userID == Privates.MyUserID;

        if (isAdmin) {
            DatabaseConnection.DBAddItem(ServerID + "_User", "(`userID`, `discordUserName`, `isAdmin`, `botPermission`)", "('" + userID + "', '" + discordUsername.replace("'", "~") + "', 'true', 'true')");
        } else if (userID == Privates.MyUserID) {
            DatabaseConnection.DBAddItem(ServerID + "_User", "(`userID`, `discordUserName`, `isAdmin`, `botPermission`)", "('" + userID + "', '" + discordUsername.replace("'", "~") + "', 'false', 'true')");
        } else {
            DatabaseConnection.DBAddItem(ServerID + "_User", "(`userID`, `discordUserName`, `isAdmin`, `botPermission`)", "('" + userID + "', '" + discordUsername.replace("'", "~") + "', 'false', 'false')");
        }

        int DB_ID = DatabaseConnection.DBGetDB_ID(ServerID + "_User", "userID", String.valueOf(userID));
        DBServer.getServer(ServerID).getUsers().setData(DB_ID, userID, discordUsername, isAdmin, botPermission);
    }
}
