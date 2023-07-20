// package im.langchainjava.tool.askuser;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import im.langchainjava.im.ImService;
// import im.langchainjava.llm.entity.function.FunctionCall;
// import im.langchainjava.llm.entity.function.FunctionProperty;
// import im.langchainjava.memory.ChatMemoryProvider;
// import im.langchainjava.tool.BasicTool;
// import im.langchainjava.utils.StringUtil;

// public class InformUserTool extends BasicTool{

//     private static String PARAM_MSG = "message";

//     public static String PARAM_EXP = "example";

//     ImService wechat;

//     String defaultMsg;

//     public InformUserTool(ChatMemoryProvider memoryProvider, ImService wechat){
//         super(memoryProvider);
//         this.wechat = wechat;
//         this.defaultMsg = null;
//     }

//     public InformUserTool defaultMessage(String message){
//         this.defaultMsg = message;
//         return this;
//     }

//     @Override
//     public String getName() {
//         return "inform_user";
//     }

//     @Override
//     public String getDescription() {
//         return " never use this function to search for answers to the question. "
//             + " Use this function to reply to the user's message. "
//             + " If the user's intention is greeting, you should greet back. "
//             + " Whatever the user said, you should always reply politly in Chinese. ";
//     }

//     @Override
//     public Map<String, FunctionProperty> getProperties() {
//         FunctionProperty fp = FunctionProperty.builder()
//                 .description("Your message to the user in Chinese, followed by `[提示: some examples of user's next input in Chinese]`. "
//                                 + " Example: things to inform user in Chinese. [提示: examples of user's next input in Chinese]`."
//                                 + " If you have nothing to say, you may say `请问有什么需要我帮助的吗？`")
//                 .build();
//         Map<String, FunctionProperty> properties = new HashMap<>();
//         properties.put(PARAM_MSG, fp);
//         FunctionProperty fp2 = FunctionProperty.builder()
//                 .description("User's top 3 most possible input in Chinese.")
//                 .build();
//         properties.put(PARAM_EXP, fp2);

//         return properties;
//     }

//     @Override
//     public List<String> getRequiredProperties() {
//         List<String> required = new ArrayList<>();
//         required.add(PARAM_MSG);
//         return required;
//     }

//     @Override
//     public ToolOut doInvoke(String user, FunctionCall call) {
//         String message = call.getParsedArguments().get(PARAM_MSG).asText();

//         String prompt = call.getParsedArguments().get(PARAM_EXP).asText();
//         if(!StringUtil.isNullOrEmpty(prompt)){
//             message = message + "\n" + prompt;
//         }
        
//         wechat.sendMessageToUser(user, message);
//         return waitUserInput(user);
//     }

// }
