package de.moinFT.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.*;

import java.util.ArrayList;
import java.util.List;

import static de.moinFT.main.Main.DBServer;

public class SlashCommandManagement {
    private static final Logger log = LogManager.getLogger(SlashCommandManagement.class.getName());

    public static void create(DiscordApi client) {
        SlashCommand.with("ping", "Test der Verbindung")
                .createGlobal(client)
                .join();

        List<SlashCommand> adminSlashCommands = new ArrayList<>();

        adminSlashCommands.add(
                SlashCommand.with("bot-permission", "Befehl zum setzen der Bot-Berechtigungen von einem Nutzer",
                                List.of(
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "set", "Setzt die Bot-Berechtigung für einen Nutzer",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.USER, "USER", "Nutzer dessen Bot-Berechtigungen geändert werden sollen", true)
                                                )
                                        )
                                )
                        )
                        .setDefaultPermission(false)
                        .createGlobal(client)
                        .join()
        );

        SlashCommand.with("show", "Zeigt Information",
                        List.of(
                                SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "bot-permission", "Zeigt, welche Nutzer Bot-Berechtigungen haben")
                        )
                )
                .setDefaultPermission(true)
                .createGlobal(client)
                .join();

        for (SlashCommand slashCommand : adminSlashCommands) {
            for (int iServer = 0; iServer < DBServer.count(); iServer++) {
                List<SlashCommandPermissions> slashCommandPermissions = new ArrayList<>();

                for (int iRole = 0; iRole < DBServer.getServer(iServer).getRoles().count(); iRole++) {
                    if (client.getServerById(DBServer.getServer(iServer).getServerID()).get().getRoleById(DBServer.getServer(iServer).getRoles().getRole(iRole).getRoleID()).get().getPermissions().getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED) {
                        slashCommandPermissions.add(SlashCommandPermissions.create(DBServer.getServer(iServer).getRoles().getRole(iRole).getRoleID(), SlashCommandPermissionType.ROLE, true));
                    }
                }
                new SlashCommandPermissionsUpdater(client.getServerById(DBServer.getServer(iServer).getServerID()).get()).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
            }
        }

        log.debug("SlashCommands are created!");
    }
}
