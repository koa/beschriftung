package com.itextpdf.samples.sandbox.objects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import ch.teamkoenig.tool.beschriftung.layout.Drawable;
import ch.teamkoenig.tool.beschriftung.layout.HarnessNumberLayout;
import ch.teamkoenig.tool.beschriftung.layout.HeadNumberLayout;
import ch.teamkoenig.tool.beschriftung.layout.WagonNumberLayout;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Grid {
	private static float MM = (float) (72 / 25.4);
	public static final String DEST = "./target/test/resources/sandbox/objects/grid.pdf";

	public static void main(final String[] args) throws IOException {
		final File file = new File(DEST);
		file.getParentFile().mkdirs();
		new Grid().manipulatePdf(DEST);
	}

	private void appendHorse(final List<Drawable> layouts, final String number, final String name,
			final String nameDetail) {
		layouts.add(new HarnessNumberLayout(number, name, nameDetail));
		layouts.add(new HeadNumberLayout(number, name, nameDetail));
		layouts.add(new HeadNumberLayout(number, name, nameDetail));
	}

	public void manipulatePdf(final String dest) throws IOException {
		final PdfDocument pdfDoc = new PdfDocument(new PdfWriter(DEST));

		final PageSize pageSize = PageSize.A4.rotate();
		pdfDoc.setDefaultPageSize(pageSize);

		// canvas.setLineWidth(10);
		// canvas.setColor(ColorConstants.BLACK, false);
		// canvas.saveState();
		// canvas.concatMatrix(AffineTransform.getTranslateInstance(100, 0));
		// canvas.restoreState();
		// canvas.concatMatrix(AffineTransform.getTranslateInstance(0, 100));

		// final float x = 10 * MM;
		// final float y = 10 * MM;
		// final float width = 70 * MM;
		// final float height = 60 * MM;

		final float margin = 10 * MM;
		final Rectangle printShape = pageSize.clone().applyMargins(margin, margin, margin, margin, false);

		final List<Drawable> layouts = new ArrayList<>();
		appendHorse(layouts, "26C", "Aronia CH", "102NJ221x");
		appendHorse(layouts, "26A", "Maerlin CH", "102WC26");
		appendHorse(layouts, "26B", "Nevado X CH", "103ME90");
		layouts.add(new WagonNumberLayout("26", false));
		layouts.add(new WagonNumberLayout("26", true));
		layouts.add(new WagonNumberLayout("26", true));

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
				if (currentRowHeight < nextLayout.getHeight())
					currentRowHeight = nextLayout.getHeight();
				canvas.saveState();
				canvas.concatMatrix(AffineTransform.getTranslateInstance(printShape.getX() + currentX,
						printShape.getY() + currentY));
				nextLayout.draw(canvas);
				currentX += nextLayout.getWidth();
				canvas.restoreState();
			}
		}

		// final HarnessNumberLayout layout1 = new HarnessNumberLayout("26C", "Aronia
		// CH", "102NJ221x");
		// layout1.draw(canvas);
		// canvas.concatMatrix(AffineTransform.getTranslateInstance(layout1.getWidth(),
		// 0));
		// // final HarnessNumberLayout layout2 = new HarnessNumberLayout("26C", "Aronia
		// // CH", "102NJ221g");
		// // layout2.draw(canvas);
		// //
		// canvas.concatMatrix(AffineTransform.getTranslateInstance(layout2.getWidth(),
		// // 0));
		// final HeadNumberLayout layout3 = new HeadNumberLayout("26C", "Aronia CH",
		// "102NJ221");
		// //
		// canvas.concatMatrix(AffineTransform.getTranslateInstance(layout3.getHeight(),
		// // 0));
		// // canvas.concatMatrix(AffineTransform.getRotateInstance(Math.PI / 2));
		// layout3.draw(canvas);
		// canvas.concatMatrix(AffineTransform.getTranslateInstance(layout3.getWidth(),
		// 0));
		//
		// final WagonNumberLayout wagonNumberLayout = new WagonNumberLayout("26",
		// true);
		// wagonNumberLayout.draw(canvas);

		// canvas.moveTo(x, y);
		// canvas.lineTo(x + width, y);
		// canvas.curveTo(x + width + height / 3, y + height / 2, x + width, y +
		// height);
		// canvas.lineTo(x, y + height);
		// canvas.curveTo(x - height / 3, y + height / 2, x, y);
		// canvas.stroke();
		// final PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
		// final VerticalTextDistribution textDistribution = new
		// VerticalTextDistribution(height);
		//
		// textDistribution.appendLine(font.getAscent(number, numberFontSize),
		// font.getDescent(number, numberFontSize));
		// textDistribution.appenMultiLine(font.getAscent(name, nameSize),
		// font.getDescent(name, nameSize), 2);
		// final List<Float> textVerticalpos = textDistribution.calcVerticalPos();
		//
		// canvas.setFontAndSize(font, numberFontSize);
		// canvas.moveTo(x + width / 2 - font.getWidth(number, numberFontSize) / 2, y +
		// textVerticalpos.get(0));
		// canvas.showText(number);
		// canvas.setFontAndSize(font, nameSize);
		// canvas.moveTo(x + width / 2 - font.getWidth(name, nameSize) / 2, y +
		// textVerticalpos.get(1));
		// canvas.showText(name);
		// canvas.moveTo(x + width / 2 - font.getWidth(nameDetail, nameSize) / 2, y +
		// textVerticalpos.get(2));
		// canvas.showText(nameDetail);

		// for (float x = 0; x < pageSize.getWidth();) {
		// for (float y = 0; y < pageSize.getHeight();) {
		// canvas.circle(x, y, 1f);
		// y += 72f;
		// }
		// x += 72f;
		// }
		// canvas.fill();

		pdfDoc.close();
	}

	private Drawable takeNextLayout(final List<Drawable> layouts, final float heightLimit, final float widthLimit) {
		float currentMaxHeight = 0;
		Drawable currentDrawable = null;
		for (final Drawable drawable : layouts) {
			if (drawable.getHeight() > heightLimit)
				continue;
			if (drawable.getWidth() > widthLimit)
				continue;
			if (drawable.getHeight() > currentMaxHeight) {
				currentDrawable = drawable;
				currentMaxHeight = drawable.getHeight();
			}
		}
		if (currentDrawable != null)
			layouts.remove(currentDrawable);
		return currentDrawable;
	}
}
