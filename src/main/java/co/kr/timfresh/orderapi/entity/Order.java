package co.kr.timfresh.orderapi.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(nullable = false)
    @Getter
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @Getter
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private final List<OrderItem> orderItems = new ArrayList<>();

    protected Order() {

    }

    private Order(String orderNumber, Customer customer) {
        this.orderNumber = orderNumber;
        this.customer = customer;
    }

    public static Order create(String orderNumber, Customer customer) {
        return new Order(orderNumber, customer);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItem.assignOrder(this);
        orderItems.add(orderItem);
    }
}




