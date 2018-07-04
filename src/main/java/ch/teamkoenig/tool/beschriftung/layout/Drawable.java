package ch.teamkoenig.tool.beschriftung.layout;

import java.io.IOException;

import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

public interface Drawable {

	void draw(PdfCanvas canvas) throws IOException;

	float getHeight();

	float getWidth();

}