package co.kr.timfresh.orderapi.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {
    public InsufficientStockException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
