package mapper.orient.exception;

@FunctionalInterface
public interface ConsumerThrowing<T> {
    void accept(T t) throws Exception;
}
