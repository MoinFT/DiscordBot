package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import static de.moinFT.main.Main.DBServer;

public class URoleRemoveListener implements UserRoleRemoveListener {

    private Server Server = null;
    private long ServerID = 0;
    private User User = null;
    private long UserID = 0;

    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        User = event.getUser();
        UserID = User.getId();

        boolean isAdmin = DBServer.getServer(ServerID).getUsers().getUser(User.getId()).getIsAdmin();

        if (isAdmin) {
            if (!Server.isAdmin(User)) {
                DBServer.getServer(ServerID).getUsers().getUser(UserID).updateBotPermission(false);
                DBServer.getServer(ServerID).getUsers().getUser(UserID).updateIsAdmin(false);
                DatabaseConnection.DBUpdateItem(ServerID + "_User", DBServer.getServer(ServerID).getUsers().getUser(User.getId()).getDB_ID(), "`botPermission` = '" + false + "'");
                DatabaseConnection.DBUpdateItem(ServerID + "_User", DBServer.getServer(ServerID).getUsers().getUser(User.getId()).getDB_ID(), "`isAdmin` = '" + false + "'");

                int adminChannelID = DBServer.getServer(Server.getId()).getChannels().getChannel("admin").getID();

                if (adminChannelID != -1) {
                    ServerChannel adminChannel = Server.getChannelById(DBServer.getServer(ServerID).getChannels().getChannel(adminChannelID).getChannelID()).get();
                    new ServerChannelUpdater(adminChannel).addPermissionOverwrite(User, new PermissionsBuilder().setUnset(PermissionType.READ_MESSAGES).build()).update();
                }
            }
        }
    }
}
