package de.moinFT.main.listener.message;

import de.moinFT.main.DatabaseConnection;
import de.moinFT.main.FileIn;
import de.moinFT.main.RKICorona;
import de.moinFT.main.listener.message.SlashCommand.SlashCommand_Role;
import de.moinFT.main.listener.message.SlashCommand.SlashCommand_Set;
import de.moinFT.main.listener.message.SlashCommand.SlashCommand_Show;
import de.moinFT.utils.BotRoleType;
import de.moinFT.utils.CommandRequestType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static de.moinFT.main.Functions.createHelpMessage;
import static de.moinFT.main.Main.CommandRequestArray;
import static de.moinFT.main.Main.DBServer;

public class SlashCommandListener implements SlashCommandCreateListener {
    private static final Logger log = LogManager.getLogger(SlashCommandListener.class.getName());

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
        Server server;
        long serverID;

        SlashCommandInteractionOption firstOption;

        if (slashCommandInteraction.getServer().isPresent()) {
            server = slashCommandInteraction.getServer().get();
            serverID = server.getId();
        } else {
            slashCommandInteraction.createImmediateResponder()
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .setContent("FEHLER: Server wurde nicht gefunden!")
                    .respond();
            return;
        }

        String logInfos = "Server: " + server.getName() + " (" + server.getId() + ") | User: " + slashCommandInteraction.getUser().getDiscriminatedName() + " (" + slashCommandInteraction.getUser().getId() + ")\n";

