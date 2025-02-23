package co.kr.timfresh.orderapi.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class VipPriceStrategy implements PriceStrategy {
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity) {
        BigDecimal totalPrice = basePrice.multiply(BigDecimal.valueOf(quantity));
        return totalPrice.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP); // 10% 할인 적용 (소수점 반올림)
    }
}
