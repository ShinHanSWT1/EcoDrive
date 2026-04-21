package com.gorani.market.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NoSuchElementException.class, IllegalArgumentException.class})
    public String handleKnownException(RuntimeException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error";
    }
}

