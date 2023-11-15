package com.apptive.marico.controller;

import com.apptive.marico.dto.error.ErrorDto;
import com.apptive.marico.dto.error.UnknownErrorDto;
import com.apptive.marico.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.apptive.marico.exception.ErrorCode.*;

@RestControllerAdvice
public class ExceptionHandlers {
    @ExceptionHandler({ CustomException.class })
    protected ResponseEntity handleCustomException(CustomException exception) {
        return new ResponseEntity(new ErrorDto(exception.getErrorCode().getStatus(), exception.getErrorCode().getMessage()), HttpStatus.valueOf(exception.getErrorCode().getStatus()));
    }

    @ExceptionHandler({ Exception.class })
    protected ResponseEntity handleServerException(Exception exception) {
        System.out.println(exception.getMessage());
        exception.printStackTrace();
        return new ResponseEntity(new UnknownErrorDto(INTERNAL_SERVER_ERROR.getStatus(), INTERNAL_SERVER_ERROR.getMessage(), exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ BadCredentialsException.class })
    protected ResponseEntity handleAuthenticationException(BadCredentialsException exception) {
        System.out.println(exception.getMessage());
        exception.printStackTrace();
        return new ResponseEntity(new ErrorDto(ID_OR_PASSWORD_NOT_MATCH.getStatus(), ID_OR_PASSWORD_NOT_MATCH.getMessage()), HttpStatus.valueOf(ID_OR_PASSWORD_NOT_MATCH.getStatus()));
    }
}
