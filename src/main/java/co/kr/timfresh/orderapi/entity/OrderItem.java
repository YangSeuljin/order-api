package co.kr.timfresh.orderapi.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @Getter
    private Product product;

    @Column(nullable = false)
    @Getter
    private int quantity;

    @Column(nullable = false)
    @Getter
    private BigDecimal price;

    protected OrderItem() {

    }

    public static OrderItem create(Product product, int quantity, BigDecimal price) {
        return new OrderItem(product, quantity, price);
    }

    private OrderItem(Product product, int quantity, BigDecimal price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public void assignOrder(Order order) {
        if (this.order != null) {
            throw new IllegalStateException("이미 주문에 할당된 주문 항목입니다.");
        }
        this.order = order;
    }
}
