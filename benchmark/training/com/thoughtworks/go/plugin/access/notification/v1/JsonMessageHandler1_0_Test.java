/**
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thoughtworks.go.plugin.access.notification.v1;


import com.thoughtworks.go.domain.notificationdata.AgentNotificationData;
import com.thoughtworks.go.plugin.api.response.Result;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class JsonMessageHandler1_0_Test {
    private JsonMessageHandler1_0 messageHandler;

    @Test
    public void shouldBuildNotificationsInterestedInFromResponseBody() throws Exception {
        String responseBody = "{notifications=[\"pipeline-status\",\"stage-status\"]}";
        List<String> notificationsInterestedIn = messageHandler.responseMessageForNotificationsInterestedIn(responseBody);
        Assert.assertThat(notificationsInterestedIn, Matchers.is(Arrays.asList("pipeline-status", "stage-status")));
    }

    @Test
    public void shouldValidateIncorrectJsonResponseForNotificationsInterestedIn() {
        Assert.assertThat(errorMessageForNotificationsInterestedIn("{\"notifications\":{}}"), Matchers.is("Unable to de-serialize json response. 'notifications' should be of type list of string"));
        Assert.assertThat(errorMessageForNotificationsInterestedIn("{\"notifications\":[{},{}]}"), Matchers.is("Unable to de-serialize json response. Notification 'name' should be of type string"));
    }

    @Test
    public void shouldBuildSuccessResultFromNotify() throws Exception {
        String responseBody = "{\"status\":\"success\",messages=[\"message-one\",\"message-two\"]}";
        Result result = messageHandler.responseMessageForNotify(responseBody);
        assertSuccessResult(result, Arrays.asList("message-one", "message-two"));
    }

    @Test
    public void shouldBuildFailureResultFromNotify() throws Exception {
        String responseBody = "{\"status\":\"failure\",messages=[\"message-one\",\"message-two\"]}";
        Result result = messageHandler.responseMessageForNotify(responseBody);
        assertFailureResult(result, Arrays.asList("message-one", "message-two"));
    }

    @Test
    public void shouldHandleNullMessagesForNotify() throws Exception {
        assertSuccessResult(messageHandler.responseMessageForNotify("{\"status\":\"success\"}"), new ArrayList());
        assertFailureResult(messageHandler.responseMessageForNotify("{\"status\":\"failure\"}"), new ArrayList());
    }

    @Test
    public void shouldValidateIncorrectJsonForResult() {
        Assert.assertThat(errorMessageForNotifyResult(""), Matchers.is("Unable to de-serialize json response. Empty response body"));
        Assert.assertThat(errorMessageForNotifyResult("[{\"result\":\"success\"}]"), Matchers.is("Unable to de-serialize json response. Notify result should be returned as map, with key represented as string and messages represented as list"));
        Assert.assertThat(errorMessageForNotifyResult("{\"status\":true}"), Matchers.is("Unable to de-serialize json response. Notify result 'status' should be of type string"));
        Assert.assertThat(errorMessageForNotifyResult("{\"result\":true}"), Matchers.is("Unable to de-serialize json response. Notify result 'status' is a required field"));
        Assert.assertThat(errorMessageForNotifyResult("{\"status\":\"success\",\"messages\":{}}"), Matchers.is("Unable to de-serialize json response. Notify result 'messages' should be of type list of string"));
        Assert.assertThat(errorMessageForNotifyResult("{\"status\":\"success\",\"messages\":[{},{}]}"), Matchers.is("Unable to de-serialize json response. Notify result 'message' should be of type string"));
    }

    @Test
    public void shouldConstructTheStageNotificationRequest() throws Exception {
        Pipeline pipeline = createPipeline();
        String gitModifiedTime = new SimpleDateFormat(StageConverter.DATE_PATTERN).format(pipeline.getBuildCause().getMaterialRevisions().getMaterialRevision(0).getLatestModification().getModifiedTime());
        String hgModifiedTime = new SimpleDateFormat(StageConverter.DATE_PATTERN).format(pipeline.getBuildCause().getMaterialRevisions().getMaterialRevision(1).getLatestModification().getModifiedTime());
        String svnModifiedTime = new SimpleDateFormat(StageConverter.DATE_PATTERN).format(pipeline.getBuildCause().getMaterialRevisions().getMaterialRevision(2).getLatestModification().getModifiedTime());
        String tfsModifiedTime = new SimpleDateFormat(StageConverter.DATE_PATTERN).format(pipeline.getBuildCause().getMaterialRevisions().getMaterialRevision(3).getLatestModification().getModifiedTime());
        String p4ModifiedTime = new SimpleDateFormat(StageConverter.DATE_PATTERN).format(pipeline.getBuildCause().getMaterialRevisions().getMaterialRevision(4).getLatestModification().getModifiedTime());
        String dependencyModifiedTime = new SimpleDateFormat(StageConverter.DATE_PATTERN).format(pipeline.getBuildCause().getMaterialRevisions().getMaterialRevision(5).getLatestModification().getModifiedTime());
        String packageMaterialModifiedTime = new SimpleDateFormat(StageConverter.DATE_PATTERN).format(pipeline.getBuildCause().getMaterialRevisions().getMaterialRevision(6).getLatestModification().getModifiedTime());
        String pluggableScmModifiedTime = new SimpleDateFormat(StageConverter.DATE_PATTERN).format(pipeline.getBuildCause().getMaterialRevisions().getMaterialRevision(7).getLatestModification().getModifiedTime());
        String expected = (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("{\n" + (((((((((((((((("\t\"pipeline\": {\n" + "\t\t\"name\": \"pipeline-name\",\n") + "\t\t\"counter\": \"1\",\n") + "\t\t\"group\": \"pipeline-group\",\n") + "\t\t\"build-cause\": [{\n") + "\t\t\t\"material\": {\n") + "\t\t\t\t\"git-configuration\": {\n") + "\t\t\t\t\t\"shallow-clone\": false,\n") + "\t\t\t\t\t\"branch\": \"branch\",\n") + "\t\t\t\t\t\"url\": \"http://user:******@gitrepo.com\"\n") + "\t\t\t\t},\n") + "\t\t\t\t\"type\": \"git\"\n") + "\t\t\t},\n") + "\t\t\t\"changed\": true,\n") + "\t\t\t\"modifications\": [{\n") + "\t\t\t\t\"revision\": \"1\",\n") + "\t\t\t\t\"modified-time\": \"")) + gitModifiedTime) + "\",\n") + "\t\t\t\t\"data\": {}\n") + "\t\t\t}]\n") + "\t\t}, {\n") + "\t\t\t\"material\": {\n") + "\t\t\t\t\"type\": \"mercurial\",\n") + "\t\t\t\t\"mercurial-configuration\": {\n") + "\t\t\t\t\t\"url\": \"http://user:******@hgrepo.com\"\n") + "\t\t\t\t}\n") + "\t\t\t},\n") + "\t\t\t\"changed\": true,\n") + "\t\t\t\"modifications\": [{\n") + "\t\t\t\t\"revision\": \"1\",\n") + "\t\t\t\t\"modified-time\": \"") + hgModifiedTime) + "\",\n") + "\t\t\t\t\"data\": {}\n") + "\t\t\t}]\n") + "\t\t}, {\n") + "\t\t\t\"material\": {\n") + "\t\t\t\t\"svn-configuration\": {\n") + "\t\t\t\t\t\"check-externals\": false,\n") + "\t\t\t\t\t\"url\": \"http://user:******@svnrepo.com\",\n") + "\t\t\t\t\t\"username\": \"username\"\n") + "\t\t\t\t},\n") + "\t\t\t\t\"type\": \"svn\"\n") + "\t\t\t},\n") + "\t\t\t\"changed\": true,\n") + "\t\t\t\"modifications\": [{\n") + "\t\t\t\t\"revision\": \"1\",\n") + "\t\t\t\t\"modified-time\": \"") + svnModifiedTime) + "\",\n") + "\t\t\t\t\"data\": {}\n") + "\t\t\t}]\n") + "\t\t}, {\n") + "\t\t\t\"material\": {\n") + "\t\t\t\t\"type\": \"tfs\",\n") + "\t\t\t\t\"tfs-configuration\": {\n") + "\t\t\t\t\t\"domain\": \"domain\",\n") + "\t\t\t\t\t\"project-path\": \"project-path\",\n") + "\t\t\t\t\t\"url\": \"http://user:******@tfsrepo.com\",\n") + "\t\t\t\t\t\"username\": \"username\"\n") + "\t\t\t\t}\n") + "\t\t\t},\n") + "\t\t\t\"changed\": true,\n") + "\t\t\t\"modifications\": [{\n") + "\t\t\t\t\"revision\": \"1\",\n") + "\t\t\t\t\"modified-time\": \"") + tfsModifiedTime) + "\",\n") + "\t\t\t\t\"data\": {}\n") + "\t\t\t}]\n") + "\t\t}, {\n") + "\t\t\t\"material\": {\n") + "\t\t\t\t\"perforce-configuration\": {\n") + "\t\t\t\t\t\"view\": \"view\",\n") + "\t\t\t\t\t\"use-tickets\": false,\n") + "\t\t\t\t\t\"url\": \"127.0.0.1:1666\",\n") + "\t\t\t\t\t\"username\": \"username\"\n") + "\t\t\t\t},\n") + "\t\t\t\t\"type\": \"perforce\"\n") + "\t\t\t},\n") + "\t\t\t\"changed\": true,\n") + "\t\t\t\"modifications\": [{\n") + "\t\t\t\t\"revision\": \"1\",\n") + "\t\t\t\t\"modified-time\": \"") + p4ModifiedTime) + "\",\n") + "\t\t\t\t\"data\": {}\n") + "\t\t\t}]\n") + "\t\t}, {\n") + "\t\t\t\"material\": {\n") + "\t\t\t\t\"pipeline-configuration\": {\n") + "\t\t\t\t\t\"pipeline-name\": \"pipeline-name\",\n") + "\t\t\t\t\t\"stage-name\": \"stage-name\"\n") + "\t\t\t\t},\n") + "\t\t\t\t\"type\": \"pipeline\"\n") + "\t\t\t},\n") + "\t\t\t\"changed\": true,\n") + "\t\t\t\"modifications\": [{\n") + "\t\t\t\t\"revision\": \"pipeline-name/1/stage-name/1\",\n") + "\t\t\t\t\"modified-time\": \"") + dependencyModifiedTime) + "\",\n") + "\t\t\t\t\"data\": {}\n") + "\t\t\t}]\n") + "\t\t}, {\n") + "\t\t\t\"material\": {\n") + "\t\t\t\t\"plugin-id\": \"pluginid\",\n") + "\t\t\t\t\"package-configuration\": {\n") + "\t\t\t\t\t\"k3\": \"package-v1\"\n") + "\t\t\t\t},\n") + "\t\t\t\t\"repository-configuration\": {\n") + "\t\t\t\t\t\"k1\": \"repo-v1\"\n") + "\t\t\t\t},\n") + "\t\t\t\t\"type\": \"package\"\n") + "\t\t\t},\n") + "\t\t\t\"changed\": true,\n") + "\t\t\t\"modifications\": [{\n") + "\t\t\t\t\"revision\": \"1\",\n") + "\t\t\t\t\"modified-time\": \"") + packageMaterialModifiedTime) + "\",\n") + "\t\t\t\t\"data\": {}\n") + "\t\t\t}]\n") + "\t\t}, {\n") + "\t\t\t\"material\": {\n") + "\t\t\t\t\"plugin-id\": \"pluginid\",\n") + "\t\t\t\t\"scm-configuration\": {\n") + "\t\t\t\t\t\"k1\": \"v1\"\n") + "\t\t\t\t},\n") + "\t\t\t\t\"type\": \"scm\"\n") + "\t\t\t},\n") + "\t\t\t\"changed\": true,\n") + "\t\t\t\"modifications\": [{\n") + "\t\t\t\t\"revision\": \"1\",\n") + "\t\t\t\t\"modified-time\": \"") + pluggableScmModifiedTime) + "\",\n") + "\t\t\t\t\"data\": {}\n") + "\t\t\t}]\n") + "\t\t}],\n") + "\t\t\"stage\": {\n") + "\t\t\t\"name\": \"stage-name\",\n") + "\t\t\t\"counter\": \"1\",\n") + "\t\t\t\"approval-type\": \"success\",\n") + "\t\t\t\"approved-by\": \"changes\",\n") + "\t\t\t\"state\": \"Passed\",\n") + "\t\t\t\"result\": \"Passed\",\n") + "\t\t\t\"create-time\": \"2011-07-13T19:43:37.100Z\",\n") + "\t\t\t\"last-transition-time\": \"2011-07-13T19:43:37.100Z\",\n") + "\t\t\t\"jobs\": [{\n") + "\t\t\t\t\"name\": \"job-name\",\n") + "\t\t\t\t\"schedule-time\": \"2011-07-13T19:43:37.100Z\",\n") + "\t\t\t\t\"complete-time\": \"2011-07-13T19:43:37.100Z\",\n") + "\t\t\t\t\"state\": \"Completed\",\n") + "\t\t\t\t\"result\": \"Passed\",\n") + "\t\t\t\t\"agent-uuid\": \"uuid\"\n") + "\t\t\t}]\n") + "\t\t}\n") + "\t}\n") + "}";
        String request = messageHandler.requestMessageForNotify(new com.thoughtworks.go.domain.notificationdata.StageNotificationData(pipeline.getFirstStage(), pipeline.getBuildCause(), "pipeline-group"));
        assertThatJson(expected).isEqualTo(request);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfAnUnhandledObjectIsPassed() {
        thrown.expectMessage(String.format("Converter for %s not supported", Pipeline.class.getCanonicalName()));
        messageHandler.requestMessageForNotify(new Pipeline());
    }

    @Test
    public void shouldNotHandleAgentNotificationRequest() throws Exception {
        thrown.expect(NotImplementedException.class);
        thrown.expectMessage(String.format("Converter for %s not supported", AgentNotificationData.class.getCanonicalName()));
        messageHandler.requestMessageForNotify(new AgentNotificationData(null, null, false, null, null, null, null, null, null, null));
    }
}
