package com.forest.organization.certification.service;

import com.forest.starter.ocr.OcrClient;
import com.forest.starter.ocr.OcrRequest;
import com.forest.starter.ocr.OcrResult;
import org.springframework.stereotype.Service;

/**
 * 默认营业执照 OCR 业务适配实现。
 */
@Service
public class DefaultBusinessLicenseOcrService implements BusinessLicenseOcrService {
    private static final String SCENE = "business-license";

    private final OcrClient ocrClient;

    public DefaultBusinessLicenseOcrService(OcrClient ocrClient) {
        this.ocrClient = ocrClient;
    }

    @Override
    public BusinessLicenseOcrResult recognize(String fileName, String contentType, byte[] content) {
        OcrResult result = ocrClient.recognize(new OcrRequest(fileName, contentType, content, SCENE));
        return new BusinessLicenseOcrResult(
            result.status(),
            null,
            null,
            null,
            result.text()
        );
    }
}
