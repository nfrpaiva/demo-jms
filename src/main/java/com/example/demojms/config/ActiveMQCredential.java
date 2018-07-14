package com.example.demojms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ActiveMQCredential {

    @Value("spring.activemq.broker-url2")
    private String brokerUrl = "tcp://localhost:61616;tcp://localhost:61616";

    @Value("${spring.activemq.user}")
    private String user;

    @Value("${spring.activemq.password}")
    private String password;

}
