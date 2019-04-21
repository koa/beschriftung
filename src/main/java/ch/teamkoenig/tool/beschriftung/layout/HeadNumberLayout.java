package ch.teamkoenig.tool.beschriftung.layout;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.IOException;
import java.util.List;

public class HeadNumberLayout implements Drawable {
  private static float MM = (float) (72 / 25.4);
  private static final float width = 60 * MM;
  private static final float height = 40 * MM;
  private final String number;

  private final String name;
  private final String nameDetail;

  public HeadNumberLayout(final String number, final String name, final String nameDetail) {
    this.number = number;
    this.name = name;
    this.nameDetail = nameDetail;
  }

  @Override
  public void draw(final PdfCanvas canvas) throws IOException {

    final int numberFontSize = 75;
    final int nameSize = 18;
    canvas.setLineWidth(0.2f);

    canvas.saveState();
    canvas.concatMatrix(AffineTransform.getTranslateInstance(height, 0));
    canvas.concatMatrix(AffineTransform.getRotateInstance(Math.PI / 2));

    canvas.moveTo(0, 0);
    canvas.lineTo(width, 0);
    canvas.lineTo(width, height);
    canvas.lineTo(0, height);
    canvas.lineTo(0, 0);
    canvas.stroke();
    final PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
    final VerticalTextDistribution textDistribution = new VerticalTextDistribution(height);

    final String allName = name + nameDetail;

    textDistribution.appendLine(
        font.getAscent(number, numberFontSize), -font.getDescent(number, numberFontSize));
    textDistribution.appenMultiLine(
        font.getAscent(allName, nameSize), -font.getDescent(allName, nameSize), 2);
    final List<Float> textVerticalpos = textDistribution.calcVerticalPos();

    canvas.saveState();
    canvas.beginText();
    canvas.setFontAndSize(font, numberFontSize);
    canvas.moveText(width / 2 - font.getWidth(number, numberFontSize) / 2, textVerticalpos.get(0));
    canvas.showText(number);
    canvas.endText();
    canvas.restoreState();
    canvas.setFontAndSize(font, nameSize);
    canvas.saveState();
    canvas.beginText();
    canvas.moveText(width / 2 - font.getWidth(name, nameSize) / 2, textVerticalpos.get(1));
    canvas.showText(name);
    canvas.endText();
    canvas.restoreState();
    canvas.saveState();
    canvas.beginText();
    canvas.moveText(width / 2 - font.getWidth(nameDetail, nameSize) / 2, textVerticalpos.get(2));
    canvas.showText(nameDetail);
    canvas.endText();
    canvas.restoreState();

    canvas.restoreState();
  }

  @Override
  public float getHeight() {
    return width;
  }

  @Override
  public float getWidth() {
    return height;
  }
}
