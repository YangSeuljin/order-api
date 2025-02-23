package co.kr.timfresh.orderapi.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DefaultPriceStrategy implements PriceStrategy {
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity) {
        return basePrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP); // 소수점 반올림 처리
    }
}
