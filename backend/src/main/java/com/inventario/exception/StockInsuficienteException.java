package com.inventario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String message) {
        super(message);
    }

    public StockInsuficienteException(String producto, Integer stockActual, Integer cantidadSolicitada) {
        super(String.format("Stock insuficiente para '%s'. Stock actual: %d, Cantidad solicitada: %d",
            producto, stockActual, cantidadSolicitada));
    }
}

