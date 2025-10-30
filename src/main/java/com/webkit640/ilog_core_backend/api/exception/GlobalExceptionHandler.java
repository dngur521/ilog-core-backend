package com.webkit640.ilog_core_backend.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.webkit640.ilog_core_backend.api.response.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //기본 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e){
        ErrorResponse error = new ErrorResponse(
                e.getErrorCode().getStatus(),
                e.getErrorCode().getMessage()
        );
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(error);
    }
    
    //Valid 검증 실패 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e){
        e.printStackTrace(); // 개발이 끝난 뒤에는 삭제해야한다.
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,"서버 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
