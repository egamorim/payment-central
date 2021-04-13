package br.com.zup.paymentcentral.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@Setter
@Getter
@ConfigurationProperties("payments.kafka")
public class KafkaProperties {

    @NotNull
    private String host;

    @NotNull
    private String port;

    @NotNull
    private String kafkaBroker;

    @NotNull
    private EventProperty tedIncluded;

    @Getter
    @Setter
    @Validated
    public static class EventProperty{

        @NonNull
        public String topicName;

    }
}
