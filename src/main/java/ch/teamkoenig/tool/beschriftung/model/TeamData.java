package ch.teamkoenig.tool.beschriftung.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class TeamData {
	private String teamName;
	private String teamCode;
	private List<HorseData> horses;
}
