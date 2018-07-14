package com.example.demojms.config;

import com.example.demojms.MyListener;
import com.google.common.collect.Maps;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.Arrays;
import java.util.Map;

@Configuration
public class CustomQueueBeanDefinition implements BeanDefinitionRegistryPostProcessor {

    @Value("${spring.activemq.broker-url2}")
    private String brokerUrl = "tcp://localhost:61616;tcp://localhost:61616";

    @Value("${spring.activemq.user}")
    private String user;

    @Value("${spring.activemq.password}")
    private String password;

    private Map<Integer,String> configurations = Maps.newHashMap();

    public CustomQueueBeanDefinition (){
        super();
        //TODO : Essa lógica é apenas um exemplo. Algum tratamento para resolução e transformação de URL failover precisa ser feita.
        final Integer[] index = {0};
        Arrays.stream(brokerUrl.split(";")).forEach(url -> {
            configurations.put(index[0]++, url);
        });
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        configurations.forEach((key, url)  -> {
            connectionFactoryRegistry(registry, key, url);
            messageListenerContainerRegistry (registry, key);

        });

    }

    private void connectionFactoryRegistry(BeanDefinitionRegistry registry, Integer key, String url) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(ActiveMQConnectionFactory.class)
                .addConstructorArgValue("${spring.activemq.user}")
                .addConstructorArgValue("${spring.activemq.password}")
                /**
                 * É possível utilizar expressão el mas no nosso cenário é necessario "splitar" a url
                 */
                //.addConstructorArgValue("${spring.activemq.broker-url}");
                .addConstructorArgValue(url);
        registry.registerBeanDefinition(key+"-connectionFactory", builder.getBeanDefinition());
    }

    private void messageListenerContainerRegistry (BeanDefinitionRegistry registry, Integer key){
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(DefaultMessageListenerContainer.class)
                .addDependsOn(key+"-connectionFactory")
                .addPropertyReference("connectionFactory",key+"-connectionFactory" )
                .addPropertyReference("messageListener", "myListener")
                .addPropertyValue("autoStartup", true)
                .addPropertyValue("destinationName", "queue.churros" + key);
        registry.registerBeanDefinition(key+"-listenerContainer", builder.getBeanDefinition());

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
