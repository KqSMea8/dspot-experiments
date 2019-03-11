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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.yarn.server.nodemanager.containermanager.linux.runtime.docker;


import org.apache.hadoop.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the docker images command and its command
 * line arguments.
 */
public class TestDockerImagesCommand {
    private DockerImagesCommand dockerImagesCommand;

    private static final String IMAGE_NAME = "foo";

    @Test
    public void testGetCommandOption() {
        Assert.assertEquals("images", dockerImagesCommand.getCommandOption());
    }

    @Test
    public void testAllImages() {
        Assert.assertEquals("images", StringUtils.join(",", dockerImagesCommand.getDockerCommandWithArguments().get("docker-command")));
        Assert.assertEquals(1, dockerImagesCommand.getDockerCommandWithArguments().size());
    }

    @Test
    public void testSingleImage() {
        dockerImagesCommand = dockerImagesCommand.getSingleImageStatus(TestDockerImagesCommand.IMAGE_NAME);
        Assert.assertEquals("images", StringUtils.join(",", dockerImagesCommand.getDockerCommandWithArguments().get("docker-command")));
        Assert.assertEquals("image name", "foo", StringUtils.join(",", dockerImagesCommand.getDockerCommandWithArguments().get("image")));
        Assert.assertEquals(2, dockerImagesCommand.getDockerCommandWithArguments().size());
    }
}
