package im.langchainjava.llm.entity.function;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FunctionCall {
    String name;
    String arguments;
    @JsonIgnore
    Map<String, String> parsedArguments;
}
