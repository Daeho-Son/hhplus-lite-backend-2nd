package io.hhplus.tdd;

import io.hhplus.tdd.exception.InvalidUserIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(value = InvalidUserIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserIdException(InvalidUserIdException e) {
        log.error("Exception Occurred: {}", e.getMessage());
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Exception Occurred: {}", e.getMessage());
        return ResponseEntity.status(400).body(new ErrorResponse("400", "잘못된 요청입니다."));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception Occurred: {}", e.getMessage());
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }
}
