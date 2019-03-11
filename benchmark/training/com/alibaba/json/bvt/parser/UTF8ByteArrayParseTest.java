package com.alibaba.json.bvt.parser;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import junit.framework.TestCase;


public class UTF8ByteArrayParseTest extends TestCase {
    public void test_utf8() throws Exception {
        byte[] bytes = "{\'name\':\'xxx\', age:3, birthday:\"2009-09-05\"}".getBytes("UTF-8");
        JSON.parseObject(bytes, JSONObject.class);
    }
}
