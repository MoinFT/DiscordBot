package de.moinFT.main.listener.member;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import static de.moinFT.main.Main.DBServer;

public class RoleRemoveListener implements UserRoleRemoveListener {

    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent event) {
        Server server = event.getServer();
        long serverID = server.getId();
        User user = event.getUser();
        long userID = user.getId();

        boolean isAdmin = DBServer.getServer(serverID).getUsers().getUser(user.getId()).getIsAdmin();

        if (isAdmin) {
            if (!server.isAdmin(user) && !user.isBotOwner()) {
                DBServer.getServer(serverID).getUsers().getUser(userID).updateBotPermission(false);
                DBServer.getServer(serverID).getUsers().getUser(userID).updateIsAdmin(false);
                DatabaseConnection.SQL_Execute("UPDATE user SET botPermission = 'false', isAdmin = 'false' WHERE serverID = '" + serverID + "' AND userID = '" + userID + "'");

                int adminChannelID = DBServer.getServer(server.getId()).getChannels().getChannel("admin").getID();

                if (adminChannelID != -1) {
                    RegularServerChannel adminChannel = server.getChannelById(DBServer.getServer(serverID).getChannels().getChannel(adminChannelID).getChannelID()).get().asRegularServerChannel().get();
                    adminChannel.createUpdater().addPermissionOverwrite(user, new PermissionsBuilder().setUnset(PermissionType.READ_MESSAGES).build()).update();
                }
            }
        }
    }
}
