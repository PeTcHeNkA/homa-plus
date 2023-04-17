package plus.common.mashine.item.recept;

@FunctionalInterface
public interface IReseptValidator {
    boolean valid(ReseptKey key);
}