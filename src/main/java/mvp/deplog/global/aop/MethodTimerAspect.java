package mvp.deplog.global.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MethodTimerAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(MethodTimer)")
    public Object measureMethodCallTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Timer.Sample sample = Timer.start(meterRegistry);
        Object result = joinPoint.proceed();
//        sample.stop(Timer.builder(className + "." + methodName + ".call")
        sample.stop(Timer.builder("method.call")
                .description(methodName + " 메소드 호출 시간")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "success")
                .register(meterRegistry));
        return result;
    }
}

