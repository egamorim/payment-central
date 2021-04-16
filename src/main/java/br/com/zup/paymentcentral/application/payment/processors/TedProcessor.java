package br.com.zup.paymentcentral.application.payment.processors;

import br.com.zup.paymentcentral.application.payment.ted.included.TedIncludedMapper;
import br.com.zup.paymentcentral.integration.PaymentDTO;
import br.com.zup.paymentcentral.ted_included.TedIncluded;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Qualifier("TedProcessor")
public class TedProcessor implements PaymentProcessor {

    private final TedIncludedMapper tedIncludedMapper;

    public TedProcessor(TedIncludedMapper tedIncludedMapper) {
        this.tedIncludedMapper = tedIncludedMapper;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        TedIncluded tedIncluded = tedIncludedMapper.paymentDtoToTedIncluded(exchange.getIn().getBody(PaymentDTO.class));
        exchange.getIn().setHeader("CamelAwsDdbItem", buildDocument(tedIncluded));
    }

    private Map<String, AttributeValue> buildDocument(TedIncluded tedIncluded) {
        Map<String, AttributeValue> newBody = new HashMap();

        newBody.put("id", new AttributeValue(tedIncluded.getId().toString()));
        newBody.put("datePayment", new AttributeValue(tedIncluded.getPaymentDate().toString()));
        newBody.put("valuePayment", new AttributeValue(tedIncluded.getPaymentValue().toString()));

        return newBody;
    }
}
