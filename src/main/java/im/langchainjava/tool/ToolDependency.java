package im.langchainjava.tool;

import java.util.Map;

import lombok.Data;

@Data
public class ToolDependency {
    Tool dependency;
    Map<String, String> extractions;
}
