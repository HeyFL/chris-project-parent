
package org.chris.common.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * JDK8 lambda工具类
 *
 * @author caizq
 * @date 2018/8/28
 * @since v1.0.0
 */
public class Jdk8LambdaUtil {
    /**
     * 两个List 求自定义交集(对比参数都为String类型)
     *
     * @param firstList             第一个list
     * @param firstListCompareParm  第一个list使用的方法 如:Student::getName
     * @param secondList            第二个list
     * @param secondListCompareParm 第二个list使用的方法 如:Teacher::getName
     * @param <T>
     * @return
     */
    public static <T, E> List<T> getIntersectionByStringParm(List<T> firstList, Function<T, String> firstListCompareParm, List<E> secondList, Function<E, String> secondListCompareParm) {
        if (firstList == null || firstList.isEmpty() || secondList == null || secondList.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> resultList = new LinkedList<>();
        for (T sourceObj : firstList) {
            for (E paiSongQuYu : secondList) {
                if (firstListCompareParm.apply(sourceObj).equalsIgnoreCase(secondListCompareParm.apply(paiSongQuYu))) {
                    resultList.add(sourceObj);
                }
            }
        }
        return resultList;
    }

    /**
     * 两个List 求自定义交集(对比的参数都是Integer类型)
     *
     * @param firstList             第一个list
     * @param firstListCompareParm  第一个list使用的方法 如:Student::getName
     * @param secondList            第二个list
     * @param secondListCompareParm 第二个list使用的方法 如:Teacher::getName
     * @param <T>
     * @return
     */
    public static <T, E> List<T> getIntersectionByIntegerParm(List<T> firstList, Function<T, Integer> firstListCompareParm, List<E> secondList, Function<E, Integer> secondListCompareParm) {
        if (firstList == null || firstList.isEmpty() || secondList == null || secondList.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> resultList = new LinkedList<>();
        for (T sourceObj : firstList) {
            for (E paiSongQuYu : secondList) {
                Integer sourceInteger = firstListCompareParm.apply(sourceObj);
                if (sourceInteger == null) {
                    break;
                }
                if (sourceInteger.equals(secondListCompareParm.apply(paiSongQuYu))) {
                    resultList.add(sourceObj);
                }
            }
        }
        return resultList;
    }
}
