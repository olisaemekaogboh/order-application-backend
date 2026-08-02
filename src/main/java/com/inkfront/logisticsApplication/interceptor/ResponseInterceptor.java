package com.inkfront.logisticsApplication.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ResponseInterceptor implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {

        // Skip file downloads (PDF, Excel, CSV, etc.)
        return !ResourceHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // Already wrapped
        if (body instanceof ApiResponseDTO) {
            return body;
        }

        // Skip error responses
        if (response instanceof ServletServerHttpResponse servletResponse) {
            HttpServletResponse httpServletResponse = servletResponse.getServletResponse();

            if (httpServletResponse.getStatus() >= 400) {
                return body;
            }
        }

        // Skip binary responses
        if (MediaType.APPLICATION_PDF.includes(selectedContentType)
                || MediaType.APPLICATION_OCTET_STREAM.includes(selectedContentType)
                || MediaType.TEXT_PLAIN.includes(selectedContentType)
                || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                .equalsIgnoreCase(selectedContentType.toString())) {
            return body;
        }

        // Wrap all normal JSON responses
        return ApiResponseDTO.success("Success", body);
    }
}