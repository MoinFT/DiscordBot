package de.moinFT.main.listener.member;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.channel.RegularServerChannel;
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
        Server server = event.getServer();
        long serverID = server.getId();
        User user = event.getUser();
        long userID = user.getId();

        if (server.isAdmin(user)) {
            DBServer.getServer(serverID).getUsers().getUser(userID).updateBotPermission(true);
            DBServer.getServer(serverID).getUsers().getUser(userID).updateIsAdmin(true);
            DatabaseConnection.SQL_Execute("UPDATE user SET botPermission = 'true', isAdmin = 'true' WHERE serverID = '" + serverID + "' AND userID = '" + userID + "'");

            int adminChannelID = DBServer.getServer(server.getId()).getChannels().getChannel("admin").getID();

            if (adminChannelID != -1) {
                RegularServerChannel adminChannel = server.getChannelById(DBServer.getServer(serverID).getChannels().getChannel(adminChannelID).getChannelID()).get().asRegularServerChannel().get();
                adminChannel.createUpdater().addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
            }
        }
    }
}
