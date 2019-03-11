package com.alibaba.json.bvt.basicType;


import Feature.SupportArrayToBean;
import com.alibaba.fastjson.JSON;
import junit.framework.TestCase;


/**
 * Created by wenshao on 04/08/2017.
 */
public class FloatTest extends TestCase {
    public void test_0() throws Exception {
        String json = "{\"v1\":-0.012671709,\"v2\":0.6042485,\"v3\":0.13231707,\"v4\":0.80090785,\"v5\":0.6192943}";
        String json2 = "{\"v1\":\"-0.012671709\",\"v2\":\"0.6042485\",\"v3\":\"0.13231707\",\"v4\":\"0.80090785\",\"v5\":\"0.6192943\"}";
        FloatTest.Model m1 = JSON.parseObject(json, FloatTest.Model.class);
        FloatTest.Model m2 = JSON.parseObject(json2, FloatTest.Model.class);
        TestCase.assertNotNull(m1);
        TestCase.assertNotNull(m2);
        TestCase.assertEquals((-0.012671709F), m1.v1);
        TestCase.assertEquals(0.6042485F, m1.v2);
        TestCase.assertEquals(0.13231707F, m1.v3);
        TestCase.assertEquals(0.80090785F, m1.v4);
        TestCase.assertEquals(0.6192943F, m1.v5);
        TestCase.assertEquals((-0.012671709F), m2.v1);
        TestCase.assertEquals(0.6042485F, m2.v2);
        TestCase.assertEquals(0.13231707F, m2.v3);
        TestCase.assertEquals(0.80090785F, m2.v4);
        TestCase.assertEquals(0.6192943F, m2.v5);
    }

    public void test_array_mapping() throws Exception {
        String json = "[-0.012671709,0.6042485,0.13231707,0.80090785,0.6192943]";
        String json2 = "[\"-0.012671709\",\"0.6042485\",\"0.13231707\",\"0.80090785\",\"0.6192943\"]";
        FloatTest.Model m1 = JSON.parseObject(json, FloatTest.Model.class, SupportArrayToBean);
        FloatTest.Model m2 = JSON.parseObject(json2, FloatTest.Model.class, SupportArrayToBean);
        TestCase.assertNotNull(m1);
        TestCase.assertNotNull(m2);
        TestCase.assertEquals((-0.012671709F), m1.v1);
        TestCase.assertEquals(0.6042485F, m1.v2);
        TestCase.assertEquals(0.13231707F, m1.v3);
        TestCase.assertEquals(0.80090785F, m1.v4);
        TestCase.assertEquals(0.6192943F, m1.v5);
        TestCase.assertEquals((-0.012671709F), m2.v1);
        TestCase.assertEquals(0.6042485F, m2.v2);
        TestCase.assertEquals(0.13231707F, m2.v3);
        TestCase.assertEquals(0.80090785F, m2.v4);
        TestCase.assertEquals(0.6192943F, m2.v5);
    }

    public static class Model {
        public float v1;

        public float v2;

        public float v3;

        public float v4;

        public float v5;
    }
}
