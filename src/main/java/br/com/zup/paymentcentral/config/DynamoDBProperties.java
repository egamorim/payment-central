package br.com.zup.paymentcentral.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@Setter
@Getter
@ConfigurationProperties("aws.dynamodb")
public class DynamoDBProperties {

    @NotNull
    private String awsDdb;

    @NotNull
    private String endpoint;

    @NotNull
    private String accesskey;

    @NotNull
    private String secretkey;

    @NotNull
    private String awsDynamoDBClient;

    @NotNull
    private String paymentsTed;

}
