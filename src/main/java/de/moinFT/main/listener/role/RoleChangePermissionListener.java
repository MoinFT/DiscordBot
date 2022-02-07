package de.moinFT.main.listener.role;

import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleChangePermissionsEvent;
import org.javacord.api.interaction.ApplicationCommandPermissionType;
import org.javacord.api.interaction.ApplicationCommandPermissions;
import org.javacord.api.interaction.ApplicationCommandPermissionsUpdater;
import org.javacord.api.interaction.SlashCommand;

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
                List<ApplicationCommandPermissions> slashCommandPermissions = new ArrayList<>();

                slashCommandPermissions.add(ApplicationCommandPermissions.create(role.getId(), ApplicationCommandPermissionType.ROLE, true));
                new ApplicationCommandPermissionsUpdater(server).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
            }
        } else {
            for (SlashCommand slashCommand : AdminSlashCommands) {
                List<ApplicationCommandPermissions> slashCommandPermissions = new ArrayList<>();

                slashCommandPermissions.add(ApplicationCommandPermissions.create(role.getId(), ApplicationCommandPermissionType.ROLE, false));
                new ApplicationCommandPermissionsUpdater(server).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
            }
        }
    }
}
