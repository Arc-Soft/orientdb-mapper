package mapper.orient;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import mapper.orient.reflect.ReflectionMapper;
import mapper.orient.session.OrientTransaction;
import mapper.orient.session.SessionProvider;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.orientechnologies.orient.core.record.ODirection.OUT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static mapper.orient.names.PropertyName.*;
import static mapper.orient.reflect.OrientReflectionAccessor.objectToVertex;
import static mapper.orient.reflect.OrientReflectionAccessor.vertexName;

@Component
@OrientTransaction
public class OQuery {

    private final SessionProvider session;

    public OQuery(SessionProvider session) {
        this.session = session;
    }

    public <T> List<T> findAll(Class<T> clazz) {
        return session.getSession().command("SELECT FROM " + vertexName(clazz)).stream()
                .map(new ReflectionMapper<>(clazz))
                .collect(toList());
    }

    public Optional<OResult> findOne(Query q) {
        return perform(q).stream().findAny();
    }

    public <T> Optional<T> findOne(Query q, Class<T> clazz) {
        return session.getSession().query(q.getQuery(), q.getParams()).stream()
                .findAny()
                .map(new ReflectionMapper<>(clazz));
    }

    public <T> List<T> findVertices(Query q, Class<T> clazz) {
        return perform(q).stream().map(new ReflectionMapper<>(clazz)).collect(toList());
    }

    public <T> Optional<OVertex> findVertex(String id, Class<T> clazz) {
        var q = byIdQuery(id, vertexName(clazz));
        return session.getSession().query(q.getQuery(), q.getParams()).vertexStream().findFirst();
    }

    public OVertex findVertex(Query q) {
        return perform(q).stream().findAny().get().getVertex().get();
    }

    public <T> Optional<T> findOneById(String id, Class<T> clazz) {
        var q = byIdQuery(id, vertexName(clazz));
        return session.getSession().query(q.getQuery(), q.getParams()).stream()
                .map(new ReflectionMapper<>(clazz))
                .findFirst();
    }

    public <T> OVertex updateById(T obj, String id) {
        var v = findVertex(id, obj.getClass()).get();
        return objectToVertex(obj, v).save();
    }

    public <T> void deleteVertexById(String id, Class<T> clazz) {
        findVertex(id, clazz).ifPresent(v -> perform(session -> session.delete(v)));
    }

    public Long countByProperty(String vertex, String prop, String value) {
        var query = format("SELECT count(*) as %s from %s where %s=:%s", COUNT, vertex, prop, VALUE);
        return session.getSession().query(query, Map.of(VALUE, value)).stream().findAny()
                .map(v -> (Long) v.getProperty(COUNT)).orElse(0L);
    }

    public Optional<OVertex> findByProperty(String vertex, String prop, String value) {
        var query = format("SELECT FROM %s where %s=:%s", vertex, prop, VALUE);
        return session.getSession().query(query, Map.of(VALUE, value)).vertexStream().findFirst();
    }

    public OEdge createEdge(OVertex from, OVertex to, String clazz, Map<String, Object> props) {
        var edge = session.getSession().newEdge(from, to, clazz);
        props.forEach(edge::setProperty);
        return edge.save();
    }

    public OEdge createEdge(OVertex from, OVertex to, String clazz) {
        return createEdge(from, to, clazz, Collections.emptyMap());
    }

    private List<OEdge> findEdges(OVertex from, OVertex to, String clazz) {
        var q = new Query(format("SELECT FROM %s WHERE in = :in AND out = :out", clazz),
                Map.of("in", to, "out", from));

        return perform(q).edgeStream().collect(Collectors.toList());
    }

    public void deleteEdges(OVertex from, OVertex to, String clazz) {
        findEdges(from, to, clazz).forEach(e -> perform(session -> session.delete(e)));
    }

    public <T> OVertex storeVertex(T object) {
        var v = session.getSession().newVertex(vertexName(object.getClass()));
        return objectToVertex(object, v).save();
    }

    public boolean edgeExist(OVertex parent, OVertex child, String clazz) {
        return stream(parent.getEdges(OUT, clazz).spliterator(), false).anyMatch(e -> e.getTo().equals(child));
    }

    public Optional<OEdge> getEdge(OVertex parent, OVertex child, String clazz) {
        return stream(parent.getEdges(OUT, clazz).spliterator(), false).findAny();
    }

    private OResultSet perform(Query q) {
        return session.getSession().command(q.getQuery(), q.getParams());
    }

    private <R> void perform(Function<ODatabaseSession, R> operation) {
        operation.apply(session.getSession());
    }

    private Query byIdQuery(String id, String vertexName) {
        return new Query(format("SELECT FROM %s WHERE id = :id", vertexName), Map.of(ID, id));
    }
}
