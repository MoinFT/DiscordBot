package de.moinFT.main.listener.message.SlashCommand;

import de.moinFT.main.DatabaseConnection;
import de.moinFT.utils.BotRoleType;
import de.moinFT.utils.DBChannelArray;
import de.moinFT.utils.DBRoleArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import static de.moinFT.main.Main.DBServer;

public class SlashCommand_Set {
    private static final Logger log = LogManager.getLogger(SlashCommand_Set.class.getName());

    public SlashCommand_Set(Server server, SlashCommandInteraction slashCommandInteraction) {
        long serverID = server.getId();
        SlashCommandInteractionOption firstOption;
        String logInfos = "Server: " + server.getName() + " (" + server.getId() + ") | User: " + slashCommandInteraction.getUser().getDiscriminatedName() + " (" + slashCommandInteraction.getUser().getId() + ")\n";

        if (slashCommandInteraction.getFirstOption().isPresent()) {
            firstOption = slashCommandInteraction.getFirstOption().get();
        } else {
            slashCommandInteraction.createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .setContent("Der gesendete SlashCommand ist ungültig!")
                    .respond();

            log.error("SlashCommand was send without firstOption.");
            return;
        }

        switch (firstOption.getName()) {
            case "command-timeout": {
                int commandTimeout = firstOption.getOptions().get(0).getIntValue().get() * 1000;

                DBServer.getServer(serverID).updateCommandTimeout(commandTimeout);
                DatabaseConnection.SQL_Execute("UPDATE server SET commandTimeout = '" + commandTimeout + "' WHERE serverID = '" + serverID + "'");

                slashCommandInteraction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("Command-Timeout wurde erfolgreich gesetzt.")
                        .respond();

                log.info(logInfos + "\t\t\tCommand: set command-timeout | Command-Timeout: " + commandTimeout);
            }
            break;
            case "musicbot-prefix": {
                String prefix = firstOption.getOptions().get(0).getStringValue().get().substring(0, 1);

                DBServer.getServer(serverID).updateMusicBotPrefix(prefix);
                DatabaseConnection.SQL_Execute("UPDATE server SET musicBotPrefix = '" + prefix + "' WHERE serverID = '" + serverID + "'");

                slashCommandInteraction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("Neuer Prefix wurde efolgreich gesetzt.")
                        .respond();

                log.info(logInfos + "\t\t\tCommand: set musicbot-prefix | New Prefix: " + prefix);
            }
            break;
            case "channel": {
                Channel channel = firstOption.getOptions().get(0).getChannelValue().get();
                String channelName = firstOption.getOptions().get(1).getStringValue().get();

                DBChannelArray channelArray = DBServer.getServer(serverID).getChannels();

                if (!channelName.equals("_")) {
                    for (int i = 0; i < channelArray.count(); i++) {
                        if (channelArray.getChannel(i).getChannelName().equals(channelName)) {
                            log.warn(logInfos + "\t\t\tCommand: set channel | Channel: " + channel.asServerChannel().get().getName() + " (" + channel.asServerChannel().get().getId() + ") | Channel Name: " + channelName + " | ChannelName exist already in DB!");
                            return;
                        }
                    }
                } else {
                    channelName = "";
                }

                DBServer.getServer(serverID).getChannels().getChannel(channel.getId()).updateChannelName(channelName);
                DatabaseConnection.SQL_Execute("UPDATE channel SET channelName = '" + channelName + "' WHERE serverID = '" + serverID + "' AND channelID = '" + channel.getId() + "'");

                slashCommandInteraction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("Channel Name wurde erfolgreich gesetzt.")
                        .respond();

                log.info(logInfos + "\t\t\tCommand: set channel | Channel: " + channel.asServerChannel().get().getName() + " (" + channel.asServerChannel().get().getId() + ") | Channel Name: " + channelName);
            }
            break;
            case "role": {
                Role role = firstOption.getOptions().get(0).getRoleValue().get();
                String roleName = firstOption.getOptions().get(1).getStringValue().get();
                String roleTypeString = firstOption.getOptions().get(2).getStringValue().get();

                BotRoleType roleType = BotRoleType.UNKNOWN;

                DBRoleArray roleArray = DBServer.getServer(serverID).getRoles();

                if (!roleName.equals("_")) {
                    for (int i = 0; i < roleArray.count(); i++) {
                        if (roleArray.getRole(i).getRoleName().equals(roleName)) {
                            log.warn(logInfos + "\t\t\tCommand: set role | Role: " + role.getName() + " (" + role.getId() + ") | Channel Name: " + roleName + " | RoleName exist already in DB!");
                            return;
                        }
                    }
                } else {
                    roleName = "";
                }

                switch (roleTypeString) {
                    case "user":
                        roleType = BotRoleType.USER;
                        break;
                    case "color":
                        roleType = BotRoleType.COLOR;
                        break;
                }

                DBServer.getServer(serverID).getRoles().getRole(role.getId()).updateRoleType(roleType);
                DBServer.getServer(serverID).getRoles().getRole(role.getId()).updateRoleName(roleName);
                DatabaseConnection.SQL_Execute("UPDATE role SET roleType = '" + roleType + "', roleName = '" + roleName + "' WHERE serverID = '" + serverID + "' AND roleID = '" + role.getId() + "'");

                slashCommandInteraction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("Rollen Name und Typ wurden erfolgreich gesetzt.")
                        .respond();

                log.info(logInfos + "\t\t\tCommand: set role | Role: " + role.getName() + " (" + role.getId() + ") | Channel Name: " + roleName);
            }
            break;
        }
    }
}
