package de.moinFT.main.listener.role;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleDeleteEvent;

import static de.moinFT.main.Main.DBServer;

public class RoleDeleteListener implements org.javacord.api.listener.server.role.RoleDeleteListener {

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        Server Server = event.getServer();
        long ServerID = Server.getId();
        Role Role = event.getRole();
        long RoleID = Role.getId();

        DatabaseConnection.SQL_Execute("DELETE FROM role WHERE serverID = " + ServerID + " AND roleID = " + RoleID);
        DBServer.getServer(ServerID).getRoles().delete(RoleID);
    }
}
