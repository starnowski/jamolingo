package com.github.starnowski.jamolingo.demo;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handle(Exception ex) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "error",
                ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
  }
}
