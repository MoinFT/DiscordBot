package de.moinFT.main.listener.message;

import de.moinFT.main.DatabaseConnection;
import de.moinFT.utils.CommandRequestType;
import de.moinFT.utils.Privates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.moinFT.main.Main.*;

public class ComponentListener implements MessageComponentCreateListener {
    private static final Logger log = LogManager.getLogger(ComponentListener.class.getName());

    @Override
    public void onComponentCreate(MessageComponentCreateEvent event) {
        MessageComponentInteraction messageComponentInteraction = event.getMessageComponentInteraction();
        String customID = messageComponentInteraction.getCustomId();
        Server server = messageComponentInteraction.getServer().get();
        long serverID = server.getId();

        //Get the adminChannel for the permission set if available
        int adminChannelID = DBServer.getServer(serverID).getChannels().getChannel("admin").getID();
        RegularServerChannel adminChannel = null;

        if (adminChannelID != -1) {
            adminChannel = server.getChannelById(DBServer.getServer(serverID).getChannels().getChannel(adminChannelID).getChannelID()).get().asRegularServerChannel().get();
        }

        try {
            messageComponentInteraction.getMessage().delete();
        } catch (Exception e) {
            log.warn("Failed to delete message\n" + e.getMessage());
        }

        if (customID.startsWith("botPermission")) {
            User user = CommandRequestArray.getUser(CommandRequestType.BOT_PERMISSION, server);

            if (CommandRequestArray.getTimeStamp(CommandRequestType.BOT_PERMISSION, server) + 120000 > new Date().getTime()) {
                switch (customID) {
                    case "botPermissionAllow":
                        //Permission allow set for the adminChannel if available
                        if (adminChannel != null) {
                            adminChannel.createUpdater().addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
                        }

                        //Allow the bot-permission in the DB
                        DBServer.getServer(serverID).getUsers().getUser(user.getId()).updateBotPermission(true);
                        DatabaseConnection.SQL_Execute("UPDATE user SET botPermission = 'true' WHERE serverID = '" + serverID + "' AND userID = '" + user.getId() + "'");

                        //Send respond on the request
                        messageComponentInteraction.createImmediateResponder()
                                .setContent("Bot-Berechtigungen erlaubt für: " + user.getDiscriminatedName())
                                .respond();

                        for (SlashCommand slashCommand : BotPermissionSlashCommands) {
                            List<ApplicationCommandPermissions> slashCommandPermissions = new ArrayList<>();
                            slashCommandPermissions.add(ApplicationCommandPermissions.create(user.getId(), ApplicationCommandPermissionType.USER, true));
                            new ApplicationCommandPermissionsUpdater(server).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
                        }

                        log.info("Bot-permission allowed for user: " + user.getDiscriminatedName());
                        break;
                    case "botPermissionDeny":
                        if (user.getId() == Privates.MyUserID) {
                            //Send respond on the request
                            messageComponentInteraction.createImmediateResponder()
                                    .setContent("Bot-Berechtigungen nicht abgelehnt für: " + user.getDiscriminatedName() + " (Bot-Besitzer)")
                                    .respond();

                            log.info("Bot-permission not denied for user: " + user.getDiscriminatedName() + " (Bot-Owner)");
                        } else if (server.isAdmin(user)) {
                            //Send respond on the request
                            messageComponentInteraction.createImmediateResponder()
                                    .setContent("Bot-Berechtigungen nicht abgelehnt für: " + user.getDiscriminatedName() + " (Admin)")
                                    .respond();

                            log.info("Bot-permission not denied for user: " + user.getDiscriminatedName() + " (Admin)");
                        } else if (user.getId() == messageComponentInteraction.getUser().getId()) {
                            //Send respond on the request
                            messageComponentInteraction.createImmediateResponder()
                                    .setContent("Bot-Berechtigungen nicht abgelehnt für: " + user.getDiscriminatedName() + " (Benutzer)")
                                    .respond();

                            log.info("Bot-permission not denied for user: " + user.getDiscriminatedName() + " (User)");
                        } else {
                            //Permission deny set for the adminChannel if available
                            if (adminChannel != null) {
                                adminChannel.createUpdater().addPermissionOverwrite(user, new PermissionsBuilder().setUnset(PermissionType.READ_MESSAGES).build()).update();
                            }

                            //Deny the bot-permission in the DB
                            DBServer.getServer(serverID).getUsers().getUser(user.getId()).updateBotPermission(false);
                            DatabaseConnection.SQL_Execute("UPDATE user SET botPermission = 'false' WHERE serverID = '" + serverID + "' AND userID = '" + user.getId() + "'");

                            //Send respond on the request
                            messageComponentInteraction.createImmediateResponder()
                                    .setContent("Bot-Berechtigungen abgelehnt für: " + user.getDiscriminatedName())
                                    .respond();

                            for (SlashCommand slashCommand : BotPermissionSlashCommands) {
                                List<ApplicationCommandPermissions> slashCommandPermissions = new ArrayList<>();
                                slashCommandPermissions.add(ApplicationCommandPermissions.create(user.getId(), ApplicationCommandPermissionType.USER, false));
                                new ApplicationCommandPermissionsUpdater(server).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
                            }

                            log.info("Bot-permission denied for user: " + user.getDiscriminatedName());
                        }
                        break;
                }
            } else {
                //Send respond on the request
                messageComponentInteraction.createImmediateResponder()
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .setContent("Zeit ist abgelaufen! (Mehr als zwei Minuten vergangen)")
                        .respond();
            }
        }
    }
}
