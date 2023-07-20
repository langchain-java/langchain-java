// package im.langchainjava.tool.vectorstore;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import im.langchainjava.im.ImService;
// import im.langchainjava.llm.entity.function.FunctionCall;
// import im.langchainjava.llm.entity.function.FunctionProperty;
// import im.langchainjava.memory.ChatMemoryProvider;
// import im.langchainjava.tool.BasicTool;
// import im.langchainjava.vectorstore.Document;
// import im.langchainjava.vectorstore.VectorStore;

// public class VectorStoreTool extends BasicTool{

//     private static String PARAM_QUERY = "query";

//     String name;

//     VectorStore vectorStore;

//     ImService imService;

//     public static int MAX_LENGTH = 1536;

//     public VectorStoreTool(ChatMemoryProvider memory, String name, VectorStore store, ImService im){
//         super(memory);
//         this.name = name;
//         this.vectorStore = store;
//         this.imService = im;
//     }

//     @Override
//     public String getName() {
//         return name;
//     }

//     @Override
//     public String getDescription() {
//         return ""
//             + "Only use " + name + " function if user's intented place is in " + name + ". "
//             + " Never assume user's departure or arrival location is in " + name + ".";   
//     }

//     @Override
//     public Map<String, FunctionProperty> getProperties() {
//         FunctionProperty query = FunctionProperty.builder()
//                 .description("The query string to the vector store.")
//                 .build();
//         Map<String, FunctionProperty> properties = new HashMap<>();
//         properties.put(PARAM_QUERY, query);
//         return properties;
//     }

//     @Override
//     public List<String> getRequiredProperties() {
//         List<String> required = new ArrayList<>();
//         required.add(PARAM_QUERY);
//         return required;
//     }

//     @Override
//     public ToolOut doInvoke(String user, FunctionCall call) {
//         String query = call.getParsedArguments().get(PARAM_QUERY).asText();
//         List<Document> docs = this.vectorStore.similaritySearch(user, query, 5); 
//         if(docs == null || docs.isEmpty()){ 
//             return null;
//         }
//         StringBuilder sb = new StringBuilder();
//         int i = 1;
//         for(Document d : docs){
//             sb.append("\n <" + i + "> ").append(d.getContent().substring(0, Math.min(d.getContent().length(), 800))).append("\n");
//             i++;
//         }
//         String resp = sb.substring(0, Math.min(sb.length(), MAX_LENGTH));
//         return onResult(user, resp);
//     }

    
    
    
// }
