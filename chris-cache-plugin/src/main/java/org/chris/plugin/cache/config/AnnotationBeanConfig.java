
package org.chris.plugin.cache.config;

import org.chris.plugin.cache.annotation.IdempotentCacheAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author caizq
 * @date 2018/8/17
 * @since v1.0.0
 */
@Configuration
public class AnnotationBeanConfig {

    @Bean
    public IdempotentCacheAspect idempotentCacheAspect() {
        return new IdempotentCacheAspect();
    }
}
