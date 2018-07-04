package ch.teamkoenig.tool.beschriftung.layout;

import java.io.IOException;
import java.util.List;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

public class HarnessNumberLayout implements Drawable {
	private static float MM = (float) (72 / 25.4);

	private final String number;
	private final String name;
	private final String nameDetail;

	public HarnessNumberLayout(final String number, final String name, final String nameDetail) {
		this.number = number;
		this.name = name;
		this.nameDetail = nameDetail;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.teamkoenig.tool.beschriftung.layout.Drawable#draw(com.itextpdf.kernel.pdf.
	 * canvas.PdfCanvas)
	 */
	@Override
	public void draw(final PdfCanvas canvas) throws IOException {
		final float x = 10 * MM;
		final float y = 0 * MM;
		final float width = 70 * MM;
		final float height = 60 * MM;

		final int numberFontSize = 96;
		final int nameSize = 18;
		canvas.setLineWidth(0.2f);

		canvas.moveTo(x, y);
		canvas.lineTo(x + width, y);
		canvas.curveTo(x + width + height / 3, y + height / 2, x + width, y + height);
		canvas.lineTo(x, y + height);
		canvas.curveTo(x - height / 3, y + height / 2, x, y);
		canvas.stroke();
		final PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
		final VerticalTextDistribution textDistribution = new VerticalTextDistribution(height);

		final String allName = name + nameDetail;

		textDistribution.appendLine(font.getAscent(number, numberFontSize), -font.getDescent(number, numberFontSize));
		textDistribution.appenMultiLine(font.getAscent(allName, nameSize), -font.getDescent(allName, nameSize), 2);
		final List<Float> textVerticalpos = textDistribution.calcVerticalPos();

		canvas.saveState();
		canvas.beginText();
		canvas.setFontAndSize(font, numberFontSize);
		canvas.moveText(x + width / 2 - font.getWidth(number, numberFontSize) / 2, y + textVerticalpos.get(0));
		canvas.showText(number);
		canvas.endText();
		canvas.restoreState();
		canvas.setFontAndSize(font, nameSize);
		canvas.saveState();
		canvas.beginText();
		canvas.moveText(x + width / 2 - font.getWidth(name, nameSize) / 2, y + textVerticalpos.get(1));
		canvas.showText(name);
		canvas.endText();
		canvas.restoreState();
		canvas.saveState();
		canvas.beginText();
		canvas.moveText(x + width / 2 - font.getWidth(nameDetail, nameSize) / 2, y + textVerticalpos.get(2));
		canvas.showText(nameDetail);
		canvas.endText();
		canvas.restoreState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.teamkoenig.tool.beschriftung.layout.Drawable#getHeight()
	 */
	@Override
	public float getHeight() {
		return 60 * MM;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.teamkoenig.tool.beschriftung.layout.Drawable#getWidth()
	 */
	@Override
	public float getWidth() {
		return 90 * MM;
	}
}
