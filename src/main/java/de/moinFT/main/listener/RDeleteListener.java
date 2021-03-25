package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleDeleteEvent;
import org.javacord.api.listener.server.role.RoleDeleteListener;

import static de.moinFT.main.Main.DBServer;

public class RDeleteListener implements RoleDeleteListener {

    private Server Server = null;
    private long ServerID = 0;
    private Role Role = null;
    private long RoleID = 0;

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        Role = event.getRole();
        RoleID = Role.getId();

        DatabaseConnection.DBDeleteItem(ServerID + "_Role", DBServer.getServer(ServerID).getRoles().getRole(RoleID).getDB_ID());
        DBServer.getServer(ServerID).getRoles().delete(RoleID);
    }
}
