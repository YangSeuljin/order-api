package co.kr.timfresh.orderapi.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(nullable = false)
    @Getter
    private String name;

    @Column(nullable = false)
    @Getter
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Getter
    private CustomerType customerType;

    protected Customer() {

    }

    /**
     * 고객을 생성하는 정적 팩토리 메서드
     *
     * @param name         고객 이름
     * @param address      고객 주소
     * @param customerType 고객 유형 (기본값: DEFAULT)
     * @return 생성된 Customer 객체
     */
    public static Customer create(String name, String address, CustomerType customerType) {
        return new Customer(name, address, customerType);
    }

    private Customer(String name, String address, CustomerType customerType) {
        this.name = name;
        this.address = address;
        this.customerType = customerType;
    }

}

