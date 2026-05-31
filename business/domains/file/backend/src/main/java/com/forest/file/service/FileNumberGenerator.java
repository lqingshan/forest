package com.forest.file.service;

import com.forest.starter.time.ForestTime;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * 生成文件和上传会话的业务编号。
 */
@Component
public class FileNumberGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String nextFileNo() {
        return next("FILE");
    }

    public String nextUploadSessionNo() {
        return next("FUS");
    }

    private String next(String prefix) {
        String date = ForestTime.now().format(DATE_FORMAT);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
        return prefix + date + random;
    }
}
