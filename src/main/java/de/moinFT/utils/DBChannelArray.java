package de.moinFT.utils;

public class DBChannelArray {
    private int ID;
    private int DB_ID;
    private long ChannelID;
    private String DiscordChannelName;
    private String ChannelType;
    private String ChannelName;

    private boolean free;
    private DBChannelArray follower;

    public DBChannelArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.ChannelID = 0;
        this.DiscordChannelName = "";
        this.ChannelType = "";
        this.ChannelName = "";

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long ChannelID, String DiscordChannelName, String ChannelType, String ChannelName) {
        if (this.free) {
            this.DB_ID = DB_ID;
            this.ChannelID = ChannelID;
            this.DiscordChannelName = DiscordChannelName;
            this.ChannelType = ChannelType;
            this.ChannelName = ChannelName;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBChannelArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, ChannelID, DiscordChannelName, ChannelType, ChannelName);
        }
    }

    public void delete(long ChannelID) {
        if (this.ChannelID == ChannelID) {
            if (this.follower != null) {
                this.DB_ID = this.follower.DB_ID;
                this.ChannelID = this.follower.ChannelID;
                this.DiscordChannelName = this.follower.DiscordChannelName;
                this.ChannelType = this.follower.ChannelType;
                this.ChannelName = this.follower.ChannelName;
            } else {
                this.DB_ID = 0;
                this.ChannelID = 0;
                this.DiscordChannelName = "";
                this.ChannelType = "";
                this.ChannelName = "";
                this.free = true;
            }
        } else {
            if (this.follower != null) {
                this.follower.delete(ChannelID);
            }
        }
    }

    public DBChannelArray getChannel(long ChannelID) {
        if (this.ChannelID == ChannelID) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getChannel(ChannelID);
            } else {
                return null;
            }
        }
    }

    public DBChannelArray getChannel(int ID) {
        if (this.ID == ID) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getChannel(ID);
            } else {
                return null;
            }
        }
    }

    public DBChannelArray getChannel(String ChannelName) {
        if (this.ChannelName.equals(ChannelName)) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getChannel(ChannelName);
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

    public long getChannelID() {
        return this.ChannelID;
    }

    public String getDiscordChannelName() {
        return this.DiscordChannelName;
    }

    public String getChannelType() {
        return this.ChannelType;
    }

    public void updateChannelName(String ChannelName) {
        this.ChannelName = ChannelName;
    }

    public String getChannelName() {
        return this.ChannelName;
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
