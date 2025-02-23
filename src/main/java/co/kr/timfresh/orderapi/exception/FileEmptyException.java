package co.kr.timfresh.orderapi.exception;

import org.springframework.http.HttpStatus;

public class FileEmptyException extends BaseException {
    public FileEmptyException() {
        super(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다.");
    }
}
