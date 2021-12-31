package de.moinFT.main.listener.member;

import de.moinFT.main.Functions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

import static de.moinFT.main.Main.DBServer;

public class MemberJoinListener implements ServerMemberJoinListener {

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        Server Server = event.getServer();
        long ServerID = Server.getId();
        User User = event.getUser();

        if (!User.isBot()) {
            int DBRoleCount = DBServer.getServer(ServerID).getRoles().count();

            for (int i = 0; i < DBRoleCount; i++) {
                if (DBServer.getServer(ServerID).getRoles().getRole(i).getRoleType().equalsIgnoreCase("user")) {
                    Role role = Server.getRoleById(DBServer.getServer(ServerID).getRoles().getRole(i).getRoleID()).get();

                    User.addRole(role);
                }
            }
        }

        Functions.addUserToDB(ServerID, User);
    }
}
