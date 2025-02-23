package co.kr.timfresh.orderapi.controller;

import co.kr.timfresh.orderapi.dto.OrderRequestDto;
import co.kr.timfresh.orderapi.dto.OrderResponseDto;
import co.kr.timfresh.orderapi.exception.FileEmptyException;
import co.kr.timfresh.orderapi.exception.UnsupportedFileTypeException;
import co.kr.timfresh.orderapi.service.OrderExcelService;
import co.kr.timfresh.orderapi.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-excel" // .xls
    );

    private final OrderService orderService;
    private final OrderExcelService orderExcelService;

    /**
     * 주문을 생성하는 API
     *
     * @param requestDto 주문 요청 정보
     * @return 생성된 주문의 응답 DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto createOrder(@Valid @RequestBody OrderRequestDto requestDto) {
        return orderService.createOrder(requestDto);
    }

    /**
     * 엑셀 파일을 업로드하여 주문을 등록하는 API
     *
     * @param file 업로드할 엑셀 파일
     * @return 생성된 주문 목록의 응답 DTO 리스트
     * @throws FileEmptyException 파일이 비어 있을 경우 예외 발생
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public List<OrderResponseDto> uploadOrders(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileEmptyException();
        }

        // 파일 MIME 타입 체크
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new UnsupportedFileTypeException("지원하지 않는 파일 형식입니다.");
        }

        return orderExcelService.processExcelOrders(file);
    }
}

