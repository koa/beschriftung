package ch.teamkoenig.tool.beschriftung.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class TeamData {
  private String teamName;
  private String teamCode;
  private List<HorseData> horses;
  private boolean active;
  private int bigWagonNumberCount;
  private int smallWagonNumberCount;
}
