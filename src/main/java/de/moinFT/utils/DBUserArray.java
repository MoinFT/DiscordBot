package de.moinFT.utils;

public class DBUserArray {
    private int ID;
    private int DB_ID;
    private long UserID;
    private boolean IsAdmin;
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

    public void setData(int DB_ID, long UserID, boolean IsAdmin, boolean BotPermission) {
        if (this.free) {
            this.DB_ID = DB_ID;
            this.UserID = UserID;
            this.IsAdmin = IsAdmin;
            this.BotPermission = BotPermission;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBUserArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, UserID, IsAdmin, BotPermission);
        }
    }

    public void delete(int ID) {
        if (this.ID == ID) {
            if (this.follower != null) {
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
            if (this.follower != null) {
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

    public DBUserArray getUser(long UserID) {
        if (this.UserID == UserID) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getUser(UserID);
            } else {
                return null;
            }
        }
    }

    public DBUserArray getUser(int ID) {
        if (this.ID == ID) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getUser(ID);
            } else {
                return null;
            }
        }
    }

    public int getDB_ID() {
        return this.DB_ID;
    }

    public long getUserID() {
        return this.UserID;
    }

    public void updateIsAdmin(boolean IsAdmin) {
        this.IsAdmin = IsAdmin;
    }

    public boolean getIsAdmin() {
        return this.IsAdmin;
    }

    public void updateBotPermission(boolean BotPermission) {
        this.BotPermission = BotPermission;
    }

    public boolean getBotPermission() {
        return this.BotPermission;
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
