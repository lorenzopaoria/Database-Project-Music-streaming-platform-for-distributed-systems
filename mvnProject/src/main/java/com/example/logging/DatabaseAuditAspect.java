package com.example.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class DatabaseAuditAspect {
    private final DatabaseAuditLogger auditLogger = DatabaseAuditLogger.getInstance();
    private final ThreadLocal<AuthContext> authContextHolder = new ThreadLocal<>();
    private final ThreadLocal<QueryContext> queryContextHolder = new ThreadLocal<>();

    private static class AuthContext {
        String clientId;
        String sessionId;
        String email;
    }

    private static class QueryContext {
        String sessionId;
        String query;
    }

    // Pointcut per ObjectInputStream.readObject() nel contesto di handleAuthentication
    @Pointcut("call(* java.io.ObjectInputStream.readObject()) && within(com.example.DatabaseServer.ClientHandler)")
    public void readObjectPointcut() {}

    // Pointcut per il metodo handleAuthentication
    @Pointcut("execution(* com.example.DatabaseServer.ClientHandler.handleAuthentication())")
    public void authenticationPointcut() {}

    // Pointcut per il metodo handleQuery
    @Pointcut("execution(* com.example.DatabaseServer.ClientHandler.handleQuery())")
    public void queryPointcut() {}

    // prima dell'autenticazione inizializzo il contesto
    @Before("authenticationPointcut()")

    public void beforeAuthentication(JoinPoint joinPoint) {
        AuthContext context = new AuthContext();
        context.clientId = ((com.example.DatabaseServer.ClientHandler) joinPoint.getTarget()).getClientId();
        authContextHolder.set(context);
    }

    // cattura i parametri durante la lettura
    @AfterReturning(
        pointcut = "readObjectPointcut() && withincode(* handleAuthentication())",
        returning = "result"
    )

    public void afterReadAuthData(JoinPoint joinPoint, Object result) {
        if (result instanceof String) {
            AuthContext context = authContextHolder.get();
            if (context != null) {
                if (context.email == null) {
                    context.email = (String) result;
                }
            }
        }
    }

    // prima della query inizializzo il contesto
    @Before("queryPointcut()")
    public void beforeQuery(JoinPoint joinPoint) {
        QueryContext context = new QueryContext();
        queryContextHolder.set(context);
    }

    // cattura i parametri della query durante la lettura
    @AfterReturning(
        pointcut = "readObjectPointcut() && withincode(* handleQuery())",
        returning = "result"
    )

    public void afterReadQueryData(JoinPoint joinPoint, Object result) {
        if (result instanceof String) {
            QueryContext context = queryContextHolder.get();
            if (context != null) {
                if (context.sessionId == null) {
                    context.sessionId = (String) result;
                } else if (context.query == null) {
                    context.query = (String) result;
                }
            }
        }
    }

    // Dopo l'autenticazione
    @AfterReturning(
    pointcut = "authenticationPointcut()",
    returning = "result"
    )

    public void afterAuthentication(JoinPoint joinPoint, Object result) {
        AuthContext context = authContextHolder.get();
        if (context != null) {
            com.example.DatabaseServer.ClientHandler handler = (com.example.DatabaseServer.ClientHandler) joinPoint.getTarget();
            
            String currentRole = handler.getCurrentUserRole();
            boolean success = currentRole != null;
            
            auditLogger.logAuthentication(
                context.clientId,
                context.sessionId,
                context.email,
                currentRole,
                success
            );
            authContextHolder.remove();
        }
    }

    // Dopo l'esecuzione della query
    @AfterReturning(
    pointcut = "queryPointcut()",
    returning = "result"
    )

    public void afterQuery(JoinPoint joinPoint, Object result) {
        QueryContext context = queryContextHolder.get();
        if (context != null) {
            com.example.DatabaseServer.ClientHandler handler = (com.example.DatabaseServer.ClientHandler) joinPoint.getTarget();
            
            String currentResult = handler.getResult();
            boolean success = !currentResult.equals("Access denied: Insufficient permissions") && !currentResult.startsWith("Query execution error:");

            auditLogger.logQuery(
                context.sessionId,
                context.query,
                success
            );
            queryContextHolder.remove();
        }
    }
}