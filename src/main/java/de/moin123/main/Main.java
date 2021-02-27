package de.moin123.main;

import de.moin123.utils.Privates;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;

public class Main{

    public static void main(String[] args) {
        DiscordApi client = new DiscordApiBuilder().setToken(Privates.botToken).login().join();

        client.updateActivity(ActivityType.LISTENING,"!help");

        client.addMessageCreateListener(new MessageListener());

        System.out.println("Invite the Bot using following Link: " + client.createBotInvite());
    }

}
