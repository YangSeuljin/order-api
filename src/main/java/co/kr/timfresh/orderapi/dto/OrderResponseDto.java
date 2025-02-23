package co.kr.timfresh.orderapi.dto;

import co.kr.timfresh.orderapi.entity.Order;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderResponseDto {

    private final String orderNumber;
    private final String customerName;
    private final String customerAddress;
    private final List<OrderItemDto> orderItems;

    public OrderResponseDto(Order order) {
        this.orderNumber = order.getOrderNumber();
        this.customerName = order.getCustomer().getName();
        this.customerAddress = order.getCustomer().getAddress();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }
}

