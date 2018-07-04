package ch.teamkoenig.tool.beschriftung.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class HorseData {
	private String horseName;
	private String horseDetailText;
	private String horseCode;
	private boolean active;
	private int headNumberCount;
	private int harnessNumberCount;
}
