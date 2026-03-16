package com.sevenshop.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("【业务异常】请求地址: {}, 异常信息: {}",
                request.getRequestURI(), e.getMessage());
        // 未登录异常(401)返回HTTP 401状态码，其他业务异常返回HTTP 400
        HttpStatus status = e.getCode() == 401 ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("【参数校验异常】请求地址: {}, 异常信息: {}",
                request.getRequestURI(), errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest(errors));
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e, HttpServletRequest request) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("【参数绑定异常】请求地址: {}, 异常信息: {}",
                request.getRequestURI(), errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest(errors));
    }

    /**
     * 处理运行时异常 - RuntimeException 是 Exception 的子类，会被优先匹配
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("【运行时异常】请求地址: {}, 请求方法: {}, 异常类型: {}, 异常信息: {}",
                request.getRequestURI(),
                request.getMethod(),
                e.getClass().getName(),
                e.getMessage());

        // 检查是否是业务相关的异常
        String message = e.getMessage();
        boolean isBusinessException = message != null && (
                message.contains("用户") ||
                message.contains("密码") ||
                message.contains("不存在") ||
                message.contains("错误") ||
                message.contains("已存在")
        );

        String responseMessage;
        if (isBusinessException) {
            responseMessage = message;
            log.warn("【业务异常】已处理: {}", message);
        } else {
            log.error("【非业务运行时异常】类型: {}, 消息: {}", e.getClass().getName(), message);
            log.error("【堆栈信息】", e);
            responseMessage = "系统繁忙，请稍后再试";
        }

        if (isBusinessException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(responseMessage));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(responseMessage));
        }
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("【系统异常】请求地址: {}, 请求方法: {}, 异常类型: {}, 异常信息: {}",
                request.getRequestURI(),
                request.getMethod(),
                e.getClass().getName(),
                e.getMessage());
        log.error("【堆栈信息】", e);

        String responseMessage = "系统繁忙，请稍后再试";

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(responseMessage));
    }
}
