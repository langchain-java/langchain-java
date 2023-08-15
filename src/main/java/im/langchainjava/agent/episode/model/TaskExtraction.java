package im.langchainjava.agent.episode.model;

import lombok.Data;

@Data
public class TaskExtraction {
    String name;
    String description;
    
    public TaskExtraction(String name, String description){
        this.name = name;
        this.description = description;
    }
} 
