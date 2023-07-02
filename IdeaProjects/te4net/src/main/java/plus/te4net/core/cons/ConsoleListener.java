package plus.te4net.core.cons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;


public class ConsoleListener {
    protected boolean enabled = false;
    private Thread t;

    protected HashMap<String, ConsoleCommandExucator> executors = new HashMap<>();

    private static ConsoleListener l;
    public static ConsoleListener getGlobal(){
        return l;
    }


    public static void setGlobal(ConsoleListener listener){
        if(l != null) l.stop();
        l = listener;
        if(listener != null) l.start();
    }


    public ConsoleListener(){
        addCommand(
                new ParentableConsoleCommand("/stop-console") {
                    @Override
                    public void onSelfCommand(String... args) {
                        ConsoleListener.this.stop();
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


    public void stop(){
        enabled = false;
        try {
            t.stop();
        } catch (Exception ignored){}
    }


    public ConsoleListener start(){
        enabled = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        t = new Thread(() -> {
            while (enabled){
                try {
                    onRawCommand(br.readLine());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Thread.currentThread().stop();
        });
        t.start();
        return this;
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