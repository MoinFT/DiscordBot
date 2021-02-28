package de.moinFT.utils;

public class DBUserArray {
    private int ID;
    private int DB_ID;
    private String UserID;
    private boolean botPermission;

    private boolean free;
    private DBUserArray follower;

    public DBUserArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.UserID = "";
        this.botPermission = false;

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, String UserID, boolean botPermission){
        if (this.free){
            this.DB_ID = DB_ID;
            this.UserID = UserID;
            this.botPermission = botPermission;

            this.free = false;
        } else {
            if (this.follower == null){
                this.follower = new DBUserArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, UserID, botPermission);
        }
    }

    public int getID(String UserID){
        if(this.UserID == UserID){
            return this.ID;
        } else {
            if (this.follower != null){
                return this.follower.getID(UserID);
            } else {
                return -1;
            }
        }
    }

    public int getDB_ID(int ID){
        if(this.ID == ID){
            return this.ID;
        } else {
            if (this.follower != null){
                return this.follower.getDB_ID(ID);
            } else {
                return -1;
            }
        }
    }

    public String getUserID(int ID){
        if(this.ID == ID){
            return this.UserID;
        } else {
            if (this.follower != null){
                return this.follower.getUserID(ID);
            } else {
                return "";
            }
        }
    }

    public int count(){
        if(!this.free){
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
