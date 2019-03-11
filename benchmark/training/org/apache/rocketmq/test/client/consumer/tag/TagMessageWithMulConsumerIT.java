/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.rocketmq.test.client.consumer.tag;


import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.rocketmq.test.base.BaseConf;
import org.apache.rocketmq.test.client.rmq.RMQNormalConsumer;
import org.apache.rocketmq.test.client.rmq.RMQNormalProducer;
import org.apache.rocketmq.test.factory.MQMessageFactory;
import org.apache.rocketmq.test.factory.TagMessage;
import org.apache.rocketmq.test.listener.rmq.concurrent.RMQNormalListener;
import org.apache.rocketmq.test.util.VerifyUtils;
import org.junit.Assert;
import org.junit.Test;


public class TagMessageWithMulConsumerIT extends BaseConf {
    private static Logger logger = Logger.getLogger(TagMessageWith1ConsumerIT.class);

    private RMQNormalProducer producer = null;

    private String topic = null;

    @Test
    public void testSendTwoTag() {
        String tag1 = "jueyin1";
        String tag2 = "jueyin2";
        int msgSize = 10;
        RMQNormalConsumer consumerTag1 = BaseConf.getConsumer(BaseConf.nsAddr, topic, tag1, new RMQNormalListener());
        RMQNormalConsumer consumerTag2 = BaseConf.getConsumer(BaseConf.nsAddr, topic, tag2, new RMQNormalListener());
        List<Object> tag1Msgs = MQMessageFactory.getRMQMessage(tag1, topic, msgSize);
        producer.send(tag1Msgs);
        Assert.assertEquals("Not all are sent", msgSize, producer.getAllUndupMsgBody().size());
        List<Object> tag2Msgs = MQMessageFactory.getRMQMessage(tag2, topic, msgSize);
        producer.send(tag2Msgs);
        Assert.assertEquals("Not all are sent", (msgSize * 2), producer.getAllUndupMsgBody().size());
        consumerTag1.getListener().waitForMessageConsume(MQMessageFactory.getMessageBody(tag1Msgs), BaseConf.consumeTime);
        consumerTag2.getListener().waitForMessageConsume(MQMessageFactory.getMessageBody(tag2Msgs), BaseConf.consumeTime);
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerTag1.getListener().getAllMsgBody())).containsExactlyElementsIn(MQMessageFactory.getMessageBody(tag1Msgs));
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerTag2.getListener().getAllMsgBody())).containsExactlyElementsIn(MQMessageFactory.getMessageBody(tag2Msgs));
    }

    @Test
    public void testSendMessagesWithTwoTag() {
        String[] tags = new String[]{ "jueyin1", "jueyin2" };
        int msgSize = 10;
        TagMessage tagMessage = new TagMessage(tags, topic, msgSize);
        RMQNormalConsumer consumerTag1 = BaseConf.getConsumer(BaseConf.nsAddr, topic, tags[0], new RMQNormalListener());
        RMQNormalConsumer consumerTag2 = BaseConf.getConsumer(BaseConf.nsAddr, topic, tags[1], new RMQNormalListener());
        List<Object> tagMsgs = tagMessage.getMixedTagMessages();
        producer.send(tagMsgs);
        Assert.assertEquals("Not all are sent", (msgSize * (tags.length)), producer.getAllUndupMsgBody().size());
        consumerTag1.getListener().waitForMessageConsume(tagMessage.getMessageBodyByTag(tags[0]), BaseConf.consumeTime);
        consumerTag2.getListener().waitForMessageConsume(tagMessage.getMessageBodyByTag(tags[1]), BaseConf.consumeTime);
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerTag1.getListener().getAllMsgBody())).containsExactlyElementsIn(tagMessage.getMessageBodyByTag(tags[0]));
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerTag2.getListener().getAllMsgBody())).containsExactlyElementsIn(tagMessage.getMessageBodyByTag(tags[1]));
    }

    @Test
    public void testTwoConsumerOneMatchOneOtherMatchAll() {
        String[] tags = new String[]{ "jueyin1", "jueyin2" };
        String sub1 = String.format("%s||%s", tags[0], tags[1]);
        String sub2 = String.format("%s|| noExist", tags[0]);
        int msgSize = 10;
        TagMessage tagMessage = new TagMessage(tags, topic, msgSize);
        RMQNormalConsumer consumerTag1 = BaseConf.getConsumer(BaseConf.nsAddr, topic, sub1, new RMQNormalListener());
        RMQNormalConsumer consumerTag2 = BaseConf.getConsumer(BaseConf.nsAddr, topic, sub2, new RMQNormalListener());
        List<Object> tagMsgs = tagMessage.getMixedTagMessages();
        producer.send(tagMsgs);
        Assert.assertEquals("Not all are sent", (msgSize * (tags.length)), producer.getAllUndupMsgBody().size());
        consumerTag1.getListener().waitForMessageConsume(tagMessage.getMessageBodyByTag(tags), BaseConf.consumeTime);
        consumerTag2.getListener().waitForMessageConsume(tagMessage.getMessageBodyByTag(tags[0]), BaseConf.consumeTime);
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerTag1.getListener().getAllMsgBody())).containsExactlyElementsIn(tagMessage.getAllTagMessageBody());
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerTag2.getListener().getAllMsgBody())).containsExactlyElementsIn(tagMessage.getMessageBodyByTag(tags[0]));
    }

    @Test
    public void testSubKindsOf() {
        String[] tags = new String[]{ "jueyin1", "jueyin2" };
        String sub1 = String.format("%s||%s", tags[0], tags[1]);
        String sub2 = String.format("%s|| noExist", tags[0]);
        String sub3 = tags[0];
        String sub4 = "*";
        int msgSize = 10;
        RMQNormalConsumer consumerSubTwoMatchAll = BaseConf.getConsumer(BaseConf.nsAddr, topic, sub1, new RMQNormalListener());
        RMQNormalConsumer consumerSubTwoMachieOne = BaseConf.getConsumer(BaseConf.nsAddr, topic, sub2, new RMQNormalListener());
        RMQNormalConsumer consumerSubTag1 = BaseConf.getConsumer(BaseConf.nsAddr, topic, sub3, new RMQNormalListener());
        RMQNormalConsumer consumerSubAll = BaseConf.getConsumer(BaseConf.nsAddr, topic, sub4, new RMQNormalListener());
        producer.send(msgSize);
        Assert.assertEquals("Not all are sent", msgSize, producer.getAllUndupMsgBody().size());
        Collection<Object> msgsWithNoTag = producer.getMsgBodysCopy();
        TagMessage tagMessage = new TagMessage(tags, topic, msgSize);
        List<Object> tagMsgs = tagMessage.getMixedTagMessages();
        producer.send(tagMsgs);
        Assert.assertEquals("Not all are sent", (msgSize * 3), producer.getAllUndupMsgBody().size());
        consumerSubTwoMatchAll.getListener().waitForMessageConsume(tagMessage.getMessageBodyByTag(tags), BaseConf.consumeTime);
        consumerSubTwoMachieOne.getListener().waitForMessageConsume(tagMessage.getMessageBodyByTag(tags[0]), BaseConf.consumeTime);
        consumerSubTag1.getListener().waitForMessageConsume(tagMessage.getMessageBodyByTag(tags[0]), BaseConf.consumeTime);
        consumerSubAll.getListener().waitForMessageConsume(MQMessageFactory.getMessage(msgsWithNoTag, tagMessage.getAllTagMessageBody()), BaseConf.consumeTime);
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerSubTwoMatchAll.getListener().getAllMsgBody())).containsExactlyElementsIn(tagMessage.getAllTagMessageBody());
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerSubTwoMachieOne.getListener().getAllMsgBody())).containsExactlyElementsIn(tagMessage.getMessageBodyByTag(tags[0]));
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerSubTag1.getListener().getAllMsgBody())).containsExactlyElementsIn(tagMessage.getMessageBodyByTag(tags[0]));
        assertThat(VerifyUtils.getFilterdMessage(producer.getAllMsgBody(), consumerSubAll.getListener().getAllMsgBody())).containsExactlyElementsIn(MQMessageFactory.getMessage(msgsWithNoTag, tagMessage.getAllTagMessageBody()));
    }
}
