package ru.practicum.stats.exeptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.practicum.common.util.StatsUtil;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException ex, final WebRequest request) {
        String path = request.getDescription(false).substring(4);
        log.error("[INVALID DATA]: In: {}. Path: {}; Message: {}.", getClassAndMethodName(ex.getStackTrace()), path, ex);

     ErrorResponse error = new ErrorResponse(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                path,
                ex.getMessage()
        );


     return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Error-Class", ex.getClass().getSimpleName()).body(error);

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMissingRequestParamException(
            final MissingServletRequestParameterException ex,
            final WebRequest request)  {
        String path = request.getDescription(false).substring(4);
        log.error("[REQUEST PRAM ERROR]: In: {}. Path: {}; Message: {}.", getClassAndMethodName(ex.getStackTrace()), path, ex);

       ErrorResponse error = new ErrorResponse(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                path,
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Error-Class", ex.getClass().getSimpleName()).body(error);



    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex,
            final WebRequest request) {
        String errors = ex.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" "));
        String path = request.getDescription(false).substring(4);
        log.error("[VALIDATION ERROR]: Path: {}; Message: {}.", path, ex);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now().format(StatsUtil.DATE_TIME_FORMATTER),
                HttpStatus.BAD_REQUEST.value(),
                path,
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Error-Class", ex.getClass().getSimpleName()).body(error);

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleThrowable(final Throwable ex, final WebRequest request) {

        String path = request.getDescription(false).substring(4);
        log.error("[INTERNAL SERVER ERROR]: Path: {}: Message: {}", path, ex);
       ErrorResponse error = new ErrorResponse(
                LocalDateTime.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                path,
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Error-Class", ex.getClass().getSimpleName()).body(error);

    }

    private String getClassAndMethodName(StackTraceElement[] stackTrace) {
        if (stackTrace.length > 1) {
            StackTraceElement element = stackTrace[0];
            return element.getClassName() + "." + element.getMethodName();
        } else {
            return "";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ErrorResponse {

        private String timestamp;

        private int status;

        private String path;

        private String error;
    }
}
