package de.moinFT.main.listener.role;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleDeleteEvent;

import static de.moinFT.main.Main.DBServer;

public class RoleDeleteListener implements org.javacord.api.listener.server.role.RoleDeleteListener {

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        Server server = event.getServer();
        long serverID = server.getId();
        Role role = event.getRole();
        long roleID = role.getId();

        DatabaseConnection.SQL_Execute("DELETE FROM role WHERE serverID = " + serverID + " AND roleID = " + roleID);
        DBServer.getServer(serverID).getRoles().delete(roleID);
    }
}
