package co.kr.timfresh.orderapi.repository;

import co.kr.timfresh.orderapi.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}