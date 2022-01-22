package de.moinFT.main.listener.role;

import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleChangePermissionsEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandPermissionType;
import org.javacord.api.interaction.SlashCommandPermissions;
import org.javacord.api.interaction.SlashCommandPermissionsUpdater;

import java.util.ArrayList;
import java.util.List;

import static de.moinFT.main.Main.AdminSlashCommands;

public class RoleChangePermissionListener implements org.javacord.api.listener.server.role.RoleChangePermissionsListener {
    @Override
    public void onRoleChangePermissions(RoleChangePermissionsEvent event) {
        Server server = event.getServer();
        Role role = event.getRole();
        Permissions newPermissions = event.getNewPermissions();

        if (newPermissions.getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED) {
            for (SlashCommand slashCommand : AdminSlashCommands) {
                List<SlashCommandPermissions> slashCommandPermissions = new ArrayList<>();

                slashCommandPermissions.add(SlashCommandPermissions.create(role.getId(), SlashCommandPermissionType.ROLE, true));
                new SlashCommandPermissionsUpdater(server).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
            }
        } else {
            for (SlashCommand slashCommand : AdminSlashCommands) {
                List<SlashCommandPermissions> slashCommandPermissions = new ArrayList<>();

                slashCommandPermissions.add(SlashCommandPermissions.create(role.getId(), SlashCommandPermissionType.ROLE, false));
                new SlashCommandPermissionsUpdater(server).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
            }
        }
    }
}
