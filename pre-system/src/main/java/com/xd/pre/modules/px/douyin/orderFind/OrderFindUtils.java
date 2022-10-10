package com.xd.pre.modules.px.douyin.orderFind;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class OrderFindUtils {
    public static String findOrder(FindOrderDto findOrderDto) {
        try {
            String url = String.format("https://aweme.snssdk.com/aweme/v1/commerce/order/detailInfo/?order_id=%s&device_id=%s&aid=1128&app_name=aweme&channel=dy_tiny_juyouliang_dy_and24&iid=%s",
                    findOrderDto.getOrderId(), findOrderDto.getDevice_id(), findOrderDto.getIid()
            );
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Cookie", findOrderDto.getCk())
                    .build();
            Response response = client.newCall(request).execute();
            String orderDetail = response.body().string();
            log.info("msg查询订单结果msg:{}", orderDetail);
            response.close();
            return orderDetail;
        } catch (Exception e) {
            log.error("查询报错msg:{}", e.getMessage());
        }
        return null;
    }
}
