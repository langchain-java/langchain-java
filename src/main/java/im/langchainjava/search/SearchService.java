package im.langchainjava.search;

import java.util.List;

import lombok.Data;

public interface SearchService {
    public List<SearchResultItem> search(String query, int index, int size);

    @Data
    public static class SearchResultLabel{
        String name;
        String displayName;
        String content;
    }

    @Data
    public static class SearchResultItem{
        String title;
        String link;
        String snippet;
        List<SearchResultLabel> labels;
    }

}
