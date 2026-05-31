package com.forest.organization.certification.service;

import com.forest.starter.ocr.OcrStatus;

/**
 * 定义营业执照 OCR 业务入口。
 *
 * <p>OCR 技术能力由 starter-ocr 提供，本接口只负责组织域里的营业执照识别语义。</p>
 */
public interface BusinessLicenseOcrService {
    BusinessLicenseOcrResult recognize(String fileName, String contentType, byte[] content);

    record BusinessLicenseOcrResult(
        OcrStatus status,
        String companyName,
        String unifiedSocialCreditCode,
        String legalRepresentativeName,
        String rawText
    ) {
    }
}
