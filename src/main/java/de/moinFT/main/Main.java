package de.moinFT.main;

import de.moinFT.main.listener.*;
import de.moinFT.utils.DBServerArray;
import de.moinFT.utils.Privates;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.permission.Permissions;

import static de.moinFT.main.Functions.*;

public class Main {
    public static DBServerArray DBServer;
    public static DiscordApi client;

    public static void main(String[] args) {

        DBServer = new DBServerArray();
        DatabaseConnection.DBGetAllData();

        client = new DiscordApiBuilder()
                .setToken(Privates.botToken)
                .setAllIntents()
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
}
