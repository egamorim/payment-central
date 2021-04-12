package br.com.zup.paymentcentral.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Setter
@Getter
@ConfigurationProperties("payments.kafka")
public class KafkaProperties {

    private String url;

    private EventProperty tedIncluded;

    @Getter
    @Setter
    @Validated
    public static class EventProperty{

        @NonNull
        public String topicName;

    }
}
