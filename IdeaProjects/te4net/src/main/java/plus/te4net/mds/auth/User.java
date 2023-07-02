package plus.te4net.mds.auth;

import plus.tson.TsonMap;
import java.nio.charset.StandardCharsets;


public class User extends TsonMap {
    public final String login;
    public final String password;

    public User(String login, String password, byte[] data) {
        super(new String(data,StandardCharsets.UTF_8));
        this.login = login;
        this.password = password;
    }


    public byte[] getData(){
        return super.toString().getBytes(StandardCharsets.UTF_8);
    }


    @Override
    public String toString() {
        return "User: [" + login + "] password: [" + password + "] data: "+super.toString();
    }
}