package im.langchainjava.llm.entity.function;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FunctionParameter {
    String type;
    Map<String, FunctionProperty> properties;
    List<String> required;
}
