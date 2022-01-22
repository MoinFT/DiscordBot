package de.moinFT.main.listener.message.SlashCommand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import static de.moinFT.main.Functions.getUserHighestRole;

public class SlashCommand_Role {
    private static final Logger log = LogManager.getLogger(SlashCommand_Role.class.getName());

    public SlashCommand_Role(Server server, SlashCommandInteraction slashCommandInteraction) {
        SlashCommandInteractionOption firstOption;
        String logInfos = "Server: " + server.getName() + " (" + server.getId() + ") | User: " + slashCommandInteraction.getUser().getDiscriminatedName() + " (" + slashCommandInteraction.getUser().getId() + ")\n";

        if (slashCommandInteraction.getFirstOption().isPresent()) {
            firstOption = slashCommandInteraction.getFirstOption().get();
        } else {
            slashCommandInteraction.createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .setContent("Der gesendete SlashCommand ist ungültig!")
                    .respond();

            log.error(logInfos + "SlashCommand was send without firstOption.");
            return;
        }

        int highestRolePostion = getUserHighestRole(server, slashCommandInteraction.getUser()).getPosition();

        Role role = firstOption.getOptions().get(0).getRoleValue().get();
        User user = firstOption.getOptions().get(1).getUserValue().get();

        switch (firstOption.getName()) {
            case "add": {
                if (highestRolePostion >= role.getPosition() || slashCommandInteraction.getUser().isBotOwner()) {
                    if (!user.getRoles(server).contains(role)) {
                        slashCommandInteraction.createImmediateResponder()
                                .setContent("Rolle wurde dem Nutzer hinzugefügt. (" + role.getName() + " | " + user.getDiscriminatedName() + ")")
                                .respond();
                        server.addRoleToUser(user, role);

                        log.info(logInfos + "\t\t\tCommand: role add | Role: " + role.getName() + " (" + role.getId() + ") | User: " + user.getDiscriminatedName() + " (" + user.getId() + ")");
                    } else {
                        slashCommandInteraction.createImmediateResponder()
                                .setFlags(MessageFlag.EPHEMERAL)
                                .setContent("Rolle konnte nicht hinzugefügt werden. Nutzer besitzt diese Rolle schon.")
                                .respond();

                        log.warn(logInfos + "\t\t\tCommand: role add | Role: " + role.getName() + " (" + role.getId() + ") | User: " + user.getDiscriminatedName() + " (" + user.getId() + ") | User has this role already!");
                    }
                } else {
                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL)
                            .setContent("Du kannst keine Rolle vergeben die mehr Rechte hat als du!")
                            .respond();

                    log.warn(logInfos + "\t\t\tCommand: role add | Role: " + role.getName() + " (" + role.getId() + ") | User: " + user.getDiscriminatedName() + " (" + user.getId() + ") | User has not the needed permission!");
                }
            }
            break;
            case "remove": {
                if (highestRolePostion >= role.getPosition() || slashCommandInteraction.getUser().isBotOwner()) {
                    if (user.getRoles(server).contains(role)) {
                        slashCommandInteraction.createImmediateResponder()
                                .setContent("Rolle wurde dem Nutzer entfernt. (" + role.getName() + " | " + user.getDiscriminatedName() + ")")
                                .respond();
                        server.removeRoleFromUser(user, role);

                        log.info(logInfos + "\t\t\tCommand: role remove | Role: " + role.getName() + " (" + role.getId() + ") | User: " + user.getDiscriminatedName() + " (" + user.getId() + ")");
                    } else {
                        slashCommandInteraction.createImmediateResponder()
                                .setFlags(MessageFlag.EPHEMERAL)
                                .setContent("Rolle konnte nicht entfernt werden. Nutzer besitzt diese Rolle nicht.")
                                .respond();

                        log.warn(logInfos + "\t\t\tCommand: role remove | Role: " + role.getName() + " (" + role.getId() + ") | User: " + user.getDiscriminatedName() + " (" + user.getId() + ") | User doesn't have this role!");
                    }
                } else {
                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL)
                            .setContent("Du kannst keine Rolle entfernen die mehr Rechte hat als du!")
                            .respond();

                    log.warn(logInfos + "\t\t\tCommand: role remove | Role: " + role.getName() + " (" + role.getId() + ") | User: " + user.getDiscriminatedName() + " (" + user.getId() + ") | User has not the needed permission!");
                }
            }
            break;
        }
    }
}
