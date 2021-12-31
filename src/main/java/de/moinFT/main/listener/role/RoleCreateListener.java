package de.moinFT.main.listener.role;

import de.moinFT.main.Functions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleCreateEvent;

public class RoleCreateListener implements org.javacord.api.listener.server.role.RoleCreateListener {

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        Server Server = event.getServer();
        long ServerID = Server.getId();
        Role Role = event.getRole();

        Functions.addRoleToDB(ServerID, Role);
    }
}
