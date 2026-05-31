package com.forest.starter.ocr;

/**
 * Generic OCR request.
 *
 * @param fileName original or temporary file name, used for diagnostics only
 * @param contentType MIME type of the input content
 * @param content binary content to recognize
 * @param scene optional caller scene, for example business-license
 */
public record OcrRequest(
    String fileName,
    String contentType,
    byte[] content,
    String scene
) {
}
