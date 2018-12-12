package mapper.orient.session;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TransactionAspect {

    private final SessionProvider sessionProvider;

    public TransactionAspect(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    @Around("@annotation(OrientTransaction)")
    public Object serviceTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        ODatabaseSession conn = null;
        boolean sessionStartedByCurrentCall = sessionProvider.noSession();
        try {
            conn = sessionProvider.getSession();
            conn.begin();
            Object proceed = joinPoint.proceed();
            conn.commit();
            return proceed;
        } catch (Exception e) {
            if (conn != null && !conn.isClosed()) {
                conn.rollback(true);
            }
            throw new RuntimeException("Can not execute query", e);
        } finally {
            if (sessionStartedByCurrentCall) {
                sessionProvider.closeSession();
            }
        }
    }
}
