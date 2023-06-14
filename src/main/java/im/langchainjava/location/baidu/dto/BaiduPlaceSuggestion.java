package im.langchainjava.location.baidu.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaiduPlaceSuggestion {
    Integer status;
    String message;
    List<BaiduPlaceResult> result;
}
