package co.kr.timfresh.orderapi.service;

import co.kr.timfresh.orderapi.dto.OrderResponseDto;
import co.kr.timfresh.orderapi.entity.*;
import co.kr.timfresh.orderapi.exception.InsufficientStockException;
import co.kr.timfresh.orderapi.repository.CustomerRepository;
import co.kr.timfresh.orderapi.repository.OrderRepository;
import co.kr.timfresh.orderapi.repository.ProductRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderExcelServiceTest {

    @Autowired
    private OrderExcelService orderExcelService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private MockMultipartFile validExcelFile;
    private Long customerId;
    private Long productId;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 고객 및 상품 데이터 저장
        Customer customer = customerRepository.save(Customer.create("테스트 고객", "서울시 강남구", CustomerType.DEFAULT));
        Product product = productRepository.save(Product.create("테스트 상품", new BigDecimal("100000"), 10));

        customerId = customer.getId();
        productId = product.getId();

        validExcelFile = createValidExcelFile();
    }

    @Test
    void 주문_엑셀_처리_성공() {
        // When
        List<OrderResponseDto> responses = orderExcelService.processExcelOrders(validExcelFile);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());

        OrderResponseDto response = responses.get(0);
        assertNotNull(response.getOrderNumber());

        Optional<Order> savedOrder = orderRepository.findById(response.getOrderNumber());
        assertTrue(savedOrder.isPresent(), "주문이 실제 DB에 저장되어야 함");

        Order order = savedOrder.get();
        assertEquals(customerId, order.getCustomer().getId(), "주문한 고객이 일치해야 함");

        assertEquals(1, order.getOrderItems().size(), "주문 항목이 1개여야 함");
        OrderItem orderItem = order.getOrderItems().get(0);
        assertEquals(productId, orderItem.getProduct().getId(), "상품 ID가 일치해야 함");
        assertEquals(2, orderItem.getQuantity(), "주문 수량이 일치해야 함");
    }

    @Test
    void 주문_실패_재고부족() {
        // Given: 재고보다 많은 수량 주문 요청
        Product product = productRepository.findById(productId).orElseThrow();
        product.decreaseStock(10); // ✅ 재고 소진
        productRepository.save(product);

        // When & Then
        assertThrows(InsufficientStockException.class, () -> orderExcelService.processExcelOrders(validExcelFile));
    }

    /**
     * 엑셀 파일 생성 함수 (MockMultipartFile)
     */
    private MockMultipartFile createValidExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");

        // 첫 번째 행 (헤더)
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Customer ID");
        headerRow.createCell(1).setCellValue("Product ID");
        headerRow.createCell(2).setCellValue("Quantity");

        // 두 번째 행 (테스트 데이터)
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(customerId); // Customer ID
        dataRow.createCell(1).setCellValue(productId); // Product ID
        dataRow.createCell(2).setCellValue(2); // Quantity

        // 엑셀 파일을 바이트 배열로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile("file", "orders.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                outputStream.toByteArray());
    }
}




