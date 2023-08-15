package im.langchainjava.tool;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ToolDependency {
    Tool dependency;
    String extraction;
    // Map<String, String> extractions;
}
