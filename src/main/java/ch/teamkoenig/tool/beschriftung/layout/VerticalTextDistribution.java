package ch.teamkoenig.tool.beschriftung.layout;

import java.util.ArrayList;
import java.util.List;

import lombok.Value;

public class VerticalTextDistribution {
	@Value
	private static class LineData {
		float ascending;
		float descending;
		float spacing;
		int lineCount;
	}

	private final float totalHeight;
	private final List<LineData> lines = new ArrayList<>();

	public VerticalTextDistribution(final float totalHeight) {
		this.totalHeight = totalHeight;
	}

	public void appendLine(final float ascending, final float descending) {
		lines.add(new LineData(ascending, descending, 0, 1));
	}

	public void appenMultiLine(final float ascending, final float descending, final int count) {
		appenMultiLine(ascending, descending, count, (ascending + descending) * 1.5f);
	}

	public void appenMultiLine(final float ascending, final float descending, final int count, final float spacing) {
		lines.add(new LineData(ascending, descending, spacing, count));
	}

	public List<Float> calcVerticalPos() {
		float totalTextHeight = 0;
		for (final LineData lineData : lines) {
			totalTextHeight += lineData.getAscending() + lineData.getDescending()
					+ lineData.getSpacing() * (lineData.getLineCount() - 1);
		}
		final float spacing = (totalHeight - totalTextHeight) / (lines.size() + 1);
		final List<Float> pos = new ArrayList<>();
		float currentPos = totalHeight;
		for (final LineData lineData : lines) {
			currentPos -= spacing + lineData.getAscending();
			pos.add(currentPos);
			for (int i = lineData.getLineCount(); i > 1; i--) {
				currentPos -= lineData.getSpacing();
				pos.add(currentPos);
			}
			currentPos -= lineData.getDescending();
		}
		return pos;
	}

}
