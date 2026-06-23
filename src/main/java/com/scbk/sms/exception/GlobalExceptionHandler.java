package com.scbk.sms.exception;

import com.scbk.sms.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 공통 예외 변환 지점. Controller에서 예외를 삼키지 않고 이 클래스로 전파한다.
 *
 * - JSON 요청(/data, /create 등): ApiResponse로 변환
 * - 화면 요청(Accept: text/html): 공통 에러 페이지(error/error)로 변환
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** [1] 비즈니스 CustomException */
    @ExceptionHandler(CustomException.class)
    protected Object handleCustomException(CustomException e, HttpServletRequest request) {
        log.error("handleCustomException", e);
        ErrorCode errorCode = e.getErrorCode();
        return respond(errorCode.getStatus(), errorCode.getMessage(), request);
    }

    /** [2] @Valid 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                           HttpServletRequest request) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
        log.warn("입력값 검증 실패: {}", errorMessage);
        return respond(ErrorCode.INVALID_INPUT_VALUE.getStatus(),
            errorMessage != null ? errorMessage : ErrorCode.INVALID_INPUT_VALUE.getMessage(),
            request);
    }

    /**
     * [3] URL 매핑 없음 (404) — 컨트롤러가 없거나 메뉴 URL이 잘못된 경우
     */
    @ExceptionHandler(NoResourceFoundException.class)
    protected Object handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        String url = e.getResourcePath();
        String message = String.format(
            "요청한 URL [%s]에 대한 화면이 없습니다. "
            + "원인: ① 컨트롤러 미등록 ② 메뉴 URL 오타 ③ 파일 미배치 중 하나를 확인하세요.", url);
        log.warn("NoResourceFoundException — URL: {}", url);
        return respond(HttpStatus.NOT_FOUND, message, request);
    }

    /** [4] 그 외 서버 내부 오류 */
    @ExceptionHandler(Exception.class)
    protected Object handleException(Exception e, HttpServletRequest request) {
        log.error("handleException", e);
        return respond(ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
            ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
            request);
    }

    private Object respond(HttpStatus status, String message, HttpServletRequest request) {
        if (isHtmlRequest(request)) {
            ModelAndView mav = new ModelAndView("error/error");
            mav.setStatus(status);
            mav.addObject("status", status.value());
            mav.addObject("message", message);
            return mav;
        }
        return new ResponseEntity<>(ApiResponse.error(status.value(), message), status);
    }

    /** 브라우저 화면 이동(Accept: text/html)과 JSON 호출(axios/fetch)을 구분한다. */
    private boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }
}
