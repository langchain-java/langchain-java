package im.langchainjava.search.google.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Label {
    String name;
    String displayName;
    @JsonProperty("label_with_op")
    String labelWithOp;
}
