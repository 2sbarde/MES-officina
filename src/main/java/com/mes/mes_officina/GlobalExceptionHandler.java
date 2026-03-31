package com.mes.mes_officina;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException ex) {

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "errore", ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "errore", "Errore interno"
                ));
    }
}