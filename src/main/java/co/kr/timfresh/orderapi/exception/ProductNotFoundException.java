package co.kr.timfresh.orderapi.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends BaseException {
    public ProductNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
