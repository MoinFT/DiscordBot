package de.moinFT.main.listener.member;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;

import static de.moinFT.main.Main.DBServer;

public class RoleAddListener implements UserRoleAddListener {

    @Override
    public void onUserRoleAdd(UserRoleAddEvent event) {
        Server Server = event.getServer();
        long ServerID = Server.getId();
        User User = event.getUser();
        long UserID = User.getId();

        if (Server.isAdmin(User)) {
            DBServer.getServer(ServerID).getUsers().getUser(UserID).updateBotPermission(true);
            DBServer.getServer(ServerID).getUsers().getUser(UserID).updateIsAdmin(true);
            DatabaseConnection.SQL_Execute("UPDATE user SET botPermission = 'true', isAdmin = 'true' WHERE serverID = '" + ServerID + "' AND userID = '" + UserID + "'");

            int adminChannelID = DBServer.getServer(Server.getId()).getChannels().getChannel("admin").getID();

            if (adminChannelID != -1) {
                ServerChannel adminChannel = Server.getChannelById(DBServer.getServer(ServerID).getChannels().getChannel(adminChannelID).getChannelID()).get();
                new ServerChannelUpdater(adminChannel).addPermissionOverwrite(User, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
            }
        }
    }
}
