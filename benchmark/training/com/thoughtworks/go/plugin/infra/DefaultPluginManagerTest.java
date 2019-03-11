/**
 * Copyright 2018 ThoughtWorks, Inc.
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
package com.thoughtworks.go.plugin.infra;


import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.infra.commons.PluginUploadResponse;
import com.thoughtworks.go.plugin.infra.listeners.DefaultPluginJarChangeListener;
import com.thoughtworks.go.plugin.infra.monitor.DefaultPluginJarLocationMonitor;
import com.thoughtworks.go.plugin.infra.plugininfo.DefaultPluginRegistry;
import com.thoughtworks.go.plugin.infra.plugininfo.GoPluginDescriptor;
import com.thoughtworks.go.util.SystemEnvironment;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;


public class DefaultPluginManagerTest {
    private File nonJarFile;

    private File newJarFile;

    @Mock
    private DefaultPluginJarLocationMonitor monitor;

    @Mock
    private DefaultPluginRegistry registry;

    @Mock
    private GoPluginOSGiFramework goPluginOSGiFramework;

    @Mock
    private DefaultPluginJarChangeListener jarChangeListener;

    @Mock
    private SystemEnvironment systemEnvironment;

    @Mock
    private PluginRequestProcessorRegistry pluginRequestProcessorRegistry;

    @Mock
    private PluginWriter pluginWriter;

    @Mock
    private PluginValidator pluginValidator;

    private File bundleDir;

    private File pluginExternalDir;

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldProceedToPluginWriterWithValidJarFile() throws Exception {
        DefaultPluginManager defaultPluginManager = new DefaultPluginManager(monitor, registry, goPluginOSGiFramework, jarChangeListener, null, pluginWriter, pluginValidator, systemEnvironment);
        Mockito.when(pluginValidator.namecheckForJar(newJarFile.getName())).thenReturn(true);
        defaultPluginManager.addPlugin(newJarFile, newJarFile.getName());
        ArgumentCaptor<File> fileArgumentCaptor = ArgumentCaptor.forClass(File.class);
        ArgumentCaptor<String> filenameArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(pluginWriter).addPlugin(fileArgumentCaptor.capture(), filenameArgumentCaptor.capture());
        Assert.assertThat(fileArgumentCaptor.getValue(), is(newJarFile));
        Assert.assertThat(filenameArgumentCaptor.getValue(), is(newJarFile.getName()));
    }

    @Test
    public void shouldReturnTheResponseReturnedByPluginWriterWithValidJarFile() throws Exception {
        newJarFile.createNewFile();
        DefaultPluginManager defaultPluginManager = new DefaultPluginManager(monitor, registry, goPluginOSGiFramework, jarChangeListener, null, pluginWriter, pluginValidator, systemEnvironment);
        Mockito.when(pluginValidator.namecheckForJar(newJarFile.getName())).thenReturn(true);
        PluginUploadResponse expectedResponse = PluginUploadResponse.create(true, "successful!", null);
        Mockito.when(pluginWriter.addPlugin(newJarFile, newJarFile.getName())).thenReturn(expectedResponse);
        PluginUploadResponse response = defaultPluginManager.addPlugin(newJarFile, newJarFile.getName());
        Assert.assertThat(response, is(expectedResponse));
    }

    @Test
    public void shouldReturnResponseWithErrorsWithInvalidJarFile() throws Exception {
        DefaultPluginManager defaultPluginManager = new DefaultPluginManager(monitor, registry, goPluginOSGiFramework, jarChangeListener, null, pluginWriter, pluginValidator, systemEnvironment);
        Mockito.when(pluginValidator.namecheckForJar(nonJarFile.getName())).thenReturn(false);
        PluginUploadResponse response = defaultPluginManager.addPlugin(nonJarFile, "not a jar");
        Assert.assertThat(response.success(), isEmptyString());
        Assert.assertFalse(response.isSuccess());
        Assert.assertTrue(response.errors().containsKey(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
        Assert.assertThat(response.errors().get(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE), is("Please upload a jar."));
    }

    @Test
    public void shouldCleanTheBundleDirectoryAtStart() throws Exception {
        String pluginJarFile = "descriptor-aware-test-plugin.should.be.deleted.jar";
        copyPluginToTheDirectory(bundleDir, pluginJarFile);
        startInfrastructure(true);
        Assert.assertThat(bundleDir.exists(), is(false));
    }

    @Test
    public void shouldStartOSGiFrameworkBeforeStartingMonitor() throws Exception {
        startInfrastructure(true);
        InOrder inOrder = Mockito.inOrder(goPluginOSGiFramework, monitor);
        inOrder.verify(goPluginOSGiFramework).start();
        inOrder.verify(monitor).start();
    }

    @Test
    public void shouldAllowRegistrationOfPluginChangeListeners() throws Exception {
        DefaultPluginManagerTest.GoPlugginOSGiFrameworkStub frameworkStub = new DefaultPluginManagerTest.GoPlugginOSGiFrameworkStub();
        PluginManager pluginManager = new DefaultPluginManager(monitor, registry, frameworkStub, jarChangeListener, null, pluginWriter, pluginValidator, systemEnvironment);
        String pluginId1 = "test-plugin-id-1";
        String pluginId2 = "test-plugin-id-2";
        GoPluginDescriptor descriptor1 = Mockito.mock(GoPluginDescriptor.class);
        Mockito.when(descriptor1.id()).thenReturn(pluginId1);
        GoPluginDescriptor descriptor2 = Mockito.mock(GoPluginDescriptor.class);
        Mockito.when(descriptor2.id()).thenReturn(pluginId2);
        final int[] pluginLoaded = new int[]{ 0 };
        final int[] pluginUnloaded = new int[]{ 0 };
        PluginChangeListener someInterfaceListener = new PluginChangeListener() {
            @Override
            public void pluginLoaded(GoPluginDescriptor pluginDescriptor) {
                (pluginLoaded[0])++;
            }

            @Override
            public void pluginUnLoaded(GoPluginDescriptor pluginDescriptor) {
                (pluginUnloaded[0])++;
            }
        };
        pluginManager.addPluginChangeListener(someInterfaceListener);
        frameworkStub.pluginChangeListener.pluginLoaded(descriptor1);
        frameworkStub.pluginChangeListener.pluginLoaded(descriptor2);
        frameworkStub.pluginChangeListener.pluginUnLoaded(descriptor1);
        frameworkStub.pluginChangeListener.pluginUnLoaded(descriptor2);
        Assert.assertThat(pluginLoaded[0], is(2));
        Assert.assertThat(pluginUnloaded[0], is(2));
    }

    @Test
    public void shouldGetPluginDescriptorForGivenPluginIdCorrectly() throws Exception {
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, goPluginOSGiFramework, jarChangeListener, null, pluginWriter, pluginValidator, systemEnvironment);
        GoPluginDescriptor pluginDescriptorForP1 = new GoPluginDescriptor("p1", "1.0", null, null, null, true);
        Mockito.when(registry.getPlugin("valid-plugin")).thenReturn(pluginDescriptorForP1);
        Mockito.when(registry.getPlugin("invalid-plugin")).thenReturn(null);
        MatcherAssert.assertThat(pluginManager.getPluginDescriptorFor("valid-plugin"), is(pluginDescriptorForP1));
        MatcherAssert.assertThat(pluginManager.getPluginDescriptorFor("invalid-plugin"), is(nullValue()));
    }

    @Test
    public void shouldSubmitPluginApiRequestToGivenPlugin() throws Exception {
        String extensionType = "sample-extension";
        GoPluginApiRequest request = Mockito.mock(GoPluginApiRequest.class);
        GoPluginApiResponse expectedResponse = Mockito.mock(GoPluginApiResponse.class);
        final GoPlugin goPlugin = Mockito.mock(GoPlugin.class);
        final GoPluginDescriptor descriptor = Mockito.mock(GoPluginDescriptor.class);
        Mockito.when(goPlugin.handle(request)).thenReturn(expectedResponse);
        ArgumentCaptor<PluginAwareDefaultGoApplicationAccessor> captor = ArgumentCaptor.forClass(PluginAwareDefaultGoApplicationAccessor.class);
        Mockito.doNothing().when(goPlugin).initializeGoApplicationAccessor(captor.capture());
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ActionWithReturn<GoPlugin, GoPluginApiResponse> action = ((ActionWithReturn<GoPlugin, GoPluginApiResponse>) (invocationOnMock.getArguments()[3]));
                return action.execute(goPlugin, descriptor);
            }
        }).when(goPluginOSGiFramework).doOn(ArgumentMatchers.eq(GoPlugin.class), ArgumentMatchers.eq("plugin-id"), ArgumentMatchers.eq(extensionType), ArgumentMatchers.any(ActionWithReturn.class));
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, goPluginOSGiFramework, jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        GoPluginApiResponse actualResponse = pluginManager.submitTo("plugin-id", extensionType, request);
        Assert.assertThat(actualResponse, is(expectedResponse));
        PluginAwareDefaultGoApplicationAccessor accessor = captor.getValue();
        Assert.assertThat(accessor.pluginDescriptor(), is(descriptor));
    }

    @Test
    public void shouldSayPluginIsOfGivenExtensionTypeWhenReferenceIsFound() throws Exception {
        String pluginId = "plugin-id";
        String extensionType = "sample-extension";
        GoPluginIdentifier pluginIdentifier = new GoPluginIdentifier(extensionType, Arrays.asList("1.0"));
        final GoPlugin goPlugin = Mockito.mock(GoPlugin.class);
        final GoPluginDescriptor descriptor = Mockito.mock(GoPluginDescriptor.class);
        Mockito.when(goPluginOSGiFramework.hasReferenceFor(GoPlugin.class, pluginId, extensionType)).thenReturn(true);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ActionWithReturn<GoPlugin, GoPluginApiResponse> action = ((ActionWithReturn<GoPlugin, GoPluginApiResponse>) (invocationOnMock.getArguments()[2]));
                return action.execute(goPlugin, descriptor);
            }
        }).when(goPluginOSGiFramework).doOn(ArgumentMatchers.eq(GoPlugin.class), ArgumentMatchers.eq(pluginId), ArgumentMatchers.eq(extensionType), ArgumentMatchers.any(ActionWithReturn.class));
        Mockito.when(goPlugin.pluginIdentifier()).thenReturn(pluginIdentifier);
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, goPluginOSGiFramework, jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        Assert.assertTrue(pluginManager.isPluginOfType(extensionType, pluginId));
    }

    @Test
    public void shouldSayAPluginIsNotOfAnExtensionTypeWhenReferenceIsNotFound() throws Exception {
        final String pluginThatDoesNotImplement = "plugin-that-does-not-implement";
        String extensionType = "extension-type";
        Mockito.when(goPluginOSGiFramework.hasReferenceFor(GoPlugin.class, pluginThatDoesNotImplement, extensionType)).thenReturn(false);
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, goPluginOSGiFramework, jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        boolean pluginIsOfExtensionType = pluginManager.isPluginOfType(extensionType, pluginThatDoesNotImplement);
        Assert.assertFalse(pluginIsOfExtensionType);
        Mockito.verify(goPluginOSGiFramework).hasReferenceFor(GoPlugin.class, pluginThatDoesNotImplement, extensionType);
        Mockito.verify(goPluginOSGiFramework, Mockito.never()).doOn(ArgumentMatchers.eq(GoPlugin.class), ArgumentMatchers.eq(pluginThatDoesNotImplement), ArgumentMatchers.eq(extensionType), ArgumentMatchers.any(ActionWithReturn.class));
    }

    @Test
    public void shouldResolveToCorrectExtensionVersion() throws Exception {
        String pluginId = "plugin-id";
        String extensionType = "sample-extension";
        GoPlugin goPlugin = Mockito.mock(GoPlugin.class);
        DefaultPluginManagerTest.GoPlugginOSGiFrameworkStub osGiFrameworkStub = new DefaultPluginManagerTest.GoPlugginOSGiFrameworkStub(goPlugin);
        osGiFrameworkStub.addHasReferenceFor(GoPlugin.class, pluginId, extensionType, true);
        Mockito.when(goPlugin.pluginIdentifier()).thenReturn(new GoPluginIdentifier(extensionType, Arrays.asList("1.0", "2.0")));
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, osGiFrameworkStub, jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        Assert.assertThat(pluginManager.resolveExtensionVersion(pluginId, extensionType, Arrays.asList("1.0", "2.0", "3.0")), is("2.0"));
    }

    @Test
    public void shouldThrowExceptionIfMatchingExtensionVersionNotFound() throws Exception {
        String pluginId = "plugin-id";
        String extensionType = "sample-extension";
        GoPlugin goPlugin = Mockito.mock(GoPlugin.class);
        DefaultPluginManagerTest.GoPlugginOSGiFrameworkStub osGiFrameworkStub = new DefaultPluginManagerTest.GoPlugginOSGiFrameworkStub(goPlugin);
        osGiFrameworkStub.addHasReferenceFor(GoPlugin.class, pluginId, extensionType, true);
        Mockito.when(goPlugin.pluginIdentifier()).thenReturn(new GoPluginIdentifier(extensionType, Arrays.asList("1.0", "2.0")));
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, osGiFrameworkStub, jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        try {
            pluginManager.resolveExtensionVersion(pluginId, extensionType, Arrays.asList("3.0", "4.0"));
            Assert.fail("should have thrown exception for not finding matching extension version");
        } catch (Exception e) {
            Assert.assertThat(e.getMessage(), is("Could not find matching extension version between Plugin[plugin-id] and Go"));
        }
    }

    @Test
    public void shouldAddPluginChangeListener() throws Exception {
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, Mockito.mock(GoPluginOSGiFramework.class), jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        pluginManager.startInfrastructure(true);
        InOrder inOrder = Mockito.inOrder(monitor);
        inOrder.verify(monitor).addPluginJarChangeListener(jarChangeListener);
    }

    @Test
    public void isPluginLoaded_shouldReturnTrueWhenPluginIsLoaded() {
        final GoPluginDescriptor dockerPluginDescriptor = Mockito.mock(GoPluginDescriptor.class);
        Mockito.when(dockerPluginDescriptor.isInvalid()).thenReturn(false);
        Mockito.when(registry.getPlugin("cd.go.elastic-agent.docker")).thenReturn(dockerPluginDescriptor);
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, Mockito.mock(GoPluginOSGiFramework.class), jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        Assert.assertTrue(pluginManager.isPluginLoaded("cd.go.elastic-agent.docker"));
    }

    @Test
    public void isPluginLoaded_shouldReturnFalseWhenPluginIsLoadedButIsInInvalidState() {
        final GoPluginDescriptor dockerPluginDescriptor = Mockito.mock(GoPluginDescriptor.class);
        Mockito.when(dockerPluginDescriptor.isInvalid()).thenReturn(true);
        Mockito.when(registry.getPlugin("cd.go.elastic-agent.docker")).thenReturn(dockerPluginDescriptor);
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, Mockito.mock(GoPluginOSGiFramework.class), jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        Assert.assertFalse(pluginManager.isPluginLoaded("cd.go.elastic-agent.docker"));
    }

    @Test
    public void isPluginLoaded_shouldReturnFalseWhenPluginIsNotLoaded() {
        Mockito.when(registry.getPlugin("cd.go.elastic-agent.docker")).thenReturn(null);
        DefaultPluginManager pluginManager = new DefaultPluginManager(monitor, registry, Mockito.mock(GoPluginOSGiFramework.class), jarChangeListener, pluginRequestProcessorRegistry, pluginWriter, pluginValidator, systemEnvironment);
        Assert.assertFalse(pluginManager.isPluginLoaded("cd.go.elastic-agent.docker"));
    }

    private static interface SomeInterface {}

    private static interface SomeOtherInterface {}

    private static class GoPlugginOSGiFrameworkStub implements GoPluginOSGiFramework {
        public PluginChangeListener pluginChangeListener;

        private GoPluginOSGiFramework goPluginOSGiFramework = Mockito.mock(GoPluginOSGiFramework.class);

        private Object serviceReferenceInstance;

        GoPlugginOSGiFrameworkStub() {
        }

        GoPlugginOSGiFrameworkStub(Object serviceReferenceInstance) {
            this.serviceReferenceInstance = serviceReferenceInstance;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public Bundle loadPlugin(GoPluginDescriptor pluginDescriptor) {
            return null;
        }

        @Override
        public void unloadPlugin(GoPluginDescriptor pluginDescriptor) {
        }

        @Override
        public void addPluginChangeListener(PluginChangeListener pluginChangeListener) {
            this.pluginChangeListener = pluginChangeListener;
        }

        @Override
        public void setPluginExtensionsAndVersionValidator(PluginExtensionsAndVersionValidator pluginExtensionsAndVersionValidator) {
        }

        @Override
        public <T, R> R doOn(Class<T> serviceReferenceClass, String pluginId, String extensionType, ActionWithReturn<T, R> action) {
            return action.execute(((T) (serviceReferenceInstance)), Mockito.mock(GoPluginDescriptor.class));
        }

        @Override
        public <T> boolean hasReferenceFor(Class<T> serviceReferenceClass, String pluginId, String extensionType) {
            return goPluginOSGiFramework.hasReferenceFor(serviceReferenceClass, pluginId, extensionType);
        }

        @Override
        public <T extends GoPlugin> Map<String, List<String>> getExtensionsInfoFromThePlugin(String pluginId) {
            return null;
        }

        public void addHasReferenceFor(Class<?> serviceRef, String pluginId, String extensionType, boolean hasReference) {
            Mockito.when(goPluginOSGiFramework.hasReferenceFor(serviceRef, pluginId, extensionType)).thenReturn(hasReference);
        }
    }
}
