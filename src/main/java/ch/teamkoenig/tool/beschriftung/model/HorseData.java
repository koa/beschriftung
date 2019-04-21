package ch.teamkoenig.tool.beschriftung.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = HorseData.HorseDataBuilder.class)
public class HorseData {
  private String horseName;
  private String horseDetailText;
  private String horseCode;
  private boolean active;
  private int headNumberCount;
  private int harnessNumberCount;

  @JsonPOJOBuilder(withPrefix = "")
  public static class HorseDataBuilder {}
}
