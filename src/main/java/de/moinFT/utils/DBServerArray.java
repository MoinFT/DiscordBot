package de.moinFT.utils;

public class DBServerArray {
    private int ID;
    private int DB_ID;
    private long ServerID;
    private String prefix;
    private String musicBotPrefix;
    private DBUserArray Users;
    private DBRoleArray Roles;
    private DBChannelArray Channels;

    private boolean free;
    private DBServerArray follower;

    public DBServerArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.ServerID = 0;
        this.prefix = "";
        this.musicBotPrefix = "";
        this.Users = new DBUserArray();
        this.Roles = new DBRoleArray();
        this.Channels = new DBChannelArray();

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long serverID, String prefix, String musicBotPrefix) {
        if (this.free) {
            this.DB_ID = DB_ID;
            this.ServerID = serverID;
            this.prefix = prefix;
            this.musicBotPrefix = musicBotPrefix;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBServerArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, serverID, prefix, musicBotPrefix);
        }
    }

    public void delete(int ID) {
        if (this.ID == ID) {
            if (this.follower != null) {
                this.DB_ID = this.follower.DB_ID;
                this.ServerID = this.follower.DB_ID;
                this.prefix = this.follower.prefix;
                this.musicBotPrefix = this.follower.musicBotPrefix;
                this.Users = this.follower.Users;
                this.Roles = this.follower.Roles;
                this.Channels = this.follower.Channels;
            } else {
                this.DB_ID = 0;
                this.ServerID = 0;
                this.prefix = "";
                this.musicBotPrefix = "";
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

    public int getID(long ServerID) {
        if (this.ServerID == ServerID) {
            return this.ID;
        } else {
            if (this.follower != null) {
                return this.follower.getID(ServerID);
            } else {
                return -1;
            }
        }
    }

    public int getDB_ID(int ID) {
        if (this.ID == ID) {
            return this.DB_ID;
        } else {
            if (this.follower != null) {
                return this.follower.getDB_ID(ID);
            } else {
                return -1;
            }
        }
    }

    public int getDB_ID() {
        return this.DB_ID;
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

    public String getPrefix() {
        return this.prefix;
    }

    public String getMusicBotPrefix() {
        return this.musicBotPrefix;
    }

    public void updateMusicBotPrefix(String musicBotPrefix) {
        this.musicBotPrefix = musicBotPrefix;
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
