package co.kr.timfresh.orderapi.strategy;

import co.kr.timfresh.orderapi.entity.CustomerType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PriceStrategyFactory {
    private final Map<CustomerType, PriceStrategy> strategyMap;

    public PriceStrategyFactory(DefaultPriceStrategy defaultPriceStrategy, VipPriceStrategy vipPriceStrategy) {
        this.strategyMap = Map.of(
                CustomerType.VIP, vipPriceStrategy,
                CustomerType.DEFAULT, defaultPriceStrategy
        );
    }

    public PriceStrategy getStrategy(CustomerType customerType) {
        return strategyMap.getOrDefault(customerType, strategyMap.get(CustomerType.DEFAULT));
    }
}