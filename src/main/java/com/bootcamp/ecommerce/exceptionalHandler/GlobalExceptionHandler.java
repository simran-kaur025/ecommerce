package com.bootcamp.ecommerce.exceptionalHandler;

import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.constant.Constant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleException(Exception ex) {

        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.builder()
                        .status(Constant.FAIL)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDTO.builder()
                        .status(Constant.FAIL)
                        .message(ex.getMessage())
                        .build());
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .toList();

        ErrorDetail exceptionDetail = new ErrorDetail(
                HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return new ResponseEntity<>(exceptionDetail, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ResponseDTO> handleValidationException(
            ValidationException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.builder()
                        .status(Constant.FAIL)
                        .message("Validation failed")
                        .data(ex.getErrors())
                        .build());
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ResponseDTO> handleInvalidOperation(
            InvalidOperationException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.builder()
                        .status(Constant.FAIL)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDTO.builder()
                        .status("FAIL")
                        .message(ex.getMessage())
                        .build());

    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleCustomerNotFound(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDTO.builder()
                        .status("FAIL")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseDTO> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDTO.builder()
                        .status(Constant.FAIL)
                        .message(ex.getMessage())
                        .build());
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseDTO> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.builder()
                        .status(Constant.FAIL)
                        .message(ex.getMessage())
                        .build());
    }
}


