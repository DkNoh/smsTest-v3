package com.example.sms.dto.common;

import java.time.LocalDateTime;

/**
 * 모든 JSON API 응답을 감싸는 공통 규격.
 * 성공/실패와 관계없이 프론트엔드는 항상 이 규격의 JSON을 받는다.
 *
 * 성공: return ResponseEntity.ok(ApiResponse.success(data));
 * 실패: return ResponseEntity.status(400).body(ApiResponse.error(400, "잘못된 요청"));
 */
public class ApiResponse<T> {

    private final String timestamp;
    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(int code, String message, T data) {
        this.timestamp = LocalDateTime.now().toString();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "SUCCESS", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
