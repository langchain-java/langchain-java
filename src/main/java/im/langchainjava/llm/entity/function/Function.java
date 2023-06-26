package im.langchainjava.llm.entity.function;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Function {
    String name;
    String description;
    FunctionParameter parameters;
}
