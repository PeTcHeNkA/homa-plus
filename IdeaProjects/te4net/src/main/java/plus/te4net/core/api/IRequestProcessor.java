package plus.te4net.core.api;


@PublicUsage
public interface IRequestProcessor {
    void handle(RequestSession session);
}