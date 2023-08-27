package im.langchainjava.agent.episode.focus;

import java.util.List;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class FocusManager {
    final List<Focus> focuses;

    public FocusManager(){
        this.focuses = new ArrayList<>();
    }

    public FocusManager(List<Focus> focuses){
        this.focuses = focuses;
    }

    public FocusManager focus(Focus f){
        this.focuses.add(f);
        return this;
    }


}
