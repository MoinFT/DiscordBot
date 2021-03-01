package de.moinFT.utils;

public class DBChannelArray {
    private int ID;
    private int DB_ID;
    private long channelID;
    private String channelType;
    private String channelName;

    private boolean free;
    private DBChannelArray follower;

    public DBChannelArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.channelID = 0;
        this.channelType = "";
        this.channelName = "";

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long channelID, String channelType, String channelName){
        if (this.free){
            this.DB_ID = DB_ID;
            this.channelID = channelID;
            this.channelType = channelType;
            this.channelName = channelName;

            this.free = false;
        } else {
            if (this.follower == null){
                this.follower = new DBChannelArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, channelID, channelType, channelName);
        }
    }

    public void updateChannelName(long channelID, String channelName){
        if(this.channelID == channelID){
            this.channelName = channelName;
        } else {
            if (this.follower != null){
                this.follower.updateChannelName(channelID, channelName);
            }
        }
    }

    public int getID(long channelID){
        if(this.channelID == channelID){
            return this.ID;
        } else {
            if (this.follower != null){
                return this.follower.getID(channelID);
            } else {
                return -1;
            }
        }
    }

    public int getID(String channelName){
        if(this.channelName.equalsIgnoreCase(channelName)){
            return this.ID;
        } else {
            if (this.follower != null){
                return this.follower.getID(channelName);
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

    public int getDB_ID(long channelID){
        if(this.channelID == channelID){
            return this.DB_ID;
        } else {
            if (this.follower != null){
                return this.follower.getDB_ID(channelID);
            } else {
                return -1;
            }
        }
    }

    public int getDB_ID(String channelName){
        if(this.channelName.equalsIgnoreCase(channelName)){
            return this.DB_ID;
        } else {
            if (this.follower != null){
                return this.follower.getDB_ID(channelName);
            } else {
                return -1;
            }
        }
    }

    public long getChannelID(int ID){
        if (this.ID == ID){
            return this.channelID;
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
            return this.channelName;
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
