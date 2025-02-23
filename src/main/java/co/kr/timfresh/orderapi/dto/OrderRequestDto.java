package co.kr.timfresh.orderapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderRequestDto {

    @NotNull(message = "고객 ID는 필수 입력값입니다.")
    private Long customerId;

    @Valid
    @NotNull(message = "주문 항목 목록은 필수 입력값입니다.")
    private List<OrderItemDto> orderItems;

    public OrderRequestDto(Long customerId, List<OrderItemDto> orderItems) {
        this.customerId = customerId;
        this.orderItems = orderItems;
    }
}

