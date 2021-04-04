package de.moinFT.utils;

public class DBRoleArray {
    private int ID;
    private int DB_ID;
    private long RoleID;
    private String DiscordRoleName;
    private String RoleName;
    private String RoleType;

    private boolean free;
    private DBRoleArray follower;

    public DBRoleArray() {
        this.ID = 0;
        this.DB_ID = 0;
        this.RoleID = 0;
        this.DiscordRoleName = "";
        this.RoleName = "";
        this.RoleType = "";

        this.free = true;
        this.follower = null;
    }

    public void setData(int DB_ID, long RoleID, String DiscordRoleName, String roleType, String roleName) {
        if (this.free) {
            this.DB_ID = DB_ID;
            this.RoleID = RoleID;
            this.DiscordRoleName = DiscordRoleName;
            this.RoleName = roleName;
            this.RoleType = roleType;

            this.free = false;
        } else {
            if (this.follower == null) {
                this.follower = new DBRoleArray();
                this.follower.ID = this.ID + 1;
            }

            this.follower.setData(DB_ID, RoleID, DiscordRoleName, roleType, roleName);
        }
    }

    public void delete(long RoleID) {
        if (this.RoleID == RoleID) {
            if (this.follower != null) {
                this.DB_ID = this.follower.DB_ID;
                this.RoleID = this.follower.RoleID;
                this.DiscordRoleName = this.follower.DiscordRoleName;
                this.RoleType = this.follower.RoleType;
                this.RoleName = this.follower.RoleName;
            } else {
                this.DB_ID = 0;
                this.RoleID = 0;
                this.DiscordRoleName = "";
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

    public int getDB_ID() {
        return this.DB_ID;
    }

    public long getRoleID() {
        return this.RoleID;
    }

    public void updateRoleName(String roleName) {
            this.RoleName = roleName;
    }

    public String getDiscordRoleName() {
        return this.DiscordRoleName;
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
