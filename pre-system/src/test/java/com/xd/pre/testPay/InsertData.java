package com.xd.pre.testPay;

import cn.hutool.core.io.file.FileReader;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;

public class InsertData {

    public static void main(String[] args) throws Exception{
        String path="C:\\Users\\Administrator\\Desktop\\git\\xxx\\rizhi\\";
        File file = new File(path);
        String[] list = file.list();
        for (String s : list) {
            if(s.contains(".json")){
                String filePathR = path + s;
                FileReader fileReader = new FileReader(filePathR);
                String result = fileReader.readString();
                if(result.contains("install_id_str") && result.contains("device_id_str")){
                    JSONObject parseObject = JSON.parseObject(result);
                    String format = String.format("INSERT INTO douyin_device_iid (  device_id, iid )VALUES('%s', '%s');", parseObject.getString("device_id_str"), parseObject.getString("install_id_str"));
                    System.out.println(format);
                }
            }

        }
    }
}
