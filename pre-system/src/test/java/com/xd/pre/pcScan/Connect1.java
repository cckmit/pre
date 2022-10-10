package com.xd.pre.pcScan;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Connect1 {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/pre?useUnicode=true&useSSL=false&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useAffectedRows=true&allowMultiQueries=true", "root", "123456");
        //这里输入的是数据库名字qq
        Statement state = con.createStatement();
        ResultSet rs = state.executeQuery("select * from ck_youxiao  ");
        Map<Integer, String> map = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
        while (rs.next()) {
            String ck = rs.getString("ck");
            // update ck_youxiao set pt_pin ='' where ck = ''
            //state.execute(String.format("INSERT INTO ck_youxiao   VALUES(%d,'%s','%s') ", id, ck.trim(), resStr));
            String[] split = ck.split(";");
            for (String cks : split) {
                if(cks.contains("pin=")){
                    Statement statement = con.createStatement();
                    int i = statement.executeUpdate(String.format("update ck_youxiao set pt_pin ='%s' where ck = '%s'", cks.split("=")[1], ck));
                }
            }
        }
    }

}
