package de.moinFT.utils;

public class DBRoleArray {
    private int ID;
    private long ServerID;
    private long RoleID;
    private String RoleName;
    private String RoleType;

    private boolean free;
    private DBRoleArray follower;

    public DBRoleArray() {
        this.ID = 0;
        this.ServerID = 0;
        this.RoleID = 0;
        this.RoleName = "";
        this.RoleType = "";

        this.free = true;
        this.follower = null;
    }

    public void setData(long ServerID, long RoleID, String roleType, String roleName) {
        if (this.free) {
            this.ServerID = ServerID;
            this.RoleID = RoleID;
            this.RoleName = roleName;
            this.RoleType = roleType;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBRoleArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(ServerID, RoleID, roleType, roleName);
        }
    }

    public void delete(long RoleID) {
        if (this.RoleID == RoleID) {
            if (this.follower != null) {
                this.ServerID = this.follower.ServerID;
                this.RoleID = this.follower.RoleID;
                this.RoleType = this.follower.RoleType;
                this.RoleName = this.follower.RoleName;
            } else {
                this.ServerID = 0;
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

    public DBRoleArray getRole(long RoleID) {
        if (this.RoleID == RoleID) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getRole(RoleID);
            } else {
                return null;
            }
        }
    }

    public DBRoleArray getRole(int ID) {
        if (this.ID == ID) {
            return this;
        } else {
            if (this.follower != null) {
                return this.follower.getRole(ID);
            } else {
                return null;
            }
        }
    }

    public long getRoleID() {
        return this.RoleID;
    }

    public void updateRoleName(String roleName) {
            this.RoleName = roleName;
    }

    public String getRoleName() {
        return this.RoleName;
    }

    public void updateRoleType(String roleType) {
            this.RoleType = roleType;
    }

    public String getRoleType() {
        return this.RoleType;
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
