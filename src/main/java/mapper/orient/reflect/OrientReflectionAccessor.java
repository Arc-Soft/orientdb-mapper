package mapper.orient.reflect;

import com.orientechnologies.orient.core.record.OVertex;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static mapper.orient.exception.ExceptionWrapper.wrap;
import static mapper.orient.names.PropertyName.ID;
import static org.springframework.util.StringUtils.isEmpty;

public class OrientReflectionAccessor {

    public static OVertex objectToVertex(Object o, OVertex v) {
        getVertexField(o).filter(f -> !f.getName().equals(ID))
                .forEach(wrap(f -> v.setProperty(vertexPropertyName(f), f.get(o))));
        return v;
    }

    static String vertexPropertyName(Field f) {
        String name = f.getAnnotation(VertexProperty.class).name();
        return isEmpty(name) ? f.getName() : name;
    }

    public static <T> String vertexName(Class<T> clazz) {
        try {
            return clazz.getAnnotation(Vertex.class).value();
        } catch (Exception e) {
            throw new RuntimeException("There is no vertex annotation in class " + clazz.getName());
        }
    }


    private static Stream<Field> getVertexField(Object o) {
        return Stream.of(o.getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(VertexProperty.class))
                .peek(f -> f.setAccessible(true));
    }
}
