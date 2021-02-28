package de.moinFT.main;

import de.moinFT.utils.Privates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static de.moinFT.main.Main.DBServer;

public class DatabaseConnection {

    private static final String database = "discordBot";

    public static void DBGetAllData(){
        try{
            ResultSet res;

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            res = statement.executeQuery("SELECT * FROM `server`");

            while (res.next()){
                DBServer.setData(res.getInt("id"), res.getString("serverID"));
            }

            for(int i = 0; i < DBServer.count(); i++){
                res = statement.executeQuery("SELECT * FROM `" + DBServer.getServerID(i) + "_User`");

                while (res.next()){
                    DBServer.getServer(i).getUsers().setData(res.getInt("id"), res.getString("userID"), res.getBoolean("botPermission"));
                }
            }

            res.close();
            statement.close();
            con.close();

        } catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("An error occured. Please try again1.");
        }
    }

    public static void DBAddItem(String table, String columns, String values){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            statement.execute("INSERT `" + table + "` " + columns + " VALUES " + values);

            statement.close();
            con.close();

        } catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("An error occured. Please try again.");
        }
    }

    public static void DBDeleteItem(String table, int DB_ID){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            Connection con = DriverManager.getConnection(Privates.DBConnectionURL, Privates.DBUser, Privates.DBPassword);

            Statement statement = con.createStatement();
            statement.execute("USE " + database);

            statement.execute("DELETE FROM `" + table + "` WHERE `id`= " + DB_ID);
            statement.close();
            con.close();

        } catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("An error occured. Please try again.");
        }
    }
}
