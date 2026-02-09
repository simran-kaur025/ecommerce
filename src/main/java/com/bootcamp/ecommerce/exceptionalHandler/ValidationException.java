package com.bootcamp.ecommerce.exceptionalHandler;

import com.bootcamp.ecommerce.DTO.UserValidationDTO;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<UserValidationDTO> errors;

    public ValidationException(List<UserValidationDTO> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public List<UserValidationDTO> getErrors() {
        return errors;
    }
}

