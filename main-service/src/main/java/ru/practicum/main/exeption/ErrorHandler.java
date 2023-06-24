package ru.practicum.main.exeption;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.main.util.DTFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {


    @Getter
    private static class ApiError {

        private String status;

        private String reason;

        private String message;

        private String errors;

        private String timestamp;

        public ApiError(String status, String reason, String message, String errors, String timestamp) {
            this.status = status;
            this.reason = reason;
            this.message = message;
            this.errors = errors;
            this.timestamp = timestamp;
        }

        public ApiError(String status, String reason, String message, String timestamp) {
            this.status = status;
            this.reason = reason;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        log.error("[VALIDATION ERROR]: {}.", ex.getMessage(), ex);
        String asString = getAsString(ex);
        BindingResult bindingResult = ex.getBindingResult();
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(fieldError -> String.format("Field: %s. Error: %s Value: %s.",
                        fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue()))
                .collect(Collectors.joining(" "));
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "Incorrectly made request.",
                errorMessage,
                asString,
                LocalDateTime.now().format(DTFormatter.DATE_TIME_FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(final MissingServletRequestParameterException ex) {
        log.error("[REQUEST PARAMETER ERROR]: {}.", ex.getMessage(), ex);
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "Incorrectly made request.",
                ex.getMessage(),
                getAsString(ex),
                LocalDateTime.now().format(DTFormatter.DATE_TIME_FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final DataIntegrityViolationException ex) {
        log.error("[DATABASE CONSTRAINT ERROR]: {}.", ex.getMessage(), ex);
        return new ApiError(
                HttpStatus.CONFLICT.name(),
                "Integrity constraint has been violated.",
                ex.getMostSpecificCause().getMessage(), //Integrity constraint has been violated
                getAsString(ex),
                LocalDateTime.now().format(DTFormatter.DATE_TIME_FORMATTER)
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(final ConstraintViolationException ex) {
        log.error("[DATABASE CONSTRAINT ERROR]: {}.", ex.getMessage(), ex);
        return new ApiError(
                HttpStatus.CONFLICT.name(),
                "Integrity constraint has been violated.",
                ex.getCause().getMessage(),
                getAsString(ex),
                LocalDateTime.now().format(DTFormatter.DATE_TIME_FORMATTER)
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException ex) {
        log.error("[NOT FOUND ERROR]: {}.", ex.getMessage(), ex);
        return new ApiError(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                ex.getMessage(),
                getAsString(ex),
                LocalDateTime.now().format(DTFormatter.DATE_TIME_FORMATTER)
        );

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final BadRequest ex) {
        log.error("[Bad Request ERROR]: {},", ex.getCause(), ex);
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "For the requested operation the conditions are not met.",
                ex.getMessage(),
                getAsString(ex),
                LocalDateTime.now().format(DTFormatter.DATE_TIME_FORMATTER)
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException ex) {
        log.error("[Conflict ERROR]: {},", ex.getCause(), ex);
        return new ApiError(
                HttpStatus.FORBIDDEN.name(),
                "For the requested operation the conditions are not met",
                ex.getMessage(),
                getAsString(ex),
                LocalDateTime.now().format(DTFormatter.DATE_TIME_FORMATTER)
        );
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleExceptionError(final Exception ex) {
        log.error("[INTERNAL SERVER ERROR]: {},", ex.getMessage(), ex);
        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Unhandled exception",
                ex.getMessage(),
                getAsString(ex),
                LocalDateTime.now().format(DTFormatter.DATE_TIME_FORMATTER)
        );
    }

    private String getAsString(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

}
