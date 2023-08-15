package im.langchainjava.tool.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.search.SearchService;
import im.langchainjava.search.SearchService.SearchResultItem;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.ToolOuts;
import im.langchainjava.tool.askuser.form.FormBuilders;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleSearchGoogleTool extends Tool{

    public static String PARAM_QUERY = "query";
    public static String PARAM_DESC = "the query string to the web search engine";

    public static String EXTRACTION_NAME = "web_search_engine_results";
    public static String EXTRACTION = "the top 1 result that most relevant with the query";

    public static int MAX_LINK_LENGTH = 100;

    public static int NUM_RESULT = 5;

    ImService im;

    SearchService searchService;

    LlmService llm;

    int number;

    public SimpleSearchGoogleTool(ImService im, SearchService searchService, LlmService llm){
        this.im = im;
        this.searchService = searchService;
        this.llm = llm;
        this.number = NUM_RESULT;

        dependencyAndProperty(im, FormBuilders.textForm(llm, PARAM_QUERY, PARAM_DESC));
        extractionName(EXTRACTION_NAME);
        extraction(EXTRACTION);
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
        return "Always use this function when you need to use the search engine to search information with a online web search engine.\r\n"
                +"Always use this function when you need to search for attractions and recommandations of places to visit.";
    }

    // @Override
    // public Map<String, FunctionProperty> getProperties() {
    //     FunctionProperty query = FunctionProperty.builder()
    //             .description("The query string to the web search engine.")
    //             .build();
    //     Map<String, FunctionProperty> properties = new HashMap<>();
    //     properties.put(PARAM_QUERY, query);
    //     return properties;
    // }

    // @Override
    // public List<String> getRequiredProperties() {
    //     List<String> required = new ArrayList<>();
    //     required.add(PARAM_QUERY);
    //     return required;
    // }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        String query = call.getParsedArguments().get(PARAM_QUERY).asText();
        try{

            List<SearchResultItem> results = searchService.search(query, 1, number); 

            if(results == null || results.isEmpty()){
                String msg = "[搜索引擎]" + query + "\n" + "没有找到任何结果。";
                im.sendMessageToUser(user, msg); 
                return ToolOuts.onEmptyResult(user, msg);
            }
            List<SearchOutput> outs = new ArrayList<>();
            for(SearchResultItem item : results){
                if(item.getLink() != null && item.getLink().length() >= MAX_LINK_LENGTH){
                    continue;
                } 
                SearchOutput output = new SearchOutput();
                output.setTitle(item.getTitle());
                output.setSnippet(item.getSnippet());
                output.setLink(item.getLink());
                outs.add(output);
            }

            if(outs.isEmpty()){
                String msg = "[搜索引擎]" + query + "\n" + "没有找到任何结果。";
                im.sendMessageToUser(user, msg); 
                return ToolOuts.onEmptyResult(user, msg);
            }

            im.sendMessageToUser(user, "[搜索引擎]" + query + "\n" + "已经找到" + outs.size()+"个结果，正在整理结果。\n" + formatResults(outs)); 
            return ToolOuts.onResult(user, formatResults(outs));

            // resp = JsonUtils.fromList(outs);
            // return onResult(user, resp);
            
            // ChatMessage response = null;
            // GoogleSearchLlmErrorHandler h = new GoogleSearchLlmErrorHandler(outs, false);

            // while(true){
            //     response = this.llm.chatCompletion(user, getPrompt(query, h.getOuts()), null, null, h);
            //     if(response != null || !h.isShouldRetry()){
            //         break;
            //     }
            // }

            // if(response != null && response.getContent() != null){
            //     wechat.sendMessageToUser(user, "[搜索引擎]" + query + "\n" + response.getContent());
            //     return onResult(user, response.getContent());
            // }
            // return onToolError(user);
        }catch(Exception e){
            e.printStackTrace();
            return ToolOuts.onToolError(user, "使用搜索引擎遇到故障：" + e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    public static class SearchOutput{
        String title;
        String snippet;
        String link;
    }

    private String formatResults(List<SearchOutput> outs){
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(SearchOutput o : outs){
            sb.append(o.getTitle()).append("|").append(o.getSnippet()).append("|").append(o.getLink()).append("\n");
            if(++i >= NUM_RESULT){
                break;
            }
        }
        return sb.toString();
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        // this method will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public List<String> getRequiredProperties() {
        // this method will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredProperties'");
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        // this method will never be used.
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }

    // private List<ChatMessage> getPrompt(String query, List<SearchOutput> outs){
    //         String resp = JsonUtils.fromList(outs);
           
    //         String prompt = new StringBuilder()
    //             .append("You are an search engine output reviewer. Your task is to run the following steps with the provided search engine outputs:\r\n")
    //             .append("1. Select at most 3 useful results from the search engine outputs to answer the search query.\r\n")
    //             .append("2. Summarize the selected results with a short scentence.\r\n")
    //             .append("Search Query:\"\"\"\r\n")
    //             .append(query)
    //             .append("\"\"\"\r\n")
    //             .append("Search Engine Output:\"\"\"\r\n")
    //             .append(resp)
    //             .append("\"\"\"\r\n")
    //             .append("Your response should be in the following format:\"\"\"\r\n")
    //             .append("<one-scentence summary of the selected search results>\r\n")
    //             .append("1. <summary of selected result 1 in Chinese>:<link of selected result 1>\r\n")
    //             .append("2. <summary of selected result 2 in Chinese>:<link of selected result 2>\r\n")
    //             .append("3. <summary of selected result 3 in Chinese>:<link of selected result 3>\"\"\"\r\n")
    //             .toString();
    //         // log.info(prompt);
    //         ChatMessage msg = new ChatMessage(ROLE_SYSTEM, prompt);
    //         List<ChatMessage> msgs = new ArrayList<>();
    //         msgs.add(msg);
    //         return msgs;
    // }

    // @Data
    // @AllArgsConstructor
    // public static class GoogleSearchLlmErrorHandler implements LlmErrorHandler {
    //     List<SearchOutput> outs;
    //     boolean shouldRetry = false;
    //     @Override
    //     public void onAiException(String user, Exception e) {
    //         shouldRetry = false;
    //     }

    //     @Override
    //     public void onMaxTokenExceeded(String user) {
    //         this.shouldRetry = false;
    //         if(outs.size() >= 4){
    //             log.info("Shrinking search engine output list.");
    //             shouldRetry = true;
    //             outs = outs.subList(0, outs.size()/2);
    //         }
    //     }
    // }


}
