package com.cnytez.app.exception;

import com.cnytez.app.dto.response.ApiError;
import com.cnytez.app.dto.response.ApiErrorResponse;
import com.cnytez.app.dto.response.ErrorDetail;
import com.cnytez.app.logging.LogLevel;
import com.cnytez.app.logging.LogManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LogManager logManager;


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                "The requested endpoint does not exist.",
                List.of(),
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "The request body is missing or contains invalid JSON.",
                List.of(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "A request parameter has an invalid format.",
                List.of(),
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                exception.getMessage(),
                List.of(),
                request
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                exception.getMessage(),
                List.of(),
                request
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.CONFLICT,
                "CONFLICT",
                exception.getMessage(),
                List.of(),
                request
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                exception.getMessage(),
                List.of(),
                request
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            ForbiddenException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                exception.getMessage(),
                List.of(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorDetail(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "The supplied data is invalid.",
                details,
                request
        );
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiErrorResponse> handleUnprocessableEntity(
            UnprocessableEntityException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "UNPROCESSABLE_ENTITY",
                exception.getMessage(),
                List.of(),
                request
        );
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleInternalServerError(
            InternalServerErrorException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                exception.getMessage(),
                List.of(),
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(
            HttpStatus status,
            String code,
            String message,
            List<ErrorDetail> details,
            HttpServletRequest request
    ) {
        logManager.log(message, LogLevel.ERROR);

        ApiError error = new ApiError(
                code,
                message,
                details
        );

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                error,
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}