package mapper.orient.session;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.springframework.stereotype.Component;

@Component
public class SessionProvider {
    private static ThreadLocal<ODatabaseSession> sessionContext = new ThreadLocal<>();
    private final ODatabasePool pool;

    public SessionProvider(ODatabasePool pool) {
        this.pool = pool;
    }

    public ODatabaseSession getSession() {
        if (noSession()) {
            sessionContext.set(pool.acquire());
        }
        return sessionContext.get();
    }

    boolean noSession() {
        return sessionContext.get() == null;
    }

    void closeSession() {
        if (noSession()) {
            return;
        }
        sessionContext.get().close();
        sessionContext.remove();
    }
}
