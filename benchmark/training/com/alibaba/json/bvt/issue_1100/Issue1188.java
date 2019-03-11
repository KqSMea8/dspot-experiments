package com.alibaba.json.bvt.issue_1100;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import java.util.List;
import junit.framework.TestCase;


/**
 * Created by wenshao on 09/05/2017.
 */
public class Issue1188 extends TestCase {
    public void test_for_issue_1188() throws Exception {
        String json = "{\"ids\":\"a1,a2\",\"name\":\"abc\"}";
        Issue1188.Info info = JSON.parseObject(json, Issue1188.Info.class);
        TestCase.assertNull(info.ids);
    }

    public static class Info {
        @JSONField(deserialize = false)
        private List<Integer> ids;

        private String name;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
