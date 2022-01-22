package de.moinFT.main.listener.member;

import de.moinFT.main.Functions;
import de.moinFT.utils.BotRoleType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

import static de.moinFT.main.Main.DBServer;

public class MemberJoinListener implements ServerMemberJoinListener {

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        Server server = event.getServer();
        long serverID = server.getId();
        User user = event.getUser();

        if (!user.isBot()) {
            int DBRoleCount = DBServer.getServer(serverID).getRoles().count();

            for (int i = 0; i < DBRoleCount; i++) {
                if (DBServer.getServer(serverID).getRoles().getRole(i).getRoleType() == BotRoleType.USER) {
                    Role role = server.getRoleById(DBServer.getServer(serverID).getRoles().getRole(i).getRoleID()).get();

                    user.addRole(role);
                }
            }
        }

        Functions.addUserToDB(serverID, user);
    }
}
