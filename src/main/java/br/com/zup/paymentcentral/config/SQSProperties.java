package br.com.zup.paymentcentral.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties("payments.sqs")
public class SQSProperties {

    private String url;
    private String accesskey;
    private String secretkey;
    private SQSQueueProperties queue;

    @Getter
    @Setter
    public static class SQSQueueProperties{
        private String ted;
    }
}