package com.xd.pre.pcScan;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.xd.pre.modules.px.appstorePc.pcScan.ScanUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Connect {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/pre?useUnicode=true&useSSL=false&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useAffectedRows=true&allowMultiQueries=true", "root", "123456");
        //这里输入的是数据库名字qq
        Statement state = con.createStatement();
        ResultSet rs = state.executeQuery("select * from jd_ck  where ck like '%wskey%' order by id  ");
        Map<Integer, String> map = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
        while (rs.next()) {
            String ck = rs.getString("ck");
            String[] split = ck.split(";");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (s.contains("pin") && !s.contains("pt_pin")) {
                    String s2 = s.split("=")[1];
                    sb.append("pin=" + URLEncoder.encode(s2) + ";");
                }
                if (s.contains("wskey")) {
                    sb.append(s.trim() + ";");
                }
            }
            ids.add(rs.getInt("id"));
            map.put(rs.getInt("id"), sb.toString());
        }
        Map<String, String> ipAndPort = getIpAndPort();
        OkHttpClient client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
        ScanUtils scanUtils = new ScanUtils();
        for (Integer id : ids) {
            //155083
            String loginInfo = scanUtils.getLoginInfo(client, map.get(id), id);
            if (StrUtil.isNotBlank(loginInfo) && loginInfo.length() == 1 && Integer.valueOf(loginInfo) == 1) {
                ipAndPort = getIpAndPort();
                client = Demo.getOkHttpClient(ipAndPort.get("ip"), Integer.valueOf(ipAndPort.get("port")));
                scanUtils.getLoginInfo(client, map.get(id), id);
            }
        }

    }

    public static Map<String, String> getIpAndPort() {
        String s = HttpUtil.get("http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=&city=0&yys=0&port=1&time=1&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=");
        String[] split = s.split(":");
        HashMap<String, String> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("ip", split[0]);
        stringObjectHashMap.put("port", split[1].replace("\r\n", ""));
        return stringObjectHashMap;
    }


}
