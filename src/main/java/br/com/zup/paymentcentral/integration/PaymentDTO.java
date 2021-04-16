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

    @JsonProperty("payment_value")
    private BigDecimal paymentValue;

    @JsonProperty("payment_date")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate paymentDate;

    @Override
    public String toString() {
        return "PaymentDTO{" +
                "id='" + id.toString() + '\'' +
                ", payment_value='" + paymentValue.toString() + '\'' +
                ", payment_date_=" + paymentDate.toString() +
                '}';
    }
}
