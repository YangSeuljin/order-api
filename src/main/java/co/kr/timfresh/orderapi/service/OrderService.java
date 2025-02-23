package co.kr.timfresh.orderapi.service;

import co.kr.timfresh.orderapi.dto.OrderItemDto;
import co.kr.timfresh.orderapi.dto.OrderRequestDto;
import co.kr.timfresh.orderapi.dto.OrderResponseDto;
import co.kr.timfresh.orderapi.entity.Customer;
import co.kr.timfresh.orderapi.entity.Order;
import co.kr.timfresh.orderapi.entity.OrderItem;
import co.kr.timfresh.orderapi.entity.Product;
import co.kr.timfresh.orderapi.exception.CustomerNotFoundException;
import co.kr.timfresh.orderapi.exception.ProductNotFoundException;
import co.kr.timfresh.orderapi.repository.CustomerRepository;
import co.kr.timfresh.orderapi.repository.OrderRepository;
import co.kr.timfresh.orderapi.repository.ProductRepository;
import co.kr.timfresh.orderapi.strategy.PriceStrategy;
import co.kr.timfresh.orderapi.strategy.PriceStrategyFactory;
import co.kr.timfresh.orderapi.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final PriceStrategyFactory priceStrategyFactory;

    /**
     * 주문 생성 메서드 (Pessimistic Lock 적용, 가격 전략 반영)
     *
     * @param requestDto 주문 요청 정보
     * @return 생성된 주문의 응답 DTO
     */
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {

        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("고객 정보를 찾을 수 없습니다."));

        Order order = Order.create(OrderNumberGenerator.generateOrderNumber(), customer);

        for (OrderItemDto itemDto : requestDto.getOrderItems()) {

            Product product = productRepository.findByIdWithLock(itemDto.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));

            product.decreaseStock(itemDto.getQuantity());

            PriceStrategy priceStrategy = priceStrategyFactory.getStrategy(customer.getCustomerType());
            BigDecimal finalPrice = priceStrategy.calculatePrice(product.getPrice(), itemDto.getQuantity());

            order.addOrderItem(OrderItem.create(product, itemDto.getQuantity(), finalPrice));
        }

        // 주문 저장
        orderRepository.save(order);

        // 응답 DTO 반환
        return new OrderResponseDto(order);
    }
}
