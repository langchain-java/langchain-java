package im.langchainjava.tool.vectorstore;

import java.util.List;

import im.langchainjava.im.ImService;
import im.langchainjava.parser.Action;
import im.langchainjava.tool.Tool;
import im.langchainjava.vectorstore.Document;
import im.langchainjava.vectorstore.VectorStore;

public class VectorStoreTool implements Tool{

    String name;

    VectorStore vectorStore;

    ImService imService;

    String desc;

    public VectorStoreTool(String name, VectorStore store, ImService im){
        this.name = name;
        this.vectorStore = store;
        this.imService = im;
        this.desc = null;
    }

    public VectorStoreTool(String name, VectorStore store, ImService im, String desc){
        this.name = name;
        this.vectorStore = store;
        this.imService = im;
        this.desc = desc;
    }

    @Override
    public String getToolName() {
        return name + "";
    }

    @Override
    public String getToolDescription() {
        if(this.desc != null){
            return this.desc;
        }
        return ""
            + " only use " + name + " tool if user's intented place is in " + name + ". "
            + " Never assume user's departure or arrival location is in " + name + ". "
            + " Input should be a fully formed English question. ";   
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
        return ToolOuts
                .of(user, true)
                .message(Tool.KEY_OBSERVATION, sb.substring(0, Math.min(sb.length(), 1536)))
                .message(Tool.KEY_THOUGHT, "Now I have the results from " + name + ". I will inform user with the summarized result.")
                .sync();
    }

    
    @Override
    public void onClearedMemory(String user) {
    }
    
}
