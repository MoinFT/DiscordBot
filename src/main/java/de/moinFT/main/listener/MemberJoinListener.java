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
    private String DiscordUserName = "";

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        User = event.getUser();
        UserID = User.getId();
        DiscordUserName = User.getName();

        if (!User.isBot()) {
            int DBRoleCount = DBServer.getServer(ServerID).getRoles().count();

            for (int i = 0; i < DBRoleCount; i++) {
                if (DBServer.getServer(ServerID).getRoles().getRole(i).getRoleType().equalsIgnoreCase("user")) {
                    Role role = Server.getRoleById(DBServer.getServer(ServerID).getRoles().getRole(i).getRoleID()).get();

                    User.addRole(role);
                }
            }
        }

        boolean isAdmin = client.getServerById(ServerID).get().isAdmin(User) || client.getServerById(ServerID).get().isOwner(User);
        boolean botPermission = client.getServerById(ServerID).get().isAdmin(User) || client.getServerById(ServerID).get().isOwner(User) || UserID == Privates.MyUserID;

        if (isAdmin) {
            DatabaseConnection.DBAddItem(ServerID + "_User", "(`userID`, `discordUserName`, `isAdmin`, `botPermission`)", "('" + UserID + "', '" + DiscordUserName + "', 'true', 'true')");
        } else if (UserID == Privates.MyUserID) {
            DatabaseConnection.DBAddItem(ServerID + "_User", "(`userID`, `discordUserName`, `isAdmin`, `botPermission`)", "('" + UserID + "', '" + DiscordUserName + "', 'false', 'true')");
        } else {
            DatabaseConnection.DBAddItem(ServerID + "_User", "(`userID`, `discordUserName`, `isAdmin`, `botPermission`)", "('" + UserID + "', '" + DiscordUserName + "', 'false', 'false')");
        }

        int DB_ID = DatabaseConnection.DBGetDB_ID(ServerID + "_User", "userID", String.valueOf(UserID));

        DBServer.getServer(ServerID).getUsers().setData(DB_ID, UserID, DiscordUserName, isAdmin, botPermission);
    }
}
