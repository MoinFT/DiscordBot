package de.moinFT.utils;

public class DBRoleArray {
    private int ID;
    private int DB_ID;
    private long RoleID;
    private String RoleName;
    private String RoleType;

    private boolean free;
    private DBRoleArray follower;

    public DBRoleArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.RoleID = 0;
        this.RoleName = "";
        this.RoleType = "";

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long RoleID, String roleType, String roleName){
        if (this.free){
            this.DB_ID = DB_ID;
            this.RoleID = RoleID;
            this.RoleName = roleName;
            this.RoleType = roleType;

            this.free = false;
        } else {
            if (this.follower == null){
                this.follower = new DBRoleArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, RoleID, roleType, roleName);
        }
    }

    public void updateRoleType(long RoleID, String roleType){
        if(this.RoleID == RoleID){
            this.RoleType = roleType;
        } else {
            if (this.follower != null){
                this.follower.updateRoleType(RoleID, roleType);
            }
        }
    }

    public void updateRoleName(long RoleID, String roleName){
        if(this.RoleID == RoleID){
            this.RoleName = roleName;
        } else {
            if (this.follower != null){
                this.follower.updateRoleName(RoleID, roleName);
            }
        }
    }

    public void delete(long RoleID) {
        if (this.RoleID == RoleID) {
            if(this.follower != null){
                this.DB_ID = this.follower.DB_ID;
                this.RoleID = this.follower.RoleID;
                this.RoleType = this.follower.RoleType;
                this.RoleName = this.follower.RoleName;
            } else {
                this.DB_ID = 0;
                this.RoleID = 0;
                this.RoleType = "";
                this.RoleName = "";

                this.free = true;
            }
        } else {
            if (this.follower != null) {
                this.follower.delete(RoleID);
            }
        }
    }

    public int getID(long RoleID){
        if(this.RoleID == RoleID){
            return this.ID;
        } else {
            if (this.follower != null){
                return this.follower.getID(RoleID);
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

    public int getDB_ID(long RoleID){
        if (this.RoleID == RoleID){
            return this.DB_ID;
        } else {
            if (this.follower != null){
                return this.follower.getDB_ID(RoleID);
            } else {
                return 0;
            }
        }
    }

    public long getRoleID(int ID){
        if (this.ID == ID){
            return this.RoleID;
        } else {
            if (this.follower != null){
                return this.follower.getRoleID(ID);
            } else {
                return 0;
            }
        }
    }

    public String getRoleName(int ID){
        if (this.ID == ID){
            return this.RoleName;
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
            return this.RoleType;
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
