package com.forest.starter.ocr.disabled;

import com.forest.starter.ocr.OcrClient;
import com.forest.starter.ocr.OcrRequest;
import com.forest.starter.ocr.OcrResult;

/**
 * Default OCR implementation used before a real provider is configured.
 */
public class DisabledOcrClient implements OcrClient {
    @Override
    public OcrResult recognize(OcrRequest request) {
        return OcrResult.disabled();
    }
}
