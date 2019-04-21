package ch.teamkoenig.tool.beschriftung.layout;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.IOException;

public class WagonNumberLayout implements Drawable {
  private static final int FONT_SIZE = 300;
  private static float MM = (float) (72 / 25.4);
  private static final float WIDTH = 130 * MM;
  private static final float HEIGHT = 100 * MM;
  private static final float FULL_HEIGHT = 180 * MM;
  private static final float MAX_FONT_WIDTH = 110 * MM;
  private final String number;

  private final boolean extraHeight;

  public WagonNumberLayout(final String number, final boolean extraHeight) {
    this.number = number;
    this.extraHeight = extraHeight;
  }

  @Override
  public void draw(final PdfCanvas canvas) throws IOException {
    final float rectangleHeight = extraHeight ? FULL_HEIGHT : HEIGHT;
    canvas.setLineWidth(0.2f);
    canvas.moveTo(0, 0);
    canvas.lineTo(WIDTH, 0);
    canvas.lineTo(WIDTH, rectangleHeight);
    canvas.lineTo(0, rectangleHeight);
    canvas.lineTo(0, 0);
    canvas.stroke();
    final PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
    for (int fontSize = FONT_SIZE; fontSize > 10; fontSize--) {
      final float numberWidth = font.getWidth(number, fontSize);
      if (numberWidth > MAX_FONT_WIDTH) continue;
      final int numberAscent = font.getAscent(number, fontSize);
      final int numberDescent = -font.getDescent(number, fontSize);
      final float numberHeight = numberAscent + numberDescent;
      canvas.beginText();
      canvas.moveText(WIDTH / 2 - numberWidth / 2, HEIGHT / 2 - numberHeight / 2 + numberDescent);
      canvas.setFontAndSize(font, fontSize);
      canvas.showText(number);
      canvas.endText();
      break;
    }
  }

  @Override
  public float getHeight() {
    return extraHeight ? FULL_HEIGHT : HEIGHT;
  }

  @Override
  public float getWidth() {
    return WIDTH;
  }
}
