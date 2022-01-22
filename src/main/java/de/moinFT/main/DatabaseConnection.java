package de.moinFT.main;

import de.moinFT.utils.BotRoleType;
import de.moinFT.utils.Privates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static de.moinFT.main.Main.DBServer;

public class DatabaseConnection {
    private static final Logger log = LogManager.getLogger(DatabaseConnection.class.getName());

    private static final String database = "discordBot";

    public static void DBGetAllData() {
        try {
            ResultSet res;

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            res = statement.executeQuery("SELECT * FROM server");

            while (res.next()) {
                DBServer.setData(res.getLong("serverID"), res.getLong("commandTimeoutTimestamp"), res.getInt("commandTimeout"), res.getString("prefix"), res.getString("musicBotPrefix"));
            }

            for (int i = 0; i < DBServer.count(); i++) {
                long serverID = DBServer.getServer(i).getServerID();

                res = statement.executeQuery("SELECT * FROM user WHERE serverID = " + serverID);

                while (res.next()) {
                    DBServer.getServer(i).getUsers().setData(res.getLong("serverID"), res.getLong("userID"), res.getBoolean("isAdmin"), res.getBoolean("botPermission"));
                }
            }

            for (int i = 0; i < DBServer.count(); i++) {
                long serverID = DBServer.getServer(i).getServerID();

                res = statement.executeQuery("SELECT * FROM role WHERE serverID = " + serverID);

                while (res.next()) {
                    DBServer.getServer(i).getRoles().setData(res.getLong("serverID"), res.getLong("roleID"), BotRoleType.fromValue(res.getInt("roleType")), res.getString("roleName"));
                }
            }

            for (int i = 0; i < DBServer.count(); i++) {
                long serverID = DBServer.getServer(i).getServerID();

                res = statement.executeQuery("SELECT * FROM channel WHERE serverID = " + serverID);

                while (res.next()) {
                    DBServer.getServer(i).getChannels().setData(res.getLong("serverID"), res.getLong("channelID"), res.getString("channelType"), res.getString("channelName"));
                }
            }

            res.close();
            statement.close();
            con.close();

        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("An error occurred. Please try again.");
        }
    }

    public static void SQL_Execute(String sqlString) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            statement.execute(sqlString);
            statement.close();
            con.close();

        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("An error occurred. Please try again.");
        }
    }
}
