package com.forest.starter.ocr;

import java.util.List;

/**
 * Generic OCR recognition result.
 *
 * @param status recognition status
 * @param text merged text, if the implementation can provide it
 * @param textBlocks recognized text blocks
 * @param confidence overall confidence from 0 to 1, if available
 * @param rawResult provider raw result for diagnostics; callers must not rely on its structure
 */
public record OcrResult(
    OcrStatus status,
    String text,
    List<OcrTextBlock> textBlocks,
    Double confidence,
    String rawResult
) {
    public static OcrResult disabled() {
        return new OcrResult(OcrStatus.DISABLED, "", List.of(), null, null);
    }
}
