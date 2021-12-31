package de.moinFT.utils;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class ServerUserRequest {
    private String Type;
    private Server Server;
    private User User;
    private long TimeStamp;

    private boolean free;
    private ServerUserRequest follower;

    public ServerUserRequest() {
        this.Type = "";
        this.Server = null;
        this.User = null;
        this.TimeStamp = 0;

        this.free = true;
        this.follower = null;
    }

    public void setData(String Type, Server Server, User User, long TimeStamp) {
        if (this.free || (this.Type.equals(Type) && this.Server.getId() == Server.getId())) {
            this.Type = Type;
            this.Server = Server;
            this.User = User;
            this.TimeStamp = TimeStamp;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new ServerUserRequest();
            }

            this.follower.setData(Type, Server, User, TimeStamp);
        }
    }

    public User getUser(String Type, Server Server) {
        if (this.Type.equals(Type) && this.Server.getId() == Server.getId()) {
            if (!this.free) {
                return this.User;
            }
        } else {
            if (this.follower != null) {
                this.follower.getUser(Type, Server);
            }
        }

        return null;
    }

    public long getTimeStamp(String Type, Server Server) {
        if (this.Type.equals(Type) && this.Server.getId() == Server.getId()) {
            if (!this.free) {
                return this.TimeStamp;
            }
        } else {
            if (this.follower != null) {
                this.follower.getTimeStamp(Type, Server);
            }
        }

        return 0;
    }
}
