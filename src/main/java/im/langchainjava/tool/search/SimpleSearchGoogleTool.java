package im.langchainjava.tool.search;

import static im.langchainjava.memory.BasicChatMemory.ROLE_SYSTEM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmErrorHandler;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.ChatMessage;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.search.SearchService;
import im.langchainjava.search.SearchService.SearchResultItem;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleSearchGoogleTool extends BasicTool{

    public static String PARAM_QUERY = "query";
    public static int MAX_LINK_LENGTH = 100;

    public static int NUM_RESULT = 5;

    ImService wechat;

    SearchService searchService;

    LlmService llm;

    int number;

    public SimpleSearchGoogleTool(ImService wechat, SearchService searchService, LlmService llm){
        // super(memory);
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
        return "Always use this function when you need to use the search engine to search information with a online web search engine.\r\n"
                +"Always use this function when you need to search for attractions and recommandations of places to visit.";
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
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        String query = call.getParsedArguments().get(PARAM_QUERY).asText();
        wechat.sendMessageToUser(user, "[搜索引擎]\n正在搜索：" + query); 
        // String resp = "Could not find any result from the search engine.";
        try{

            List<SearchResultItem> results = searchService.search(query, 1, number); 

            if(results == null || results.isEmpty()){
                wechat.sendMessageToUser(user, "[搜索引擎]" + query + "\n" + "没有找到任何结果。"); 
                return onEmptyResult(user);
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
                wechat.sendMessageToUser(user, "[搜索引擎]" + query + "\n" + "没有找到任何结果。"); 
                return onEmptyResult(user);
            }

            wechat.sendMessageToUser(user, "[搜索引擎]" + query + "\n" + "已经找到" + outs.size()+"个结果，正在整理结果。"); 

            // resp = JsonUtils.fromList(outs);
            // return onResult(user, resp);
            
            ChatMessage response = null;
            GoogleSearchLlmErrorHandler h = new GoogleSearchLlmErrorHandler(outs, false);

            while(true){
                response = this.llm.chatCompletion(user, getPrompt(query, h.getOuts()), null, null, h);
                if(response != null || !h.isShouldRetry()){
                    break;
                }
            }

            if(response != null && response.getContent() != null){
                wechat.sendMessageToUser(user, "[搜索引擎]" + query + "\n" + response.getContent());
                return onResult(user, response.getContent());
            }
            return onToolError(user);
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

    private List<ChatMessage> getPrompt(String query, List<SearchOutput> outs){
            String resp = JsonUtils.fromList(outs);
           
            String prompt = new StringBuilder()
                .append("You are an search engine output reviewer. Your task is to run the following steps with the provided search engine outputs:\r\n")
                .append("1. Select at most 3 useful results from the search engine outputs to answer the search query.\r\n")
                .append("2. Summarize the selected results with a short scentence.\r\n")
                .append("Search Query:\"\"\"\r\n")
                .append(query)
                .append("\"\"\"\r\n")
                .append("Search Engine Output:\"\"\"\r\n")
                .append(resp)
                .append("\"\"\"\r\n")
                .append("Your response should be in the following format:\"\"\"\r\n")
                .append("<one-scentence summary of the selected search results>\r\n")
                .append("1. <summary of selected result 1 in Chinese>:<link of selected result 1>\r\n")
                .append("2. <summary of selected result 2 in Chinese>:<link of selected result 2>\r\n")
                .append("3. <summary of selected result 3 in Chinese>:<link of selected result 3>\"\"\"\r\n")
                .toString();
            // log.info(prompt);
            ChatMessage msg = new ChatMessage(ROLE_SYSTEM, prompt);
            List<ChatMessage> msgs = new ArrayList<>();
            msgs.add(msg);
            return msgs;
    }

    @Data
    @AllArgsConstructor
    public static class GoogleSearchLlmErrorHandler implements LlmErrorHandler {
        List<SearchOutput> outs;
        boolean shouldRetry = false;
        @Override
        public void onAiException(String user, Exception e) {
            shouldRetry = false;
        }

        @Override
        public void onMaxTokenExceeded(String user) {
            this.shouldRetry = false;
            if(outs.size() >= 4){
                log.info("Shrinking search engine output list.");
                shouldRetry = true;
                outs = outs.subList(0, outs.size()/2);
            }
        }
    }


}
