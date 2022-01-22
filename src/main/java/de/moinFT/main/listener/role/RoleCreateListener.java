package de.moinFT.main.listener.role;

import de.moinFT.main.Functions;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandPermissionType;
import org.javacord.api.interaction.SlashCommandPermissions;
import org.javacord.api.interaction.SlashCommandPermissionsUpdater;

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
                List<SlashCommandPermissions> slashCommandPermissions = new ArrayList<>();

                slashCommandPermissions.add(SlashCommandPermissions.create(role.getId(), SlashCommandPermissionType.ROLE, true));
                new SlashCommandPermissionsUpdater(server).setPermissions(slashCommandPermissions).update(slashCommand.getId()).join();
            }
        }
    }
}
