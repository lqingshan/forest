package com.forest.starter.ocr;

/**
 * A recognized text block.
 *
 * @param text recognized text
 * @param confidence block confidence from 0 to 1, if available
 * @param x left coordinate, if available
 * @param y top coordinate, if available
 * @param width block width, if available
 * @param height block height, if available
 */
public record OcrTextBlock(
    String text,
    Double confidence,
    Integer x,
    Integer y,
    Integer width,
    Integer height
) {
}
