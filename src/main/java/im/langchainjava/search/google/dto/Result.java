package im.langchainjava.search.google.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    String title;
    String htmlTitle;
    String link;
    String displayLink;
    String snippet;
    String htmlSnippet;
    String cacheId;
    String formattedUrl;
    String htmlFormattedUrl;
    String mime;
    String fileFormat;
    List<Label> labels;
}
