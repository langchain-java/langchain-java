package im.langchainjava.llm.entity.function;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FunctionProperty {
    String type;

    @JsonProperty("enum")
    List<String> enumerate;
    
    String description; 

    // @JsonIgnore
    // String tag;
}
