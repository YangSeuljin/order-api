package co.kr.timfresh.orderapi.service;


import co.kr.timfresh.orderapi.dto.OrderItemDto;
import co.kr.timfresh.orderapi.dto.OrderRequestDto;
import co.kr.timfresh.orderapi.dto.OrderResponseDto;
import co.kr.timfresh.orderapi.exception.CustomerNotFoundException;
import co.kr.timfresh.orderapi.exception.ExcelProcessingException;
import co.kr.timfresh.orderapi.repository.CustomerRepository;
import co.kr.timfresh.orderapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 엑셀을 통해 주문을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class OrderExcelService {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    /**
     * 엑셀 파일을 통해 주문을 처리
     *
     * @param file 업로드된 엑셀 파일
     * @return 생성된 주문 응답 DTO 리스트
     */
    @Transactional
    public List<OrderResponseDto> processExcelOrders(MultipartFile file) {
        List<OrderRequestDto> orderRequestDtoList = parseExcelFile(file);
        List<OrderResponseDto> orderResponseDtoList = new ArrayList<>();

        for (OrderRequestDto requestDto : orderRequestDtoList) {
            orderResponseDtoList.add(orderService.createOrder(requestDto));
        }

        return orderResponseDtoList;
    }

    /**
     * 엑셀 파일을 파싱하여 주문 요청 DTO 리스트를 생성
     *
     * @param file 업로드된 엑셀 파일
     * @return 주문 요청 DTO 리스트
     * @throws ExcelProcessingException 파일 처리 중 오류 발생 시
     */
    private List<OrderRequestDto> parseExcelFile(MultipartFile file) {
        List<OrderRequestDto> orderRequests = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 첫 번째 행(헤더) 스킵

                OrderRequestDto orderRequest = validateAndExtractData(row);
                orderRequests.add(orderRequest);
            }

        } catch (IOException e) {
            throw new ExcelProcessingException("엑셀 파일을 처리하는 중 오류가 발생했습니다.");
        }

        return orderRequests;
    }

    /**
     * 개별 엑셀 행을 검증하고 주문 요청 DTO로 변환
     *
     * @param row 엑셀 행
     * @return 주문 요청 DTO
     * @throws CustomerNotFoundException, ProductNotFoundException 데이터 유효성 검사 실패 시
     */
    private OrderRequestDto validateAndExtractData(Row row) {
        Long customerId = (long) row.getCell(0).getNumericCellValue();
        Long productId = (long) row.getCell(1).getNumericCellValue();
        int quantity = (int) row.getCell(2).getNumericCellValue();

        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("고객이 존재하지 않습니다. (고객 ID: " + customerId + ")"));
        productRepository.findById(productId)
                .orElseThrow(() -> new CustomerNotFoundException("상품이 존재하지 않습니다. (상품 ID: " + productId + ")"));

        return new OrderRequestDto(customerId, new ArrayList<>(List.of(new OrderItemDto(productId, quantity))));
    }
}