        if (slashCommandInteraction.getCommandName().equals("bot-permission")) {
            if (slashCommandInteraction.getOptionByIndex(0).isPresent()) {
                firstOption = slashCommandInteraction.getOptionByIndex(0).get();
            } else {
                slashCommandInteraction.createImmediateResponder()
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .setContent("Der gesendete SlashCommand ist ungültig!")
                        .respond();

                log.error("SlashCommand was send without firstOption.");
                return;
            }

            switch (firstOption.getName()) {
                case "set": {
                    User user = firstOption.getOptions().get(0).getUserValue().get();

                    //Send respond on the request
                    slashCommandInteraction.createImmediateResponder()
                            .setContent("Setzt Bot-Berechtigungen für: " + user.getDiscriminatedName())
                            .addComponents(
                                    ActionRow.of(Button.success("botPermissionAllow", "Erlauben"),
                                            Button.danger("botPermissionDeny", "Ablehnen")))
                            .respond();

                    CommandRequestArray.setData(CommandRequestType.BOT_PERMISSION, server, user, new Date().getTime());
                }
                break;
            }
        } else if (slashCommandInteraction.getCommandName().equals("role")) {
            /*
             *   Get the role SlashCommand
             *   - role add
             *   - role remove
             * */

            new SlashCommand_Role(server, slashCommandInteraction);
        } else if (slashCommandInteraction.getCommandName().equals("set")) {
            /*
             *   Get the set SlashCommand
             *   - set command-timeout
             *   - set musicbot-prefix
             *   - set channel
             *   - set role
             * */

            new SlashCommand_Set(server, slashCommandInteraction);
        } else if (slashCommandInteraction.getCommandName().equals("show")) {
            /*
             *   Get the show SlashCommand
             *   - show bot-permission
             *   - show users
             *   - show user
             *   - show channels
             *   - show roles
             *   - show server
             * */

            new SlashCommand_Show(server, slashCommandInteraction);
        } else if (slashCommandInteraction.getCommandName().equals("message")) {
            /*
             *   Get the message SlashCommand
             *   - message clear
             * */

            if (slashCommandInteraction.getOptionByIndex(0).isPresent()) {
                firstOption = slashCommandInteraction.getOptionByIndex(0).get();
            } else {
                slashCommandInteraction.createImmediateResponder()
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .setContent("Der gesendete SlashCommand ist ungültig!")
                        .respond();

                log.error("SlashCommand was send without firstOption.");
                return;
            }

            TextChannel textChannel = slashCommandInteraction.getChannel().get();
            long messageAmount = firstOption.getOptions().get(0).getLongValue().get();

            if (messageAmount > 10) {
                messageAmount = 10;
            }

            try {
                MessageSet messages = textChannel.getMessages((int) messageAmount).get();
                messages.deleteAll();

                if (messages.size() == 1) {
                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .setContent("Es wurde 1 Nachricht gelöscht.")
                            .respond();
                } else {
                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .setContent("Es wurden " + messages.size() + " Nachrichten gelöscht.")
                            .respond();
                }
            } catch (InterruptedException | ExecutionException e) {
                slashCommandInteraction.createImmediateResponder()
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .setContent("Die Nachrichten konnten nicht gelöscht werden. (Berechtigungen prüfen)")
                        .respond();

                log.error("SlashCommand was send without firstOption.");

                e.printStackTrace();
                log.error("Error: Messages couldn't be deleted (check Permissions)!");
            }
        } else if (slashCommandInteraction.getCommandName().equals("color")) {
            int DBRoleCount = DBServer.getServer(serverID).getRoles().count();

            if (DBRoleCount > 0) {
                for (Role userRole : slashCommandInteraction.getUser().getRoles(server)) {
                    for (int i = 0; i < DBRoleCount; i++) {
                        if (DBServer.getServer(serverID).getRoles().getRole(i).getRoleType() == BotRoleType.COLOR) {
                            if (userRole.getId() == DBServer.getServer(serverID).getRoles().getRole(i).getRoleID()) {
                                slashCommandInteraction.getUser().removeRole(userRole);
                            }
                        }
                    }
                }

                if (!slashCommandInteraction.getOptions().isEmpty()) {
                    String roleName = slashCommandInteraction.getOptions().get(0).getStringValue().get();

                    for (int i = 0; i < DBRoleCount; i++) {
                        if (DBServer.getServer(serverID).getRoles().getRole(i).getRoleName().equalsIgnoreCase(roleName) && DBServer.getServer(serverID).getRoles().getRole(i).getRoleType() == BotRoleType.COLOR) {
                            Role role = server.getRoleById(DBServer.getServer(serverID).getRoles().getRole(i).getRoleID()).get();

                            slashCommandInteraction.getUser().addRole(role);
                        }
                    }

                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .setContent("Dein Name hat jetzt die Farbe: " + roleName)
                            .respond();

                    log.info(logInfos + "\t\t\tCommand: color (" + roleName + ")");
                } else {
                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .setContent("Dein Name hat jetzt keine Farbe.")
                            .respond();

                    log.info(logInfos + "\t\t\tCommand: color");
                }
            }
        } else if (slashCommandInteraction.getCommandName().equals("corona")) {
            slashCommandInteraction.createImmediateResponder()
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .setContent("Aktueller Inzidenzwert für Nienburg (Weser): " + RKICorona.incidenceValue() + "\n(Stand: " + RKICorona.dateOfIncidenceValue() + ")")
                    .respond();

            log.info(logInfos + "\t\t\tCommand: corona");
        } else if (slashCommandInteraction.getCommandName().equals("m-a")) {
            long commandTimeoutTimestamp = DBServer.getServer(serverID).getCommandTimeoutTimestamp();
            long commandTimeout = DBServer.getServer(serverID).getCommandTimeout();
            long timestamp = new Date().getTime();
            ServerVoiceChannel voiceChannel;

            try {
                voiceChannel = slashCommandInteraction.getUser().getConnectedVoiceChannel(server).get();
            } catch (Exception e) {
                slashCommandInteraction.createImmediateResponder()
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .setContent("Du musst in einen Sprachkanal sein!")
                        .respond();

                log.warn("User isn't connected to a VoiceChannel!");
                return;
            }

            if ((timestamp - commandTimeoutTimestamp) > commandTimeout) {
                Iterator<User> users = voiceChannel.getConnectedUsers().iterator();

                User user = users.next();
                if (!user.isMuted(server)) {
                    user.mute(server);
                    while (users.hasNext()) {
                        user = users.next();
                        user.mute(server);
                    }
                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .setContent("Alle Nutzer gestummt")
                            .respond();
                } else {
                    user.unmute(server);
                    while (users.hasNext()) {
                        user = users.next();
                        user.unmute(server);
                    }
                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .setContent("Alle Nutzer entstummt")
                            .respond();
                }

                DBServer.getServer(serverID).updateCommandTimeoutTimestamp(timestamp);
                DatabaseConnection.SQL_Execute("UPDATE server SET commandTimeoutTimestamp = '" + timestamp + "' WHERE serverID = '" + serverID + "'");
            } else {
                slashCommandInteraction.createImmediateResponder()
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .setContent("Der Befehl hat einen Timeout von " + commandTimeout / 1000 + " Sekunden!")
                        .respond();
            }

            log.info(logInfos + "\t\t\tCommand: m-a");
        } else if (slashCommandInteraction.getCommandName().equals("help")) {
            String StringHelp = FileIn.read("/json/help.json");
            JSONObject JSONHelp = new JSONObject(StringHelp).getJSONObject("attributes");

            MessageBuilder message =  new MessageBuilder();

            JSONArray normalHelp = JSONHelp.getJSONArray("normalHelp");
            message.append("Bot-Befehle", MessageDecoration.CODE_LONG);

            String messageContent = createHelpMessage(normalHelp, 20);

            if (DBServer.getServer(serverID).getRoles().countRoleType(BotRoleType.COLOR) != 0) {
                messageContent += "\n/color              Verändert die Farbe des eigenen Namen.";
            }

            message.append(messageContent, MessageDecoration.CODE_LONG);

            if (DBServer.getServer(serverID).getUsers().getUser(slashCommandInteraction.getUser().getId()).getBotPermission()) {
                message.append("Bot-Hilfe (Bot-Berechtigungen)", MessageDecoration.CODE_LONG);

                messageContent = "!help-all";
                messageContent += "\n!help-set";

                message.append(messageContent, MessageDecoration.CODE_LONG);
            }

            slashCommandInteraction.createImmediateResponder()
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .setContent(message.getStringBuilder().toString())
                    .respond();

            log.info(logInfos + "\t\t\tCommand: help");
        } else if (slashCommandInteraction.getCommandName().equals("ping")) {
            slashCommandInteraction.createImmediateResponder()
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .setContent("Ping!")
                    .respond();

            log.info(logInfos + "\t\t\tCommand: ping");
        }
    }
}
