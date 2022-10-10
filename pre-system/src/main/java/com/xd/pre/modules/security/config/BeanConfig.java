package com.xd.pre.modules.security.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.concurrent.Executor;

@Configuration
public class BeanConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;

    @Value("${spring.activemq.queue-name}")
    private String queueName;

    @Value("${spring.activemq.topic-name}")
    private String topicName;

    @Bean(name = "queue")
    public Queue queue() {
        return new ActiveMQQueue(queueName);
    }

    @Bean(name = "findQueue")
    public Queue findQueue() {
        return new ActiveMQQueue("findQueue");
    }

    @Bean(name = "product_stock_queue")
    public Queue product_stock() {
        return new ActiveMQQueue("product_stock_queue");
    }

    @Bean(name = "product_proxy_task")
    public Queue product_proxy_task() {
        return new ActiveMQQueue("product_proxy_task");
    }

    @Bean(name = "notify_success")
    public Queue notify_success() {
        return new ActiveMQQueue("notify_success");
    }

    @Bean(name = "match2_queue")
    public Queue match2_queue() {
        return new ActiveMQQueue("match2_queue");
    }

    @Bean(name = "product_ip_queue")
    public Queue product_ip_queue() {
        return new ActiveMQQueue("product_ip_queue");
    }

    @Bean(name = "check_data_queue")
    public Queue check_data_queue() {
        return new ActiveMQQueue("check_data_queue");
    }

    @Bean(name = "ios_product_queue")
    public Queue ios_product_queue() {
        return new ActiveMQQueue("ios_product_queue");
    }


    @Bean(name = "activate_queue")
    public Queue activate_queue() {
        return new ActiveMQQueue("activate_queue");
    }

    @Bean(name = "activate_meituan_queue")
    public Queue activate_meituan_queue() {
        return new ActiveMQQueue("activate_meituan_queue");
    }

    @Bean(name = "cancel_queue")
    public Queue cancel_queue() {
        return new ActiveMQQueue("cancel_queue");
    }


    @Bean(name = "create_order_wph_queue")
    public Queue create_order_wph_queue() {
        return new ActiveMQQueue("create_order_wph_queue");
    }

    @Bean(name = "create_order_wph_queue_code")
    public Queue create_order_wph_queue_code() {
        return new ActiveMQQueue("create_order_wph_queue_code");
    }


    @Bean(name = "create_account_wph_queue")
    public Queue create_account_wph_queue() {
        return new ActiveMQQueue("create_account_wph_queue");
    }

    @Bean(name = "create_account_wph_queue_code")
    public Queue create_account_wph_queue_code() {
        return new ActiveMQQueue("create_account_wph_queue_code");
    }

    @Bean(name = "findwph_queue")
    public Queue findwph_queue() {
        return new ActiveMQQueue("findwph_queue");
    }

    @Bean(name = "product_douyin_stock_queue")
    public Queue product_douyin_stock_queue() {
        return new ActiveMQQueue("product_douyin_stock_queue");
    }

    @Bean(name = "findwph_queue_code")
    public Queue findwph_queue_code() {
        return new ActiveMQQueue("findwph_queue_code");
    }

    @Bean(name = "product_pc_account")
    public Queue product_pc_account() {
        return new ActiveMQQueue("product_pc_account");
    }

    @Bean(name = "topic")
    public Topic topic() {
        return new ActiveMQTopic(topicName);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(username, password, brokerUrl);
    }

    @Bean
    public JmsMessagingTemplate jmsMessageTemplate() {
        return new JmsMessagingTemplate(connectionFactory());
    }

    // 在Queue模式中，对消息的监听需要对containerFactory进行配置
    @Bean("queueListener")
    public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false);
        return factory;
    }

    //在Topic模式中，对消息的监听需要对containerFactory进行配置
    @Bean("topicListener")
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

    @Bean
    public Executor asyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(80);
        executor.setThreadGroupName("asyncPool");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor asyncPoolRet() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(500);
        executor.setThreadGroupName("asyncPoolRet");
        executor.initialize();
        return executor;
    }
}