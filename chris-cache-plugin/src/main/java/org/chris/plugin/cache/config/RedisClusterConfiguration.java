
package org.chris.plugin.cache.config;

import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @author caizq
 * @date 2018/4/25
 * @since v1.0.0
 */
@Component
@ConfigurationProperties(prefix = "spring.redis.cluster.cache")
@Data
public class RedisClusterConfiguration {
    private String clusterNodes;
    private Integer commandTimeout;
    private String password;

    @Bean
    public JedisCluster getJedisCluster() {
        String[] serverArray = this.getClusterNodes().split(",");
        Set<HostAndPort> nodes = new HashSet<>();
        for (String ipPort : serverArray) {
            String[] ipPortPair = ipPort.split(":");
            nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
        }
        return new JedisCluster(nodes, this.getCommandTimeout(), 5000, 5, password, new GenericObjectPoolConfig());
    }
}
