package im.langchainjava.review.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Period {
    WeekdayTime open;
    WeekdayTime close;
}
