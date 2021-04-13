package br.com.zup.paymentcentral.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SQSProperties.class)
public class SQSConfig {
}
