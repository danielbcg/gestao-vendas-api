package com.daniel.gestaovendasfacil.controller;

import com.daniel.gestaovendasfacil.dto.CampoErroDTO;
import com.daniel.gestaovendasfacil.dto.ErroRespostaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroRespostaDTO> handleValidationException(MethodArgumentNotValidException ex) {
        List<CampoErroDTO> erros = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erros.add(new CampoErroDTO(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        ErroRespostaDTO resposta = new ErroRespostaDTO("Dados inválidos. Por favor, verifique os campos informados.", erros);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroRespostaDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErroRespostaDTO resposta = new ErroRespostaDTO(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroRespostaDTO> handleGenericException(Exception ex) {
        ErroRespostaDTO resposta = new ErroRespostaDTO("Ocorreu um erro interno: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }
}

