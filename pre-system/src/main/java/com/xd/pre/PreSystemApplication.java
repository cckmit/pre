package com.xd.pre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统入口
 */
@SpringBootApplication
@EnableScheduling
@EnableJms    //启动消息队列
@EnableAsync
@Slf4j
public class PreSystemApplication {

    public static void main(String[] args) {
        log.info("开始启动");
        SpringApplication.run(PreSystemApplication.class, args);
    }

}
