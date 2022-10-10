package com.xd.pre.modules.px.douyin.buyRender;

import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.px.douyin.buyRender.req.BuyRenderReq;
import com.xd.pre.modules.px.douyin.buyRender.req.Shop_requests;

public class BuyRenderUtils {
    public static BuyRenderReq buildBuyRenderReq(String product_id, String sku_id) {
        BuyRenderReq buyRenderReq = JSON.parseObject("{" +
                "\"address\": null," +
                "\"display_scene\": \"buy_again\"," +
                "\"platform_coupon_id\": null," +
                "\"kol_coupon_id\": null," +
                "\"auto_select_best_coupons\": true," +
                "\"customize_pay_type\": \"{\\\"checkout_id\\\":1,\\\"bio_type\\\":\\\"1\\\"}\"," +
                "\"first_enter\": true," +
                "\"source_type\": \"2\"," +
                "\"shape\": 0," +
                "\"marketing_channel\": \"\"," +
                "\"forbid_redpack\": false," +
                "\"support_redpack\": true," +
                "\"use_marketing_combo\": false," +
                "\"entrance_params\": \"{\\\"previous_page\\\":\\\"toutiao_mytab\\\",\\\"new_source_type\\\":\\\"order_list_page\\\",\\\"carrier_source\\\":\\\"order_list_page\\\",\\\"source_method\\\":\\\"order_buy_once\\\",\\\"is_groupbuying\\\":0}\"," +
                "\"shop_requests\": [" +
                "{" +
                "\"shop_id\": \"GceCTPIk\"," +
                "\"product_requests\": [" +
                "{" +
                "\"product_id\": \"3556357046087622442\"," +
                "\"sku_id\": \"1736502463777799\"," +
                "\"sku_num\": 1," +
                "\"new_source_type\": \"order_list_page\"," +
                "\"select_privilege_properties\": []" +
                "}" +
                "]" +
                "}" +
                "]" + "}", BuyRenderReq.class);
        Shop_requests shop_requests = buyRenderReq.getShop_requests().get(0);
        shop_requests.getProduct_requests().get(0).setProduct_id(product_id);
        shop_requests.getProduct_requests().get(0).setSku_id(sku_id);
        return buyRenderReq;
    }

    public static void main(String[] args) {
        System.out.println(JSON.toJSONString(buildBuyRenderReq("3556357046087622442","1736502463777799")));
    }
}
