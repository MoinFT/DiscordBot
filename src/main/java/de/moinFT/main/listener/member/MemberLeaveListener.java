package de.moinFT.main.listener.member;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;

import static de.moinFT.main.Main.DBServer;

public class MemberLeaveListener implements ServerMemberLeaveListener {

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        Server Server = event.getServer();
        long ServerID = Server.getId();
        User User = event.getUser();
        long UserID = User.getId();

        DatabaseConnection.SQL_Execute("DELETE FROM user WHERE serverID = " + ServerID + " AND userID = " + UserID);
        DBServer.getServer(ServerID).getUsers().delete(UserID);
    }
}
