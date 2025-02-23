package co.kr.timfresh.orderapi.repository;

import co.kr.timfresh.orderapi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByOrderNumber(String orderNumber);
}
