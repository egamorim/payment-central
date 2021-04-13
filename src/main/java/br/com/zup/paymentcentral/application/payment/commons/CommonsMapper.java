package br.com.zup.paymentcentral.application.payment.commons;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CommonsMapper {

     default String uuidToCharSequence(BigDecimal value) {

        return Objects.nonNull(value) ? value.toString() : null;
    }

    default String uuidToCharSequence(UUID uuid) {
        return Objects.nonNull(uuid) ? uuid.toString() : null;
    }

    default UUID uuidToCharSequence(CharSequence uuid) {
        return StringUtils.isNotBlank(uuid) ? UUID.fromString(uuid.toString()) : null;
    }
}
