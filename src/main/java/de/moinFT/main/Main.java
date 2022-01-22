package de.moinFT.main;

import de.moinFT.main.listener.channel.ChannelCreateListener;
import de.moinFT.main.listener.channel.ChannelDeleteListener;
import de.moinFT.main.listener.client.ClientJoinListener;
import de.moinFT.main.listener.client.ClientLeaveListener;
import de.moinFT.main.listener.member.MemberJoinListener;
import de.moinFT.main.listener.member.MemberLeaveListener;
import de.moinFT.main.listener.member.RoleAddListener;
import de.moinFT.main.listener.member.RoleRemoveListener;
import de.moinFT.main.listener.message.ComponentListener;
import de.moinFT.main.listener.message.MessageListener;
import de.moinFT.main.listener.role.RoleChangePermissionListener;
import de.moinFT.main.listener.role.RoleCreateListener;
import de.moinFT.main.listener.role.RoleDeleteListener;
import de.moinFT.main.listener.message.SlashCommandListener;
import de.moinFT.utils.DBServerArray;
import de.moinFT.utils.Privates;
import de.moinFT.utils.CommandRequestArray;
import org.apache.logging.log4j.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.interaction.SlashCommand;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static DBServerArray DBServer;
    public static CommandRequestArray CommandRequestArray;
    public static DiscordApi client;

    public static final List<SlashCommand> AdminSlashCommands = new ArrayList<>();
    public static final List<SlashCommand> BotPermissionSlashCommands = new ArrayList<>();

    public static void main(String[] args) {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DBServer = new DBServerArray();
        DatabaseConnection.DBGetAllData();

        CommandRequestArray = new CommandRequestArray();

        client = new DiscordApiBuilder()
                .setToken(Privates.botToken)
                .setAllIntents()
                .login()
                .join();

        log.info("Bot logged in as: " + client.getYourself().getDiscriminatedName());

        client.updateActivity(ActivityType.LISTENING, "/help");

        Functions.compareDBServer_WithServer();

        for (int i = 0; i < DBServer.count(); i++) {
            Functions.compareDBUser_WithGuildUser(i);
            Functions.compareDBRole_WithGuildRole(i);
            Functions.compareDBChannel_WithGuildChannel(i);
        }

        client.addServerJoinListener(new ClientJoinListener());
        client.addServerLeaveListener(new ClientLeaveListener());

        client.addServerMemberJoinListener(new MemberJoinListener());
        client.addServerMemberLeaveListener(new MemberLeaveListener());

        client.addRoleCreateListener(new RoleCreateListener());
        client.addRoleChangePermissionsListener(new RoleChangePermissionListener());
        client.addRoleDeleteListener(new RoleDeleteListener());

        client.addServerChannelCreateListener(new ChannelCreateListener());
        client.addServerChannelDeleteListener(new ChannelDeleteListener());

        client.addUserRoleAddListener(new RoleAddListener());
        client.addUserRoleRemoveListener(new RoleRemoveListener());

        client.addMessageCreateListener(new MessageListener());
        client.addSlashCommandCreateListener(new SlashCommandListener());
        client.addMessageComponentCreateListener(new ComponentListener());

        SlashCommandManagement.create(client);
    }
}
