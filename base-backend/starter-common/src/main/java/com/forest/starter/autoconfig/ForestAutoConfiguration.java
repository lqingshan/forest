package com.forest.starter.autoconfig;

import com.forest.starter.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 项目自动配置类。
 *
 * 提供项目通用的自动配置：
 * - 全局异常处理
 * - 统一响应格式
 */
@Configuration
@ConditionalOnClass(BusinessException.class)
public class ForestAutoConfiguration {

    /**
     * 全局异常处理器
     */
    @RestControllerAdvice
    public static class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(BusinessException.class)
        public com.forest.starter.common.Result<?> handleBusinessException(BusinessException ex) {
            if (ex.getCause() != null) {
                log.warn("Business exception with cause: {}", ex.getMessage(), ex);
            }
            return com.forest.starter.common.Result.error(ex.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public com.forest.starter.common.Result<?> handleUnexpectedException(Exception ex) {
            log.error("Unexpected exception", ex);
            return com.forest.starter.common.Result.error(ex.getMessage());
        }
    }
}
