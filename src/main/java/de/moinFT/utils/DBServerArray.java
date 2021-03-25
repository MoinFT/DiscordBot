package de.moinFT.utils;

public class DBServerArray {
    private int ID;
    private int DB_ID;
    private long ServerID;
    private long CommandTimeout;
    private String Prefix;
    private String MusicBotPrefix;
    private DBUserArray Users;
    private DBRoleArray Roles;
    private DBChannelArray Channels;

    private boolean free;
    private DBServerArray follower;

    public DBServerArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.ServerID = 0;
        this.CommandTimeout = 0;
        this.Prefix = "";
        this.MusicBotPrefix = "";
        this.Users = new DBUserArray();
        this.Roles = new DBRoleArray();
        this.Channels = new DBChannelArray();

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long ServerID, long CommandTimeout, String Prefix, String MusicBotPrefix) {
        if (this.free) {
            this.DB_ID = DB_ID;
            this.ServerID = ServerID;
            this.CommandTimeout = CommandTimeout;
            this.Prefix = Prefix;
            this.MusicBotPrefix = MusicBotPrefix;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBServerArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, ServerID, CommandTimeout, Prefix, MusicBotPrefix);
        }
    }

    public void delete(int ID) {
        if (this.ID == ID) {
            if (this.follower != null) {
                this.DB_ID = this.follower.DB_ID;
                this.ServerID = this.follower.ServerID;
                this.CommandTimeout = this.follower.CommandTimeout;
                this.Prefix = this.follower.Prefix;
                this.MusicBotPrefix = this.follower.MusicBotPrefix;
                this.Users = this.follower.Users;
                this.Roles = this.follower.Roles;
                this.Channels = this.follower.Channels;
            } else {
                this.DB_ID = 0;
                this.ServerID = 0;
                this.CommandTimeout = 0;
                this.Prefix = "";
                this.MusicBotPrefix = "";
                this.Users = new DBUserArray();
                this.Roles = new DBRoleArray();
                this.Channels = new DBChannelArray();
            }
        } else {
            if (this.follower != null) {
                this.follower.delete(ID);
            }
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

    public int getID() {
        return this.ID;
    }

    public int getDB_ID() {
        return this.DB_ID;
    }

    public long getServerID() {
        return this.ServerID;
    }

    public void updateCommandTimeout(long CommandTimeout) {
        this.CommandTimeout = CommandTimeout;
    }

    public long getCommandTimeout() {
        return this.CommandTimeout;
    }

    public String getPrefix() {
        return this.Prefix;
    }

    public String getMusicBotPrefix() {
        return this.MusicBotPrefix;
    }

    public void updateMusicBotPrefix(String MusicBotPrefix) {
        this.MusicBotPrefix = MusicBotPrefix;
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
