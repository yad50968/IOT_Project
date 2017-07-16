package liutzuyuan.gateway;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static liutzuyuan.gateway.Setting.sqlaccount;
import static liutzuyuan.gateway.Setting.sqlip;
import static liutzuyuan.gateway.Setting.sqlname;
import static liutzuyuan.gateway.Setting.sqlpassword;


class DBHelper {


    private Connection con = null;


    DBHelper() {
        new Thread(){
            public void run() {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    String sqlurl = "jdbc:mysql://" + sqlip + "/" + sqlname + "?useUnicode=true&characterEncoding=utf-8";

                    con = DriverManager.getConnection(sqlurl, sqlaccount, sqlpassword);
                }
                catch(ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    boolean addUserData(User user) {

        String insertdbSQL = "insert ignore into User_data(UserID, IDX, XPV, UKP, Ku) VALUES" +
                "(?, ?, ?, ?, ?);";
        int c = 0;
        try {
            PreparedStatement preparedStatement = con.prepareStatement(insertdbSQL);
            preparedStatement.setString(1, user.getID());
            preparedStatement.setString(2, user.getIDX());
            preparedStatement.setString(3, user.getXPV());
            preparedStatement.setString(4, user.getUKP());
            preparedStatement.setString(5, user.getKu());
            c = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c == 1;
    }


    User selectUserData(String ID) {

        User userData = null;
        try {
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM User_data WHERE UserID=\"" + ID +"\"";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                userData = new User(rs.getString("UserID"), rs.getString("IDX"), rs.getString("XPV"), rs.getString("UKP"), rs.getString("Ku"));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userData;
    }


    void addOrUpdateUserToken(String ID, String  token) {

        try {
            Statement stmt = con.createStatement();
            String sql = "INSERT INTO User_data (UserID, token) VALUES ('" + ID + "', '" + token + "')" +
                    "ON DUPLICATE KEY UPDATE token = '" + token + "'";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    void addOrUpdateResData(String ID, String RES) {

        try {
            Statement stmt = con.createStatement();
            String sql = "INSERT INTO User_data (UserID, RES) VALUES ('" + ID + "', '" + RES + "')" +
                    "ON DUPLICATE KEY UPDATE RES = '" + RES + "'";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    String selectUserToken(String ID) {

        String token = null;
        try {
            Statement stmt = con.createStatement();

            String sql = "SELECT token FROM User_data WHERE UserID=\"" + ID +"\"";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                token = rs.getString("token");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return token;
    }


    String selectResData(String ID) {

        String res = "";
        try {
            Statement stmt = con.createStatement();

            String sql = "SELECT RES FROM User_data WHERE UserID=\"" + ID +"\"";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                res = rs.getString("RES");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }


    String selectUserDataXPV(String ID) {

        String xpv = "";
        try {
            Statement stmt = con.createStatement();

            String sql = "SELECT XPV FROM User_data WHERE UserID=\"" + ID +"\"";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                xpv = rs.getString("XPV");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return xpv;
    }

    String selectUserDataAdmin(String ID) {

        String admin = "";
        try {
            Statement stmt = con.createStatement();

            String sql = "SELECT admin FROM User_data WHERE UserID=\"" + ID +"\"";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                admin = rs.getString("admin");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return admin;
    }


    JSONObject getRealTimeData(int type) {

        JSONObject js = new JSONObject();
        JSONArray ja_s = new JSONArray();

        JSONObject tmp = null;
        try {
            Statement stmt = con.createStatement();

            String sql = "SELECT DataType, Value, TimeStamp FROM device_value WHERE TimeStamp >= NOW() - INTERVAL 20 MINUTE AND DataType =" + type ;
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                try {
                    tmp = new JSONObject();
                    tmp.put("DataType", rs.getString("DataType"));
                    tmp.put("TimeStamp", rs.getString("TimeStamp"));
                    tmp.put("Value", rs.getString("Value"));
                    System.out.println(tmp);
                } catch (JSONException e){
                    e.printStackTrace();
                }
                ja_s.put(tmp);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            js.put(Integer.toString(type), ja_s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }


    JSONObject historyDataSendToServer() {

        JSONObject js = new JSONObject();
        JSONArray ja = new JSONArray();
        JSONObject tmp = null;
        try {
            Statement stmt = con.createStatement();

            String sql = "SELECT * FROM device_value WHERE TimeStamp < NOW() - INTERVAL 20 MINUTE";
            String sql2 = "DELETE FROM device_value WHERE TimeStamp < NOW() - INTERVAL 20 MINUTE";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
                try {
                    tmp = new JSONObject();
                    tmp.put("DataType", rs.getInt("DataType"));
                    tmp.put("TimeStamp", rs.getString("TimeStamp"));
                    tmp.put("Value", rs.getDouble("Value"));
                    tmp.put("name", rs.getString("name"));
                    tmp.put("reportid", rs.getString("reportid"));
                    tmp.put("DeviceId", rs.getString("DeviceId"));
                } catch (JSONException e){
                    e.printStackTrace();
                }
                ja.put(tmp);
            }
            stmt.executeUpdate(sql2);
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            js.put("data", ja);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }


    void insertSensorData(String DeviceId, String TimeStamp, String name, String reportid, int DataType, double Value) {

        String sql = "INSERT INTO device_value(DeviceId, TimeStamp, name, reportid, DataType, Value) VALUES" +
                "(?, ?, ?, ?, ?, ?);";

        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, DeviceId);
            preparedStatement.setString(2, TimeStamp);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, reportid);
            preparedStatement.setInt(5, DataType);
            preparedStatement.setFloat(6, (float)Value);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}