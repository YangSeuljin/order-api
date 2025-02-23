package co.kr.timfresh.orderapi.strategy;

import java.math.BigDecimal;

public interface PriceStrategy {
    BigDecimal calculatePrice(BigDecimal basePrice, int quantity);
}
