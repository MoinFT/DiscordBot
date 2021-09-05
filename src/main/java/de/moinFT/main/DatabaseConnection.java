package de.moinFT.main;

import de.moinFT.utils.Privates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static de.moinFT.main.Main.DBServer;

public class DatabaseConnection {

    private static final String database = "discordBot";

    public static void DBGetAllData() {
        try {
            ResultSet res;

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            res = statement.executeQuery("SELECT * FROM `server`");

            while (res.next()) {
                DBServer.setData(res.getInt("id"), res.getLong("serverID"), res.getLong("commandTimeoutTimestamp"), res.getInt("commandTimeout"), res.getString("prefix"), res.getString("musicBotPrefix"));
            }

            for (int i = 0; i < DBServer.count(); i++) {
                res = statement.executeQuery("SELECT * FROM `" + DBServer.getServer(i).getServerID() + "_User`");

                while (res.next()) {
                    DBServer.getServer(i).getUsers().setData(res.getInt("id"), res.getLong("userID"), res.getBoolean("isAdmin"), res.getBoolean("botPermission"));
                }
            }

            for (int i = 0; i < DBServer.count(); i++) {
                res = statement.executeQuery("SELECT * FROM `" + DBServer.getServer(i).getServerID() + "_Role`");

                while (res.next()) {
                    DBServer.getServer(i).getRoles().setData(res.getInt("id"), res.getLong("roleID"), res.getString("roleType"), res.getString("roleName"));
                }
            }

            for (int i = 0; i < DBServer.count(); i++) {
                res = statement.executeQuery("SELECT * FROM `" + DBServer.getServer(i).getServerID() + "_Channel`");

                while (res.next()) {
                    DBServer.getServer(i).getChannels().setData(res.getInt("id"), res.getLong("channelID"), res.getString("channelType"), res.getString("channelName"));
                }
            }

            res.close();
            statement.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("An error occurred. Please try again.");
        }
    }

    public static int DBGetDB_ID(String table, String compareColumn, String compareValue) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            ResultSet res = statement.executeQuery("SELECT * FROM `" + table + "` WHERE `" + compareColumn + "` = '" + compareValue + "'");

            res.next();
            int result = res.getInt("id");

            res.close();
            statement.close();
            con.close();

            return result;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("An error occurred. Please try again.");
            return 0;
        }
    }

    public static void DBAddItem(String table, String columns, String values) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            statement.execute("INSERT `" + table + "` " + columns + " VALUES " + values);

            statement.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("An error occurred. Please try again.");
        }
    }

    public static void DBUpdateItem(String table, int DB_ID, String columnsAndValues) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            statement.execute("UPDATE `" + table + "` SET " + columnsAndValues + " WHERE `id`= " + DB_ID);
            statement.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("An error occurred. Please try again.");
        }
    }

    public static void DBDeleteItem(String table, int DB_ID) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            statement.execute("DELETE FROM `" + table + "` WHERE `id`= " + DB_ID);
            statement.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("An error occurred. Please try again.");
        }
    }

    public static void DB_SQL_Execute(String sqlString) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            statement.execute(sqlString);
            statement.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("An error occurred. Please try again.");
        }
    }
}
