package ch.teamkoenig.tool.beschriftung.layout;

import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public class Layouter {
  public static void layout(
      final Collection<Drawable> things,
      final OutputStream target,
      final PageSize pageSize,
      final float margin)
      throws IOException {

    final Collection<Drawable> layouts = new ArrayList<>(things);
    @Cleanup
    final PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new BufferedOutputStream(target)));
    pdfDoc.setDefaultPageSize(pageSize);
    final Rectangle printShape =
        pageSize.clone().applyMargins(margin, margin, margin, margin, false);
    float currentX = 0;
    float currentY = 0;
    float currentRowHeight = 0;
    PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage());

    while (!layouts.isEmpty()) {
      final float heightLimit = printShape.getHeight() - currentY;
      final float widthLimit = printShape.getWidth() - currentX;

      final Drawable nextLayout = takeNextLayout(layouts, heightLimit, widthLimit);
      if (nextLayout == null) {
        if (currentRowHeight > 0) {
          currentY += currentRowHeight;
          currentX = 0;
          currentRowHeight = 0;
        } else {
          if (currentX == 0 && currentY == 0) {
            log.info("Cannot place remaining layouts: " + layouts);
            break;
          }
          canvas = new PdfCanvas(pdfDoc.addNewPage());
          currentX = 0;
          currentY = 0;
          currentRowHeight = 0;
        }
      } else {
        if (currentRowHeight < nextLayout.getHeight()) currentRowHeight = nextLayout.getHeight();
        canvas.saveState();
        canvas.concatMatrix(
            AffineTransform.getTranslateInstance(
                printShape.getX() + currentX, printShape.getY() + currentY));
        nextLayout.draw(canvas);
        currentX += nextLayout.getWidth();
        canvas.restoreState();
      }
    }
  }

  private static Drawable takeNextLayout(
      final Collection<Drawable> layouts, final float heightLimit, final float widthLimit) {
    float currentMaxHeight = 0;
    Drawable currentDrawable = null;
    for (final Drawable drawable : layouts) {
      if (drawable.getHeight() > heightLimit) continue;
      if (drawable.getWidth() > widthLimit) continue;
      if (drawable.getHeight() > currentMaxHeight) {
        currentDrawable = drawable;
        currentMaxHeight = drawable.getHeight();
      }
    }
    if (currentDrawable != null) layouts.remove(currentDrawable);
    return currentDrawable;
  }
}
