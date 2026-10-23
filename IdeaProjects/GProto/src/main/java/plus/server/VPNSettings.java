package plus.server;

import java.lang.reflect.InvocationTargetException;
import java.util.prefs.Preferences;

public class VPNSettings {
    public static String ITN = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";

    public boolean enabled = false;
    public String host;
    public String proxyOverride;


    public static VPNSettings Read() throws Exception {
        var r = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, ITN, "ProxyServer");

        System.out.println(r);

        var s = new VPNSettings();


        return s;
    }


    public static void main(String[] args) throws Exception {
        var s = Read();

    }
}







