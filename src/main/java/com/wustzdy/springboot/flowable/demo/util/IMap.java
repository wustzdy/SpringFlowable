package com.wustzdy.springboot.flowable.demo.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

@Slf4j
public class IMap extends HashMap<String, Object> {

    private static final long serialVersionUID = 2973700516712219678L;
    HttpServletRequest request;
    /**
     * 自动将页面数据放入PageData对象中,当从页面获取数据时 new的时候要传request
     * request.getParameter(arg0): get(arg0)或getString(arg0)
     * request.getParameterValues(arg0): getValues(arg0)方法
     * 本类也可以当做一个普通的Map使用，new的时候不要传参数 author：liuqf5
     */
    Map<String, Object> returnMap = null;
    private String charset = "UTF-8";

    // 普通的Map
    public IMap() {
        returnMap = new HashMap<>();
    }

    // 从页面获取数据
    public IMap(HttpServletRequest request) {
        this.request = request;
        // 返回值Map
        returnMap = new HashMap<>();
        // 参数Map
        //@SuppressWarnings("unchecked")
        @SuppressWarnings("unchecked")
        Map<String, String[]> properties = request.getParameterMap();
        Set<Entry<String, String[]>> entrySet = properties.entrySet();
        for (Entry<String, String[]> entry : entrySet) {
            String key = entry.getKey() == null ? null : convert2Decode(entry.getKey(), charset);
            Object valueObj = entry.getValue();
            String value = "";
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i].trim();
                    if (request.getMethod().equalsIgnoreCase("get")) {
                        value = values == null ? null : convert2Character(value, charset);
                    }
                    value = values == null ? null : convert2Decode(value, charset) + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnMap.put(key, value);
        }
    }

    private static String convert2Decode(String target, String charset) {
        try {
            System.out.println("---------URLDecoder处理前：--------" + target + "--->处理后===" + URLDecoder.decode(target, charset));
            return URLDecoder.decode(target, charset);
        } catch (UnsupportedEncodingException e) {
            return target;
        }
    }

    @Override
    public Object get(Object key) {
        return returnMap.get(key);
    }

    // 本方法只能用于从页面获取数组，例如相同名字的checkbox
    public String[] getValues(Object key) {
        return request == null ? null : request.getParameterValues((String) key);
    }

    public String getString(Object key) {
        Object o = get(key);
        return o == null ? null : o.toString();
    }

    public String getString(String name, String defaultValue) {
        String value = getString(name);
        return value == null ? defaultValue : value;
    }

    /**
     * get names
     *
     * @return String[]
     */
    public String[] getNames() {
        String[] names = (String[]) keySet().toArray(new String[0]);
        Arrays.sort(names);
        return names;
    }

    /**
     * get int
     *
     * @param name
     * @return int
     */
    public int getInt(String name) {
        return getInt(name, 0);
    }

    /**
     * get int
     *
     * @param name
     * @param defaultValue
     * @return int
     */
    public int getInt(String name, int defaultValue) {
        String value = getString(name, "");
        return "".equals(value) ? defaultValue : Integer.parseInt(value);
    }

    /**
     * get double
     *
     * @param name
     * @return double
     */
    public double getDouble(String name) {
        return getDouble(name, 0);
    }

    /**
     * get double
     *
     * @param name
     * @param defaultValue
     * @return double
     */
    public double getDouble(String name, double defaultValue) {
        String value = getString(name, "");
        return "".equals(value) ? defaultValue : Double.parseDouble(value);
    }

    /**
     * get boolean
     *
     * @param name
     * @return boolean
     */
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    /**
     * get boolean
     *
     * @param name
     * @param defaultValue
     * @return boolean
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        String value = getString(name, "");
        return "".equals(value) ? defaultValue : Boolean.valueOf(value)
                .booleanValue();
    }

    @Override
    public Object put(String key, Object value) {
        return returnMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return returnMap.remove(key);
    }

    @Override
    public void clear() {
        returnMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return returnMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return returnMap.containsValue(value);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return returnMap.entrySet();
    }

    @Override
    public boolean isEmpty() {
        return returnMap.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return returnMap.keySet();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        returnMap.putAll(map);
    }

    @Override
    public int size() {
        return returnMap.size();
    }

    @Override
    public Collection<Object> values() {
        return returnMap.values();
    }

    public String convert2Character(String target, String charset) {
        System.out.println("编码转换之前：" + target);
        try {
            return new String(target.trim().getBytes("ISO-8859-1"), charset);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return target;
        }
    }

    public IMap add(String key, String value) {
        put(key, value);
        return this;
    }

    public static class Builder {
        public static IMap build() {
            return new IMap();
        }
    }
}
