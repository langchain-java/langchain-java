package im.langchainjava.tool.question;

import java.util.List;
import java.util.Map;

import im.langchainjava.im.ImService;
import im.langchainjava.llm.LlmService;
import im.langchainjava.llm.entity.function.FunctionCall;
import im.langchainjava.llm.entity.function.FunctionProperty;
import im.langchainjava.memory.ChatMemoryProvider;
import im.langchainjava.tool.Tool;
import im.langchainjava.tool.ToolDependency;
import im.langchainjava.tool.ToolOut;
import im.langchainjava.tool.askuser.form.FormBuilders;

public class QuestionExtractionTool extends Tool{

    private static String PARAM_Q_TYPE = "the_question_type";
    private static String PARAM_Q_TYPE_DESC = "";

    public QuestionExtractionTool(ImService im, LlmService llm){
        dependencyAndProperty(im, FormBuilders.textForm(llm, PARAM_Q_TYPE, PARAM_Q_TYPE_DESC));
        extractionName(EXTRACTION_NAME);
        extraction(EXTRACTION);
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDescription'");
    }

    @Override
    public Map<String, FunctionProperty> getProperties() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public List<String> getRequiredProperties() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredProperties'");
    }

    @Override
    public Map<String, ToolDependency> getDependencies() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }

    @Override
    public ToolOut doInvoke(String user, FunctionCall call, ChatMemoryProvider memory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doInvoke'");
    }
}
