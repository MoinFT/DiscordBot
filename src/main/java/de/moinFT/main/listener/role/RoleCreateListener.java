package de.moinFT.main.listener.role;

import de.moinFT.main.Functions;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleCreateEvent;
import org.javacord.api.interaction.ApplicationCommandPermissionType;
import org.javacord.api.interaction.ApplicationCommandPermissions;
import org.javacord.api.interaction.ApplicationCommandPermissionsUpdater;
import org.javacord.api.interaction.SlashCommand;

import java.util.ArrayList;
import java.util.List;

import static de.moinFT.main.Main.AdminSlashCommands;

public class RoleCreateListener implements org.javacord.api.listener.server.role.RoleCreateListener {

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        Server server = event.getServer();
        long serverId = server.getId();
        Role role = event.getRole();

        Functions.addRoleToDB(serverId, role);

        Permissions permission = event.getRole().getPermissions();

        if (permission.getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED) {
            for (SlashCommand slashCommand : AdminSlashCommands) {
                List<ApplicationCommandPermissions> slashCommandPermissions = new ArrayList<>();

                slashCommandPermissions.add(ApplicationCommandPermissions.create(role.getId(), ApplicationCommandPermissionType.ROLE, true));
                new ApplicationCommandPermissionsUpdater(server).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
            }
        }
    }
}
