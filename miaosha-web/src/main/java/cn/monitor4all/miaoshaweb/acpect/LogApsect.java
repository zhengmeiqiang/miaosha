package cn.monitor4all.miaoshaweb.acpect;

import cn.monitor4all.miaoshaweb.utils.RedisUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;

@Component
@Aspect
public class LogApsect {

    private static final Logger logger = LoggerFactory.getLogger(LogApsect.class);

    @Autowired
    private RedisUtils redisUtils;

    ThreadLocal<Long> startTime = new ThreadLocal<>();


    // 第一个*代表返回类型不限
    // 第二个*代表所有类
    // 第三个*代表所有方法
    // (..) 代表参数不限
    @Pointcut("execution(public * cn.monitor4all.miaoshaweb.controller.*.*(..))")
    public void pointCut(){};

    @Pointcut("@annotation(cn.monitor4all.miaoshaweb.annoation.Check)")
    @Order(1) // Order 代表优先级，数字越小优先级越高
    public void annoationPoint(){};


//    @Before(value = "annoationPoint() && pointCut()")
//    public void before(JoinPoint joinPoint){
//        System.out.println("方法执行前执行......before");
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes.getRequest();
//        logger.info("<=====================================================");
//        logger.info("请求来源： =》" + request.getRemoteAddr());
//        logger.info("请求URL：" + request.getRequestURL().toString());
//        logger.info("请求方式：" + request.getMethod());
//        logger.info("响应方法：" + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
//        logger.info("请求参数：" + Arrays.toString(joinPoint.getArgs()));
//        logger.info("------------------------------------------------------");
//        startTime.set(System.currentTimeMillis());
//    }


    @Around("annoationPoint()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
//        logger.info("<=====================================================");
//        logger.info("请求URL：" + request.getServletPath());
//        logger.info("请求方式：" + request.getMethod());
//        logger.info("响应方法：" + pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName());
//        logger.info("请求参数：" + Arrays.toString(pjp.getArgs()));
        try{
            //暂时写死一个token，token应该是前端从后台获取，然后传递过来
           if (redisUtils.get("token") == null){
               logger.info("====================请不要重复提交请求");
               return "请不要重复提交请求";
           }
            String result = pjp.proceed().toString();
            redisUtils.del("token");
            System.out.println(result);
        }catch (Throwable e){
            e.printStackTrace();
        }
        return pjp.proceed();
    }

    @AfterThrowing("annoationPoint()")
    public void afterThrowing(){
        System.out.println("异常出现之后...afterThrowing");
    }

}
