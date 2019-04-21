package ch.teamkoenig.tool.beschriftung.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = TeamData.TeamDataBuilder.class)
public class TeamData {
  private String teamName;
  private String teamCode;
  private List<HorseData> horses;
  private boolean active;
  private int bigWagonNumberCount;
  private int smallWagonNumberCount;

  @JsonPOJOBuilder(withPrefix = "")
  public static class TeamDataBuilder {}
}
