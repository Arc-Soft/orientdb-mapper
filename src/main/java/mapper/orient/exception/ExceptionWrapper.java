package mapper.orient.exception;

import java.util.function.Consumer;
import java.util.function.Function;

public class ExceptionWrapper {

    public static <T> Consumer<T> wrap(ConsumerThrowing<T> consumer) {
        return i -> {
            try {
                consumer.accept(i);
            } catch (Exception e) {
                throw new RuntimeException("Exception wrapped", e);
            }
        };
    }

    public static <T, R> Function<T, R> wrapFun(FunctionThrowing<T, R> function) {
        return i -> {
            try {
                return function.accept(i);
            } catch (Exception e) {
                throw new RuntimeException("Exception wrapped", e);
            }
        };
    }
}
