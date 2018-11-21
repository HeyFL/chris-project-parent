package org.chris.plugin.cache.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * @author caizq
 * @date 2017/12/27
 * @since v1.0.0
 */
@Slf4j
@Component
public class JedisClusterUtil {
    private static final int DEFAULT_SINGLE_EXPIRE_TIME = 5;
    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 循环获得锁,直到成功 返回true
     * 『注意』 单个锁操作时间不能超过 DEFAULT_SINGLE_EXPIRE_TIME
     *
     * @param key         锁键
     * @param requestUUID 被谁锁定
     * @return true
     * @throws InterruptedException
     */
    private static final String LOCK_SUCCESS = "OK";
    /**
     * NX|XX, NX – 如果不存在设置key，存在则不做； XX – 如果key存在才设置，不存在不做
     */
    private static final String SET_IF_NOT_EXIST = "NX";

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return jedisCluster.exists(key);
    }

    public boolean cacheIfNotExists(final String key, String value, Integer expireTime) {
        if (!exists(key)) {
            return set(key, value, expireTime);
        }
        return true;
    }




    /**
     * 获取指定key中的数据长度
     *
     * @param key
     * @return
     */
    public Long llen(final String key) {
        return jedisCluster.llen(key);
    }

    /**
     * 从队列右边pop出数据，为阻塞方法
     *
     * @param key
     * @param timeout 阻塞时间
     * @return
     */
    public List<String> brpop(final int timeout, final String key) {
        return jedisCluster.brpop(timeout, key);
    }

    /**
     * 从队列左边push取数据
     *
     * @param key
     * @param string 可变参数
     * @return
     */
    public Long lpush(final String key, final String... string) {
        return jedisCluster.lpush(key, string);
    }

    /**
     * 支持自定义过期时间的setnx方法
     *
     * @param key        要保存的key
     * @param expireTime 过期时间，单位为：秒
     * @return 返回值描述
     */
    public boolean setnx(final String key, final int expireTime) {
        String result = jedisCluster.set(key, "", SET_IF_NOT_EXIST, SECOND_EXPIRE_TIME, expireTime);
        return LOCK_SUCCESS.equals(result);
    }

    /**
     * 向hash集合存数据
     *
     * @param key   hash集合名字
     * @param field hash集合key
     * @param value hash集合value
     * @return
     */
    public Long hset(final String key, final String field, final String value) {
        return jedisCluster.hset(key, field, value);
    }

    /**
     * 从hash集合获取数据
     *
     * @param key   hash集合名字
     * @param field hash集合key
     * @return
     */
    public String hget(final String key, final String field) {
        return jedisCluster.hget(key, field);
    }

    /**
     * 从hash集合删除数据
     *
     * @param key   hash集合名字
     * @param field hash集合key
     * @return
     */
    public Long hdel(final String key, final String... field) {
        return jedisCluster.hdel(key, field);
    }

    /**
     * @param key
     * @param score
     * @param member
     * @return
     */
    public Long zadd(String key, double score, String member) {
        return jedisCluster.zadd(key, score, member);
    }



    /**
     * EX|PX, EX—失效时间单位为秒，PX—失效时间单位为毫秒
     */
    private static final String SECOND_EXPIRE_TIME = "EX";

    /**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(final String pattern) {
        jedisCluster.del(pattern);
        log.debug("del key >" + pattern);
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            jedisCluster.del(key);
            log.debug("del key >" + key);
        } else {
            log.debug("del key >" + key + "not exist");
        }
    }

    /**
     * 根据表达式 读取所有匹配的key (KEYS命令 生产不建议使用)
     *
     * @param pattern (例如:KEYS TraceNode_*)
     * @return
     */
    @Deprecated
    public TreeSet<String> keys(String pattern) {
        log.debug("Start getting keys...");
        TreeSet<String> keys = new TreeSet<>();
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
        for (String k : clusterNodes.keySet()) {
            log.debug("Getting keys from: {}", k);
            JedisPool jp = clusterNodes.get(k);
            Jedis connection = jp.getResource();
            try {
                keys.addAll(connection.keys(pattern));
            } catch (Exception e) {
                log.error("Getting keys error: {}", e);
            } finally {
                log.debug("Connection closed.");
                connection.close();//用完一定要close这个链接！！！
            }
        }
        log.debug("Keys gotten!");
        return keys;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, String value) {
        boolean result = false;
        try {
            jedisCluster.set(key, value);
            result = true;
        } catch (Exception e) {
            log.error("jedis error:{}", e);
        }
        return result;
    }

    /**
     * 写入缓存并设置缓存有效期
     *
     * @param key
     * @param value
     * @param expireTime 单位是秒
     * @return
     */
    public boolean set(final String key, String value, int expireTime) {
        boolean result = false;
        try {
            jedisCluster.setex(key, expireTime, value);
            result = true;
        } catch (Exception e) {
            log.error("jedis error:{}", e);
        }
        return result;
    }

    /**
     * 获取锁 如果锁可用   立即返回true，  否则立即返回false，作为非阻塞式锁使用
     * <p>
     * 该方法主要作用:
     * 当并发高时,会导致一些线程"尝试获取"锁的时间超时, 超时发生时  说明并发过高  超时后可以进行熔断处理
     *
     * @param key
     * @param requestUUID
     * @param tryLockTime 单位: 秒
     * @param expireTime  锁过期时间 单位: 秒
     * @return
     */
    public boolean tryLock(String key, String requestUUID, Long tryLockTime, Integer expireTime) {
        try {
            return tryLockUntilTryLockTimeOut(key, requestUUID, tryLockTime, TimeUnit.SECONDS, expireTime);
        } catch (Exception e) {
            log.warn("获取锁头失败 :{}", e);
        }
        return false;
    }

    /**
     * 锁在给定的等待时间内空闲，则获取锁成功 返回true， 否则返回false，作为阻塞式锁使用
     * <p>
     * 【注意】 线程多/处理时间长时,需要设置timeout时间足够大，不然会获取锁失败( 不过不建议设置很大的timeout时间)
     * 如果是必须要获取到锁的情况，建议使用tryLockUntilTrue的思想
     *
     * @param key                                  锁键
     * @param requestUUID                          被谁锁定
     * @param tryLockTime                              尝试获取锁时长
     * @param unit，推荐TimeUnit.MILLISECONDS(5000毫秒) 其次TimeUnit.SECONDS(5秒)
     * @param expireTime                           锁过期时间
     * @return
     * @throws InterruptedException
     */
    private boolean tryLockUntilTryLockTimeOut(String key, String requestUUID, long tryLockTime, TimeUnit unit, int expireTime) throws InterruptedException {

        return getLock(key, requestUUID, tryLockTime, unit, expireTime);
        //因超时没有获得锁
    }

    private Boolean getLock(String key, String requestUUID, long tryLockTime, TimeUnit unit, int expireTime) throws InterruptedException {
        //纳秒
        long begin = System.nanoTime();
        do {
            String result = jedisCluster.set(key, requestUUID, SET_IF_NOT_EXIST, SECOND_EXPIRE_TIME, expireTime);

            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
            if (tryLockTime == 0) {
                break;
            }
            //在其睡眠的期间，锁可能被解，也可能又被他人占用，但会尝试继续获取锁直到指定的时间
            Thread.sleep(100);
        }
        while ((System.nanoTime() - begin) < unit.toNanos(tryLockTime));
        return false;
    }


    /**
     * 释放单个锁
     *
     * @param key         锁键
     * @param requestUUID UUID
     * @return
     */
    public boolean unLock(String key, String requestUUID) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedisCluster.eval(script, Collections.singletonList(key), Collections.singletonList(requestUUID));
            return result.equals(1L);

        } catch (Exception e) {
            log.error("redis 释放锁失败{}", e);
            return false;
        }
    }



    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public String get(final String key) {
        return jedisCluster.get(key);
    }

    /**
     * 尝试获取分布式锁
     *
     * @param key         锁键
     * @param requestUUID 请求标识
     * @param expireTime  超期时间
     * @param unit        时间单位 PX(毫秒) | EX(秒)
     * @return 是否获取成功
     */
    public boolean tryLockNotWait(String key, String requestUUID, String unit, int expireTime) {
        String result = jedisCluster.set(key, requestUUID, SET_IF_NOT_EXIST, unit, expireTime);
        return LOCK_SUCCESS.equals(result);
    }


    /**
     * 读取缓存(尽量还是不要用字节的,因为线上运维时不好查)
     * <p>
     * 推荐存json格式的字符串
     *
     * @param key
     * @return
     */
    @Deprecated
    public Object getObject(final String key) {
        byte[] keyByte = objectToByteArray(key);
        return jedisCluster.get(keyByte);
    }

    /**
     * 对象转Byte数组
     * @param obj
     * @return
     */
    private byte[] objectToByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            log.error("objectToByteArray failed, " + e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    log.error("close objectOutputStream failed, " + e);
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    log.error("close byteArrayOutputStream failed, " + e);
                }
            }

        }
        return bytes;
    }

    /**
     * Byte数组转对象
     *
     * @param bytes
     * @return
     */
    private Object byteArrayToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            log.error("byteArrayToObject failed, " + e);
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    log.error("close byteArrayInputStream failed, " + e);
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    log.error("close objectInputStream failed, " + e);
                }
            }
        }
        return obj;
    }
}
