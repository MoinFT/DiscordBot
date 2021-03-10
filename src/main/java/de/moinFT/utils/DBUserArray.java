package de.moinFT.utils;

public class DBUserArray {
    private int ID;
    private int DB_ID;
    private long UserID;
    private boolean BotPermission;

    private boolean free;
    private DBUserArray follower;

    public DBUserArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.UserID = 0;
        this.BotPermission = false;

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long UserID, boolean botPermission) {
        if (this.free) {
            this.DB_ID = DB_ID;
            this.UserID = UserID;
            this.BotPermission = botPermission;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBUserArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, UserID, botPermission);
        }
    }

    public void updateBotPermission(long UserID, boolean BotPermission){
        if(this.UserID == UserID){
            this.BotPermission = BotPermission;
        } else {
            if (this.follower != null){
                this.follower.updateBotPermission(UserID, BotPermission);
            }
        }
    }

    public void delete(int ID) {
        if (this.ID == ID) {
            if(this.follower != null){
                this.DB_ID = this.follower.DB_ID;
                this.UserID = this.follower.UserID;
                this.BotPermission = this.follower.BotPermission;
            } else {
                this.DB_ID = 0;
                this.UserID = 0;
                this.BotPermission = false;

                this.free = true;
            }
        } else {
            if (this.follower != null) {
                this.follower.delete(ID);
            }
        }
    }

    public void delete(long UserID) {
        if (this.UserID == UserID) {
            if(this.follower != null){
                this.DB_ID = this.follower.DB_ID;
                this.UserID = this.follower.UserID;
                this.BotPermission = this.follower.BotPermission;
            } else {
                this.DB_ID = 0;
                this.UserID = 0;
                this.BotPermission = false;

                this.free = true;
            }
        } else {
            if (this.follower != null) {
                this.follower.delete(UserID);
            }
        }
    }

    public int getID(long UserID) {
        if (this.UserID == UserID) {
            return this.ID;
        } else {
            if (this.follower != null) {
                return this.follower.getID(UserID);
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

    public int getDB_ID(long UserID) {
        if (this.UserID == UserID) {
            return this.DB_ID;
        } else {
            if (this.follower != null) {
                return this.follower.getDB_ID(UserID);
            } else {
                return -1;
            }
        }
    }

    public long getUserID(int ID) {
        if (this.ID == ID) {
            return this.UserID;
        } else {
            if (this.follower != null) {
                return this.follower.getUserID(ID);
            } else {
                return 0;
            }
        }
    }

    public boolean getBotPermission(long UserID) {
        if (this.UserID == UserID) {
            return this.BotPermission;
        } else {
            if (this.follower != null) {
                return this.follower.getBotPermission(UserID);
            } else {
                return false;
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
