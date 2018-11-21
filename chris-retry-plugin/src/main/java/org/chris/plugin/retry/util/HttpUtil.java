package org.chris.plugin.retry.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.chris.plugin.retry.enums.EnumCommomSysErrorCode;
import org.chris.plugin.retry.exception.BusinessRuntimeException;
import org.chris.plugin.retry.exception.SystemRuntimeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http请求工具类
 *
 * @author caizq
 * @date 2018/9/12
 * @since v1.0.0
 */
@Slf4j
public class HttpUtil {

    private static final CloseableHttpClient httpclient = HttpClients.createDefault();

    /**
     * 发送HttpGet请求
     *
     * @param url
     * @return
     */
    public static String sendGet(String url) {

        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response;
        try {
            response = httpclient.execute(httpget);
        } catch (IOException e1) {
            throw SystemRuntimeException.buildUnknownException(e1);
        }
        String result = null;
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                throw SystemRuntimeException.buildUnknownException(e);
            }
        }
        return result;
    }

    /**
     * 发送HttpPost请求，参数为map
     *
     * @param url
     * @param map key为参数名,value为json字符串
     * @return
     */
    public static String sendPost(String url, Map<String, String> map) {
        List<NameValuePair> formparams = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(entity);
        CloseableHttpResponse response;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            throw BusinessRuntimeException.buildBusyException(EnumCommomSysErrorCode.REMOTE_SERVICE_REQUEST_FAILED,e.getMessage());
        }
        HttpEntity entity1 = response.getEntity();
        String result;
        try {
            result = EntityUtils.toString(entity1);
        } catch (ParseException | IOException e) {
            throw BusinessRuntimeException.buildUnknownException(e);
        }
        return result;
    }

    /**
     * 发送HttpPost请求，参数为map
     *
     * @param url
     * @param data json字符串
     * @return
     */
    public static String sendPostJson(String url, String data) {
        StringEntity entity = new StringEntity(data, ContentType.APPLICATION_JSON);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(entity);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            throw BusinessRuntimeException.buildUnknownException(e);
        }
        HttpEntity responseEntity = response.getEntity();
        String result = null;
        try {
            result = EntityUtils.toString(responseEntity);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 发送不带参数的HttpPost请求
     *
     * @param url
     * @return
     */
    public static String sendPost(String url) {
        HttpPost httppost = new HttpPost(url);
        CloseableHttpResponse response;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            throw SystemRuntimeException.buildUnknownException();
        }
        HttpEntity entity = response.getEntity();
        String result;
        try {
            result = EntityUtils.toString(entity);
        } catch (ParseException | IOException e) {
            throw SystemRuntimeException.buildUnknownException();
        }
        return result;
    }

}
