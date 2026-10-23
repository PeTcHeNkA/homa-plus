package plus.Motions;

import org.bukkit.entity.Player;
import plus.Motions.parser.LinearParser;
import plus.Motions.parser.MapParser;
import plus.Motions.parser.MotionContainer;
import plus.Motions.parser.Parser;
import plus.util.Chat.ChatParser;
import plus.util.CordMath;

import java.util.ArrayList;
import java.util.List;

public class MotionWithBreakPoints {

    private final MotionContainer container;
    private final double[] startPos;
    private boolean hasAnimation = false;
    private String animation = null;
    private double speedAnimation = 0;

    private final DialogScene c;

    public MotionWithBreakPoints(DialogScene c, String t, List<String> data, String rawStartPos, String rawLookTarget, int time, boolean Invert){
        this.c = c;
        double[] lookTarget = CordMath.parseCords(rawLookTarget);
        startPos = CordMath.parseCords(rawStartPos);

        Parser parser = null;
        if(t.equalsIgnoreCase("LINEAR")){
            parser = new LinearParser(data, startPos, lookTarget, time);
        }
        if(t.equalsIgnoreCase("MAP")){
            parser = new MapParser(data, startPos, lookTarget, time);
        }
        parser.setInvertLook(Invert);
        container = parser.parse();
    }

    private boolean BreakPoint = false;
    public void createBreakPoint(){
        BreakPoint = true;
    }
    public boolean hasBreakPoint(){
        return BreakPoint;
    }


    public void run(Player player, int CameraUid, int speed){

        boolean runText = !container.Messages.isEmpty();

        if(hasAnimation){
            c.character.playAnimation(animation, speedAnimation);
        }

        try {
            Motion.teleportCameraToStart(player, CameraUid, startPos, container.Cameras.get(0));

            for (int i = 1; i < container.Cameras.size(); i++) {
                Motion.editCamera(
                        player,
                        CameraUid,
                        container.Motions.get(i - 1),
                        container.Cameras.get(i)
                );
                if (runText && container.Messages.containsKey(i)) {
                    Motion.runMessages(player, container.Messages.get(i));
                }
                Thread.sleep(speed);
            }
            if(BreakPoint){
                boolean invert = true;
                int i = container.Cameras.size()-1;

                while (c.players.contains(player.getName())){
                    short[] m = container.Motions.get(i - 1);;
                    if(invert){
                        m = new short[]{(short) -m[0], (short) -m[1], (short) -m[2]};
                        i -= 1;
                    } else {
                        i += 1;
                    }
                    Motion.editCamera(
                            player,
                            CameraUid,
                            m,
                            container.Cameras.get(i)
                    );
                    if(invert){
                        if(i<2)invert = false;
                    } else {
                        if(i>=container.Cameras.size()-1)invert = true;
                    }
                    Thread.sleep(speed);
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void setAnimation(String animation, double speed){
        hasAnimation = true;
        this.animation = animation;
        speedAnimation = speed;
    }

    public void addMessages(Integer index, List<String> rawMessage){
        container.Messages.put(index, ChatParser.Parse(rawMessage));
    }
}
