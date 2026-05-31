package com.forest.starter.ocr;

/**
 * Defines generic OCR capability for extracting text from binary input.
 *
 * <p>This starter only understands OCR as a technical capability. Business modules are responsible for
 * interpreting the returned text as a business license, ID card, invoice, contract, or any other document type.</p>
 */
public interface OcrClient {
    OcrResult recognize(OcrRequest request);
}
