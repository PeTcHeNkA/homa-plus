package plus.mobs.utl;

public interface LogicalExpression<T> {
    boolean check(T pbj);
}