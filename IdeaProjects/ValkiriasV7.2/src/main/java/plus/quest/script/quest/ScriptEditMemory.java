package plus.quest.script.quest;

import plus.quest.AbstractQuestSession;
import plus.quest.QuestConfig;
import plus.quest.memory.AbstractMemorySector;
import plus.quest.memory.MemorySector;
import plus.quest.memory.QuestMemory;
import plus.quest.script.quest.logic.Conduction;
import plus.quest.script.quest.logic.Mather;
import plus.util.cfg.Sector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ScriptEditMemory implements QuestScript{

    private final Request[] requests;

    public ScriptEditMemory(Sector s, String path){
        List<String> rawRequests;
        if(s instanceof QuestConfig c){
            rawRequests = c.getOrSetSingleStringList(path, ".value",
                    Arrays.asList(
                            "INT:example_section1 = 10",
                            "INT:example_section2 = MATH: NUM:10 + NUM:1",
                            "BOOL:example_section3 = true",
                            "BOOL:example_section4 = NUM:10 > NUM:1",
                            "STR:example_section5 = Hello_world!"
                            )
            );
        } else {
            rawRequests = s.getOrSetStringList(path + ".value",
                    Arrays.asList(
                            "INT:example_section1 = 10",
                            "INT:example_section2 = MATH: NUM:10 + NUM:1",
                            "BOOL:example_section3 = true",
                            "BOOL:example_section4 = NUM:10 > NUM:1",
                            "STR:example_section5 = Hello_world!"
                    )
            );
        }
        List<Request> requests = new ArrayList<>();
        for(String rawData:rawRequests){
            try {
                requests.add(Request.build(rawData));
            } catch (Exception e){
                System.out.printf("Error in encode memory request! path=[%s], data=[%s]%n", path, rawData);
                e.printStackTrace();
            }
        }
        this.requests = requests.toArray(new Request[0]);
    }


    @Override
    public void run(AbstractQuestSession session) {
        for(Request request:requests){
            request.apply(session);
        }
        //System.out.println("===");
        //System.out.println(session.getMemory().toString());
        //System.out.println(session.getMemory().Sections().keySet().size());
        //System.out.println("===");
    }
}


abstract class Request {
    protected final String request;

    public Request(String request){
        this.request = request;
        //System.out.println("|"+request+"|");
    }

    public static Request build(String raw){
        String[] field = raw.substring(0, raw.indexOf("=")).strip().split(":");
        String data = raw.substring(raw.indexOf("=")+1).strip();

        if(field[0].equalsIgnoreCase("int")){
            if(data.startsWith("math:") || data.startsWith("MATH:")){
                data = data.substring(5).strip();
                String finalData1 = data;
                return new Request(field[1]) {
                    private final Mather mather = Mather.build(finalData1);
                    @Override
                    public void apply(AbstractQuestSession session) {
                        session.getMemory().Sections().put(request,
                                new MemorySector(mather.getValue(session))
                        );
                    }
                };
            } else {
                String finalData = data;
                return new Request(field[1]) {
                    private final AbstractMemorySector memSector =
                            QuestMemory.parseMemory("int", finalData);
                    @Override
                    public void apply(AbstractQuestSession session) {
                        session.getMemory().Sections().put(request, memSector);
                    }
                };
            }
        } else  if(field[0].equalsIgnoreCase("bool")){
            if(data.equalsIgnoreCase("true") || data.equalsIgnoreCase("false")){
                String finalData3 = data;
                return new Request(field[1]) {
                    private final boolean b = finalData3.equalsIgnoreCase("true");
                    @Override
                    public void apply(AbstractQuestSession session) {
                        session.getMemory().Sections().put(request, new MemorySector(b));
                    }
                };
            } else {
                String finalData2 = data;
                return new Request(field[1]) {
                    private final Conduction conduction = Conduction.build(finalData2);

                    @Override
                    public void apply(AbstractQuestSession session) {
                        session.getMemory().Sections().put(request,
                                new MemorySector(conduction.checkFor(session)));
                    }
                };
            }
        } else {
            String finalData4 = data;
            return new Request(field[1]) {
                @Override
                public void apply(AbstractQuestSession session) {
                    session.getMemory().Sections().put(request,
                            new MemorySector(finalData4));
                }
            };
        }
    }

    public abstract void apply(AbstractQuestSession session);
}















