package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;

import static de.moinFT.main.Main.DBServer;

public class MemberLeaveListener implements ServerMemberLeaveListener {

    private Server Server = null;
    private long ServerID = 0;
    private User User = null;
    private long UserID = 0;

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        Server = event.getServer();
        ServerID = Server.getId();
        User = event.getUser();
        UserID = User.getId();

        DatabaseConnection.DBDeleteItem(ServerID + "_User", DBServer.getServer(ServerID).getUsers().getDB_ID(UserID));
        DBServer.getServer(ServerID).getUsers().delete(UserID);
    }
}
