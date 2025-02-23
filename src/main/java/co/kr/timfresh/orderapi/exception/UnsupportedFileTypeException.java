package co.kr.timfresh.orderapi.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedFileTypeException extends BaseException {
    public UnsupportedFileTypeException(String message) {
        super(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
    }
}
