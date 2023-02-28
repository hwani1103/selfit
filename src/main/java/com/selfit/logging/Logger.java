package com.selfit.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class Logger {
    //로깅 기능. 현재는 @ClassLevelLogging 애너테이션만 사용하고 있음.
    //애너테이션을 붙이기만 하면 모든 메서드에 대해 로그가 남아서 꽤 편리하다.


    @Pointcut("@within(com.selfit.logging.ClassLevelLogging)")
    public void loggableClass() {}

    @Pointcut("@annotation(com.selfit.logging.MethodLevelLogging)")
    public void loggableMethod() {}

    @Pointcut("@annotation(com.selfit.logging.MethodLevelNotLogging)")
    public void notLoggableMethod() {}

    @Before("(loggableClass() ||  loggableMethod()) && !notLoggableMethod()")
    public void adviceBefore(JoinPoint jp) {
        log.info("=====start=====[" + jp.getSignature().toShortString() +"]");
        log.info("시그니처 = {}", jp.getSignature().toString());
        printArgs(jp.getArgs());

        log.info("타겟[{}]", stringFormatter(jp.getTarget().toString())); //타겟 : 실제 호출할 대상.
        log.info("This[{}]", stringFormatter(jp.getThis().toString()));
    }

    private void printArgs(Object[] obj){
        for (Object o : obj) {
            if(o!=null){
            log.info("매개변수 = [{}]", o);
            }
        }
    }

    private String stringFormatter(String str){
        String pattern = "member.";
        int index = str.indexOf(pattern);
        return str.substring(index + pattern.length());
    }


    @After("(loggableClass() ||  loggableMethod()) && !notLoggableMethod()")
    public void adviceAfter(JoinPoint jp)  {
        log.info("======end======[" + jp.getSignature().toShortString() +"]");
    }

    @AfterThrowing("(loggableClass() ||  loggableMethod()) && !notLoggableMethod()")
    public void adviceAfterThrowing(JoinPoint jp){
        System.out.println("===띠리릭. 에러가 발생했습니다.===");
        System.out.println("jp.getSignature() = " + jp.getSignature());
        System.out.println("jp.getTarget() = " + jp.getTarget());
        System.out.println("jp.getThis() = " + jp.getThis());
        System.out.println("jp.getStaticPart() = " + jp.getStaticPart());
        System.out.println("jp.getClass() = " + jp.getClass());
        System.out.println("jp.getSourceLocation() = " + jp.getSourceLocation());
        System.out.println("jp.getKind() = " + jp.getKind());
        printArgs(jp.getArgs());


    }



}
