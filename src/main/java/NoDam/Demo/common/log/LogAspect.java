package NoDam.Demo.common.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {
    }

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controller() {
    }

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void service() {
    }

    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void transactionalMethod() {
    }

    @Pointcut("@within(org.springframework.transaction.annotation.Transactional)")
    public void transactionalClass() {
    }

    @Pointcut("execution(* org.springframework.transaction.support.TransactionTemplate.execute*(..))")
    public void transactionTemplateExecute() {
    }

    @Around("restController() || controller()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution("CONTROLLER", joinPoint);
    }

    @Around("service()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution("SERVICE", joinPoint);
    }

    @Around("transactionalMethod() || transactionalClass() || transactionTemplateExecute()")
    public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution("TRANSACTION", joinPoint);
    }

    private Object logExecution(String type, ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String signature = joinPoint.getSignature().toShortString();

        log.info("[{} START] signature={}", type, signature);
        try {
            Object result = joinPoint.proceed();
            long executeMs = System.currentTimeMillis() - startTime;
            log.info("[{} SUCCESS] signature={} executeMs={} result={}",
                    type,
                    signature,
                    executeMs,
                    result
            );
            return result;
        } catch (Throwable throwable) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.warn("[{} FAIL] signature={} executeMs={} exception={} message={}",
                    type,
                    signature,
                    elapsedTime,
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage()
            );
            throw throwable;
        }
    }

}
