package plus.app.engine.module;

import plus.sol.api.IEnvironment;
import plus.sol.api.IVarProxy;


public class AppVarProvider {

    public static IVarProxy<User, ?> get(String name){
        if(name.equalsIgnoreCase("NULL")){
            return (IVarProxy<User, Object>) (user, env) -> null;
        }
//        if(name.charAt(0)=='"' && name.charAt(name.length()-1)=='"'){
//            String name0 = name.substring(1, name.length() - 1);
//            return (IVarProxy<User, Object>) (user, env) -> name0;
//        }
        String[] ds = name.toUpperCase().split("\\.");
        if(ds.length>=2){
            if(ds[0].equals("USER")){
                return getUserVar(ds[1]);
            } else if (ds[0].equals("SYSTEM")) {
                return getSystemVar(ds[1]);
            } else {
                throw new RuntimeException("Unknown var group: "+ds[0]);
            }
        } else if (ds.length==1) {
            return getUserVar(ds[0]);
        }
        try {
            int i = Integer.parseInt(name.trim());
            return (IVarProxy<User, Object>) (user, env) -> i;
        } catch (Exception e){

            throw new RuntimeException("Unsupported var format!");
        }
    }


    public static IVarProxy<User, ?> getUserVar(String name){
        if(name.equals("name")){
            return (IVarProxy<User, String>) (user, env) -> user.name;
        }
        if(name.startsWith("&")){
            String rName = name.substring(1);
            return new IVarProxy<User, Object>() {
                @Override
                public Object getValue(User user, IEnvironment<User> env) {
                    return user.getVar(rName);
                }

                @Override
                public void setValue(User user, IEnvironment<User> env, Object value) {
                    user.setVar(rName, value);
                }
            };
        }
        return new IVarProxy.Constant<>(name);
        //throw new RuntimeException("Unsupported var name: "+name);
    }


    public static IVarProxy<User, ?> getSystemVar(String name){
        if(name.equals("name")){
            return (IVarProxy<User, String>) (user, env) -> User.system.name;
        }
        if(name.startsWith("&")){
            String rName = name.substring(1);
            return new IVarProxy<User, Object>() {
                @Override
                public Object getValue(User user, IEnvironment<User> env) {
                    return User.system.getVar(rName);
                }

                @Override
                public void setValue(User user, IEnvironment<User> env, Object value) {
                    User.system.setVar(name, value);
                }
            };
        }
        throw new RuntimeException("Unsupported var name: "+name);
    }
}
