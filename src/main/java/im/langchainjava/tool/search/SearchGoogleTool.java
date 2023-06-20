package im.langchainjava.tool.search;

import java.util.List;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.search.SearchService;
import im.langchainjava.search.SearchService.SearchResultItem;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.utils.StringUtil;

public class SearchGoogleTool extends BasicTool{

    public static int NUM_RESULT = 5;

    ImService wechat;

    SearchService searchService;

    LlmService llm;

    int number;

    public SearchGoogleTool(ChatMemoryProvider memory, ImService wechat, SearchService searchService, LlmService llm){
        super(memory);
        this.wechat = wechat;
        this.searchService = searchService;
        this.llm = llm;
        this.number = NUM_RESULT;
    }

    public SearchGoogleTool numberOfResults(int num){
        this.number = num;
        return this;
    }

    @Override
    public String getToolName() {
        return "search_web";
    }

    @Override
    public String getDescription() {
        return "only use this tool when you need to use the search engine to search the web.";
    }

    @Override
    public String getInputFormat() {
        return "`Action Input` is the query string to the web search engine.";
    }

    @Override
    public ToolOut invoke(String user, Action<?> action) {
        String query = String.valueOf(action.getInput()).replaceAll("\"","");
        try{
            List<SearchResultItem> results = searchService.search(query, 1, number); 
            String resp = "Could not find any result.";
            if(results == null || results.isEmpty()){
                return onEmptyResult(user);
            }
            resp = "The following results are found: ";
            for(SearchResultItem item : results){
    
                resp = resp + item.getTitle() + " " ;
                if(!StringUtil.isNullOrEmpty(item.getSnippet())){
                    resp = resp + ", snippet: " + item.getSnippet();
                }
                if(!StringUtil.isNullOrEmpty(item.getLink())){
                    resp = resp + ", link:" + item.getLink();
                }
             
                resp = resp + "\n";
            }
            return onResult(user, resp);
        }catch(Exception e){
            e.printStackTrace();
            return onToolError(user);
        }
    }

}
