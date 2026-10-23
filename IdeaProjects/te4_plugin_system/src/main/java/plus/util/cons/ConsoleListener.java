package plus.util.cons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ConsoleListener {
    private static boolean enabled = false;
    private static Thread t;
    protected HashMap<String, ConsoleCommandExucator> executors = new HashMap<>();
    static final HashMap<String,ConsoleListener> consoles = new HashMap<>();
    ArrayList<String> ignoredMessages = new ArrayList<>();
    static ConsoleListener listener;
    boolean isNowActive = false;
    private final String name;


    public ConsoleListener(String name0){
        this(name0, true);
    }


    public ConsoleListener start(){
        consoles.put(name, this);
        if(listener == null) {
            isNowActive = true;
            listener = this;
            startConsole();
        }
        return this;
    }


    public ConsoleListener destroy(){
        consoles.remove(name, this);
        if(listener != null) {
            Set<Map.Entry<String, ConsoleListener>> set = consoles.entrySet();
            if(set.isEmpty()){
                stop();
                return this;
            }
            isNowActive = true;
            listener = set.iterator().next().getValue();
        }
        return this;
    }


    public ConsoleListener(String name0, boolean autoAdd){
        this.name = name0;
        if(autoAdd) {
            start();
        }
        addCommand(
                new ConsoleCommand("/console") {
                    @Override
                    public void onCommand(String... args) {
                        Logger logger = Logger.get();
                        if (args == null || args.length == 0) {
                            logger.info(help());
                            return;
                        }
                        if (args[0].equals("list")) {
                            int i = 1;

                            for (String name : consoles.keySet()) {
                                logger.info(i + "| " + name);
                            }
                        } else {
                            ConsoleListener cons = consoles.get(args[0]);
                            if (cons == null) {
                                logger.info("Console with name[" + args[0] + "] not exists!");
                                return;
                            }
                            listener.isNowActive = false;
                            listener = cons;
                            listener.isNowActive = true;
                            logger.info("Choosed console [" + args[0] + "]");
                            for (String s : listener.ignoredMessages) {
                                logger.info(s);
                            }
                            listener.ignoredMessages.clear();
                        }
                    }
                },
                new ParentableConsoleCommand("/destroy") {
                    @Override
                    public void onSelfCommand(String... args) {
                        destroy();
                    }
                },
                new ParentableConsoleCommand("/stop-console") {
                    @Override
                    public void onSelfCommand(String... args) {
                        stop();
                    }},
                new ParentableConsoleCommand("/stop") {
                    @Override
                    public void onSelfCommand(String... args) {}
                    @Override
                    public void onCommand(String... args){
                        System.out.println("Start shutdown...");
                        for(ConsoleCommandExucator c:cmds){
                            c.onCommand(args);
                        }
                        System.exit(0);
                    }},
                new ParentableConsoleCommand("/help") {
                    @Override
                    public void onSelfCommand(String... args) {
                        if(args.length>0){
                            ConsoleCommandExucator ex = executors.get(args[0]);
                            if(ex==null){
                                System.out.println("Command ["+args[0]+"] not exists. List commands:");
                            } else {
                                String hi = ex.help();
                                if(hi==null){
                                    System.out.println("Command ["+args[0]+"] has`nt help info.");
                                } else {
                                    System.out.println(hi);
                                }
                                return;
                            }
                        } else {
                            System.out.println("Registered console commands:");
                        }
                        for(String s:executors.keySet()){
                            String hi = executors.get(s).help();
                            if(hi != null) {
                                System.out.printf("| - %s - %s%n", s, hi);
                            } else {
                                System.out.printf("| - %s%n", s);
                            }
                        }
                    }
                    @Override
                    public String help() {
                        return "Print this command list";
                    }
                }
        );
    }


    private static void stop(){
        enabled = false;
        try {
            t.stop();
        } catch (Exception ignored){}
    }


    private static void startConsole(){
        enabled = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        t = new Thread(() -> {
            while (enabled){
                try {
                    String res = br.readLine();
                    if(res != null)
                        listener.onRawCommand(new String(res.getBytes(), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Thread.currentThread().stop();
        });
        t.start();
    }


    public ConsoleListener addCommand(ConsoleCommandExucator... executors){
        for(ConsoleCommandExucator e: executors) {
            if(!this.executors.containsKey(e.getName()))
                this.executors.put(e.getName(), e);
            else {
                ConsoleCommandExucator ex = this.executors.get(e.getName());
                if(ex instanceof AbstractParentableConsoleCommand exx){
                    exx.addChildCommand(e);
                }
            }
        }
        return this;
    }


    protected void onRawCommand(String raw){
        if(!enabled)return;
        int index = raw.indexOf(' ');
        ConsoleCommandExucator ex;
        if(index>0)
            ex = executors.get(raw.substring(0, raw.indexOf(' ')));
        else ex = executors.get(raw);
        if(ex==null){
            System.out.println("Unknown commands!");
        } else {
            ex.onCommand(index>0? raw.substring(index).strip().split(" "):null);
        }
    }
}