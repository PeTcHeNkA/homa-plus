package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class SqLite {
    public static Connection conn;
    public static Statement statmt;
    public static ResultSet resSet;

    public static void Conn() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:Chat_data.s3db");
            statmt = conn.createStatement();
            System.out.println("Connecting Successful");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public static void CreateDB() {
        try {
            statmt.execute("CREATE TABLE if not exists 'users' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text, 'prefix' text, 'info' text, 'color' text, 'avatar' int, 'linc' text);");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void removeData(String name) {
        try {
            statmt.execute(String.format("DELETE FROM 'users' WHERE name = '%s' ", name));
            //System.out.println("Data from "+player.getName()+" removed");
        } catch (SQLException throwables) {

        }

    }

    public static ChatUser loadChatUser(String name){

        ChatUser user = new ChatUser(name);

        String prefix = "null";
        String color = "&f";
        String PrefixLore = "null";
        String Linc = "null";
        int avatar = 4097;
        try {
            ResultSet result = statmt.executeQuery(String.format("SELECT * FROM users WHERE name = '%s' ", name));
            prefix = result.getString("prefix");
            color = result.getString("color");

            PrefixLore = result.getString("info");

            avatar = result.getInt("avatar");
        } catch (SQLException err) {
        }

        if(!prefix.equals("null")) {
            if(PrefixLore.equals("null")) {
                user.setPrefix(prefix);
            } else {
                user.setPrefix(prefix, PrefixLore);
            }
        }

        if(!Linc.equals("null")) {

            user.setLinc(Linc);

        }

        user.setColor(color);
        user.setAvatar(avatar);

        return user;
    }
    public static void saveChatUser(ChatUser user){

        //System.out.println(user.getAvatarID());
        //System.out.println(String.valueOf(user.getAvatarID()));
        removeData(user.toPlayerName());

        try {

            statmt.execute(String.format("INSERT INTO 'users' ('name', 'prefix', 'info', 'color', 'avatar', 'linc') VALUES ('%s', '%s', '%s', '%s', '%s', '%s')",
                    user.toPlayerName(),
                    user.getTextprefix(),
                    user.getLorePrefix(),
                    user.getColor(),
                    String.valueOf(user.getAvatarID()),
                    user.getLinc()
                    )
            );

        } catch (SQLException err) {
            err.printStackTrace();
        }
    }



}