package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleCreateEvent;
import org.javacord.api.listener.server.role.RoleCreateListener;

import static de.moinFT.main.Main.DBServer;

public class RCreateListener implements RoleCreateListener {

    private Server Server = null;
    private long ServerID = 0;
    private Role Role = null;
    private long RoleID = 0;
    private String DiscordRoleName = "";

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        Role = event.getRole();
        RoleID = Role.getId();
        DiscordRoleName = Role.getName();

        DatabaseConnection.DBAddItem(ServerID + "_Role", "(`roleID`, `roleType`, `roleName`)", "('" + RoleID + "', '', '')");

        int DB_ID = DatabaseConnection.DBGetDB_ID(ServerID + "_Role", "roleID", String.valueOf(RoleID));

        DBServer.getServer(ServerID).getRoles().setData(DB_ID, RoleID, DiscordRoleName, "", "");
    }
}
