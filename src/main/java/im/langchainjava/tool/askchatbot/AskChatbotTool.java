// package im.langchainjava.tool.askchatbot;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import im.langchainjava.im.ImService;
// import im.langchainjava.llm.LlmService;
// import im.langchainjava.llm.entity.ChatMessage;
// import im.langchainjava.llm.entity.function.FunctionCall;
// import im.langchainjava.llm.entity.function.FunctionProperty;
// import im.langchainjava.memory.ChatMemoryProvider;
// import im.langchainjava.tool.BasicTool;
// import im.langchainjava.tool.ToolOut;
// import lombok.Setter;

// public class AskChatbotTool extends BasicTool{

//     private static String PARAM_QUERY = "query";

//     LlmService llm;

//     ImService imService;

//     @Setter
//     String prompt;

//     public AskChatbotTool(ChatMemoryProvider memoryProvider, LlmService llmService, ImService im){
//         super(memoryProvider);
//         this.llm = llmService;
//         this.imService = im;
//         this.prompt = null;
//     }

//     @Override
//     public String getName() {
//         return "search_chatbot"; 
//     }

//     @Override
//     public String getDescription() {
//         return "Only use this function if you could not find good answer from the internet."
//         + " Don't use this function if you can not find user's intention. "
//         + " Don't use this function when you need to ask the user for more information."
//         + " Don't use this function if you find user is insulting.";
//     }
    
//     private static String sys = "Answer the question below and reply in Chinese. If you don't have the answer, you may say `我不知道`."
//         + "Input always starts with `Action Input:`. ";
    
//     private String getPrompt(){
//         if(this.prompt != null){
//             return prompt;
//         }
//         return sys;
//     }
 
//     @Override
//     public ToolOut doInvoke(String user, FunctionCall call) {
//         String input = String.valueOf(call.getParsedArguments().get(PARAM_QUERY));
//         this.imService.sendMessageToUser(user, "[系统]\n正在搜索: " + input);
//         List<ChatMessage> prompt = new ArrayList<>();
//         prompt.add(new ChatMessage("system", getPrompt()));
//         prompt.add(new ChatMessage("user", input));
//         ChatMessage message = this.llm.chatCompletion(user, prompt, null,null, null);
//         String resp = message.getContent();
//         return onResult(user, resp);
//     }

//     @Override
//     public Map<String, FunctionProperty> getProperties() {
//         FunctionProperty fp = FunctionProperty.builder()
//                 .description("A fully formed query to the chatbot.")
//                 .build();
//         Map<String, FunctionProperty> properties = new HashMap<>();
//         properties.put(PARAM_QUERY, fp);
//         return properties;
//     }

//     @Override
//     public List<String> getRequiredProperties() {
//         List<String> required = new ArrayList<>();
//         required.add(PARAM_QUERY);
//         return required;
//     }

// }
