package co.kr.timfresh.orderapi.entity;

import co.kr.timfresh.orderapi.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int stockQuantity;

    protected Product() {
    }

    public static Product create(String name, BigDecimal price, int stockQuantity) {
        return new Product(name, price, stockQuantity);
    }

    private Product(String name, BigDecimal price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new InsufficientStockException("재고가 부족합니다. (현재 재고: " + this.stockQuantity + ")");
        }
        this.stockQuantity -= quantity;
    }
}



