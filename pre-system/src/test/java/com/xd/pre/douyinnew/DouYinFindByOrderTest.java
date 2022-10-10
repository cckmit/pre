package com.xd.pre.douyinnew;

import com.xd.pre.modules.px.douyin.orderFind.FindOrderDto;
import com.xd.pre.modules.px.douyin.orderFind.OrderFindUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DouYinFindByOrderTest {

    public static void main(String[] args) {
        String ck="install_id=119169286681983; ttreq=1$c937432add1ac5543b40dc8b95cb769bf024bf3a; passport_csrf_token=dc084fdfd9182b2006ac015d23d5094e; passport_csrf_token_default=dc084fdfd9182b2006ac015d23d5094e; d_ticket=5d8498d9c5c57a18f23083f8b948b45743690; multi_sids=659356656346136%3A140a336dd81551eaa30bc0e9e8d336fd; odin_tt=31cfa0089dd41156d7ef15dc965652aac3992c75cf21a7028e7de03cc06bad99000ef807e63e702274ccb12447d4a5d88eb3277edf255dc0b5f5e277b611155de832de5a7ffbb6acd70c6d5f7e1ad8f9; n_mh=8nysT__BxDL_VpPZTRMYKZZSN1pywPhZ9o63MSmzGLg; passport_assist_user=CkCRydX49tKRiP6NfppL8EZXqhP7I0lHXjcq-1NuFi9tetbHhO7j8WgKWcNY0u1c2_pwQmIxWsLy25zu5vuCS4y2GkgKPEawcjcdGGFhQ7XJU9Cvcme37ad7_x2LoXTiOHQl20bPqoQm-Xexq_YwQPA0X1fytaQzn-aCrETNNkRTDBDpip0NGImv1lQiAQMRQLcc; sid_guard=140a336dd81551eaa30bc0e9e8d336fd%7C1664383681%7C5183999%7CSun%2C+27-Nov-2022+16%3A48%3A00+GMT; uid_tt=1e4686eabe61b69fd57f1db3639b39b0; uid_tt_ss=1e4686eabe61b69fd57f1db3639b39b0; sid_tt=140a336dd81551eaa30bc0e9e8d336fd; sessionid=140a336dd81551eaa30bc0e9e8d336fd; sessionid_ss=140a336dd81551eaa30bc0e9e8d336fd; msToken=DefrbKNjA4krIh7tp5KF_ZXXM1__4BIGoe2_r-2pbIFokQpdlsAe8eodr9epNPRS43Yu3Wpkh4HktFYRO-i2ASuuPCj8e7LOFmIy0hm1yEw=";
        String device_id = "3426504802566350";
        String iid = "2107090848974670";
        String orderId ="4983711245180223043";
        FindOrderDto findOrderDto = FindOrderDto.builder().ck(ck).device_id(device_id).iid(iid).orderId(orderId).build();
        String order = OrderFindUtils.findOrder(findOrderDto);

    }
}
