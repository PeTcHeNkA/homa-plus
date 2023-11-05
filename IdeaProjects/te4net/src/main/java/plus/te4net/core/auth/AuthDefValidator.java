package plus.te4net.core.auth;

import java.util.regex.Pattern;


public class AuthDefValidator {
    public static final Pattern mailPtr = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
    public static final Pattern pswPtr  = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!#$%?@-_+=^:;.<>*\"])(?=\\S+$).{8,24}$");
    public static final Pattern namePtr = Pattern.compile("^\\w+$");

    public static boolean validLogin(String login) {
        return mailPtr.matcher(login).find();
    }


    public static boolean validPassword(String password) {
        return pswPtr.matcher(password).find();
    }


    public static boolean validName(String name){
        return name.length() > 3 && name.length() < 16 && namePtr.matcher(name).find();
    }
}