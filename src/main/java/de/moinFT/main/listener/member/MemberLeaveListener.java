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
        Server server = event.getServer();
        long serverID = server.getId();
        User user = event.getUser();
        long userID = user.getId();

        DatabaseConnection.SQL_Execute("DELETE FROM user WHERE serverID = " + serverID + " AND userID = " + userID);
        DBServer.getServer(serverID).getUsers().delete(userID);
    }
}
