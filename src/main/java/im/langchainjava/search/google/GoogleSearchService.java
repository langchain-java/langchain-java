package im.langchainjava.search.google;

import java.util.ArrayList;
import java.util.List;

import im.langchainjava.search.SearchService;
import im.langchainjava.search.google.dto.Label;
import im.langchainjava.search.google.dto.Result;
import im.langchainjava.search.google.dto.Search;

public class GoogleSearchService implements SearchService{

    GoogleSearchConnector connector;

    String key;
    String engineId;

    public GoogleSearchService(String key, String engineId){
        this.connector = new GoogleSearchConnector();
        this.key = key;
        this.engineId = engineId;
    }

    @Override
    public List<SearchResultItem> search(String query, int index, int size) {
        List<SearchResultItem> items = new ArrayList<>();
        Search search = this.connector.search(key, engineId, query, size);
        if(search == null || search.getItems() == null){
            return items;
        }

        for(Result r : search.getItems()){
            SearchResultItem item = new SearchResultItem();
            item.setLink(r.getLink());
            item.setSnippet(r.getSnippet());
            item.setTitle(r.getTitle());
            if(r.getLabels() != null){
                List<SearchResultLabel> labels = new ArrayList<>();
                for(Label l : r.getLabels()){
                    SearchResultLabel label = new SearchResultLabel();
                    label.setName(l.getName());
                    label.setDisplayName(l.getDisplayName());
                    label.setContent(l.getLabelWithOp());
                    labels.add(label);
                }
                item.setLabels(labels);
            }
            items.add(item);
        }
        return items;
    }

}
