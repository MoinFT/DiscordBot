package de.moinFT.main;

import de.moinFT.utils.DBServerArray;
import de.moinFT.utils.Privates;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.user.User;

import java.util.Iterator;

public class Main{

    public static DBServerArray DBServer;

    public static void main(String[] args) {

        DBServer = new DBServerArray();
        DatabaseConnection.DBGetAllData();

        DiscordApi client = new DiscordApiBuilder()
                .setToken(Privates.botToken)
                .setIntents(Intent.GUILDS, Intent.GUILD_MEMBERS, Intent.DIRECT_MESSAGES)
                .login()
                .join();

        System.out.println("Bot logged in as: " + client.getYourself().getDiscriminatedName());
        System.out.println("Invite the Bot using following Link: " + client.createBotInvite());

        client.updateActivity(ActivityType.LISTENING,"!help");

        for(int i = 0; i < DBServer.count(); i++) {
            int userCount = client.getServerById(DBServer.getServerID(i)).get().getMemberCount();
            int DBUserCount = DBServer.getServer(i).getUsers().count();

            Iterator<User> users = client.getServerById(DBServer.getServerID(i)).get().getMembers().iterator();

            if(userCount > DBUserCount){
                while (users.hasNext()){
                    String userID = "" + users.next().getId();

                    boolean existUserDB = DBServer.getServer(i).getUsers().getID(userID) == -1;

                    if(existUserDB){
                        System.out.println("Add User to DB: " + userID);
                        if(userID.equals("588347358677565470") || userID.equals("" + client.getServerById(DBServer.getServerID(i)).get().getOwnerId())){
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
                        System.out.println("Delete User from DB: " + DBServer.getServer(i).getUsers().getDB_ID(i_User));
                        DatabaseConnection.DBDeleteItem(DBServer.getServerID(i) + "_User", DBServer.getServer(i).getUsers().getDB_ID(i_User));
                    }
                }
            }
        }

        client.addMessageCreateListener(new MessageListener());
    }

}
