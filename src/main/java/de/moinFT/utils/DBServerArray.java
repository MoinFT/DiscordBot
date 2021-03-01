package de.moinFT.utils;

public class DBServerArray {
    private int ID;
    private int DB_ID;
    private long ServerID;
    private DBUserArray Users;
    private DBRoleArray Roles;
    private DBChannelArray Channels;

    private boolean free;
    private DBServerArray follower;

    public DBServerArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.ServerID = 0;
        this.Users = new DBUserArray();
        this.Roles = new DBRoleArray();
        this.Channels = new DBChannelArray();

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long ServerID) {
        if (this.free) {
            this.DB_ID = DB_ID;
            this.ServerID = ServerID;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBServerArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, ServerID);
        }
    }

    public DBServerArray getServer(int ID) {
        if (this.ID == ID) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getServer(ID);
            } else {
                return null;
            }
        }
    }

    public DBServerArray getServer(long ServerID) {
        if (this.ServerID == ServerID) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getServer(ServerID);
            } else {
                return null;
            }
        }
    }

    public long getServerID(int ID) {
        if (this.ID == ID) {
            return this.ServerID;
        } else {
            if (this.follower != null) {
                return this.follower.getServerID(ID);
            } else {
                return 0;
            }
        }
    }

    public DBUserArray getUsers() {
        return this.Users;
    }

    public DBRoleArray getRoles() {
        return this.Roles;
    }

    public DBChannelArray getChannels() {
        return this.Channels;
    }

    public int count() {
        if (!this.free) {
            if (this.follower != null) {
                return 1 + this.follower.count();
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }
}
