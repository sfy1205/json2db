package com.example.demo1;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherUtil {
    static String url1 = "jdbc:postgresql://localhost:5432/lixian";
    static String usr = "postgres";
    static String psd = "123456";

    public static void main(String[] args) {
        String cityCode = "101050810";
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        httpURLGETCase();
                        Thread.sleep(1 * 1 * 10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    private static void httpURLGETCase() {
        String methodUrl = "http://api.k780.com/?app=weather.realtime&weaId=1&ag=today,futureDay,lifeIndex,futureHour&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String line = null;
        try {
            URL url = new URL(methodUrl);
            connection = (HttpURLConnection) url.openConnection();// 根据URL生成HttpURLConnection
            connection.setRequestMethod("GET");// 默认GET请求
            connection.connect();// 建立TCP连接
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));// 发送http请求
                StringBuilder result = new StringBuilder();
                // 循环读取流
                while ((line = reader.readLine()) != null) {
                    result.append(line).append(System.getProperty("line.separator"));// "\n"
                }
                JSONObject jsonObject = JSONObject.parseObject(result.toString());
                String table = "HourWeather";
                for (int i = 0; i < 24; i++) {
                    Class.forName("org.postgresql.Driver");
                    Connection conn = DriverManager.getConnection(url1, usr, psd);
                    Statement st = conn.createStatement();
                    String sql = "insert into \"" + table + "\" values ('" + jsonObject.getJSONObject("result").getJSONArray("futureHour").getJSONObject(i).get("dateYmdh") + "','" + jsonObject.getJSONObject("result").getJSONArray("futureHour").getJSONObject(i).get("wtTemp") + "')";
                    st.executeUpdate(sql);
                    st.close();
                    conn.close();
                    System.out.println(jsonObject.getJSONObject("result").getJSONArray("futureHour").getJSONObject(i).get("dateYmdh"));
                    System.out.println(jsonObject.getJSONObject("result").getJSONArray("futureHour").getJSONObject(i).get("wtTemp"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connection.disconnect();
        }
    }
}