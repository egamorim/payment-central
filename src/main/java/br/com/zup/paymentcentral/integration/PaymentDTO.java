package br.com.zup.paymentcentral.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class PaymentDTO {

    private UUID id;

    @JsonProperty("value_payment")
    private BigDecimal valuePayment;

    @JsonProperty("date_payment")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate datePayment;

    @Override
    public String toString() {
        return "PaymentDTO{" +
                "id='" + id.toString() + '\'' +
                ", value_payment='" + valuePayment.toString() + '\'' +
                ", date_payment=" + datePayment.toString() +
                '}';
    }
}
