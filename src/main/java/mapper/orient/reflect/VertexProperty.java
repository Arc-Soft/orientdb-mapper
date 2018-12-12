package mapper.orient.reflect;

import com.orientechnologies.orient.core.metadata.schema.OType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.orientechnologies.orient.core.metadata.schema.OType.STRING;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface VertexProperty {

    @AliasFor("value")
    OType type() default STRING;

    String name() default "";

    OType value() default STRING;
}
