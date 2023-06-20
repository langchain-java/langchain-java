package im.langchainjava.tool.vectorstore;

import java.util.List;

import im.langchainjava.im.ImService;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.BasicTool;
import im.langchainjava.tool.Tool;
import im.langchainjava.vectorstore.Document;
import im.langchainjava.vectorstore.VectorStore;

public class VectorStoreTool extends BasicTool{

    String name;

    VectorStore vectorStore;

    ImService imService;

    public static int MAX_LENGTH = 1536;

    public VectorStoreTool(ChatMemoryProvider memory, String name, VectorStore store, ImService im){
        super(memory);
        this.name = name;
        this.vectorStore = store;
        this.imService = im;
    }


    @Override
    public String getToolName() {
        return name;
    }

    @Override
    public String getDescription() {
        return ""
            + "only use " + name + " tool if user's intented place is in " + name + ". "
            + " Never assume user's departure or arrival location is in " + name + ".";   
    }

    @Override
    public String getInputFormat() {
        return "`Action Input` should be a fully formed English question.";
    }

    @Override
    public ToolOut invoke(String user, Action<?> action) {
        List<Document> docs = this.vectorStore.similaritySearch(user, String.valueOf(action.getInput()), 5); 
        if(docs == null || docs.isEmpty()){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(Document d : docs){
            sb.append("\n <" + i + "> ").append(d.getContent().substring(0, Math.min(d.getContent().length(), 800))).append("\n");
            i++;
        }
        String resp = sb.substring(0, Math.min(sb.length(), MAX_LENGTH));
        return onResult(user, resp);
    }

    
    
    
}
