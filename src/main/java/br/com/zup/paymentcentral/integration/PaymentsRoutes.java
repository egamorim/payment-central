package br.com.zup.paymentcentral.integration;

import br.com.zup.paymentcentral.config.KafkaProperties;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.gson.Gson;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentsRoutes extends RouteBuilder {

    private final String KAFKA_TED_INCLUDED = "kafka:%s?brokers=%s";
    private static final String DYNAMO_DB_CLIENT_ID = "awsDynamoDBClient";
    private final AmazonDynamoDB dynamoDBClient;
    private final KafkaProperties kafkaProperties;
    private final Environment env;

    public PaymentsRoutes(AmazonDynamoDB dynamoDBClient, KafkaProperties kafkaProperties,Environment env) {
        this.dynamoDBClient = dynamoDBClient;
        this.kafkaProperties = kafkaProperties;
        this.env = env;
    }

    @Override
    public void configure() throws Exception {
        bindToRegistry(DYNAMO_DB_CLIENT_ID, dynamoDBClient);

        //TODO a configuração do rest é só pra facilitar os testes em tempo de desenvolvimento, depois agente apaga esse cara
        restConfiguration()
                .contextPath(env.getProperty("camel.component.servlet.mapping.contextPath", "/rest/*"))
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Zup Payments Rest API.")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true")
                .apiContextRouteId("doc-api")
                .port("8080")
                .bindingMode(RestBindingMode.json);

        rest("payments")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .post("/").route()
                .marshal().json(JsonLibrary.Gson)
                .unmarshal().json(JsonLibrary.Gson)
                .to(String.format(KAFKA_TED_INCLUDED, kafkaProperties.getTedIncluded().getTopicName(), kafkaProperties.getUrl()))
                .end();


        // TODO o payload precisa ser ajustado de acordo com o que vai vir da processor
        from(String.format(KAFKA_TED_INCLUDED, kafkaProperties.getTedIncluded().getTopicName(), kafkaProperties.getUrl()))
                .log("New TED requested: ${body}")
                .marshal().json(JsonLibrary.Gson)
                .unmarshal().json(JsonLibrary.Gson)
                .process((Exchange exchange) -> {
                    PaymentDTO payment = this.dtoFromExchangeBody(exchange);
                    exchange.getIn().setHeader("CamelAwsDdbItem", buildDocument(payment));
                })
                .to("aws-ddb://payments-ted?amazonDDBClient=#" + DYNAMO_DB_CLIENT_ID)
                .to("aws-sqs://{{payments.sqs.queue.ted}}?accessKey={{aws.accesskey}}&secretKey={{aws.secretkey}}")
        .end();

    }

    private PaymentDTO dtoFromExchangeBody(Exchange exchange) {
        String payload = exchange.getIn().getBody(String.class);
        return new Gson().fromJson(payload, PaymentDTO.class);
    }

    //TODO isso precisa de um refacory pra ficar mais elegante
    private Map<String, AttributeValue> buildDocument(PaymentDTO body) {
        AttributeValue id = new AttributeValue(UUID.randomUUID().toString());
        AttributeValue sender = new AttributeValue(body.getSender());
        AttributeValue receiver = new AttributeValue(body.getReceiver());
        AttributeValue amount = new AttributeValue();
        amount.setN(body.getAmount().toString());

        Map<String, AttributeValue> newBody = new HashMap();
        newBody.put("id", id);
        newBody.put("sender", sender);
        newBody.put("receiver", receiver);
        newBody.put("amount", amount);

        return newBody;
    }

    private JacksonDataFormat getJacksonDataFormat(Class<?> unmarshalType) {
        JacksonDataFormat format = new JacksonDataFormat();
        format.setUnmarshalType(unmarshalType);
        return format;
    }
}
