package ch.teamkoenig.tool.beschriftung.layout;

import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.IOException;

public interface Drawable {

  void draw(PdfCanvas canvas) throws IOException;

  float getHeight();

  float getWidth();
}
