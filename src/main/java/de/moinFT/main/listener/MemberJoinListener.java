package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import de.moinFT.utils.Privates;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

import static de.moinFT.main.Main.DBServer;
import static de.moinFT.main.Main.client;

public class MemberJoinListener implements ServerMemberJoinListener {

    private Server Server = null;
    private long ServerID = 0;
    private User User = null;
    private long UserID = 0;

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        User = event.getUser();
        UserID = User.getId();

        if (!User.isBot()) {
            int DBRoleCount = DBServer.getServer(ServerID).getRoles().count();

            for (int i = 0; i < DBRoleCount; i++) {
                if (DBServer.getServer(ServerID).getRoles().getRoleType(i).equalsIgnoreCase("user")) {
                    Role role = Server.getRoleById(DBServer.getServer(ServerID).getRoles().getRoleID(i)).get();

                    User.addRole(role);
                }
            }
        }

        if (UserID == Privates.MyUserID || UserID == client.getServerById(ServerID).get().getOwnerId()) {
            DatabaseConnection.DBAddItem(ServerID + "_User", "(`userID`, `botPermission`)", "('" + UserID + "', 'true')");
        } else {
            DatabaseConnection.DBAddItem(ServerID + "_User", "(`userID`, `botPermission`)", "('" + UserID + "', 'false')");
        }

        int DB_ID = DatabaseConnection.DBGetDB_ID(ServerID + "_User", "userID", String.valueOf(UserID));

        boolean botPermission = UserID == Privates.MyUserID || UserID == client.getServerById(ServerID).get().getOwnerId();
        DBServer.getServer(ServerID).getUsers().setData(DB_ID, UserID, botPermission);
    }
}
