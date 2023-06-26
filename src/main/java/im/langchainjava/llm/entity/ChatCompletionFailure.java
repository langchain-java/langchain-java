package im.langchainjava.llm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionFailure{
    String message;
    String type;
    String param;
    String code;
}