package im.langchainjava.agent.episode.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TaskExtraction {
    String name;
    String extraction;
    List<String> enumm;
    // String action;
    
    public TaskExtraction(String name, String extraction){
        this.name = name;
        this.extraction = extraction;
        this.enumm = new ArrayList<>();
    }

    public TaskExtraction enumm(String item){
        this.enumm.add(item);
        return this;
    }

    public TaskExtraction(String name, String extraction, List<String> enumm){
        this.name = name;
        this.extraction = extraction;
        this.enumm = enumm;
    }
} 
