package co.kr.timfresh.orderapi.dto;

import co.kr.timfresh.orderapi.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemDto {

    @NotNull(message = "상품 ID는 필수 입력값입니다.")
    private final Long productId;

    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
    private final int quantity;

    @JsonInclude(JsonInclude.Include.NON_NULL) // `null` 값은 응답에서 제외
    private final BigDecimal price; // 응답 시 사용

    public OrderItemDto(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = null; // 가격 정보는 요청 시 포함되지 않음
    }

    public OrderItemDto(OrderItem orderItem) {
        this.productId = orderItem.getProduct().getId();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getPrice();
    }
}

