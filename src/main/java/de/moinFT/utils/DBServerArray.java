package de.moinFT.utils;

public class DBServerArray {
    private int ID;
    private long ServerID;
    private long CommandTimeoutTimestamp;
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
        this.ServerID = 0;
        this.CommandTimeoutTimestamp = 0;
        this.CommandTimeout = 0;
        this.Prefix = "";
        this.MusicBotPrefix = "";
        this.Users = new DBUserArray();
        this.Roles = new DBRoleArray();
        this.Channels = new DBChannelArray();

        this.free = true;
        this.follower = null;
    }

    public void setData(long ServerID, long CommandTimeoutTimestamp, int CommandTimeout, String Prefix, String MusicBotPrefix) {
        if (this.free) {
            this.ServerID = ServerID;
            this.CommandTimeoutTimestamp = CommandTimeoutTimestamp;
            this.CommandTimeout = CommandTimeout;
            this.Prefix = Prefix;
            this.MusicBotPrefix = MusicBotPrefix;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBServerArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(ServerID, CommandTimeoutTimestamp, CommandTimeout, Prefix, MusicBotPrefix);
        }
    }

    public void delete(int ID) {
        if (this.ID == ID) {
            if (this.follower != null) {
                this.ServerID = this.follower.ServerID;
                this.CommandTimeoutTimestamp = this.follower.CommandTimeoutTimestamp;
                this.CommandTimeout = this.follower.CommandTimeout;
                this.Prefix = this.follower.Prefix;
                this.MusicBotPrefix = this.follower.MusicBotPrefix;
                this.Users = this.follower.Users;
                this.Roles = this.follower.Roles;
                this.Channels = this.follower.Channels;
            } else {
                this.ServerID = 0;
                this.CommandTimeoutTimestamp = 0;
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

    public long getServerID() {
        return this.ServerID;
    }

    public long getCommandTimeout() {
        return this.CommandTimeout;
    }

    public void updateCommandTimeout(long CommandTimeout) {
        this.CommandTimeout = CommandTimeout;
    }

    public void updateCommandTimeoutTimestamp(long CommandTimeoutTimestamp) {
        this.CommandTimeoutTimestamp = CommandTimeoutTimestamp;
    }

    public long getCommandTimeoutTimestamp() {
        return this.CommandTimeoutTimestamp;
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
