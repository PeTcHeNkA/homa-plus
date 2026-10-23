package plus.AChat.Format;

public interface Formatter {
    String Format(String var);
    String Format(String... vars);
    String Format(Object o);
}
