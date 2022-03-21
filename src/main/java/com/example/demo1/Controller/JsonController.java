package com.example.demo1.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.sql.*;

@Controller
public class JsonController {

    @Autowired
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/xhc1?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT";
    public static final String user = "root";
    public static final String password = "123456";
    private static Connection conn;
    private static PreparedStatement pst;

    /**
     * 下载模型信息
     *
     * @throws IOException
     */
    @GetMapping(value = "/downloadinfo")
    @ResponseBody
    public String downloadinfo(@RequestParam("table") String table, @RequestParam("UniqueID") String UniqueID) throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(DB_URL, user, password);
        String sql1 = "select * from " + table + " where UniqueID = '" + UniqueID + "'";
        pst = conn.prepareStatement(sql1);
        ResultSet rs = pst.executeQuery(sql1);
        String str = "";
        while (rs.next()) {
            str = rs.getString("开始偏移");
        }
        return str;
    }

    @GetMapping(value = "/download")
    @ResponseBody
    public String download() throws ClassNotFoundException, SQLException {
        return "hellowordld";
    }
}
