package de.moinFT.main;

import de.moinFT.utils.DBServerArray;
import de.moinFT.utils.Privates;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

import java.nio.channels.Channel;
import java.util.Iterator;

public class Main{

    public static DBServerArray DBServer;

    public static void main(String[] args) {
        DBServer = new DBServerArray();
        DatabaseConnection.DBGetAllData();

        DiscordApi client = new DiscordApiBuilder()
                .setToken(Privates.botToken)
                .setIntents(Intent.GUILDS, Intent.GUILD_MESSAGES, Intent.GUILD_MEMBERS, Intent.DIRECT_MESSAGES)
                .login()
                .join();

        System.out.println("Bot logged in as: " + client.getYourself().getDiscriminatedName());
        System.out.println("Invite the Bot using following Link: " + client.createBotInvite());

        client.updateActivity(ActivityType.LISTENING,"!help");

        for(int i = 0; i < DBServer.count(); i++) {
            compareDBUser_WithGuildUser(client, i);
            compareDBRole_WithGuildRole(client, i);
            compareDBChannel_WithGuildChannel(client, i);
        }

        client.addMessageCreateListener(new MessageListener());
    }

    private static void compareDBUser_WithGuildUser(DiscordApi client, int i){
        int userCount = client.getServerById(DBServer.getServerID(i)).get().getMemberCount();
        int DBUserCount = DBServer.getServer(i).getUsers().count();

        Iterator<User> users = client.getServerById(DBServer.getServerID(i)).get().getMembers().iterator();

        if(userCount > DBUserCount){
            while (users.hasNext()){
                long userID = users.next().getId();

                boolean existUserDB = DBServer.getServer(i).getUsers().getID(userID) > -1;

                if(!existUserDB){
                    System.out.println("Add User to DB: " + userID);
                    if(userID == Privates.MyUserID || userID == client.getServerById(DBServer.getServerID(i)).get().getOwnerId()){
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_User", "(`userID`, `botPermission`)", "('" + userID + "', 'true')");
                    } else {
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_User", "(`userID`, `botPermission`)", "('" + userID + "', 'false')");
                    }
                }
            }
        } else if(userCount < DBUserCount){
            for(int i_User = 0; i_User < DBUserCount; i_User++) {
                try {
                    System.out.println(client.getServerById(DBServer.getServerID(i)).get().getMemberById(DBServer.getServer(i).getUsers().getUserID(i_User)).get());
                } catch (Exception e) {
                    System.out.println("Remove User from DB: " + DBServer.getServer(i).getUsers().getDB_ID(i_User));
                    DatabaseConnection.DBDeleteItem(DBServer.getServerID(i) + "_User", DBServer.getServer(i).getUsers().getDB_ID(i_User));
                }
            }
        }
    }

    private static void compareDBRole_WithGuildRole(DiscordApi client, int i){
        int roleCount = client.getServerById(DBServer.getServerID(i)).get().getMemberCount();
        int DBRoleCount = DBServer.getServer(i).getRoles().count();

        Iterator<Role> roles = client.getServerById(DBServer.getServerID(i)).get().getRoles().iterator();

        if(roleCount > DBRoleCount){
            while (roles.hasNext()){
                Role role = roles.next();

                boolean existRoleDB = DBServer.getServer(i).getRoles().getID(role.getId()) > -1;

                if(!existRoleDB) {
                    System.out.println("Add Role to DB: " + role.getId());
                    if (role.isEveryoneRole()) {
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_Role", "(`roleID`, `roleName`, `roleType`)", "('" + role.getId() + "', 'everyone', 'everyone')");
                    } else {
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_Role", "(`roleID`, `roleName`, `roleType`)", "('" + role.getId() + "', '', '')");
                    }
                }
            }
        } else if(roleCount < DBRoleCount){
            for(int i_Role = 0; i_Role < DBRoleCount; i_Role++) {
                try {
                    System.out.println(client.getServerById(DBServer.getServerID(i)).get().getMemberById(DBServer.getServer(i).getRoles().getRoleID(i_Role)).get());
                } catch (Exception e) {
                    System.out.println("Remove Role from DB: " + DBServer.getServer(i).getRoles().getDB_ID(i_Role));
                    DatabaseConnection.DBDeleteItem(DBServer.getServerID(i) + "_Role", DBServer.getServer(i).getRoles().getDB_ID(i_Role));
                }
            }
        }
    }

    private static void compareDBChannel_WithGuildChannel(DiscordApi client, int i){
        int channelCount = client.getServerById(DBServer.getServerID(i)).get().getChannels().size();
        int DBChannelCount = DBServer.getServer(i).getChannels().count();

        Iterator<ServerChannel> channels = client.getServerById(DBServer.getServerID(i)).get().getChannels().iterator();

        if(channelCount > DBChannelCount){
            while (channels.hasNext()){
                ServerChannel channel = channels.next();

                boolean existChannelDB = DBServer.getServer(i).getChannels().getID(channel.getId()) > -1;

                if(!existChannelDB) {
                    if(channel.getType().isTextChannelType()) {
                        System.out.println("Add Channel to DB: " + channel.getId());
                        DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_Channel", "(`channelID`, `channelType`, `channelName`)", "('" + channel.getId() + "', 'textChannel', '')");
                    } else if(channel.getType().isVoiceChannelType()) {
                        System.out.println("Add Channel to DB: " + channel.getId());
                        if (client.getServerById(DBServer.getServerID(i)).get().getAfkChannel().get().getId() == channel.getId()) {
                            DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_Channel", "(`channelID`, `channelType`, `channelName`)", "('" + channel.getId() + "', 'voiceChannel', 'afk')");
                        } else {
                            DatabaseConnection.DBAddItem(DBServer.getServerID(i) + "_Channel", "(`channelID`, `channelType`, `channelName`)", "('" + channel.getId() + "', 'voiceChannel', '')");
                        }
                    }
                }
            }
        } else if(channelCount < DBChannelCount){
            for(int i_Role = 0; i_Role < DBChannelCount; i_Role++) {
                try {
                    System.out.println(client.getServerById(DBServer.getServerID(i)).get().getChannelById(DBServer.getServer(i).getChannels().getChannelID(i_Role)).get());
                } catch (Exception e) {
                    System.out.println("Remove Channel from DB: " + DBServer.getServer(i).getChannels().getDB_ID(i_Role));
                    DatabaseConnection.DBDeleteItem(DBServer.getServerID(i) + "_Channel", DBServer.getServer(i).getChannels().getDB_ID(i_Role));
                }
            }
        }
    }
}
