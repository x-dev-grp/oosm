package com.xdev.ooms.documents.layout;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;

/**
 * Centers a logo inside a rectangular area defined from the top of the page (mm).
 */
public final class PdfLogoPlacement {

    private static final float PAGE_HEIGHT_PT = PageSize.A4.getHeight();

    private PdfLogoPlacement() {
    }

    public static float mmToPt(float mm) {
        return mm * 72f / 25.4f;
    }

    public static float yFromTopMm(float yMmFromTop) {
        return PAGE_HEIGHT_PT - mmToPt(yMmFromTop);
    }

    /**
     * @param xMm      box left edge from page left (mm)
     * @param yMm      box top edge from page top (mm)
     * @param widthMm  box width (mm)
     * @param heightMm box height (mm)
     * @param paddingMm inner padding on each side (mm)
     */
    public static void drawCenteredInBox(
            PdfContentByte canvas,
            Image logo,
            float xMm,
            float yMm,
            float widthMm,
            float heightMm,
            float paddingMm) throws DocumentException {
        float pad = Math.max(0f, paddingMm);
        float innerW = widthMm - 2f * pad;
        float innerH = heightMm - 2f * pad;
        if (innerW <= 0f || innerH <= 0f) {
            return;
        }

        logo.scaleToFit(mmToPt(innerW), mmToPt(innerH));

        float boxLeftPt = mmToPt(xMm);
        float boxBottomPt = yFromTopMm(yMm + heightMm);
        float boxWidthPt = mmToPt(widthMm);
        float boxHeightPt = mmToPt(heightMm);

        float imageX = boxLeftPt + (boxWidthPt - logo.getScaledWidth()) / 2f;
        float imageY = boxBottomPt + (boxHeightPt - logo.getScaledHeight()) / 2f;
        logo.setAbsolutePosition(imageX, imageY);
        canvas.addImage(logo);
    }
}
