package de.moinFT.main.Server;

import de.moinFT.main.DatabaseConnection;
import de.moinFT.utils.DBChannelArray;
import de.moinFT.utils.DBRoleArray;

import static de.moinFT.main.Main.DBServer;

public class CommandManagement {
    private final long ServerID;
    private final String BotCommand;
    private final String BotArguments;
    private String Error = "";

    public CommandManagement(long serverID, String botCommand, String botArguments) {
        this.ServerID = serverID;
        this.BotCommand = botCommand;
        this.BotArguments = botArguments;
    }

    public String getError() {
        return this.Error;
    }

    public boolean command() {
        if (this.BotCommand.equals("channelSet")) {
            long channelID;
            String channelName;

            try {
                channelID = Long.parseLong(this.BotArguments.split("&")[0]);
            } catch (Exception e) {
                System.out.println("Error: No channelID found in the command!");
                this.Error = "No channelID found in the command!";
                return false;
            }

            try {
                channelName = this.BotArguments.split("&")[1];
                if (channelName.equals("_")) {
                    channelName = "";
                }
            } catch (Exception e) {
                System.out.println("Error: No channelName found in the command!");
                this.Error = "No channelName found in the command!";
                return false;
            }

            if (!channelName.equals("")) {
                DBChannelArray channelArray = DBServer.getServer(ServerID).getChannels();

                if (channelArray.getChannel(channelID).getChannelName().equals(channelName)) {
                    System.out.println("Error: ChannelName exist already in DB!");
                    this.Error = "ChannelName exist already in DB!";
                    return false;
                }
            }

            DBServer.getServer(ServerID).getChannels().getChannel(channelID).updateChannelName(channelName);
            DatabaseConnection.DBUpdateItem(ServerID + "_Channel", DBServer.getServer(ServerID).getChannels().getChannel(channelID).getDB_ID(), "`channelName` = '" + channelName + "'");
            return true;
        } else if (this.BotCommand.equals("roleSet")) {
            long roleID;
            String roleType;
            String roleName;

            try {
                roleID = Long.parseLong(this.BotArguments.split("&")[0]);
            } catch (Exception e) {
                System.out.println("Error: No roleMention found in the command!");
                this.Error = "No roleMention found in the command!";
                return false;
            }

            try {
                roleType = this.BotArguments.split("&")[1];
                if (roleType.equals("_")) {
                    roleType = "";
                }
            } catch (Exception e) {
                System.out.println("Error: No roleType found in the command!");
                this.Error = "No roleType found in the command!";
                return false;
            }

            try {
                roleName = this.BotArguments.split("&")[2];
                if (roleName.equals("_")) {
                    roleName = "";
                }
            } catch (Exception e) {
                System.out.println("Error: No roleName found in the command!");
                this.Error = "No roleName found in the command!";
                return false;
            }

            if (!roleName.equals("") && !roleType.equals("")) {
                DBRoleArray rolesArray = DBServer.getServer(ServerID).getRoles();

                if (rolesArray.getRole(roleID).getRoleName().equals(roleName)) {
                    System.out.println("Error: RoleName exist already in DB!");
                    this.Error = "RoleName exist already in DB!";
                    return false;
                }
            }

            DBServer.getServer(ServerID).getRoles().getRole(roleID).updateRoleType(roleType);
            DBServer.getServer(ServerID).getRoles().getRole(roleID).updateRoleName(roleName);
            DatabaseConnection.DBUpdateItem(ServerID + "_Role", DBServer.getServer(ServerID).getRoles().getRole(roleID).getDB_ID(), "`roleType` = '" + roleType + "'");
            DatabaseConnection.DBUpdateItem(ServerID + "_Role", DBServer.getServer(ServerID).getRoles().getRole(roleID).getDB_ID(), "`roleName` = '" + roleName + "'");
            return true;
        }
        return false;
    }
}
