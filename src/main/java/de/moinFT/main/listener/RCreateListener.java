package de.moinFT.main.listener;

import de.moinFT.main.Functions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleCreateEvent;
import org.javacord.api.listener.server.role.RoleCreateListener;

public class RCreateListener implements RoleCreateListener {

    private Server Server = null;
    private long ServerID = 0;
    private Role Role = null;

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        Role = event.getRole();

        Functions.addRoleToDB(ServerID, Role);
    }
}
