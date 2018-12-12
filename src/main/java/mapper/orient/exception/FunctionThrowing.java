package mapper.orient.exception;

@FunctionalInterface
public interface FunctionThrowing<T, R> {
    R accept(T t) throws Exception;
}
