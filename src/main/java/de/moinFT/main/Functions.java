package de.moinFT.main;

import de.moinFT.utils.Privates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger log = LogManager.getLogger(Functions.class.getName());

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
                log.info("Remove Server from DB: " + DBServer.getServer(i_Server).getServerID());
                DatabaseConnection.SQL_Execute("DELETE FROM server WHERE serverID = " + DBServer.getServer(i_Server).getServerID());
                DatabaseConnection.SQL_Execute("DELETE FROM channel WHERE serverID = " + DBServer.getServer(i_Server).getServerID());
                DatabaseConnection.SQL_Execute("DELETE FROM role WHERE serverID = " + DBServer.getServer(i_Server).getServerID());
                DatabaseConnection.SQL_Execute("DELETE FROM user WHERE serverID = " + DBServer.getServer(i_Server).getServerID());
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
                log.info("Remove Channel from DB: " + DBServer.getServer(i).getChannels().getChannel(i_Channel).getChannelID());
                DatabaseConnection.SQL_Execute("DELETE FROM channel WHERE serverID = " + DBServer.getServer(i).getServerID() + " AND channelID = " + DBServer.getServer(i).getChannels().getChannel(i_Channel).getChannelID());
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
                log.info("Remove Role from DB: " + DBServer.getServer(i).getRoles().getRole(i_Role).getRoleID());
                DatabaseConnection.SQL_Execute("DELETE FROM role WHERE serverID = " + DBServer.getServer(i).getServerID() + " AND roleID = " + DBServer.getServer(i).getRoles().getRole(i_Role).getRoleID());
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
                log.info("Remove User from DB: " + DBServer.getServer(i).getUsers().getUser(i_User).getUserID());
                DatabaseConnection.SQL_Execute("DELETE FROM user WHERE serverID = " + DBServer.getServer(i).getServerID() + " AND userID = " + DBServer.getServer(i).getUsers().getUser(i_User).getUserID());
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

        log.info("Add Server to DB: " + serverID);
        DatabaseConnection.SQL_Execute("INSERT INTO server (serverID, commandTimeoutTimestamp, commandTimeout, prefix, musicBotPrefix) VALUES ('" + serverID + "', '0', '5000', '!', '-')");
        DBServer.setData(serverID, 0, 0, "!", "-");
    }

    public static void addChannelToDB(long ServerID, ServerChannel Channel) {
        long channelID = Channel.getId();

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

        log.info("Add Channel to DB: " + channelID);
        DatabaseConnection.SQL_Execute("INSERT INTO channel (serverID, channelID, channelType, channelName) VALUES ('" + ServerID + "', '" + channelID + "', '" + channelType + "', '" + channelName + "')");
        DBServer.getServer(ServerID).getChannels().setData(ServerID, channelID, channelType, channelName);
    }

    public static void addRoleToDB(long ServerID, Role Role) {
        long roleID = Role.getId();

        String roleName = "";
        String roleType = "";

        if (Role.isEveryoneRole()) {
            roleName = "everyone";
            roleType = "everyone";
        }

        log.info("Add Role to DB: " + roleID);
        DatabaseConnection.SQL_Execute("INSERT INTO role (serverID, roleID, roleName, roleType) VALUES ('" + ServerID + "', '" + roleID + "', '" + roleName + "', '" + roleType + "')");
        DBServer.getServer(ServerID).getRoles().setData(ServerID, roleID, roleType, roleName);
    }

    public static void addUserToDB(long ServerID, User User) {
        long userID = User.getId();

        boolean isAdmin = client.getServerById(ServerID).get().isAdmin(User) || client.getServerById(ServerID).get().isOwner(User);
        boolean botPermission = client.getServerById(ServerID).get().isAdmin(User) || client.getServerById(ServerID).get().isOwner(User) || userID == Privates.MyUserID;

        log.info("Add User to DB: " + userID);
        DatabaseConnection.SQL_Execute("INSERT INTO user (serverID, userID, isAdmin, botPermission) VALUES ('" + ServerID + "', '" + userID + "', '" + isAdmin + "', '" + botPermission + "')");
        DBServer.getServer(ServerID).getUsers().setData(ServerID, userID, isAdmin, botPermission);
    }
}
