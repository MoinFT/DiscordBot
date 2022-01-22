package de.moinFT.main;

import de.moinFT.utils.BotRoleType;
import de.moinFT.utils.DBRoleArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.moinFT.main.Main.*;

public class SlashCommandManagement {
    private static final Logger log = LogManager.getLogger(SlashCommandManagement.class.getName());

    public static void create(DiscordApi client) {
        SlashCommand.with("ping", "Test der Verbindung")
                .createGlobal(client)
                .join();

        SlashCommand.with("help", "Gibt eine Liste mit allen Befehlen aus.")
                .setDefaultPermission(true)
                .createGlobal(client)
                .join();

        AdminSlashCommands.add(
                SlashCommand.with("bot-permission", "Befehl zum setzen der Bot-Berechtigungen von einem Nutzer.",
                                List.of(
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "set", "Setzt die Bot-Berechtigung für einen Nutzer.",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.USER, "USER", "Nutzer dessen Bot-Berechtigungen geändert werden sollen.", true)
                                                )
                                        )
                                )
                        )
                        .setDefaultPermission(false)
                        .createGlobal(client)
                        .join()
        );

        AdminSlashCommands.add(
                SlashCommand.with("message", "Nachrichten Management",
                                List.of(
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "clear", "Löscht Nachrichten.",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.INTEGER, "NUMBER", "Anzahl der Nachrichten die gelöscht werden sollen.", true)
                                                )
                                        )
                                )
                        )
                        .setDefaultPermission(false)
                        .createGlobal(client)
                        .join()
        );

        BotPermissionSlashCommands.add(
                SlashCommand.with("role", "Einem Nutzer eine Rolle hinzufügen/entfernen",
                                List.of(
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "add", "Rolle zu einem Nutzer hinzufügen.",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.ROLE, "ROLE", "Rolle welche hinzugefügt werden soll.", true),
                                                        SlashCommandOption.create(SlashCommandOptionType.USER, "USER", "Nutzer dem die Rolle hizugefügt werden soll.", true)
                                                )
                                        ),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "remove", "Rolle von einem Nutzer entfernen.",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.ROLE, "ROLE", "Rolle welche entfernt werden soll.", true),
                                                        SlashCommandOption.create(SlashCommandOptionType.USER, "USER", "Nutzer dem die Rolle entfernt werden soll.", true)
                                                )
                                        )
                                )
                        )
                        .setDefaultPermission(false)
                        .createGlobal(client)
                        .join()
        );

        BotPermissionSlashCommands.add(
                SlashCommand.with("set", "Werte für den Bot setzen",
                                List.of(
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "command-timeout", "Setzt die Timeout Zeit, welche für einige Befehle genutzt wird.",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.INTEGER, "COMMAND-TIMEOUT", "Timeout Zeit in Sekunden.", true)
                                                )
                                        ),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "channel", "Fügt einem Channel einen Namen hinzu.",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "CHANNEL", "Channel der den Namen erhalten soll.", true),
                                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "CHANNEL-NAME", "Name der gesetzt werden soll.", true)
                                                )
                                        ),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "role", "Fügt einer Rolle einen Typ und einen Namen hinzu.",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.ROLE, "ROLE", "Rolle die Namen und Typ erhalten soll.", true),
                                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "ROLE-NAME", "Name der gesetzt werden soll.", true),
                                                        SlashCommandOption.createWithChoices(SlashCommandOptionType.INTEGER, "ROLE-TYPE", "Typ der gesetzt werden soll.", true,
                                                                Arrays.asList(
                                                                        SlashCommandOptionChoice.create("color", 1),
                                                                        SlashCommandOptionChoice.create("user", 2)
                                                                )
                                                        )
                                                )
                                        ),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "musicbot-prefix", "Setzt den Prefix für Musikbot Commands.",
                                                List.of(
                                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "MUSICBOT-PREFIX", "Der neue Prefix für den MusiKbot.", true)
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
                                SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "bot-permission", " Gibt alle Nutzer mit Bot-Berechtigungen aus."),
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "users", "Gibt Auskunft über alle Nutzer.",
                                        List.of(
                                                SlashCommandOption.create(SlashCommandOptionType.INTEGER, "PAGE", "Seite der Nutzer-Informationen. (16 Nutzer pro Seite)", false)
                                        )
                                ),
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "user", "Gibt Auskunft über einen Nutzer.",
                                        List.of(
                                                SlashCommandOption.create(SlashCommandOptionType.USER, "USER", "Nutzer über den Informationen ausgegeben werden sollen.", true)
                                        )
                                ),
                                SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "channels", "Gibt Auskunft über dem Bot zugewiesene Kanäle."),
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "roles", "Gibt Auskunft über dem Bot zugewiesene Rollen.",
                                        List.of(
                                                SlashCommandOption.createWithChoices(SlashCommandOptionType.INTEGER, "roles", "Gibt Auskunft über dem Bot zugewiesene Rollen.", true,
                                                        Arrays.asList(
                                                                SlashCommandOptionChoice.create("all", 1),
                                                                SlashCommandOptionChoice.create("color", 2),
                                                                SlashCommandOptionChoice.create("user", 3)
                                                        )
                                                )
                                        )
                                ),
                                SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "server", "Gibt Auskunft über den Server.")
                        )
                )
                .setDefaultPermission(true)
                .createGlobal(client)
                .join();

        SlashCommand.with("corona", "Gibt den derzeitigen Inzidenzwert des Landkreis Nienburg (Weser) aus.")
                .setDefaultPermission(true)
                .createGlobal(client)
                .join();

        SlashCommand.with("m-a", "Stummt alle Nutzer mit denen man in einem Sprachkanal ist.")
                .setDefaultPermission(true)
                .createGlobal(client)
                .join();

        for (int iServer = 0; iServer < DBServer.count(); iServer++) {
            if (DBServer.getServer(iServer).getRoles().countRoleType(BotRoleType.COLOR) != 0) {
                List<SlashCommandOptionChoice> colorChoice = new ArrayList<>();

                for (int iRole = 0; iRole < DBServer.getServer(iServer).getRoles().count(); iRole++) {
                    DBRoleArray role = DBServer.getServer(iServer).getRoles().getRole(iRole);
                    if (role.getRoleType() == BotRoleType.COLOR) {
                        colorChoice.add(SlashCommandOptionChoice.create(role.getRoleName(), role.getRoleName()));
                    }
                }

                SlashCommand.with("color", "Setzt die Farbe des eigenen Namens.",
                                List.of(
                                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "COLOR", "Farbe in welcher der Name angezeigt werden soll.", false, colorChoice)
                                )
                        )
                        .setDefaultPermission(true)
                        .createForServer(client.getServerById(DBServer.getServer(iServer).getServerID()).get())
                        .join();
            }
        }

        for (SlashCommand slashCommand : AdminSlashCommands) {
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

        for (SlashCommand slashCommand : BotPermissionSlashCommands) {
            for (int iServer = 0; iServer < DBServer.count(); iServer++) {
                List<SlashCommandPermissions> slashCommandPermissions = new ArrayList<>();

                for (int iUser = 0; iUser < DBServer.getServer(iServer).getUsers().count(); iUser++) {
                    if (DBServer.getServer(iServer).getUsers().getUser(iUser).getBotPermission()) {
                        slashCommandPermissions.add(SlashCommandPermissions.create(DBServer.getServer(iServer).getUsers().getUser(iUser).getUserID(), SlashCommandPermissionType.USER, true));
                    }
                }

                new SlashCommandPermissionsUpdater(client.getServerById(DBServer.getServer(iServer).getServerID()).get()).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
            }
        }

        log.debug("SlashCommands are created!");
    }

    public static void delete(DiscordApi client) {
        List<SlashCommand> commands = new ArrayList<>();

        try {
            commands = client.getGlobalSlashCommands().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        for (SlashCommand slashCommand : commands) {
            slashCommand.deleteGlobal();
        }

        for (int iServer = 0; iServer < DBServer.count(); iServer++) {
            Server server = client.getServerById(DBServer.getServer(iServer).getServerID()).get();

            try {
                commands = client.getServerSlashCommands(server).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            for (SlashCommand slashCommand : commands) {
                slashCommand.deleteForServer(server);
            }
        }

        log.debug("SlashCommands are deleted!");
    }
}
