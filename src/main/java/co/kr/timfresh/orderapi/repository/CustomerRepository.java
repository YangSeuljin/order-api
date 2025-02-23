package co.kr.timfresh.orderapi.repository;

import co.kr.timfresh.orderapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
