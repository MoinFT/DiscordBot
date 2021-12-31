package de.moinFT.main.listener.message;

import de.moinFT.main.DatabaseConnection;
import de.moinFT.utils.Privates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;

import java.util.Date;

import static de.moinFT.main.Main.DBServer;
import static de.moinFT.main.Main.ServerUserRequest;

public class ComponentListener implements MessageComponentCreateListener {
    private static final Logger log = LogManager.getLogger(ComponentListener.class.getName());

    @Override
    public void onComponentCreate(MessageComponentCreateEvent event) {
        MessageComponentInteraction messageComponentInteraction = event.getMessageComponentInteraction();
        String customID = messageComponentInteraction.getCustomId();
        Server Server = messageComponentInteraction.getServer().get();
        long ServerID = Server.getId();

        //Get the adminChannel for the permission set if available
        int adminChannelID = DBServer.getServer(ServerID).getChannels().getChannel("admin").getID();
        ServerChannel adminChannel = null;

        if (adminChannelID != -1) {
            adminChannel = Server.getChannelById(DBServer.getServer(ServerID).getChannels().getChannel(adminChannelID).getChannelID()).get();
        }

        messageComponentInteraction.getMessage().ifPresent(Message::delete);

        if (customID.startsWith("botPermission")) {
            User user = ServerUserRequest.getUser("botPermission", Server);

            if (ServerUserRequest.getTimeStamp("botPermission", Server) + 120000 > new Date().getTime()) {
                switch (customID) {
                    case "botPermissionAllow":
                        //Permission allow set for the adminChannel if available
                        if (adminChannel != null) {
                            new ServerChannelUpdater(adminChannel).addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
                        }

                        //Allow the bot-permission in the DB
                        DBServer.getServer(ServerID).getUsers().getUser(user.getId()).updateBotPermission(true);
                        DatabaseConnection.SQL_Execute("UPDATE user SET botPermission = 'true' WHERE serverID = '" + ServerID + "' AND userID = '" + user.getId() + "'");

                        //Send respond on the request
                        messageComponentInteraction.createImmediateResponder()
                                .setContent("Bot-Berechtigungen erlaubt für: " + user.getDiscriminatedName())
                                .respond();

                        log.info("Bot-permission allowed for user: " + user.getDiscriminatedName());
                        break;
                    case "botPermissionDeny":
                        if (user.getId() == Privates.MyUserID) {
                            //Send respond on the request
                            messageComponentInteraction.createImmediateResponder()
                                    .setContent("Bot-Berechtigungen nicht abgelehnt für: " + user.getDiscriminatedName() + " (Bot-Besitzer)")
                                    .respond();

                            log.info("Bot-permission not denied for user: " + user.getDiscriminatedName() + " (Bot-Owner)");
                        } else if (Server.isAdmin(user)) {
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
                                new ServerChannelUpdater(adminChannel).addPermissionOverwrite(user, new PermissionsBuilder().setUnset(PermissionType.READ_MESSAGES).build()).update();
                            }

                            //Deny the bot-permission in the DB
                            DBServer.getServer(ServerID).getUsers().getUser(user.getId()).updateBotPermission(false);
                            DatabaseConnection.SQL_Execute("UPDATE user SET botPermission = 'false' WHERE serverID = '" + ServerID + "' AND userID = '" + user.getId() + "'");

                            //Send respond on the request
                            messageComponentInteraction.createImmediateResponder()
                                    .setContent("Bot-Berechtigungen abgelehnt für: " + user.getDiscriminatedName())
                                    .respond();

                            log.info("Bot-permission denied for user: " + user.getDiscriminatedName());
                        }
                        break;
                }
            } else {
                //Send respond on the request
                messageComponentInteraction.createImmediateResponder()
                        .setContent("Zeit ist abgelaufen! (Mehr als zwei Minuten vergangen)")
                        .respond();
            }
        }
    }
}
