package com.hongren.careerassistant.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hongren.careerassistant.ai.AiServiceClientException;

@RestControllerAdvice
public class ApiExceptionHandler {
    
    @ExceptionHandler (AiServiceClientException.class)
    public ProblemDetail handleAiServiceClientException(
            AiServiceClientException exception) {

        // 使用固定说明，避免将异常中的内部细节或敏感文本返回给客户端。
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Unable to extract facts. Please try again later.");

        problem.setTitle("AI service request failed");

        return problem;
    }
}
