package com.gorani.oilbank.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        log.error("[Oilbank] 처리 오류", ex);
        model.addAttribute("message", ex.getMessage() == null ? "처리 중 오류 발생" : ex.getMessage());
        return "error";
    }
}

