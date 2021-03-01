package de.moinFT.utils;

public class DBRoleArray {
    private int ID;
    private int DB_ID;
    private String roleID;
    private String roleName;
    private String roleType;

    private boolean free;
    private DBRoleArray follower;

    public DBRoleArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.roleID = "";
        this.roleName = "";
        this.roleType = "";

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, String roleID, String roleType, String roleName){
        if (this.free){
            this.DB_ID = DB_ID;
            this.roleID = roleID;
            this.roleName = roleName;
            this.roleType = roleType;

            this.free = false;
        } else {
            if (this.follower == null){
                this.follower = new DBRoleArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, roleID, roleType, roleName);
        }
    }

    public String getRoleID(int ID){
        if (this.ID == ID){
            return this.roleID;
        } else {
            if (this.follower != null){
                return this.follower.getRoleID(ID);
            } else {
                return "";
            }
        }
    }

    public String getRoleName(int ID){
        if (this.ID == ID){
            return this.roleName;
        } else {
            if (this.follower != null){
                return this.follower.getRoleName(ID);
            } else {
                return "";
            }
        }
    }

    public String getRoleType(int ID){
        if (this.ID == ID){
            return this.roleType;
        } else {
            if (this.follower != null){
                return this.follower.getRoleType(ID);
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
