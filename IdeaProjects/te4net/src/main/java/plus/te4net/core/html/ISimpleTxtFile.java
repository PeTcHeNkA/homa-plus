package plus.te4net.core.html;

import plus.te4net.core.api.IRequestSession;


public interface ISimpleTxtFile {
    String type();
    byte[] get(IRequestSession session);
}
