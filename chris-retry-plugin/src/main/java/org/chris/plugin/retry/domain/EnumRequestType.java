package org.chris.plugin.retry.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * HTTP请求类型
 *
 * @author caizq
 * @since v1.0.0
 */
@Getter
public enum EnumRequestType {
    /**
     * post+json请求
     */
    POST_JSON;

    public static List<EnumRequestType> getRequestTypeList() {
        EnumRequestType[] values = EnumRequestType.values();
        return Arrays.asList(values);
    }
}
