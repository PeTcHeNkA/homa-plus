package plus.sol.utls;


public interface ILinkable<T extends ILinkable<T>> {
    T link(T other);
}