package plus.app;

import plus.app.engine.AppScriptEngine;
import plus.app.engine.module.User;
import plus.sol.utls.Logger;


public class Main {
    public static AppScriptEngine engine;

    public static void main(String... args){
        Logger.log("Started.");
        String scriptPath = "src/main/asol_example";
        if(args.length>0){
            scriptPath = args[0];
        }
        try {
            engine = new AppScriptEngine(scriptPath);

            //engine.runScriptById(4, User.system);



        } catch (Exception e){
            e.printStackTrace();
            new RuntimeException(e);
        }
    }
}
