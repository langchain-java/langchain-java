package im.langchainjava.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ToolDependency {
    Tool dependency;
    // String extraction1;
    // Map<String, String> extractions;
}
