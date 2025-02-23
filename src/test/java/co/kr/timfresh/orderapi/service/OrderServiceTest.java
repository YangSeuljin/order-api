package co.kr.timfresh.orderapi.service;

import co.kr.timfresh.orderapi.dto.OrderItemDto;
import co.kr.timfresh.orderapi.dto.OrderRequestDto;
import co.kr.timfresh.orderapi.dto.OrderResponseDto;
import co.kr.timfresh.orderapi.entity.Customer;
import co.kr.timfresh.orderapi.entity.CustomerType;
import co.kr.timfresh.orderapi.entity.Order;
import co.kr.timfresh.orderapi.entity.Product;
import co.kr.timfresh.orderapi.exception.CustomerNotFoundException;
import co.kr.timfresh.orderapi.exception.InsufficientStockException;
import co.kr.timfresh.orderapi.exception.ProductNotFoundException;
import co.kr.timfresh.orderapi.repository.CustomerRepository;
import co.kr.timfresh.orderapi.repository.OrderRepository;
import co.kr.timfresh.orderapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;


    private Long customerId;
    private Long productId;

    @BeforeEach
    @Transactional
    void setUp() {
        // ✅ 테스트를 위한 기본 데이터 설정
        Customer customer = customerRepository.save(Customer.create("테스트 고객", "서울시 강남구", CustomerType.DEFAULT));
        Product product = productRepository.save(Product.create("테스트 상품", new BigDecimal("100000"), 5));

        customerId = customer.getId();
        productId = product.getId();
    }

    /**
     * 주문 등록 성공
     */
    @Test
    @Transactional
    void 주문_등록_성공() {
        // Given: 주문을 생성
        OrderRequestDto requestDto = new OrderRequestDto(
                customerId,
                List.of(new OrderItemDto(productId, 2))
        );
        OrderResponseDto responseDto = orderService.createOrder(requestDto);

        // When: 생성된 주문을 조회
        Order savedOrder = orderRepository.findByOrderNumber(responseDto.getOrderNumber())
                .orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다."));

        // Then: 주문 정보 검증
        assertNotNull(savedOrder);
        assertEquals(customerId, savedOrder.getCustomer().getId());
        assertEquals(1, savedOrder.getOrderItems().size());
        assertEquals(2, savedOrder.getOrderItems().get(0).getQuantity());
    }

    /**
     * 주문 생성 시 재고 부족 예외 발생
     */
    @Test
    void 주문_생성_실패_재고_부족() {
        // Given: 주문 수량을 재고보다 많게 설정
        OrderRequestDto requestDto = new OrderRequestDto(
                customerId,
                List.of(new OrderItemDto(productId, 10)) // ✅ 재고(5)보다 많은 수량 주문
        );

        // When & Then: 재고 부족 예외 발생 검증
        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(requestDto));
    }

    /**
     * 존재하지 않는 고객 ID로 주문 생성 시 예외 발생
     */
    @Test
    void 주문_생성_실패_없는_고객() {
        // Given: 존재하지 않는 고객 ID 사용
        Long invalidCustomerId = 999L;
        OrderRequestDto requestDto = new OrderRequestDto(
                invalidCustomerId,
                List.of(new OrderItemDto(productId, 2))
        );

        // When & Then: 예외 발생 검증
        assertThrows(CustomerNotFoundException.class, () -> orderService.createOrder(requestDto));
    }

    /**
     * 존재하지 않는 상품 ID로 주문 생성 시 예외 발생
     */
    @Test
    void 주문_생성_실패_없는_상품() {
        // Given: 존재하지 않는 상품 ID 사용
        Long invalidProductId = 999L;
        OrderRequestDto requestDto = new OrderRequestDto(
                customerId,
                List.of(new OrderItemDto(invalidProductId, 2))
        );

        // When & Then: 예외 발생 검증
        assertThrows(ProductNotFoundException.class, () -> orderService.createOrder(requestDto));
    }

    /**
     * 주문 목록 조회
     */
    @Test
    @Transactional
    void 주문_목록_조회() {
        // Given: 여러 개의 주문 생성
        OrderRequestDto requestDto1 = new OrderRequestDto(
                customerId,
                List.of(new OrderItemDto(productId, 1))
        );
        OrderRequestDto requestDto2 = new OrderRequestDto(
                customerId,
                List.of(new OrderItemDto(productId, 1))
        );

        orderService.createOrder(requestDto1);
        orderService.createOrder(requestDto2);

        // When: 전체 주문 목록 조회
        List<Order> orders = orderRepository.findAll();

        // Then: 주문 개수 검증
        assertNotNull(orders);
        assertTrue(orders.size() >= 2); // 최소 2개 이상의 주문이 있어야 함
    }

    @Test
    void 주문_생성_성공_VIP_할인_적용() {
        // Given: VIP 고객 생성
        Customer vipCustomer = customerRepository.save(Customer.create("VIP 고객", "서울시 강남구", CustomerType.VIP));
        Product product = productRepository.save(Product.create("프리미엄 노트북", new BigDecimal("1000000"), 10));

        OrderRequestDto requestDto = new OrderRequestDto(
                vipCustomer.getId(),
                List.of(new OrderItemDto(product.getId(), 2)) // 2개 주문
        );

        // When
        OrderResponseDto responseDto = orderService.createOrder(requestDto);

        // Then
        assertNotNull(responseDto.getOrderNumber());
        assertEquals(1, responseDto.getOrderItems().size());

        // 재고 차감 검증
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(8, updatedProduct.getStockQuantity());

        // VIP 고객 할인 적용 검증
        BigDecimal expectedPrice = new BigDecimal("1000000")
                .multiply(new BigDecimal("0.9"))
                .multiply(new BigDecimal("2"));

        assertEquals(0, expectedPrice.compareTo(responseDto.getOrderItems().get(0).getPrice()), "VIP 할인 적용된 가격이 일치해야 함");
    }

    @Test
    void 동시_주문_분산락_테스트() throws InterruptedException {
        Product product = Product.create("테스트 상품", BigDecimal.valueOf(100), 5);
        Customer customer = Customer.create("양슬진", "중구", CustomerType.DEFAULT);

        customerRepository.save(customer);
        productRepository.save(product);
        Long productId = product.getId();
        Long customerId = customer.getId();

        int threadCount = 10;  // 10개의 스레드가 동시에 주문을 시도
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // AtomicInteger 사용 (thread-safe)
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderItemDto orderItem = new OrderItemDto(productId, 1); // 상품 1개 주문
                    OrderRequestDto requestDto = new OrderRequestDto(customerId, List.of(orderItem));

                    orderService.createOrder(requestDto);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();  // 모든 스레드가 종료될 때까지 대기

        assertEquals(5, successCount.get(), "최대 5개 주문만 성공해야 함");
        assertEquals(5, failureCount.get(), "나머지 5개는 재고 부족으로 실패해야 함");
        assertEquals(5, orderRepository.count(), "주문 개수 5개 검증");
        assertEquals(0, productRepository.findById(productId).orElseThrow().getStockQuantity(), "재고는 0이 되어야 함");
    }
}
