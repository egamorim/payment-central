package br.com.zup.paymentcentral.integration;

import br.com.zup.paymentcentral.config.KafkaProperties;
import br.com.zup.paymentcentral.config.SQSProperties;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentsRoutes extends RouteBuilder {

    private static final String DYNAMO_DB_CLIENT_ID = "awsDynamoDBClient";
    private final AmazonDynamoDB dynamoDBClient;
    private final KafkaProperties kafkaProperties;
    private final SQSProperties sqsProperties;

    public PaymentsRoutes(AmazonDynamoDB dynamoDBClient, KafkaProperties kafkaProperties,
                          SQSProperties sqsProperties) {
        this.dynamoDBClient = dynamoDBClient;
        this.kafkaProperties = kafkaProperties;
        this.sqsProperties = sqsProperties;
    }

    @Override
    public void configure() throws Exception {
        bindToRegistry(DYNAMO_DB_CLIENT_ID, dynamoDBClient);

        // TODO o payload precisa ser ajustado de acordo com o que vai vir da processor
        from(String.format(kafkaProperties.getKafkaBroker(), kafkaProperties.getTedIncluded().getTopicName()))
                .log("New TED requested: ${body}")
                .unmarshal()
                .json(JsonLibrary.Jackson)
                .marshal().json()
                .unmarshal(getJacksonDataFormat(PaymentDTO.class))
                .process((Exchange exchange) -> {
                    PaymentDTO paymentDTO = exchange.getIn().getBody(PaymentDTO.class);
                    exchange.getIn().setHeader("CamelAwsDdbItem", buildDocument(paymentDTO));
                })
                .to("aws-ddb://payments-ted?amazonDDBClient=#" + DYNAMO_DB_CLIENT_ID)
                .to(String.format(sqsProperties.getAwsSqs(), sqsProperties.getQueue().getTed()))
        .end();

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
