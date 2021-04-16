package br.com.zup.paymentcentral.integration;

import br.com.zup.paymentcentral.application.payment.processors.PaymentProcessor;
import br.com.zup.paymentcentral.config.DynamoDBProperties;
import br.com.zup.paymentcentral.config.KafkaProperties;
import br.com.zup.paymentcentral.config.SQSProperties;
import br.com.zup.paymentcentral.ted_included.TedIncluded;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentsRoutes extends RouteBuilder {

    private final AmazonDynamoDB dynamoDBClient;
    private final KafkaProperties kafkaProperties;
    private final SQSProperties sqsProperties;
    private final DynamoDBProperties dynamoDBProperties;
    private final PaymentProcessor tedProcessor;


    public PaymentsRoutes(@Qualifier("tedProcessor") PaymentProcessor tedProcessor,
                          AmazonDynamoDB dynamoDBClient, KafkaProperties kafkaProperties,
                          DynamoDBProperties dynamoDBProperties, SQSProperties sqsProperties) {
        this.dynamoDBClient = dynamoDBClient;
        this.kafkaProperties = kafkaProperties;
        this.sqsProperties = sqsProperties;
        this.tedProcessor = tedProcessor;
        this.dynamoDBProperties = dynamoDBProperties;
    }

    @Override
    public void configure() throws Exception {
        bindToRegistry(dynamoDBProperties.getAwsDynamoDBClient(), dynamoDBClient);

        from(String.format(kafkaProperties.getKafkaBroker(), kafkaProperties.getTedIncluded().getTopicName()))
                .log("New TED requested: ${body}")
                .unmarshal()
                .json(JsonLibrary.Jackson, PaymentDTO.class)
                .process(tedProcessor)
                .to(String.format(dynamoDBProperties.getAwsDdb(), dynamoDBProperties.getPaymentsTed()))
                .to(String.format(sqsProperties.getAwsSqs(), sqsProperties.getQueue().getTed()))
                .end();

    }
}
