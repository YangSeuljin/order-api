package co.kr.timfresh.orderapi.exception;


import org.springframework.http.HttpStatus;

public class ExcelProcessingException extends BaseException {
    public ExcelProcessingException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}