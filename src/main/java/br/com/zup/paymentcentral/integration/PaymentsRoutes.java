package br.com.zup.paymentcentral.integration;

import br.com.zup.paymentcentral.config.KafkaProperties;
import br.com.zup.paymentcentral.config.SQSProperties;
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
    private final String SQS_TED_QUEUE = "aws-sqs://%s?accessKey=%s&secretKey=%s";
    private static final String DYNAMO_DB_CLIENT_ID = "awsDynamoDBClient";
    private final AmazonDynamoDB dynamoDBClient;
    private final KafkaProperties kafkaProperties;
    private final SQSProperties sqsProperties;
    private final Environment env;

    public PaymentsRoutes(AmazonDynamoDB dynamoDBClient, KafkaProperties kafkaProperties,Environment env,
                          SQSProperties sqsProperties) {
        this.dynamoDBClient = dynamoDBClient;
        this.kafkaProperties = kafkaProperties;
        this.sqsProperties = sqsProperties;
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
        from(String.format(kafkaProperties.getKafkaBroker(), kafkaProperties.getTedIncluded().getTopicName()))
                .log("New TED requested: ${body}")
                .unmarshal()
                .json(JsonLibrary.Jackson)
                .marshal().json()
                .unmarshal(getJacksonDataFormat(PaymentDTO.class))
                .process((Exchange exchange) -> {
                    PaymentDTO paymentDTO = exchange.getIn().getBody(PaymentDTO.class);
                    exchange.getIn().setHeader("CamelAwsDdbItem", buildDocument(payment));
                })
                .to("aws-ddb://payments-ted?amazonDDBClient=#" + DYNAMO_DB_CLIENT_ID)
                .to(String.format(SQS_TED_QUEUE, sqsProperties.getQueue().getTed(), sqsProperties.getAccesskey(), sqsProperties.getSecretkey()))
        .end();

    }

    private PaymentDTO dtoFromExchangeBody(Exchange exchange) {
        String payload = exchange.getIn().getBody(String.class);
        return new Gson().fromJson(payload, PaymentDTO.class);
    }

    //TODO isso precisa de um refacory pra ficar mais elegante
    private Map<String, AttributeValue> buildDocument(PaymentDTO body) {
        AttributeValue id = new AttributeValue(body.getId().toString());
        AttributeValue datePayment = new AttributeValue(body.getDatePayment().toString());
        AttributeValue valuePayment = new AttributeValue(body.getValuePayment().toString());

        Map<String, AttributeValue> newBody = new HashMap();
        newBody.put("id", id);
        newBody.put("datePayment", datePayment);
        newBody.put("valuePayment", valuePayment);

        return newBody;
    }

    private JacksonDataFormat getJacksonDataFormat(Class<?> unmarshalType) {
        JacksonDataFormat format = new JacksonDataFormat();
        format.setUnmarshalType(unmarshalType);
        return format;
    }
}
