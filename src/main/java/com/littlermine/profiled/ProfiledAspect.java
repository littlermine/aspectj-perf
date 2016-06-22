package com.littlermine.profiled;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;

@Slf4j
@Aspect
public class ProfiledAspect {

    @Pointcut(value = "execution(* com.test..*.*(..))")
    public void anyMethod() {
    }

    @Around("anyMethod() && @annotation(profiled)")
    public Object logElapsed(ProceedingJoinPoint pjp, Profiled profiled) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            try {
                Signature signature = pjp.getSignature();
                StringBuilder sb = new StringBuilder();

                if (signature != null) {
                    String clazzName = signature.getDeclaringTypeName();
                    String clazzSimpleName = stripPackageName(clazzName);
                    String method = signature.getName();
                    String param = profiled.showArgs() ? methodParams(pjp, signature) : "";
                    sb.append(clazzSimpleName).append(".").append(method);
                    sb.append("(").append(param).append(")");
                    String desc = profiled.desc();
                    if (StringUtils.isNotBlank(desc)) {
                        sb.append(" ").append(desc);
                    }
                }
                // XxxController.getYxx(arg1=val1, arg2=val2) test
                ProfiledLogUtils.log(sb.toString(), elapsed);
            } catch (Exception e) {
                log.error("error when log elapsed", e);
            }
        }
    }

    private String methodParams(ProceedingJoinPoint pjp, Signature signature) {
        StringBuilder sb = new StringBuilder();
        try {
            CodeSignature cs = (CodeSignature) signature;
            String[] paramNames = cs.getParameterNames();
            Object[] paramValues = pjp.getArgs();

            for (int i = 0; i < paramNames.length; i++) {
                sb.append(paramNames[i]).append("=").append(ProfiledLogUtils.toStr(paramValues[i]));
                if (i < paramNames.length - 1) {
                    sb.append(", ");
                }
            }
        } catch (Exception e) {
            log.error("error when generate method param info", e);
        }
        return sb.toString();
    }

    private String stripPackageName(String name) {
        if (name == null)
            return "UNKNOWN_CLASS";
        int dot = name.lastIndexOf('.');
        if (dot == -1)
            return name;
        return name.substring(dot + 1);
    }
}
