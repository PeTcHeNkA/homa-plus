package plus.Motions.parser;


import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MotionContainer {

    public List<short[]> Motions = new ArrayList<>();
    public List<byte[]> Cameras = new ArrayList<>();
    public HashMap<Integer, TextComponent[]> Messages = new HashMap<>();

    public MotionContainer(){
    }
    public MotionContainer(List<short[]> Motions, List<byte[]> Cameras){
        this.Motions = Motions;
        this.Cameras = Cameras;
    }
    public void Clear(){
        Motions.clear();
        Cameras.clear();
        Messages.clear();
    }
    public boolean isEmpty(){
        return Motions.isEmpty() && Cameras.isEmpty();
    }

}
