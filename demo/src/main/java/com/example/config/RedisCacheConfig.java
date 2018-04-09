package com.example.config;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author qianjianfeng
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class RedisCacheConfig  extends CachingConfigurerSupport {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Long defaultExpiration;
    private String contextPath;

    public Long getDefaultExpiration() {
        return defaultExpiration;
    }

    public void setDefaultExpiration(Long defaultExpiration) {
        this.defaultExpiration = defaultExpiration;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    /**
     * 自定义key.
     * key --> 项目名 + 缓存空间值 + 所有参数的值
     * 即使@Cacheable中的value属性一样，key也会不一样。
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator(){
            @Override
            public String generate(Object o, Method method, Object... objects) {
                // This will generate a unique key of the class name, the method name
                //and all method parameters appended.
                StringBuilder sb = new StringBuilder();
                sb.append(contextPath).append("/:");
                Cacheable cacheable = method.getAnnotation(Cacheable.class);
                CachePut cachePut = method.getAnnotation(CachePut.class);
                CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
                if (cacheable != null) {
                    sb.append(Arrays.toString(cacheable.value())).append(":");
                }else if (cachePut != null) {
                    sb.append(Arrays.toString(cachePut.value())).append(":");
                }else if (cacheEvict != null) {
                    sb.append(Arrays.toString(cacheEvict.value())).append(":");
                }
                Map valueMap = new HashMap();
                for (Object obj : objects) {
                    try {
                        getStringValueMap(obj,valueMap);
                    } catch (IllegalAccessException e) {
                        logger.info("生成key的时候,[{}]转换map异常，生成的key丢弃了该值。",obj.getClass(),e);
                    }
                }
                sb.append(valueMap.toString());
                System.err.println(sb.toString());
                return sb.toString();
            }
        };
    }

    /**
     * redis模板操作类,类似于jdbcTemplate的一个类;
     * 虽然CacheManager也能获取到Cache对象，但是操作起来没有那么灵活；
     * 这里在扩展下：RedisTemplate这个类不见得很好操作，我们可以在进行扩展一个我们
     * 自己的缓存类，比如：RedisStorage类;
     * @param factory : 通过Spring进行注入，参数在application.properties进行配置；
     * @return
     */
    @Bean
    public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        //key序列化方式;（不然会出现乱码;）,但是如果方法上有Long等非String类型的话，会报类型转换错误；
        //所以在没有自己定义key生成策略的时候，以下这个代码建议不要这么写，可以不配置或者自己实现ObjectRedisSerializer
        //或者JdkSerializationRedisSerializer序列化方式;
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();//Long类型不可以会出现异常信息;
        redisTemplate.setKeySerializer(redisSerializer);
        redisTemplate.setHashKeySerializer(redisSerializer);

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 缓存管理器
     * @param redisTemplate
     * @return
     */
    @SuppressWarnings("rawtypes")
    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
        //设置缓存过期时间
        //rcm.setDefaultExpiration(60);//秒
        return rcm;
    }

    /**
     * redis数据操作异常处理
     * 这里的处理：在日志中打印出错误信息，但是放行
     * 保证redis服务器出现连接等问题的时候不影响程序的正常运行，使得能够出问题时不用缓存
     * @return
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        CacheErrorHandler cacheErrorHandler = new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                logger.error("redis异常：key=[{}]",key,e);
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                logger.error("redis异常：key=[{}]",key,e);
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                logger.error("redis异常：key=[{}]",key,e);
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                logger.error("redis异常：",e);
            }
        };
        return cacheErrorHandler;
    }

    /**
     * 取出对象及其父类定义的字段名和值存入map中
     * @param obj
     * @param valueMap
     * @throws IllegalAccessException
     */
    private void getStringValueMap(Object obj, Map valueMap) throws IllegalAccessException {
        List<Field> fields = scanfields(obj.getClass());
        for (Field field : fields){
            Boolean accessFlag = field.isAccessible();
            field.setAccessible(true);
            Object o = field.get(obj);
            if (o != null){
                if (o instanceof Integer || o instanceof Long || o instanceof Float
                        || o instanceof Double || o instanceof String || o instanceof Collections
                        || o instanceof Map || o instanceof Byte){
                    valueMap.put(field.getName(), o.toString());
                }else {
                    getStringValueMap(o,valueMap);
                }
            }
            field.setAccessible(accessFlag);
        }
    }

    /**
     * 对一个类扫描取出它和它父类定义的字段
     * @param clazz
     * @return
     */
    private List<Field> scanfields(Class clazz){
        List<Field> fields = new ArrayList<>();
        if (clazz == Object.class) {
            return fields;
        }
        Field[] fieldArray = clazz.getDeclaredFields();
        String fieldName;
        for (int i = 0; i < fieldArray.length; i++){
            fieldName = fieldArray[i].getName();
            if (!("$staticClassInfo".equals(fieldName) || "__$stMC".equals(fieldName)
                    || "metaClass".equals(fieldName) || "$staticClassInfo$".equals(fieldName)
                    || "$callSiteArray".equals(fieldName))){
                fields.add(fieldArray[i]);
            }
        }
        fields.addAll(scanfields(clazz.getSuperclass()));
        return fields;
    }
}