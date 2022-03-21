package com.example.demo1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.*;
import static org.neo4j.driver.v1.Values.parameters;

import java.io.*;
import java.net.URLDecoder;
import java.sql.*;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JsonTest {

    static String url = "jdbc:postgresql://localhost:5432/lixian";
    static String usr = "postgres";
    static String psd = "123456";
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/xhc1?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT";
    public static final String user = "root";
    public static final String password = "123456";
    private static Connection conn;
    private static PreparedStatement pst;
    public static List<String> liststring = new ArrayList<>();

    //读取json文件
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void readfile(String filepath) throws FileNotFoundException, IOException {
        File file = new File(filepath);
        String[] filelist = file.list();
        for (int i = 0; i < filelist.length; i++) {
            if (filelist[i].contains("json")) {
                int pos = filelist[i].lastIndexOf('.');
                liststring.add(filelist[i].substring(0, pos));
            }
        }
    }

    public static void WriteJsontoPQSQL() throws ClassNotFoundException, SQLException, IOException {
        readfile("D:/code/IdeaProject/demo2/src/main/resources");
        for (int m = 0; m < liststring.size(); m++) {
            String table = liststring.get(m);
            String path = JsonTest.class.getClassLoader().getResource(table + ".json").getPath();
            path = URLDecoder.decode(path, "UTF-8");
            String s = readJsonFile(path);
            JSONArray jobj = JSON.parseObject(s).getJSONObject("ModelPointClass").getJSONArray("ModelPoint");
            Connection conn1 = null;
            Class.forName("org.postgresql.Driver");
            conn1 = DriverManager.getConnection(url, usr, psd);
            Statement st = conn1.createStatement();
            for (int i = 0; i < jobj.size(); i++) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + "()");
                JSONArray jobj1 = ((JSONObject) jobj.get(i)).getJSONObject("Properties").getJSONArray("Property");
                String sql1 = "insert into " + table + " (";
                String sql2 = " values (";
                for (int j = 0; j < jobj1.size(); j++) {
                    JSONObject jobj2 = (JSONObject) jobj1.get(j);
                    String name = (String) jobj2.get("-Name");
                    String value = (String) jobj2.get("-Value");
//                    if(table.contains("锅炉房")){
//                        name = (String) jobj2.get("@_Name");
//                        value = (String) jobj2.get("@_Value");
//                    }
                    String str = "varchar(9999)";
                    if (name.equals("UniqueID")) {
                        str = "varchar(1000) primary key";
                    }
                    st.executeUpdate("DO $$ \n" +
                            "    BEGIN\n" +
                            "        BEGIN\n" +
                            "            ALTER TABLE " + table + " ADD COLUMN \"" + name + "\" " + str + " ;\n" +
                            "        EXCEPTION\n" +
                            "            WHEN duplicate_column THEN RAISE NOTICE 'column age already exists in " + table + ".';\n" +
                            "        END;\n" +
                            "    END;\n" +
                            "$$");
                    if (!sql1.contains("\"" + name + "\"")) {
                        sql1 += "\"" + name + "\",";
                        sql2 += "'" + value + "',";
                    }
                    System.out.println(name + ":" + value);
                }
                if (sql1.endsWith(",")) {
                    sql1 = sql1.substring(0, sql1.length() - 1);
                    sql2 = sql2.substring(0, sql2.length() - 1);
                }
                sql1 += ")";
                sql2 += ")";
                st.executeUpdate(sql1 + sql2 + "\n" +
                        "ON conflict(\"UniqueID\") \n" +
                        "DO nothing;");
            }
        }
    }

    public static void WriteJsontoNeo4j() throws ClassNotFoundException, SQLException, IOException {
        readfile("D:/code/IdeaProject/demo2/src/main/resources/YZ");
        Driver driver= GraphDatabase.driver("bolt://172.16.89.27:7687",AuthTokens.basic("neo4j", "123"));
        Session session = driver.session();
        for (int m = 0; m < liststring.size(); m++) {
            String table = liststring.get(m);
            String path = JsonTest.class.getClassLoader().getResource(table + ".json").getPath();
            path = URLDecoder.decode(path, "UTF-8");
            String s = readJsonFile(path);
            JSONArray jobj = JSON.parseObject(s).getJSONObject("ModelPointClass").getJSONArray("ModelPoint");
            for (int i = 0; i < jobj.size(); i++) {
                String elementid=new String();
                String uniqueid=new String();
                JSONArray jobj1 = ((JSONObject) jobj.get(i)).getJSONObject("Properties").getJSONArray("Property");
                for (int j = 0; j < jobj1.size(); j++) {
                    JSONObject jobj2 = (JSONObject) jobj1.get(j);
                    String name = (String) jobj2.get("-Name");
                    String value = (String) jobj2.get("-Value");
                    if(name.equals("ElementID")){
                        elementid=value;
                    }
                    else if(name.equals("UniqueID")){
                        uniqueid=value;
                        break;
                    }
                }
                StatementResult result = session.run( "match (n{elementId:"+elementid+"}) SET n.UniqueID = '"+uniqueid+"'");
            }
        }
//        match (n{ElementID:1934405}) SET n.uniqueId111 = 'aa65fea2-a245-4a8a-aa59-b42a6fe200f3-001d8445';
//        StatementResult result = session.run( "match (n{ElementID:1934405}) SET n.UniqueID111 = '1'");
//        StatementResult result = session.run( "match (n{ElementID:{ElementID}}) SET n.UniqueID111 = {UniqueID}}",
//                parameters( "ElementID", "1934405", "UniqueID111", "1111" ) );
        session.close();
        driver.close();
    }

    public static void WriteJsontoMySQL() throws ClassNotFoundException, SQLException, IOException {
        readfile("D:/code/IdeaProject/demo1/src/main/resources");
        for (int m = 0; m < liststring.size(); m++) {
            String table = liststring.get(m);
            String path = JsonTest.class.getClassLoader().getResource(table).getPath();
            path = URLDecoder.decode(path, "UTF-8");
            String s = readJsonFile(path);
            JSONArray jobj = JSON.parseObject(s).getJSONObject("ModelPointClass").getJSONArray("ModelPoint");
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, user, password);
            String sql0 = "CREATE TABLE IF NOT EXISTS " + table + "(\n" +
                    "  asfy varchar(255)\n" +
                    ");";
            pst = conn.prepareStatement(sql0);
            pst.executeUpdate();
            for (int i = 0; i < jobj.size(); i++) {
                JSONArray jobj1 = ((JSONObject) jobj.get(i)).getJSONObject("Properties").getJSONArray("Property");
                String sql1 = "replace into " + table + " (";
                String sql2 = " values (";
                for (int j = 0; j < jobj1.size(); j++) {
                    JSONObject jobj2 = (JSONObject) jobj1.get(j);
                    String name = (String) jobj2.get("-Name");
                    if (name.equals("D")) {
                        name = "BIGD";
                    }
                    if (name.equals("R")) {
                        name = "BIGR";
                    }
                    if (name.equals("RR")) {
                        name = "BIGRR";
                    }
                    String value = (String) jobj2.get("-Value");
                    if (table.contains("锅炉房")) {
                        name = (String) jobj2.get("@_Name");
                        value = (String) jobj2.get("@_Value");
                    }
                    String length = "";
                    length = "text";
                    if (name.equals("UniqueID")) {
                        length = "text primary key";
                    }
                    String sql11 = "DROP PROCEDURE IF EXISTS upgrade_database_1_0_to_2_0;";
                    pst = conn.prepareStatement(sql11);
                    pst.executeUpdate();
                    String sql12 = "CREATE PROCEDURE upgrade_database_1_0_to_2_0()\n" +
                            "BEGIN\n" +
                            "IF NOT EXISTS(SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='xhc1'\n" +
                            "        AND COLUMN_NAME='" + name + "' AND TABLE_NAME='" + table + "')  THEN\n" +
                            "    ALTER TABLE " + table + " ADD `" + name + "` " + length + " BINARY;\n" +
                            "END IF;\n" +
                            "END;";
                    pst = conn.prepareStatement(sql12);
                    pst.executeUpdate();
                    String sql13 = "CALL upgrade_database_1_0_to_2_0();";
                    pst = conn.prepareStatement(sql13);
                    pst.executeUpdate();
                    if (!sql1.toLowerCase().contains("`" + name.toLowerCase() + "`")) {
                        sql1 += "`" + name + "`,";
                        sql2 += "'" + value + "',";
                    }
                    System.out.println(name + ":" + value);
                }
                if (sql1.endsWith(",")) {
                    sql1 = sql1.substring(0, sql1.length() - 1);
                    sql2 = sql2.substring(0, sql2.length() - 1);
                }
                sql1 += ")";
                sql2 += ")";
                pst = conn.prepareStatement(sql1 + sql2);
                pst.executeUpdate();
                String sql21 = "DROP PROCEDURE IF EXISTS procedure2;";
                pst = conn.prepareStatement(sql21);
                pst.executeUpdate();
                String sql22 = "CREATE PROCEDURE procedure2()\n" +
                        "BEGIN\n" +
                        "IF EXISTS (select TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME from information_schema.COLUMNS where COLUMN_NAME='asfy') THEN\n" +
                        "alter table " + table + " drop asfy;\n" +
                        "END IF;\n" +
                        "END;";
                pst = conn.prepareStatement(sql22);
                pst.executeUpdate();
                String sql23 = "CALL procedure2();";
                pst = conn.prepareStatement(sql23);
                pst.executeUpdate();
            }
        }
        pst.close();
        conn.close();
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
//        WriteJsontoPQSQL();
        //WriteJsontoMySQL();
        WriteJsontoNeo4j();
    }
}