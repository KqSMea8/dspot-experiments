package com.alibaba.json.bvt.parser.creator;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import junit.framework.TestCase;
import org.junit.Assert;


public class JSONCreatorTest_default_float extends TestCase {
    public void test_create() throws Exception {
        JSONCreatorTest_default_float.Model model = JSON.parseObject("{\"name\":\"wenshao\"}", JSONCreatorTest_default_float.Model.class);
        Assert.assertTrue(((model.id) == 0));
        Assert.assertEquals("wenshao", model.name);
    }

    public static class Model {
        private final float id;

        private final String name;

        @JSONCreator
        public Model(@JSONField(name = "id")
        float id, @JSONField(name = "name")
        String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class Value {}
}
