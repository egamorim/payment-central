package br.com.zup.paymentcentral.application.payment.ted.included;

import br.com.zup.paymentcentral.application.payment.commons.CommonsMapper;
import br.com.zup.paymentcentral.integration.PaymentDTO;
import br.com.zup.paymentcentral.ted_included.TedIncluded;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(uses = CommonsMapper.class, builder = @Builder(disableBuilder = true), componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TedIncludedMapper {

    TedIncluded paymentDtoToTedIncluded(final PaymentDTO paymentDTO);
}
