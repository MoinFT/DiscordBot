package de.moinFT.utils;

public class DBChannelArray {
    private int ID;
    private int DB_ID;
    private long ChannelID;
    private String ChannelType;
    private String ChannelName;

    private boolean free;
    private DBChannelArray follower;

    public DBChannelArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.ChannelID = 0;
        this.ChannelType = "";
        this.ChannelName = "";

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long ChannelID, String ChannelType, String ChannelName){
        if (this.free){
            this.DB_ID = DB_ID;
            this.ChannelID = ChannelID;
            this.ChannelType = ChannelType;
            this.ChannelName = ChannelName;

            this.free = false;
        } else {
            if (this.follower == null){
                this.follower = new DBChannelArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, ChannelID, ChannelType, ChannelName);
        }
    }

    public void updateChannelName(long ChannelID, String ChannelName){
        if(this.ChannelID == ChannelID){
            this.ChannelName = ChannelName;
        } else {
            if (this.follower != null){
                this.follower.updateChannelName(ChannelID, ChannelName);
            }
        }
    }

    public void delete(long ChannelID) {
        if (this.ChannelID == ChannelID) {
            if(this.follower != null){
                this.DB_ID = this.follower.DB_ID;
                this.ChannelID = this.follower.ChannelID;
                this.ChannelType = this.follower.ChannelType;
                this.ChannelName = this.follower.ChannelName;
            } else {
                this.DB_ID = 0;
                this.ChannelID = 0;
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

    public int getID(long ChannelID){
        if(this.ChannelID == ChannelID){
            return this.ID;
        } else {
            if (this.follower != null){
                return this.follower.getID(ChannelID);
            } else {
                return -1;
            }
        }
    }

    public int getID(String ChannelName){
        if(this.ChannelName.equalsIgnoreCase(ChannelName)){
            return this.ID;
        } else {
            if (this.follower != null){
                return this.follower.getID(ChannelName);
            } else {
                return -1;
            }
        }
    }

    public int getDB_ID(int ID){
        if (this.ID == ID){
            return this.DB_ID;
        } else {
            if (this.follower != null){
                return this.follower.getDB_ID(ID);
            } else {
                return 0;
            }
        }
    }

    public int getDB_ID(long ChannelID){
        if(this.ChannelID == ChannelID){
            return this.DB_ID;
        } else {
            if (this.follower != null){
                return this.follower.getDB_ID(ChannelID);
            } else {
                return -1;
            }
        }
    }

    public long getChannelID(int ID){
        if (this.ID == ID){
            return this.ChannelID;
        } else {
            if (this.follower != null){
                return this.follower.getChannelID(ID);
            } else {
                return 0;
            }
        }
    }

    public String getChannelName(int ID){
        if (this.ID == ID){
            return this.ChannelName;
        } else {
            if (this.follower != null){
                return this.follower.getChannelName(ID);
            } else {
                return "";
            }
        }
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
