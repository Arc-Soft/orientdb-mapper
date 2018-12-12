package mapper.orient.reflect;

import com.orientechnologies.orient.core.sql.executor.OResult;

import java.lang.reflect.Field;
import java.util.function.Function;

import static mapper.orient.reflect.OrientReflectionAccessor.vertexPropertyName;

public class ReflectionMapper<T> implements Function<OResult, T> {

    private final Class<T> clazz;

    public ReflectionMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T apply(OResult res) {
        try {
            T instance = clazz.getConstructor().newInstance();
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                f.set(instance, res.getProperty(vertexPropertyName(f)));
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can not create class instance " + clazz.getName(), e);
        }
    }
}
