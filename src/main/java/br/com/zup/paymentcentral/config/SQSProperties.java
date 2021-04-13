package br.com.zup.paymentcentral.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@Setter
@Getter
@ConfigurationProperties("payments.sqs")
public class SQSProperties {

    @NotNull
    private String url;

    @NotNull
    private String accesskey;

    @NotNull
    private String secretkey;

    @NotNull
    private String awsSqs;

    private SQSQueueProperties queue;

    @Getter
    @Setter
    public static class SQSQueueProperties{

        @NotNull
        private String ted;
    }
}