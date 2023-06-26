package im.langchainjava.tool.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.search.SearchService;
import im.langchainjava.search.SearchService.SearchResultItem;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.utils.JsonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SimpleSearchGoogleTool extends BasicTool{

    public static String PARAM_QUERY = "query";

    public static int NUM_RESULT = 5;

    ImService wechat;

    SearchService searchService;

    LlmService llm;

    int number;

    public SimpleSearchGoogleTool(ChatMemoryProvider memory, ImService wechat, SearchService searchService, LlmService llm){
        super(memory);
        this.wechat = wechat;
        this.searchService = searchService;
        this.llm = llm;
        this.number = NUM_RESULT;
    }

    public SimpleSearchGoogleTool numberOfResults(int num){
        this.number = num;
        return this;
    }

    @Override
    public String getName() {
        return "search_web";
    }

    @Override
    public String getDescription() {
        return "Only use this function when you need to use the search engine to search the web.";
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        FunctionProperty query = FunctionProperty.builder()
                .description("The query string to the web search engine.")
                .build();
        Map<String, FunctionProperty> properties = new HashMap<>();
        properties.put(PARAM_QUERY, query);
        return properties;
    }

    @Override
    public List<String> getRequiredProperties() {
        List<String> required = new ArrayList<>();
        required.add(PARAM_QUERY);
        return required;
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call) {
        String query = call.getParsedArguments().get(PARAM_QUERY);
        try{
            List<SearchResultItem> results = searchService.search(query, 1, number); 
            String resp = "Could not find any result.";
            if(results == null || results.isEmpty()){
                return onEmptyResult(user);
            }
            List<SearchOutput> outs = new ArrayList<>();
            for(SearchResultItem item : results){
                SearchOutput output = new SearchOutput();
                output.setTitle(item.getTitle());
                output.setSnippet(item.getSnippet());
                output.setLink(item.getLink());
                outs.add(output);
            }
            resp = JsonUtils.fromList(outs);
            return onResult(user, resp);
        }catch(Exception e){
            e.printStackTrace();
            return onToolError(user);
        }
    }

    @Data
    @NoArgsConstructor
    public static class SearchOutput{
        String title;
        String snippet;
        String link;
    }

}
