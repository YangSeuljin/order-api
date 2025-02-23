package co.kr.timfresh.orderapi.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@UtilityClass
public final class OrderNumberGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    /**
     * UUID + 날짜 기반 고유한 주문번호 생성
     */
    public String generateOrderNumber() {
        return generateOrderNumber(LocalDateTime.now());
    }

    /**
     * 테스트를 용이하게 하기 위해 LocalDateTime을 주입할 수 있도록 오버로딩 메서드 추가
     */
    public String generateOrderNumber(LocalDateTime dateTime) {
        String timestamp = dateTime.format(FORMATTER);
        String uuidShort = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(); // 12자리 사용
        return timestamp + "-" + uuidShort;
    }
}
