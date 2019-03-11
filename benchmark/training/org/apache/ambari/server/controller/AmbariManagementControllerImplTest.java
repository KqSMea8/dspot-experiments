/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.controller;


import MaintenanceState.IMPLIED_FROM_SERVICE;
import MaintenanceState.OFF;
import PropertyInfo.PropertyType.NOT_MANAGED_HDFS_PATH;
import SecurityType.KERBEROS;
import SecurityType.NONE;
import ServiceOsSpecific.Package;
import State.INSTALLED;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provider;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.persistence.RollbackException;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.ClusterNotFoundException;
import org.apache.ambari.server.HostNotFoundException;
import org.apache.ambari.server.ParentObjectNotFoundException;
import org.apache.ambari.server.ServiceComponentHostNotFoundException;
import org.apache.ambari.server.ServiceComponentNotFoundException;
import org.apache.ambari.server.ServiceNotFoundException;
import org.apache.ambari.server.actionmanager.ActionDBAccessorImpl;
import org.apache.ambari.server.actionmanager.ActionManager;
import org.apache.ambari.server.agent.stomp.AgentConfigsHolder;
import org.apache.ambari.server.agent.stomp.MetadataHolder;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
import org.apache.ambari.server.configuration.Configuration;
import org.apache.ambari.server.controller.internal.RequestStageContainer;
import org.apache.ambari.server.mpack.MpackManagerFactory;
import org.apache.ambari.server.orm.dao.RepositoryVersionDAO;
import org.apache.ambari.server.orm.entities.RepositoryVersionEntity;
import org.apache.ambari.server.security.authorization.Users;
import org.apache.ambari.server.security.encryption.CredentialStoreService;
import org.apache.ambari.server.security.encryption.CredentialStoreType;
import org.apache.ambari.server.security.ldap.AmbariLdapDataPopulator;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.ComponentInfo;
import org.apache.ambari.server.state.ConfigHelper;
import org.apache.ambari.server.state.DesiredConfig;
import org.apache.ambari.server.state.Host;
import org.apache.ambari.server.state.Module;
import org.apache.ambari.server.state.Mpack;
import org.apache.ambari.server.state.PropertyInfo;
import org.apache.ambari.server.state.RepositoryInfo;
import org.apache.ambari.server.state.SecurityType;
import org.apache.ambari.server.state.Service;
import org.apache.ambari.server.state.ServiceComponent;
import org.apache.ambari.server.state.ServiceComponentHost;
import org.apache.ambari.server.state.ServiceInfo;
import org.apache.ambari.server.state.ServiceOsSpecific;
import org.apache.ambari.server.state.StackId;
import org.apache.ambari.server.state.StackInfo;
import org.apache.ambari.server.state.com.google.inject.Module;
import org.apache.ambari.server.state.stack.OsFamily;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;


/**
 * AmbariManagementControllerImpl unit tests
 */
public class AmbariManagementControllerImplTest {
    // Mocks
    private static final AmbariLdapDataPopulator ldapDataPopulator = createMock(AmbariLdapDataPopulator.class);

    private static final Clusters clusters = createNiceMock(Clusters.class);

    private static final ActionDBAccessorImpl actionDBAccessor = createNiceMock(ActionDBAccessorImpl.class);

    private static final AmbariMetaInfo ambariMetaInfo = createMock(AmbariMetaInfo.class);

    private static final Users users = createMock(Users.class);

    private static final AmbariSessionManager sessionManager = createNiceMock(AmbariSessionManager.class);

    @Test
    public void testgetAmbariServerURI() throws Exception {
        // create mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        // set expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, createNiceMock(KerberosHelper.class));
        // replay
        replay(injector);
        AmbariManagementControllerImpl controller = new AmbariManagementControllerImpl(null, null, injector);
        class AmbariConfigsSetter {
            public void setConfigs(AmbariManagementController controller, String masterProtocol, String masterHostname, Integer masterPort) throws Exception {
                // masterProtocol
                Class<?> c = controller.getClass();
                Field f = c.getDeclaredField("masterProtocol");
                f.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, ((f.getModifiers()) & (~(Modifier.FINAL))));
                f.set(controller, masterProtocol);
                // masterHostname
                f = c.getDeclaredField("masterHostname");
                f.setAccessible(true);
                modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, ((f.getModifiers()) & (~(Modifier.FINAL))));
                f.set(controller, masterHostname);
                // masterPort
                f = c.getDeclaredField("masterPort");
                f.setAccessible(true);
                modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, ((f.getModifiers()) & (~(Modifier.FINAL))));
                f.set(controller, masterPort);
            }
        }
        AmbariConfigsSetter ambariConfigsSetter = new AmbariConfigsSetter();
        ambariConfigsSetter.setConfigs(controller, "http", "hostname", 8080);
        Assert.assertEquals("http://hostname:8080/jdk_path", controller.getAmbariServerURI("/jdk_path"));
        ambariConfigsSetter.setConfigs(controller, "https", "somesecuredhost", 8443);
        Assert.assertEquals("https://somesecuredhost:8443/mysql_path", controller.getAmbariServerURI("/mysql_path"));
        ambariConfigsSetter.setConfigs(controller, "https", "othersecuredhost", 8443);
        Assert.assertEquals("https://othersecuredhost:8443/oracle/ojdbc/", controller.getAmbariServerURI("/oracle/ojdbc/"));
        ambariConfigsSetter.setConfigs(controller, "http", "hostname", 8080);
        Assert.assertEquals("http://hostname:8080/jdk_path?query", controller.getAmbariServerURI("/jdk_path?query"));
        verify(injector);
    }

    @Test
    public void testGetClusters() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        ClusterRequest request1 = new ClusterRequest(null, "cluster1", "1", Collections.emptySet());
        Cluster cluster = createNiceMock(Cluster.class);
        ClusterResponse response = createNiceMock(ClusterResponse.class);
        Set<ClusterRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, createNiceMock(KerberosHelper.class));
        // getCluster
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster);
        expect(cluster.convertToResponse()).andReturn(response);
        CredentialStoreService credentialStoreService = createNiceMock(CredentialStoreService.class);
        expect(credentialStoreService.isInitialized(anyObject(CredentialStoreType.class))).andReturn(true).anyTimes();
        // replay mocks
        replay(injector, AmbariManagementControllerImplTest.clusters, cluster, response, credentialStoreService);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        Field f = controller.getClass().getDeclaredField("credentialStoreService");
        f.setAccessible(true);
        f.set(controller, credentialStoreService);
        Set<ClusterResponse> setResponses = controller.getClusters(setRequests);
        // assert and verify
        Assert.assertEquals(1, setResponses.size());
        Assert.assertTrue(setResponses.contains(response));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, response, credentialStoreService);
    }

    @Test
    public void testGetClientHostForRunningAction_componentIsNull() throws Exception {
        Injector injector = createNiceMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component = null;
        replay(cluster, service, injector);
        AmbariManagementControllerImpl controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        String host = controller.getClientHostForRunningAction(cluster, service, component);
        Assert.assertNull(host);
        verify(cluster, service, injector);
    }

    @Test
    public void testGetClientHostForRunningAction_componentMapIsEmpty() throws Exception {
        Injector injector = createNiceMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        Map<String, ServiceComponentHost> hostMap = new HashMap<>();
        expect(component.getServiceComponentHosts()).andReturn(hostMap);
        replay(cluster, service, component, injector);
        AmbariManagementControllerImpl controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        String host = controller.getClientHostForRunningAction(cluster, service, component);
        verify(cluster, service, component, injector);
        Assert.assertNull(host);
    }

    @Test
    public void testGetClientHostForRunningAction_returnsHelathyHost() throws Exception {
        Injector injector = createNiceMock(Injector.class);
        ActionManager actionManager = createNiceMock(ActionManager.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        Map<String, ServiceComponentHost> hostMap = createNiceMock(Map.class);
        Set<String> hostsSet = createNiceMock(Set.class);
        expect(hostMap.isEmpty()).andReturn(false);
        expect(hostMap.keySet()).andReturn(hostsSet);
        expect(component.getServiceComponentHosts()).andReturn(hostMap).times(2);
        replay(cluster, service, component, injector, actionManager, hostMap, hostsSet);
        AmbariManagementControllerImpl controller = createMockBuilder(AmbariManagementControllerImpl.class).addMockedMethod("filterHostsForAction").addMockedMethod("getHealthyHost").withConstructor(actionManager, AmbariManagementControllerImplTest.clusters, injector).createMock();
        expect(controller.getHealthyHost(hostsSet)).andReturn("healthy_host");
        controller.filterHostsForAction(hostsSet, service, cluster, Resource.Type.Cluster);
        expectLastCall().once();
        replay(controller);
        String host = controller.getClientHostForRunningAction(cluster, service, component);
        Assert.assertEquals("healthy_host", host);
        verify(controller, cluster, service, component, injector, hostMap);
    }

    @Test
    public void testGetClientHostForRunningAction_clientComponent() throws Exception {
        Injector injector = createNiceMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        StackId stackId = createNiceMock(StackId.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        expect(service.getName()).andReturn("service");
        expect(service.getServiceComponent("component")).andReturn(component);
        expect(service.getDesiredStackId()).andReturn(stackId);
        expect(stackId.getStackName()).andReturn("stack");
        expect(stackId.getStackVersion()).andReturn("1.0");
        ServiceInfo serviceInfo = createNiceMock(ServiceInfo.class);
        ComponentInfo compInfo = createNiceMock(ComponentInfo.class);
        expect(serviceInfo.getClientComponent()).andReturn(compInfo);
        expect(compInfo.getName()).andReturn("component");
        expect(component.getServiceComponentHosts()).andReturn(Collections.singletonMap("host", null));
        expect(AmbariManagementControllerImplTest.ambariMetaInfo.getService("stack", "1.0", "service")).andReturn(serviceInfo);
        replay(injector, cluster, service, component, serviceInfo, compInfo, AmbariManagementControllerImplTest.ambariMetaInfo, stackId);
        AmbariManagementControllerImpl controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        ServiceComponent resultComponent = controller.getClientComponentForRunningAction(cluster, service);
        Assert.assertNotNull(resultComponent);
        Assert.assertEquals(component, resultComponent);
        verify(injector, cluster, service, component, serviceInfo, compInfo, AmbariManagementControllerImplTest.ambariMetaInfo, stackId);
    }

    @Test
    public void testGetClientHostForRunningAction_clientComponentThrowsException() throws Exception {
        Injector injector = createNiceMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        StackId stackId = createNiceMock(StackId.class);
        ServiceComponent component1 = createNiceMock(ServiceComponent.class);
        ServiceComponent component2 = createNiceMock(ServiceComponent.class);
        expect(service.getName()).andReturn("service");
        expect(service.getServiceComponent("component")).andThrow(new ServiceComponentNotFoundException("cluster", "service", "component"));
        expect(service.getDesiredStackId()).andReturn(stackId);
        expect(stackId.getStackName()).andReturn("stack");
        expect(stackId.getStackVersion()).andReturn("1.0");
        Map<String, ServiceComponent> componentsMap = new HashMap<>();
        componentsMap.put("component1", component1);
        componentsMap.put("component2", component2);
        expect(service.getServiceComponents()).andReturn(componentsMap);
        expect(component1.getServiceComponentHosts()).andReturn(Collections.emptyMap());
        expect(component2.getServiceComponentHosts()).andReturn(Collections.singletonMap("anyHost", null));
        ServiceInfo serviceInfo = createNiceMock(ServiceInfo.class);
        ComponentInfo compInfo = createNiceMock(ComponentInfo.class);
        expect(serviceInfo.getClientComponent()).andReturn(compInfo);
        expect(compInfo.getName()).andReturn("component");
        expect(AmbariManagementControllerImplTest.ambariMetaInfo.getService("stack", "1.0", "service")).andReturn(serviceInfo);
        replay(injector, cluster, service, component1, component2, serviceInfo, compInfo, AmbariManagementControllerImplTest.ambariMetaInfo, stackId);
        AmbariManagementControllerImpl controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        ServiceComponent resultComponent = controller.getClientComponentForRunningAction(cluster, service);
        Assert.assertNotNull(resultComponent);
        Assert.assertEquals(component2, resultComponent);
        verify(injector, cluster, service, component1, component2, serviceInfo, compInfo, AmbariManagementControllerImplTest.ambariMetaInfo, stackId);
    }

    @Test
    public void testGetClientHostForRunningAction_noClientComponent() throws Exception {
        Injector injector = createNiceMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        StackId stackId = createNiceMock(StackId.class);
        ServiceComponent component1 = createNiceMock(ServiceComponent.class);
        ServiceComponent component2 = createNiceMock(ServiceComponent.class);
        expect(service.getName()).andReturn("service");
        expect(service.getDesiredStackId()).andReturn(stackId);
        expect(stackId.getStackName()).andReturn("stack");
        expect(stackId.getStackVersion()).andReturn("1.0");
        Map<String, ServiceComponent> componentsMap = new HashMap<>();
        componentsMap.put("component1", component1);
        componentsMap.put("component2", component2);
        expect(service.getServiceComponents()).andReturn(componentsMap);
        expect(component1.getServiceComponentHosts()).andReturn(Collections.emptyMap());
        expect(component2.getServiceComponentHosts()).andReturn(Collections.singletonMap("anyHost", null));
        ServiceInfo serviceInfo = createNiceMock(ServiceInfo.class);
        expect(serviceInfo.getClientComponent()).andReturn(null);
        expect(AmbariManagementControllerImplTest.ambariMetaInfo.getService("stack", "1.0", "service")).andReturn(serviceInfo);
        replay(injector, cluster, service, component1, component2, serviceInfo, AmbariManagementControllerImplTest.ambariMetaInfo, stackId);
        AmbariManagementControllerImpl controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        ServiceComponent resultComponent = controller.getClientComponentForRunningAction(cluster, service);
        Assert.assertNotNull(resultComponent);
        Assert.assertEquals(component2, resultComponent);
        verify(injector, cluster, service, component1, component2, serviceInfo, AmbariManagementControllerImplTest.ambariMetaInfo, stackId);
    }

    /**
     * Ensure that ClusterNotFoundException is propagated in case where there is a single request.
     */
    @Test
    public void testGetClusters___ClusterNotFoundException() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        // requests
        ClusterRequest request1 = new ClusterRequest(null, "cluster1", "1", Collections.emptySet());
        Set<ClusterRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, createNiceMock(KerberosHelper.class));
        // getCluster
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andThrow(new ClusterNotFoundException("cluster1"));
        // replay mocks
        replay(injector, AmbariManagementControllerImplTest.clusters);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        // assert that exception is thrown in case where there is a single request
        try {
            controller.getClusters(setRequests);
            Assert.fail("expected ClusterNotFoundException");
        } catch (ClusterNotFoundException e) {
            // expected
        }
        verify(injector, AmbariManagementControllerImplTest.clusters);
    }

    /**
     * Ensure that ClusterNotFoundException is handled where there are multiple requests as would be the
     * case when an OR predicate is provided in the query.
     */
    @Test
    public void testGetClusters___OR_Predicate_ClusterNotFoundException() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        Cluster cluster = createNiceMock(Cluster.class);
        Cluster cluster2 = createNiceMock(Cluster.class);
        ClusterResponse response = createNiceMock(ClusterResponse.class);
        ClusterResponse response2 = createNiceMock(ClusterResponse.class);
        // requests
        ClusterRequest request1 = new ClusterRequest(null, "cluster1", "1", Collections.emptySet());
        ClusterRequest request2 = new ClusterRequest(null, "cluster2", "1", Collections.emptySet());
        ClusterRequest request3 = new ClusterRequest(null, "cluster3", "1", Collections.emptySet());
        ClusterRequest request4 = new ClusterRequest(null, "cluster4", "1", Collections.emptySet());
        Set<ClusterRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        setRequests.add(request2);
        setRequests.add(request3);
        setRequests.add(request4);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, createNiceMock(KerberosHelper.class));
        // getCluster
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andThrow(new ClusterNotFoundException("cluster1"));
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster2")).andReturn(cluster);
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster3")).andReturn(cluster2);
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster4")).andThrow(new ClusterNotFoundException("cluster4"));
        expect(cluster.convertToResponse()).andReturn(response);
        expect(cluster2.convertToResponse()).andReturn(response2);
        CredentialStoreService credentialStoreService = createNiceMock(CredentialStoreService.class);
        expect(credentialStoreService.isInitialized(anyObject(CredentialStoreType.class))).andReturn(true).anyTimes();
        // replay mocks
        replay(injector, AmbariManagementControllerImplTest.clusters, cluster, cluster2, response, response2, credentialStoreService);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        Field f = controller.getClass().getDeclaredField("credentialStoreService");
        f.setAccessible(true);
        f.set(controller, credentialStoreService);
        Set<ClusterResponse> setResponses = controller.getClusters(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertEquals(2, setResponses.size());
        Assert.assertTrue(setResponses.contains(response));
        Assert.assertTrue(setResponses.contains(response2));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, cluster2, response, response2, credentialStoreService);
    }

    /**
     * Ensure that validateClusterName(String clusterName) work as expected.
     * Character Requirements
     * <p>
     * A through Z
     * a through z
     * 0 through 9
     * _ (underscore)
     * - (dash)
     * Length Requirements
     * <p>
     * Minimum: 1 character
     * Maximum: 100 characters
     */
    @Test
    public void testValidateClusterName() throws Exception {
        AmbariManagementControllerImpl.validateClusterName("clustername");
        AmbariManagementControllerImpl.validateClusterName("CLUSTERNAME");
        AmbariManagementControllerImpl.validateClusterName("clustername123");
        AmbariManagementControllerImpl.validateClusterName("cluster-name");
        AmbariManagementControllerImpl.validateClusterName("cluster_name");
        try {
            AmbariManagementControllerImpl.validateClusterName(null);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // This is expected
        }
        try {
            AmbariManagementControllerImpl.validateClusterName("clusternameclusternameclusternameclusternameclusternameclusternameclusternameclusternameclusternameclustername");
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // This is expected
        }
        try {
            AmbariManagementControllerImpl.validateClusterName("clustername@#$%");
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // This is expected
        }
        try {
            AmbariManagementControllerImpl.validateClusterName("");
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // This is expected
        }
    }

    /**
     * Ensure that when the cluster id is provided and the given cluster name is different from the cluster's name
     * then the cluster rename logic is executed.
     */
    @Test
    public void testUpdateClusters() throws Exception {
        // member state mocks
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        Injector injector = createStrictMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        ActionManager actionManager = createNiceMock(ActionManager.class);
        ClusterRequest clusterRequest = createNiceMock(ClusterRequest.class);
        ConfigurationRequest configurationRequest = createNiceMock(ConfigurationRequest.class);
        // requests
        Set<ClusterRequest> setRequests = new HashSet<>();
        setRequests.add(clusterRequest);
        List<ConfigurationRequest> configRequests = new ArrayList<>();
        configRequests.add(configurationRequest);
        KerberosHelper kerberosHelper = createStrictMock(KerberosHelper.class);
        Provider<MetadataHolder> m_metadataHolder = createMock(Provider.class);
        MetadataHolder metadataHolder = createMock(MetadataHolder.class);
        Provider<AgentConfigsHolder> m_agentConfigsHolder = createMock(Provider.class);
        AgentConfigsHolder agentConfigsHolder = createMockBuilder(AgentConfigsHolder.class).addMockedMethod("updateData").createMock();
        // expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, null, kerberosHelper, m_metadataHolder, m_agentConfigsHolder);
        expect(m_metadataHolder.get()).andReturn(metadataHolder).anyTimes();
        expect(metadataHolder.updateData(anyObject())).andReturn(true).anyTimes();
        expect(m_agentConfigsHolder.get()).andReturn(agentConfigsHolder).anyTimes();
        agentConfigsHolder.updateData(anyLong(), anyObject(List.class));
        expectLastCall().anyTimes();
        expect(clusterRequest.getClusterName()).andReturn("clusterNew").times(5);
        expect(clusterRequest.getClusterId()).andReturn(1L).times(4);
        expect(clusterRequest.getDesiredConfig()).andReturn(configRequests);
        expect(configurationRequest.getVersionTag()).andReturn(null).times(1);
        expect(AmbariManagementControllerImplTest.clusters.getClusterById(1L)).andReturn(cluster).times(1);
        expect(cluster.getClusterName()).andReturn("clusterOld").times(1);
        cluster.setClusterName("clusterNew");
        expectLastCall();
        configurationRequest.setVersionTag(EasyMock.anyObject(String.class));
        expectLastCall();
        // replay mocks
        replay(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, configurationRequest, m_metadataHolder, metadataHolder, m_agentConfigsHolder, agentConfigsHolder);
        // test
        AmbariManagementController controller = partialMockBuilder(AmbariManagementControllerImpl.class).withConstructor(actionManager, AmbariManagementControllerImplTest.clusters, injector).addMockedMethod("getClusterMetadataOnConfigsUpdate").createMock();
        controller.updateClusters(setRequests, null);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, configurationRequest, m_metadataHolder, metadataHolder, m_agentConfigsHolder, agentConfigsHolder);
    }

    /**
     * Ensure that when the cluster is updated KerberosHandler.toggleKerberos is not invoked unless
     * the security type is altered
     */
    @Test
    public void testUpdateClustersToggleKerberosNotInvoked() throws Exception {
        // member state mocks
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        Injector injector = createStrictMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        ActionManager actionManager = createNiceMock(ActionManager.class);
        ClusterRequest clusterRequest = createNiceMock(ClusterRequest.class);
        // requests
        Set<ClusterRequest> setRequests = Collections.singleton(clusterRequest);
        KerberosHelper kerberosHelper = createStrictMock(KerberosHelper.class);
        Provider<MetadataHolder> m_metadataHolder = createMock(Provider.class);
        MetadataHolder metadataHolder = createMock(MetadataHolder.class);
        Provider<AgentConfigsHolder> m_agentConfigsHolder = createMock(Provider.class);
        AgentConfigsHolder agentConfigsHolder = createMockBuilder(AgentConfigsHolder.class).addMockedMethod("updateData").createMock();
        // expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, null, kerberosHelper, m_metadataHolder, m_agentConfigsHolder);
        expect(m_metadataHolder.get()).andReturn(metadataHolder).anyTimes();
        expect(metadataHolder.updateData(anyObject())).andReturn(true).anyTimes();
        expect(m_agentConfigsHolder.get()).andReturn(agentConfigsHolder).anyTimes();
        agentConfigsHolder.updateData(anyLong(), anyObject(List.class));
        expectLastCall().anyTimes();
        expect(clusterRequest.getClusterId()).andReturn(1L).times(4);
        expect(AmbariManagementControllerImplTest.clusters.getClusterById(1L)).andReturn(cluster).times(1);
        // replay mocks
        replay(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, kerberosHelper, m_metadataHolder, metadataHolder, m_agentConfigsHolder, agentConfigsHolder);
        // test
        AmbariManagementController controller = partialMockBuilder(AmbariManagementControllerImpl.class).withConstructor(actionManager, AmbariManagementControllerImplTest.clusters, injector).addMockedMethod("getClusterMetadataOnConfigsUpdate").createMock();
        controller.updateClusters(setRequests, null);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, kerberosHelper, m_metadataHolder, metadataHolder, m_agentConfigsHolder, agentConfigsHolder);
    }

    /**
     * Ensure that when the cluster security type updated from KERBEROS to KERBEROS,
     * KerberosHandler.toggleKerberos IS NOT invoked
     */
    @Test
    public void testUpdateClustersToggleKerberosReenable() throws Exception {
        // member state mocks
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        Injector injector = createStrictMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        ActionManager actionManager = createNiceMock(ActionManager.class);
        ClusterRequest clusterRequest = createNiceMock(ClusterRequest.class);
        // requests
        Set<ClusterRequest> setRequests = Collections.singleton(clusterRequest);
        KerberosHelper kerberosHelper = createStrictMock(KerberosHelper.class);
        Provider<MetadataHolder> m_metadataHolder = createMock(Provider.class);
        MetadataHolder metadataHolder = createMock(MetadataHolder.class);
        Provider<AgentConfigsHolder> m_agentConfigsHolder = createMock(Provider.class);
        AgentConfigsHolder agentConfigsHolder = createMockBuilder(AgentConfigsHolder.class).addMockedMethod("updateData").createMock();
        // expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, null, kerberosHelper, m_metadataHolder, m_agentConfigsHolder);
        expect(m_metadataHolder.get()).andReturn(metadataHolder).anyTimes();
        expect(metadataHolder.updateData(anyObject())).andReturn(true).anyTimes();
        expect(m_agentConfigsHolder.get()).andReturn(agentConfigsHolder).anyTimes();
        agentConfigsHolder.updateData(anyLong(), anyObject(List.class));
        expectLastCall().anyTimes();
        expect(clusterRequest.getClusterId()).andReturn(1L).times(4);
        expect(clusterRequest.getSecurityType()).andReturn(KERBEROS).anyTimes();
        expect(AmbariManagementControllerImplTest.clusters.getClusterById(1L)).andReturn(cluster).times(1);
        expect(cluster.getSecurityType()).andReturn(KERBEROS).anyTimes();
        expect(kerberosHelper.shouldExecuteCustomOperations(KERBEROS, null)).andReturn(false).once();
        expect(kerberosHelper.getForceToggleKerberosDirective(EasyMock.anyObject())).andReturn(false).once();
        // Note: kerberosHelper.toggleKerberos is not called
        // replay mocks
        replay(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, kerberosHelper, m_metadataHolder, metadataHolder, m_agentConfigsHolder, agentConfigsHolder);
        // test
        AmbariManagementController controller = partialMockBuilder(AmbariManagementControllerImpl.class).withConstructor(actionManager, AmbariManagementControllerImplTest.clusters, injector).addMockedMethod("getClusterMetadataOnConfigsUpdate").createMock();
        controller.updateClusters(setRequests, null);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, kerberosHelper, m_metadataHolder, metadataHolder, m_agentConfigsHolder, agentConfigsHolder);
    }

    /**
     * Ensure that when the cluster security type updated from NONE to KERBEROS, KerberosHandler.toggleKerberos
     * IS invoked
     */
    @Test
    public void testUpdateClustersToggleKerberosEnable() throws Exception {
        // member state mocks
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        Injector injector = createStrictMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        ActionManager actionManager = createNiceMock(ActionManager.class);
        ClusterRequest clusterRequest = createNiceMock(ClusterRequest.class);
        // requests
        Set<ClusterRequest> setRequests = Collections.singleton(clusterRequest);
        KerberosHelper kerberosHelper = createStrictMock(KerberosHelper.class);
        Provider<MetadataHolder> m_metadataHolder = createMock(Provider.class);
        MetadataHolder metadataHolder = createMock(MetadataHolder.class);
        Provider<AgentConfigsHolder> m_agentConfigsHolder = createMock(Provider.class);
        AgentConfigsHolder agentConfigsHolder = createMockBuilder(AgentConfigsHolder.class).addMockedMethod("updateData").createMock();
        // expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, null, kerberosHelper, m_metadataHolder, m_agentConfigsHolder);
        expect(m_metadataHolder.get()).andReturn(metadataHolder).anyTimes();
        expect(metadataHolder.updateData(anyObject())).andReturn(true).anyTimes();
        expect(m_agentConfigsHolder.get()).andReturn(agentConfigsHolder).anyTimes();
        agentConfigsHolder.updateData(anyLong(), anyObject(List.class));
        expectLastCall().anyTimes();
        expect(clusterRequest.getClusterId()).andReturn(1L).times(4);
        expect(clusterRequest.getSecurityType()).andReturn(KERBEROS).anyTimes();
        expect(AmbariManagementControllerImplTest.clusters.getClusterById(1L)).andReturn(cluster).times(1);
        expect(cluster.getSecurityType()).andReturn(NONE).anyTimes();
        expect(kerberosHelper.shouldExecuteCustomOperations(KERBEROS, null)).andReturn(false).once();
        expect(kerberosHelper.getForceToggleKerberosDirective(null)).andReturn(false).once();
        expect(kerberosHelper.getManageIdentitiesDirective(null)).andReturn(null).once();
        expect(kerberosHelper.toggleKerberos(anyObject(Cluster.class), anyObject(SecurityType.class), anyObject(RequestStageContainer.class), anyBoolean())).andReturn(null).once();
        // replay mocks
        replay(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, kerberosHelper, m_metadataHolder, metadataHolder, m_agentConfigsHolder, agentConfigsHolder);
        // test
        AmbariManagementController controller = partialMockBuilder(AmbariManagementControllerImpl.class).withConstructor(actionManager, AmbariManagementControllerImplTest.clusters, injector).addMockedMethod("getClusterMetadataOnConfigsUpdate").createMock();
        controller.updateClusters(setRequests, null);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, kerberosHelper, m_metadataHolder, metadataHolder, m_agentConfigsHolder, agentConfigsHolder);
    }

    /**
     * Ensure that when the cluster security type updated from KERBEROS to NONE, KerberosHandler.toggleKerberos
     * IS invoked
     */
    @Test
    public void testUpdateClustersToggleKerberosDisable_Default() throws Exception {
        testUpdateClustersToggleKerberosDisable(null);
    }

    /**
     * Ensure that when the cluster security type updated from KERBEROS to NONE, KerberosHandler.toggleKerberos
     * IS invoked and identities are not managed
     */
    @Test
    public void testUpdateClustersToggleKerberosDisable_NoManageIdentities() throws Exception {
        testUpdateClustersToggleKerberosDisable(Boolean.FALSE);
    }

    /**
     * Ensure that when the cluster security type updated from KERBEROS to NONE, KerberosHandler.toggleKerberos
     * IS invoked and identities are managed
     */
    @Test
    public void testUpdateClustersToggleKerberosDisable_ManageIdentities() throws Exception {
        testUpdateClustersToggleKerberosDisable(Boolean.TRUE);
    }

    /**
     * Ensure that when the cluster security type updated from KERBEROS to NONE, KerberosHandler.toggleKerberos
     * IS invoked
     */
    @Test
    public void testUpdateClustersToggleKerberos_Fail() throws Exception {
        // member state mocks
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        Injector injector = createStrictMock(Injector.class);
        Cluster cluster = createMock(Cluster.class);
        ActionManager actionManager = createNiceMock(ActionManager.class);
        ClusterRequest clusterRequest = createNiceMock(ClusterRequest.class);
        // requests
        Set<ClusterRequest> setRequests = Collections.singleton(clusterRequest);
        KerberosHelper kerberosHelper = createStrictMock(KerberosHelper.class);
        Provider<MetadataHolder> m_metadataHolder = createMock(Provider.class);
        MetadataHolder metadataHolder = createMock(MetadataHolder.class);
        Provider<AgentConfigsHolder> m_agentConfigsHolder = createMock(Provider.class);
        AgentConfigsHolder agentConfigsHolder = createMockBuilder(AgentConfigsHolder.class).addMockedMethod("updateData").createMock();
        // expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, null, kerberosHelper, m_metadataHolder, m_agentConfigsHolder);
        expect(clusterRequest.getClusterId()).andReturn(1L).times(4);
        expect(clusterRequest.getSecurityType()).andReturn(NONE).anyTimes();
        expect(AmbariManagementControllerImplTest.clusters.getClusterById(1L)).andReturn(cluster).times(1);
        expect(cluster.getResourceId()).andReturn(1L).times(3);
        expect(cluster.getSecurityType()).andReturn(KERBEROS).anyTimes();
        expect(cluster.getCurrentStackVersion()).andReturn(null).anyTimes();
        expect(cluster.getDesiredStackVersion()).andReturn(null).anyTimes();
        cluster.setCurrentStackVersion(anyObject(StackId.class));
        expectLastCall().once();
        expect(kerberosHelper.shouldExecuteCustomOperations(NONE, null)).andReturn(false).once();
        expect(kerberosHelper.getForceToggleKerberosDirective(EasyMock.anyObject())).andReturn(false).once();
        expect(kerberosHelper.getManageIdentitiesDirective(EasyMock.anyObject())).andReturn(null).once();
        expect(kerberosHelper.toggleKerberos(anyObject(Cluster.class), anyObject(SecurityType.class), anyObject(RequestStageContainer.class), anyBoolean())).andThrow(new IllegalArgumentException("bad args!")).once();
        // replay mocks
        replay(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, kerberosHelper, m_agentConfigsHolder, agentConfigsHolder, m_metadataHolder, metadataHolder);
        // test
        AmbariManagementController controller = partialMockBuilder(AmbariManagementControllerImpl.class).withConstructor(actionManager, AmbariManagementControllerImplTest.clusters, injector).addMockedMethod("getClusterMetadataOnConfigsUpdate").createMock();
        try {
            controller.updateClusters(setRequests, null);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // This is expected
        }
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager, kerberosHelper, m_agentConfigsHolder, agentConfigsHolder, m_metadataHolder, metadataHolder);
    }

    /**
     * Ensure that RollbackException is thrown outside the updateClusters method
     * when a unique constraint violation occurs.
     */
    @Test
    public void testUpdateClusters__RollbackException() throws Exception {
        // member state mocks
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        Injector injector = createStrictMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        ActionManager actionManager = createNiceMock(ActionManager.class);
        ClusterRequest clusterRequest = createNiceMock(ClusterRequest.class);
        // requests
        Set<ClusterRequest> setRequests = Collections.singleton(clusterRequest);
        // expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, createNiceMock(KerberosHelper.class));
        expect(clusterRequest.getClusterName()).andReturn("clusterNew").times(5);
        expect(clusterRequest.getClusterId()).andReturn(1L).times(4);
        expect(AmbariManagementControllerImplTest.clusters.getClusterById(1L)).andReturn(cluster).times(1);
        expect(cluster.getClusterName()).andReturn("clusterOld").times(1);
        cluster.setClusterName("clusterNew");
        expectLastCall().andThrow(new RollbackException());
        // replay mocks
        replay(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(actionManager, AmbariManagementControllerImplTest.clusters, injector);
        try {
            controller.updateClusters(setRequests, null);
            Assert.fail("Expected RollbackException");
        } catch (RollbackException e) {
            // expected
        }
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(actionManager, cluster, AmbariManagementControllerImplTest.clusters, injector, clusterRequest, AmbariManagementControllerImplTest.sessionManager);
    }

    @Test
    public void testGetHostComponents() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        final Host host = createNiceMock(Host.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        final ServiceComponentHost componentHost = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response = createNiceMock(ServiceComponentHostResponse.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        expect(maintHelper.getEffectiveState(componentHost)).andReturn(OFF).anyTimes();
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        Provider<MetadataHolder> m_metadataHolder = createMock(Provider.class);
        Provider<AgentConfigsHolder> m_agentConfigsHolder = createMock(Provider.class);
        // expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), m_metadataHolder, m_agentConfigsHolder);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster);
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host1")).andReturn(Collections.singleton(cluster));
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(new HashMap<String, Host>() {
            {
                put("host1", host);
            }
        }).anyTimes();
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getServiceComponent("component1")).andReturn(component);
        expect(service.getName()).andReturn("service1");
        expect(component.getName()).andReturn("component1");
        expect(component.getServiceComponentHosts()).andReturn(new HashMap<String, ServiceComponentHost>() {
            {
                put("host1", componentHost);
            }
        });
        expect(componentHost.convertToResponse(null)).andReturn(response);
        expect(componentHost.getHostName()).andReturn("host1").anyTimes();
        expect(maintHelper.getEffectiveState(componentHost, host)).andReturn(OFF);
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, host, response, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component, componentHost, m_agentConfigsHolder, m_metadataHolder);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> setResponses = controller.getHostComponents(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertEquals(1, setResponses.size());
        Assert.assertTrue(setResponses.contains(response));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, host, response, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component, componentHost, m_agentConfigsHolder, m_metadataHolder);
    }

    @Test
    public void testGetHostComponents___ServiceComponentHostNotFoundException() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Host host = createNiceMock(Host.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster);
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host1")).andReturn(Collections.singleton(cluster));
        // expect(cluster.getDesiredStackVersion()).andReturn(stack);
        // expect(stack.getStackName()).andReturn("stackName");
        // expect(stack.getStackVersion()).andReturn("stackVersion");
        // 
        // expect(ambariMetaInfo.getComponentToService("stackName", "stackVersion", "component1")).andReturn("service1");
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getServiceComponent("component1")).andReturn(component);
        expect(service.getName()).andReturn("service1");
        expect(component.getName()).andReturn("component1").anyTimes();
        expect(component.getServiceComponentHosts()).andReturn(null);
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        try {
            controller.getHostComponents(setRequests);
            Assert.fail("expected ServiceComponentHostNotFoundException");
        } catch (ServiceComponentHostNotFoundException e) {
            // expected
        }
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component);
    }

    @Test
    public void testGetHostComponents___ServiceComponentHostFilteredByState() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        final Host host = createNiceMock(Host.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        final ServiceComponentHost componentHost1 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response1 = createNiceMock(ServiceComponentHostResponse.class);
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        request1.setState("INSTALLED");
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        // expectations
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        expect(maintHelper.getEffectiveState(anyObject(ServiceComponentHost.class), anyObject(Host.class))).andReturn(OFF).anyTimes();
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster);
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host1")).andReturn(Collections.singleton(cluster));
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(new HashMap<String, Host>() {
            {
                put("host1", host);
            }
        }).anyTimes();
        // expect(cluster.getDesiredStackVersion()).andReturn(stack);
        // expect(stack.getStackName()).andReturn("stackName");
        // expect(stack.getStackVersion()).andReturn("stackVersion");
        // 
        // expect(ambariMetaInfo.getComponentToService("stackName", "stackVersion", "component1")).andReturn("service1");
        expect(cluster.getClusterName()).andReturn("cl1");
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getServiceComponent("component1")).andReturn(component);
        expect(service.getName()).andReturn("service1");
        expect(component.getName()).andReturn("component1").anyTimes();
        expect(component.getServiceComponentHosts()).andReturn(new HashMap<String, ServiceComponentHost>() {
            {
                put("host1", componentHost1);
            }
        });
        expect(componentHost1.getState()).andReturn(INSTALLED);
        expect(componentHost1.convertToResponse(null)).andReturn(response1);
        expect(componentHost1.getHostName()).andReturn("host1");
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component, componentHost1, response1);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> responses = controller.getHostComponents(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertTrue(((responses.size()) == 1));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component, componentHost1, response1);
    }

    @Test
    public void testGetHostComponents___ServiceComponentHostFilteredByMaintenanceState() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        final Host host = createNiceMock(Host.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        final ServiceComponentHost componentHost1 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response1 = createNiceMock(ServiceComponentHostResponse.class);
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        request1.setMaintenanceState("ON");
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        expect(maintHelper.getEffectiveState(anyObject(ServiceComponentHost.class), anyObject(Host.class))).andReturn(IMPLIED_FROM_SERVICE).anyTimes();
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster);
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host1")).andReturn(Collections.singleton(cluster));
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(new HashMap<String, Host>() {
            {
                put("host1", host);
            }
        }).anyTimes();
        expect(cluster.getClusterName()).andReturn("cl1");
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getServiceComponent("component1")).andReturn(component);
        expect(service.getName()).andReturn("service1");
        expect(component.getName()).andReturn("component1").anyTimes();
        expect(component.getServiceComponentHosts()).andReturn(new HashMap<String, ServiceComponentHost>() {
            {
                put("host1", componentHost1);
            }
        });
        expect(componentHost1.convertToResponse(null)).andReturn(response1);
        expect(componentHost1.getHostName()).andReturn("host1");
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component, componentHost1, response1);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> responses = controller.getHostComponents(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertTrue(((responses.size()) == 1));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component, componentHost1, response1);
    }

    @Test
    public void testGetHostComponents___OR_Predicate_ServiceComponentHostNotFoundException() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        final Host host = createNiceMock(Host.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component1 = createNiceMock(ServiceComponent.class);
        ServiceComponent component2 = createNiceMock(ServiceComponent.class);
        ServiceComponent component3 = createNiceMock(ServiceComponent.class);
        final ServiceComponentHost componentHost1 = createNiceMock(ServiceComponentHost.class);
        final ServiceComponentHost componentHost2 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response1 = createNiceMock(ServiceComponentHostResponse.class);
        ServiceComponentHostResponse response2 = createNiceMock(ServiceComponentHostResponse.class);
        MaintenanceStateHelper stateHelper = createNiceMock(MaintenanceStateHelper.class);
        expect(stateHelper.getEffectiveState(anyObject(ServiceComponentHost.class), anyObject(Host.class))).andReturn(OFF).anyTimes();
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(new HashMap<String, Host>() {
            {
                put("host1", host);
            }
        }).anyTimes();
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        ServiceComponentHostRequest request2 = new ServiceComponentHostRequest("cluster1", null, "component2", "host1", null);
        ServiceComponentHostRequest request3 = new ServiceComponentHostRequest("cluster1", null, "component3", "host1", null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        setRequests.add(request2);
        setRequests.add(request3);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, stateHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster).times(3);
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host1")).andReturn(Collections.singleton(cluster)).anyTimes();
        expect(cluster.getService("service1")).andReturn(service).times(3);
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getServiceComponent("component1")).andReturn(component1);
        expect(service.getName()).andReturn("service1").anyTimes();
        expect(component1.getName()).andReturn("component1");
        expect(component1.getServiceComponentHosts()).andReturn(new HashMap<String, ServiceComponentHost>() {
            {
                put("host1", componentHost1);
            }
        });
        expect(componentHost1.convertToResponse(null)).andReturn(response1);
        expect(componentHost1.getHostName()).andReturn("host1");
        expect(cluster.getServiceByComponentName("component2")).andReturn(service);
        expect(service.getServiceComponent("component2")).andReturn(component2);
        expect(component2.getName()).andReturn("component2");
        expect(component2.getServiceComponentHosts()).andReturn(null);
        expect(componentHost2.getHostName()).andReturn("host1");
        expect(cluster.getServiceByComponentName("component3")).andReturn(service);
        expect(service.getServiceComponent("component3")).andReturn(component3);
        expect(component3.getName()).andReturn("component3");
        expect(component3.getServiceComponentHosts()).andReturn(new HashMap<String, ServiceComponentHost>() {
            {
                put("host1", componentHost2);
            }
        });
        expect(componentHost2.convertToResponse(null)).andReturn(response2);
        // replay mocks
        replay(stateHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component1, component2, component3, componentHost1, componentHost2, response1, response2);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> setResponses = controller.getHostComponents(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertEquals(2, setResponses.size());
        Assert.assertTrue(setResponses.contains(response1));
        Assert.assertTrue(setResponses.contains(response2));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component1, component2, component3, componentHost1, componentHost2, response1, response2);
    }

    @Test
    public void testGetHostComponents___OR_Predicate_ServiceNotFoundException() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        final Host host = createNiceMock(Host.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component1 = createNiceMock(ServiceComponent.class);
        ServiceComponent component2 = createNiceMock(ServiceComponent.class);
        ServiceComponent component3 = createNiceMock(ServiceComponent.class);
        final ServiceComponentHost componentHost1 = createNiceMock(ServiceComponentHost.class);
        final ServiceComponentHost componentHost2 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response1 = createNiceMock(ServiceComponentHostResponse.class);
        ServiceComponentHostResponse response2 = createNiceMock(ServiceComponentHostResponse.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        expect(maintHelper.getEffectiveState(anyObject(ServiceComponentHost.class), anyObject(Host.class))).andReturn(OFF).anyTimes();
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        ServiceComponentHostRequest request2 = new ServiceComponentHostRequest("cluster1", null, "component2", "host1", null);
        ServiceComponentHostRequest request3 = new ServiceComponentHostRequest("cluster1", null, "component3", "host1", null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        setRequests.add(request2);
        setRequests.add(request3);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster).times(3);
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host1")).andReturn(Collections.singleton(cluster)).anyTimes();
        expect(cluster.getDesiredStackVersion()).andReturn(stack).anyTimes();
        expect(stack.getStackName()).andReturn("stackName").anyTimes();
        expect(stack.getStackVersion()).andReturn("stackVersion").anyTimes();
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(new HashMap<String, Host>() {
            {
                put("host1", host);
            }
        }).anyTimes();
        // expect(ambariMetaInfo.getComponentToService("stackName", "stackVersion", "component1")).andReturn("service1");
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getName()).andReturn("service1").atLeastOnce();
        expect(service.getServiceComponent("component1")).andReturn(component1);
        expect(component1.getName()).andReturn("component1");
        expect(component1.getServiceComponentHosts()).andReturn(new HashMap<String, ServiceComponentHost>() {
            {
                put("host1", componentHost1);
            }
        });
        expect(componentHost1.convertToResponse(null)).andReturn(response1);
        expect(componentHost1.getHostName()).andReturn("host1");
        expect(cluster.getServiceByComponentName("component2")).andThrow(new ServiceNotFoundException("cluster1", "service2"));
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component3")).andReturn(service);
        expect(service.getServiceComponent("component3")).andReturn(component3);
        expect(component3.getName()).andReturn("component3");
        expect(component3.getServiceComponentHosts()).andReturn(new HashMap<String, ServiceComponentHost>() {
            {
                put("host1", componentHost2);
            }
        });
        expect(componentHost2.convertToResponse(null)).andReturn(response2);
        expect(componentHost2.getHostName()).andReturn("host1");
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component1, component2, component3, componentHost1, componentHost2, response1, response2);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> setResponses = controller.getHostComponents(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertEquals(2, setResponses.size());
        Assert.assertTrue(setResponses.contains(response1));
        Assert.assertTrue(setResponses.contains(response2));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component1, component2, component3, componentHost1, componentHost2, response1, response2);
    }

    @Test
    public void testGetHostComponents___OR_Predicate_ServiceComponentNotFoundException() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        final Host host = createNiceMock(Host.class);
        Service service = createNiceMock(Service.class);
        Service service2 = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        ServiceComponent component2 = createNiceMock(ServiceComponent.class);
        ServiceComponent component3 = createNiceMock(ServiceComponent.class);
        final ServiceComponentHost componentHost1 = createNiceMock(ServiceComponentHost.class);
        final ServiceComponentHost componentHost2 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response1 = createNiceMock(ServiceComponentHostResponse.class);
        ServiceComponentHostResponse response2 = createNiceMock(ServiceComponentHostResponse.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        expect(maintHelper.getEffectiveState(anyObject(ServiceComponentHost.class), anyObject(Host.class))).andReturn(OFF).anyTimes();
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        ServiceComponentHostRequest request2 = new ServiceComponentHostRequest("cluster1", null, "component2", "host1", null);
        ServiceComponentHostRequest request3 = new ServiceComponentHostRequest("cluster1", null, "component3", "host1", null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        setRequests.add(request2);
        setRequests.add(request3);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster).times(3);
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host1")).andReturn(Collections.singleton(cluster)).anyTimes();
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(ImmutableMap.<String, Host>builder().put("host1", host).build()).anyTimes();
        expect(cluster.getDesiredStackVersion()).andReturn(stack).anyTimes();
        expect(stack.getStackName()).andReturn("stackName").anyTimes();
        expect(stack.getStackVersion()).andReturn("stackVersion").anyTimes();
        // expect(ambariMetaInfo.getComponentToService("stackName", "stackVersion", "component1")).andReturn("service1");
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getServiceComponent("component1")).andReturn(component);
        expect(service.getName()).andReturn("service1").anyTimes();
        expect(component.getName()).andReturn("component1");
        expect(component.getServiceComponentHosts()).andReturn(ImmutableMap.<String, ServiceComponentHost>builder().put("host1", componentHost1).build());
        expect(componentHost1.convertToResponse(null)).andReturn(response1);
        expect(componentHost1.getHostName()).andReturn("host1");
        // expect(ambariMetaInfo.getComponentToService("stackName", "stackVersion", "component2")).andReturn("service2");
        expect(cluster.getService("service2")).andReturn(service2);
        expect(cluster.getServiceByComponentName("component2")).andReturn(service2);
        expect(service2.getName()).andReturn("service2");
        expect(service2.getServiceComponent("component2")).andThrow(new ServiceComponentNotFoundException("cluster1", "service2", "component2"));
        // expect(ambariMetaInfo.getComponentToService("stackName", "stackVersion", "component3")).andReturn("service1");
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component3")).andReturn(service);
        expect(service.getServiceComponent("component3")).andReturn(component3);
        expect(component3.getName()).andReturn("component3");
        expect(component3.getServiceComponentHosts()).andReturn(ImmutableMap.<String, ServiceComponentHost>builder().put("host1", componentHost2).build());
        expect(componentHost2.convertToResponse(null)).andReturn(response2);
        expect(componentHost2.getHostName()).andReturn("host1");
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, service2, component, component2, component3, componentHost1, componentHost2, response1, response2);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> setResponses = controller.getHostComponents(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertEquals(2, setResponses.size());
        Assert.assertTrue(setResponses.contains(response1));
        Assert.assertTrue(setResponses.contains(response2));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, service2, component, component2, component3, componentHost1, componentHost2, response1, response2);
    }

    @Test
    public void testGetHostComponents___OR_Predicate_HostNotFoundException_hostProvidedInQuery() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        final Host host = createNiceMock(Host.class);
        Service service = createNiceMock(Service.class);
        Service service2 = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        ServiceComponent component2 = createNiceMock(ServiceComponent.class);
        ServiceComponent component3 = createNiceMock(ServiceComponent.class);
        final ServiceComponentHost componentHost1 = createNiceMock(ServiceComponentHost.class);
        final ServiceComponentHost componentHost2 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response1 = createNiceMock(ServiceComponentHostResponse.class);
        ServiceComponentHostResponse response2 = createNiceMock(ServiceComponentHostResponse.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        expect(maintHelper.getEffectiveState(anyObject(ServiceComponentHost.class), anyObject(Host.class))).andReturn(OFF).anyTimes();
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", null, null);
        ServiceComponentHostRequest request2 = new ServiceComponentHostRequest("cluster1", null, "component2", "host2", null);
        ServiceComponentHostRequest request3 = new ServiceComponentHostRequest("cluster1", null, "component3", null, null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        setRequests.add(request2);
        setRequests.add(request3);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster).times(3);
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(new HashMap<String, Host>() {
            {
                put("host1", host);
            }
        }).anyTimes();
        expect(cluster.getDesiredStackVersion()).andReturn(stack).anyTimes();
        expect(stack.getStackName()).andReturn("stackName").anyTimes();
        expect(stack.getStackVersion()).andReturn("stackVersion").anyTimes();
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getServiceComponent("component1")).andReturn(component);
        expect(service.getName()).andReturn("service1").anyTimes();
        expect(component.getName()).andReturn("component1");
        expect(component.getServiceComponentHosts()).andReturn(Collections.singletonMap("foo", componentHost1));
        expect(componentHost1.convertToResponse(null)).andReturn(response1);
        expect(componentHost1.getHostName()).andReturn("host1");
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host2")).andThrow(new HostNotFoundException("host2"));
        expect(cluster.getService("service1")).andReturn(service);
        expect(cluster.getServiceByComponentName("component3")).andReturn(service);
        expect(service.getServiceComponent("component3")).andReturn(component3);
        expect(component3.getName()).andReturn("component3");
        expect(component3.getServiceComponentHosts()).andReturn(Collections.singletonMap("foo", componentHost2));
        expect(componentHost2.convertToResponse(null)).andReturn(response2);
        expect(componentHost2.getHostName()).andReturn("host1");
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, service2, component, component2, component3, componentHost1, componentHost2, response1, response2);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> setResponses = controller.getHostComponents(setRequests);
        assertNotNull(setResponses);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertEquals(2, setResponses.size());
        Assert.assertTrue(setResponses.contains(response1));
        Assert.assertTrue(setResponses.contains(response2));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, host, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, service2, component, component2, component3, componentHost1, componentHost2, response1, response2);
    }

    @Test
    public void testGetHostComponents___OR_Predicate_HostNotFoundException_hostProvidedInURL() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        Cluster cluster = createNiceMock(Cluster.class);
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        ServiceComponentHostRequest request2 = new ServiceComponentHostRequest("cluster1", null, "component2", "host1", null);
        ServiceComponentHostRequest request3 = new ServiceComponentHostRequest("cluster1", null, "component3", "host1", null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        setRequests.add(request2);
        setRequests.add(request3);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster);
        expect(AmbariManagementControllerImplTest.clusters.getClustersForHost("host1")).andThrow(new HostNotFoundException("host1"));
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, stack, AmbariManagementControllerImplTest.ambariMetaInfo);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        try {
            controller.getHostComponents(setRequests);
            Assert.fail("expected exception");
        } catch (AmbariException e) {
            // expected
        }
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, stack, AmbariManagementControllerImplTest.ambariMetaInfo);
    }

    @Test
    public void testGetHostComponents___OR_Predicate_ClusterNotFoundException() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", "host1", null);
        ServiceComponentHostRequest request2 = new ServiceComponentHostRequest("cluster1", null, "component2", "host2", null);
        ServiceComponentHostRequest request3 = new ServiceComponentHostRequest("cluster1", null, "component3", "host1", null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        setRequests.add(request2);
        setRequests.add(request3);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andThrow(new ClusterNotFoundException("cluster1"));
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, stack, AmbariManagementControllerImplTest.ambariMetaInfo);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        try {
            controller.getHostComponents(setRequests);
            Assert.fail("expected exception");
        } catch (ParentObjectNotFoundException e) {
            // expected
        }
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        verify(injector, AmbariManagementControllerImplTest.clusters, stack, AmbariManagementControllerImplTest.ambariMetaInfo);
    }

    @Test
    public void testGetHostComponents___NullHostName() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent component = createNiceMock(ServiceComponent.class);
        ServiceComponentHost componentHost1 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHost componentHost2 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response1 = createNiceMock(ServiceComponentHostResponse.class);
        ServiceComponentHostResponse response2 = createNiceMock(ServiceComponentHostResponse.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        expect(maintHelper.getEffectiveState(anyObject(ServiceComponentHost.class), anyObject(Host.class))).andReturn(OFF).anyTimes();
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, "component1", null, null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        Map<String, ServiceComponentHost> mapHostComponents = new HashMap<>();
        mapHostComponents.put("foo", componentHost1);
        mapHostComponents.put("bar", componentHost2);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster);
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(new HashMap<String, Host>() {
            {
                put("host1", createNiceMock(Host.class));
            }
        }).anyTimes();
        expect(cluster.getService("service1")).andReturn(service);
        expect(component.getName()).andReturn("component1").anyTimes();
        expect(cluster.getServiceByComponentName("component1")).andReturn(service);
        expect(service.getServiceComponent("component1")).andReturn(component);
        expect(service.getName()).andReturn("service1");
        expect(component.getServiceComponentHosts()).andReturn(mapHostComponents);
        expect(componentHost1.convertToResponse(null)).andReturn(response1);
        expect(componentHost2.convertToResponse(null)).andReturn(response2);
        expect(componentHost1.getHostName()).andReturn("host1");
        expect(componentHost2.getHostName()).andReturn("host1");
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, response1, response2, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component, componentHost1, componentHost2);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> setResponses = controller.getHostComponents(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertEquals(2, setResponses.size());
        Assert.assertTrue(setResponses.contains(response1));
        Assert.assertTrue(setResponses.contains(response2));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, response1, response2, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service, component, componentHost1, componentHost2);
    }

    @Test
    public void testGetHostComponents___NullHostName_NullComponentName() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        StackId stack = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service1 = createNiceMock(Service.class);
        Service service2 = createNiceMock(Service.class);
        ServiceComponent component1 = createNiceMock(ServiceComponent.class);
        ServiceComponent component2 = createNiceMock(ServiceComponent.class);
        ServiceComponentHost componentHost1 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHost componentHost2 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHost componentHost3 = createNiceMock(ServiceComponentHost.class);
        ServiceComponentHostResponse response1 = createNiceMock(ServiceComponentHostResponse.class);
        ServiceComponentHostResponse response2 = createNiceMock(ServiceComponentHostResponse.class);
        ServiceComponentHostResponse response3 = createNiceMock(ServiceComponentHostResponse.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        expect(maintHelper.getEffectiveState(anyObject(ServiceComponentHost.class), anyObject(Host.class))).andReturn(OFF).anyTimes();
        // requests
        ServiceComponentHostRequest request1 = new ServiceComponentHostRequest("cluster1", null, null, null, null);
        Set<ServiceComponentHostRequest> setRequests = new HashSet<>();
        setRequests.add(request1);
        Map<String, Service> mapServices = new HashMap<>();
        mapServices.put("foo", service1);
        mapServices.put("bar", service2);
        Map<String, ServiceComponentHost> mapHostComponents = new HashMap<>();
        mapHostComponents.put("foo", componentHost1);
        mapHostComponents.put("bar", componentHost2);
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        // getHostComponent
        expect(AmbariManagementControllerImplTest.clusters.getCluster("cluster1")).andReturn(cluster);
        expect(AmbariManagementControllerImplTest.clusters.getHostsForCluster(((String) (anyObject())))).andReturn(new HashMap<String, Host>() {
            {
                put("host1", createNiceMock(Host.class));
            }
        }).anyTimes();
        expect(cluster.getServices()).andReturn(mapServices);
        expect(service1.getServiceComponents()).andReturn(Collections.singletonMap("foo", component1));
        expect(service2.getServiceComponents()).andReturn(Collections.singletonMap("bar", component2));
        expect(component1.getName()).andReturn("component1").anyTimes();
        expect(component2.getName()).andReturn("component2").anyTimes();
        expect(component1.getServiceComponentHosts()).andReturn(mapHostComponents);
        expect(componentHost1.convertToResponse(null)).andReturn(response1);
        expect(componentHost2.convertToResponse(null)).andReturn(response2);
        expect(componentHost1.getHostName()).andReturn("host1");
        expect(componentHost2.getHostName()).andReturn("host1");
        expect(componentHost3.getHostName()).andReturn("host1");
        expect(component2.getServiceComponentHosts()).andReturn(Collections.singletonMap("foobar", componentHost3));
        expect(componentHost3.convertToResponse(null)).andReturn(response3);
        // replay mocks
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, cluster, response1, response2, response3, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service1, service2, component1, component2, componentHost1, componentHost2, componentHost3);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Set<ServiceComponentHostResponse> setResponses = controller.getHostComponents(setRequests);
        // assert and verify
        Assert.assertSame(controller, controllerCapture.getValue());
        Assert.assertEquals(3, setResponses.size());
        Assert.assertTrue(setResponses.contains(response1));
        Assert.assertTrue(setResponses.contains(response2));
        Assert.assertTrue(setResponses.contains(response3));
        verify(injector, AmbariManagementControllerImplTest.clusters, cluster, response1, response2, response3, stack, AmbariManagementControllerImplTest.ambariMetaInfo, service1, service2, component1, component2, componentHost1, componentHost2, componentHost3);
    }

    @Test
    public void testPopulateServicePackagesInfo() throws Exception {
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        Injector injector = createStrictMock(Injector.class);
        MaintenanceStateHelper maintHelper = createNiceMock(MaintenanceStateHelper.class);
        ServiceInfo serviceInfo = createNiceMock(ServiceInfo.class);
        Map<String, String> hostParams = new HashMap<>();
        String osFamily = "testOSFamily";
        Map<String, ServiceOsSpecific> osSpecifics = new HashMap<>();
        ServiceOsSpecific.Package package1 = new ServiceOsSpecific.Package();
        package1.setName("testrpm1");
        ServiceOsSpecific.Package package2 = new ServiceOsSpecific.Package();
        package2.setName("testrpm2");
        ServiceOsSpecific.Package package3 = new ServiceOsSpecific.Package();
        package3.setName("testrpm3");
        List<ServiceOsSpecific.Package> packageList1 = new ArrayList<>();
        packageList1.add(package1);
        List<ServiceOsSpecific.Package> packageList2 = new ArrayList<>();
        packageList2.add(package2);
        packageList2.add(package3);
        ServiceOsSpecific osSpecific1 = new ServiceOsSpecific("testOSFamily");
        osSpecific1.addPackages(packageList1);
        ServiceOsSpecific osSpecific2 = new ServiceOsSpecific("testOSFamily1,testOSFamily,testOSFamily2");
        osSpecific2.addPackages(packageList2);
        osSpecifics.put("testOSFamily", osSpecific1);
        osSpecifics.put("testOSFamily1,testOSFamily,testOSFamily2", osSpecific2);
        expect(serviceInfo.getOsSpecifics()).andReturn(osSpecifics);
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, null, maintHelper, createNiceMock(KerberosHelper.class), null, null);
        OsFamily osFamilyMock = createNiceMock(OsFamily.class);
        EasyMock.expect(osFamilyMock.isVersionedOsFamilyExtendedByVersionedFamily("testOSFamily", "testOSFamily")).andReturn(true).times(3);
        replay(maintHelper, injector, AmbariManagementControllerImplTest.clusters, serviceInfo, osFamilyMock);
        AmbariManagementControllerImplTest.NestedTestClass nestedTestClass = this.new NestedTestClass(null, AmbariManagementControllerImplTest.clusters, injector, osFamilyMock);
        ServiceOsSpecific serviceOsSpecific = nestedTestClass.populateServicePackagesInfo(serviceInfo, hostParams, osFamily);
        Assert.assertEquals(3, serviceOsSpecific.getPackages().size());
    }

    @Test
    public void testCreateDefaultHostParams() throws Exception {
        String clusterName = "c1";
        String SOME_STACK_NAME = "SomeStackName";
        String SOME_STACK_VERSION = "1.0";
        String MYSQL_JAR = "MYSQL_JAR";
        String JAVA_HOME = "javaHome";
        String JDK_NAME = "jdkName";
        String JCE_NAME = "jceName";
        String OJDBC_JAR_NAME = "OjdbcJarName";
        String SERVER_DB_NAME = "ServerDBName";
        Map<PropertyInfo, String> notManagedHdfsPathMap = new HashMap<>();
        PropertyInfo propertyInfo1 = new PropertyInfo();
        propertyInfo1.setName("1");
        PropertyInfo propertyInfo2 = new PropertyInfo();
        propertyInfo2.setName("2");
        notManagedHdfsPathMap.put(propertyInfo1, "/tmp");
        notManagedHdfsPathMap.put(propertyInfo2, "/apps/falcon");
        Set<String> notManagedHdfsPathSet = new HashSet<>(Arrays.asList("/tmp", "/apps/falcon"));
        Gson gson = new Gson();
        ActionManager manager = createNiceMock(ActionManager.class);
        StackId stackId = createNiceMock(StackId.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Injector injector = createNiceMock(Injector.class);
        Configuration configuration = createNiceMock(Configuration.class);
        RepositoryVersionEntity repositoryVersionEntity = createNiceMock(RepositoryVersionEntity.class);
        ConfigHelper configHelper = createNiceMock(ConfigHelper.class);
        Map<String, DesiredConfig> desiredConfigs = new HashMap<>();
        expect(cluster.getClusterName()).andReturn(clusterName);
        expect(cluster.getDesiredStackVersion()).andReturn(stackId);
        expect(cluster.getDesiredConfigs()).andReturn(desiredConfigs);
        expect(stackId.getStackName()).andReturn(SOME_STACK_NAME).anyTimes();
        expect(stackId.getStackVersion()).andReturn(SOME_STACK_VERSION).anyTimes();
        expect(configuration.getMySQLJarName()).andReturn(MYSQL_JAR);
        expect(configuration.getJavaHome()).andReturn(JAVA_HOME);
        expect(configuration.getJDKName()).andReturn(JDK_NAME);
        expect(configuration.getJCEName()).andReturn(JCE_NAME);
        expect(configuration.getOjdbcJarName()).andReturn(OJDBC_JAR_NAME);
        expect(configuration.getServerDBName()).andReturn(SERVER_DB_NAME);
        expect(configuration.getJavaVersion()).andReturn(8);
        expect(configuration.areHostsSysPrepped()).andReturn("true");
        expect(configuration.getGplLicenseAccepted()).andReturn(false);
        expect(configuration.getDatabaseConnectorNames()).andReturn(new HashMap()).anyTimes();
        expect(configuration.getPreviousDatabaseConnectorNames()).andReturn(new HashMap()).anyTimes();
        expect(repositoryVersionEntity.getVersion()).andReturn("1234").anyTimes();
        expect(repositoryVersionEntity.getStackId()).andReturn(stackId).anyTimes();
        expect(configHelper.getPropertiesWithPropertyType(stackId, NOT_MANAGED_HDFS_PATH, cluster, desiredConfigs)).andReturn(notManagedHdfsPathMap);
        expect(configHelper.filterInvalidPropertyValues(notManagedHdfsPathMap, KeyNames.NOT_MANAGED_HDFS_PATH_LIST)).andReturn(notManagedHdfsPathSet);
        replay(manager, AmbariManagementControllerImplTest.clusters, cluster, injector, stackId, configuration, repositoryVersionEntity, configHelper);
        AmbariManagementControllerImpl ambariManagementControllerImpl = createMockBuilder(AmbariManagementControllerImpl.class).addMockedMethod("getRcaParameters").withConstructor(manager, AmbariManagementControllerImplTest.clusters, injector).createNiceMock();
        expect(ambariManagementControllerImpl.getRcaParameters()).andReturn(new HashMap());
        replay(ambariManagementControllerImpl);
        // Inject configuration manually
        Class<?> amciClass = AmbariManagementControllerImpl.class;
        Field f = amciClass.getDeclaredField("configs");
        f.setAccessible(true);
        f.set(ambariManagementControllerImpl, configuration);
        AmbariCustomCommandExecutionHelper helper = new AmbariCustomCommandExecutionHelper();
        Class<?> helperClass = AmbariCustomCommandExecutionHelper.class;
        f = helperClass.getDeclaredField("managementController");
        f.setAccessible(true);
        f.set(helper, ambariManagementControllerImpl);
        f = helperClass.getDeclaredField("configs");
        f.setAccessible(true);
        f.set(helper, configuration);
        f = helperClass.getDeclaredField("configHelper");
        f.setAccessible(true);
        f.set(helper, configHelper);
        f = helperClass.getDeclaredField("gson");
        f.setAccessible(true);
        f.set(helper, gson);
        Map<String, String> defaultHostParams = helper.createDefaultHostParams(cluster, repositoryVersionEntity.getStackId());
        Assert.assertEquals(16, defaultHostParams.size());
        Assert.assertEquals(MYSQL_JAR, defaultHostParams.get(KeyNames.DB_DRIVER_FILENAME));
        Assert.assertEquals(SOME_STACK_NAME, defaultHostParams.get(KeyNames.STACK_NAME));
        Assert.assertEquals(SOME_STACK_VERSION, defaultHostParams.get(KeyNames.STACK_VERSION));
        Assert.assertEquals("true", defaultHostParams.get(KeyNames.HOST_SYS_PREPPED));
        Assert.assertEquals("8", defaultHostParams.get(KeyNames.JAVA_VERSION));
        Assert.assertNotNull(defaultHostParams.get(KeyNames.NOT_MANAGED_HDFS_PATH_LIST));
        Assert.assertTrue(defaultHostParams.get(KeyNames.NOT_MANAGED_HDFS_PATH_LIST).contains("/tmp"));
    }

    @Test
    public void testSynchronizeLdapUsersAndGroupsHookDisabled() throws Exception {
        testSynchronizeLdapUsersAndGroups(false, false);
    }

    @Test
    public void testSynchronizeLdapUsersAndGroupsHookEnabled() throws Exception {
        testSynchronizeLdapUsersAndGroups(false, true);
    }

    @Test
    public void testSynchronizeLdapUsersAndGroupsPostProcessExistingUsersHookDisabled() throws Exception {
        testSynchronizeLdapUsersAndGroups(true, false);
    }

    @Test
    public void testSynchronizeLdapUsersAndGroupsPostProcessExistingUsersHookEnabled() throws Exception {
        testSynchronizeLdapUsersAndGroups(true, true);
    }

    private class MockModule implements com.google.inject.Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(AmbariLdapDataPopulator.class).toInstance(AmbariManagementControllerImplTest.ldapDataPopulator);
            binder.bind(Clusters.class).toInstance(AmbariManagementControllerImplTest.clusters);
            binder.bind(ActionDBAccessorImpl.class).toInstance(AmbariManagementControllerImplTest.actionDBAccessor);
            binder.bind(MpackManagerFactory.class).toInstance(createNiceMock(MpackManagerFactory.class));
            binder.bind(AmbariMetaInfo.class).toInstance(AmbariManagementControllerImplTest.ambariMetaInfo);
            binder.bind(Users.class).toInstance(AmbariManagementControllerImplTest.users);
            binder.bind(AmbariSessionManager.class).toInstance(AmbariManagementControllerImplTest.sessionManager);
        }
    }

    // public ServiceOsSpecific testPopulateServicePackagesInfo(ServiceInfo serviceInfo, Map<String, String> hostParams,
    // String osFamily) {
    // return super.populateServicePackagesInfo(serviceInfo, hostParams, osFamily);
    // }
    private class NestedTestClass extends AmbariManagementControllerImpl {
        public NestedTestClass(ActionManager actionManager, Clusters clusters, Injector injector, OsFamily osFamilyMock) throws Exception {
            super(actionManager, clusters, injector);
            osFamily = osFamilyMock;
        }
    }

    @Test
    public void testVerifyRepositories() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, createNiceMock(KerberosHelper.class));
        Configuration configuration = createNiceMock(Configuration.class);
        String[] suffices = new String[]{ "/repodata/repomd.xml" };
        expect(configuration.getRepoValidationSuffixes("redhat6")).andReturn(suffices);
        // replay mocks
        replay(injector, AmbariManagementControllerImplTest.clusters, AmbariManagementControllerImplTest.ambariMetaInfo, configuration);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        // Manually injected
        Class<?> c = controller.getClass();
        Field f = c.getDeclaredField("configs");
        f.setAccessible(true);
        f.set(controller, configuration);
        Set<RepositoryRequest> requests = new HashSet<>();
        RepositoryRequest request = new RepositoryRequest("stackName", "stackVersion", "redhat6", "repoId", "repo_name");
        request.setBaseUrl("file:///some/repo");
        requests.add(request);
        // A wrong file path is passed and IllegalArgumentException is expected
        try {
            controller.verifyRepositories(requests);
            fail("IllegalArgumentException is expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Could not access base url . file:///some/repo/repodata/repomd.xml . ", e.getMessage());
        }
        verify(injector, AmbariManagementControllerImplTest.clusters, AmbariManagementControllerImplTest.ambariMetaInfo, configuration);
    }

    @Test
    public void testRegisterRackChange() throws Exception {
        // member state mocks
        Injector injector = createStrictMock(Injector.class);
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        ServiceComponent serviceComponent = createNiceMock(ServiceComponent.class);
        ServiceComponentHost serviceComponentHost = createNiceMock(ServiceComponentHost.class);
        StackId stackId = createNiceMock(StackId.class);
        Capture<AmbariManagementController> controllerCapture = EasyMock.newCapture();
        // expectations
        // constructor init
        AmbariManagementControllerImplTest.constructorInit(injector, controllerCapture, createNiceMock(KerberosHelper.class));
        RepositoryInfo dummyRepoInfo = new RepositoryInfo();
        dummyRepoInfo.setRepoName("repo_name");
        expect(AmbariManagementControllerImplTest.clusters.getCluster("c1")).andReturn(cluster).anyTimes();
        expect(service.getName()).andReturn("HDFS").anyTimes();
        Map<String, ServiceComponent> serviceComponents = new HashMap<>();
        serviceComponents.put("NAMENODE", serviceComponent);
        expect(service.getServiceComponents()).andReturn(serviceComponents).anyTimes();
        Map<String, ServiceComponentHost> schMap = new HashMap<>();
        schMap.put("host1", serviceComponentHost);
        expect(serviceComponent.getServiceComponentHosts()).andReturn(schMap).anyTimes();
        serviceComponentHost.setRestartRequired(true);
        Set<String> services = new HashSet<>();
        services.add("HDFS");
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setRestartRequiredAfterRackChange(true);
        expect(AmbariManagementControllerImplTest.ambariMetaInfo.getService(service)).andReturn(serviceInfo);
        Map<String, Service> serviceMap = new HashMap<>();
        serviceMap.put("HDFS", service);
        expect(cluster.getServices()).andReturn(serviceMap).anyTimes();
        // replay mocks
        replay(injector, cluster, AmbariManagementControllerImplTest.clusters, AmbariManagementControllerImplTest.ambariMetaInfo, service, serviceComponent, serviceComponentHost, stackId);
        // test
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        controller.registerRackChange("c1");
        verify(injector, cluster, AmbariManagementControllerImplTest.clusters, AmbariManagementControllerImplTest.ambariMetaInfo, service, serviceComponent, serviceComponentHost, stackId);
    }

    @Test
    public void testCreateClusterWithRepository() throws Exception {
        Injector injector = createNiceMock(Injector.class);
        RepositoryVersionEntity repoVersion = createNiceMock(RepositoryVersionEntity.class);
        RepositoryVersionDAO repoVersionDAO = createNiceMock(RepositoryVersionDAO.class);
        expect(repoVersionDAO.findByStackAndVersion(anyObject(StackId.class), anyObject(String.class))).andReturn(repoVersion).anyTimes();
        expect(injector.getInstance(MaintenanceStateHelper.class)).andReturn(null).atLeastOnce();
        expect(injector.getInstance(Gson.class)).andReturn(null);
        expect(injector.getInstance(KerberosHelper.class)).andReturn(createNiceMock(KerberosHelper.class));
        StackId stackId = new StackId("HDP-2.1");
        Cluster cluster = createNiceMock(Cluster.class);
        Service service = createNiceMock(Service.class);
        expect(service.getDesiredStackId()).andReturn(stackId).atLeastOnce();
        expect(AmbariManagementControllerImplTest.clusters.getCluster("c1")).andReturn(cluster).atLeastOnce();
        StackInfo stackInfo = createNiceMock(StackInfo.class);
        expect(AmbariManagementControllerImplTest.ambariMetaInfo.getStack("HDP", "2.1")).andReturn(stackInfo).atLeastOnce();
        expect(AmbariManagementControllerImplTest.ambariMetaInfo.getCommonWidgetsDescriptorFile()).andReturn(null).once();
        replay(injector, AmbariManagementControllerImplTest.clusters, AmbariManagementControllerImplTest.ambariMetaInfo, stackInfo, cluster, service, repoVersionDAO, repoVersion);
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        Class<?> c = controller.getClass();
        Field f = c.getDeclaredField("repositoryVersionDAO");
        f.setAccessible(true);
        f.set(controller, repoVersionDAO);
        Properties p = new Properties();
        p.setProperty("", "");
        Configuration configuration = new Configuration(p);
        f = c.getDeclaredField("configs");
        f.setAccessible(true);
        f.set(controller, configuration);
        ClusterRequest cr = new ClusterRequest(null, "c1", "HDP-2.1", null);
        controller.createCluster(cr);
        // verification
        verify(injector, AmbariManagementControllerImplTest.clusters, AmbariManagementControllerImplTest.ambariMetaInfo, stackInfo, cluster, repoVersionDAO, repoVersion);
    }

    @Test
    public void testRegisterMpacks() throws Exception {
        MpackRequest mpackRequest = createNiceMock(MpackRequest.class);
        RequestStatusResponse response = new RequestStatusResponse(new Long(201));
        Mpack mpack = new Mpack();
        mpack.setResourceId(((long) (100)));
        mpack.setModules(new ArrayList<Module>());
        mpack.setPrerequisites(new HashMap<String, String>());
        mpack.setRegistryId(new Long(100));
        mpack.setVersion("3.0");
        mpack.setMpackUri("abc.tar.gz");
        mpack.setDescription("Test mpack");
        mpack.setName("testMpack");
        MpackResponse mpackResponse = new MpackResponse(mpack);
        Injector injector = createNiceMock(Injector.class);
        expect(injector.getInstance(MaintenanceStateHelper.class)).andReturn(null).atLeastOnce();
        expect(AmbariManagementControllerImplTest.ambariMetaInfo.registerMpack(mpackRequest)).andReturn(mpackResponse);
        AmbariManagementControllerImplTest.ambariMetaInfo.init();
        expectLastCall();
        replay(AmbariManagementControllerImplTest.ambariMetaInfo, injector);
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        assertEquals(mpackResponse, controller.registerMpack(mpackRequest));
    }

    @Test
    public void testGetPacklets() throws Exception {
        Long mpackId = new Long(100);
        ArrayList<Module> packletArrayList = new ArrayList<>();
        Module samplePacklet = new Module();
        Injector injector = createNiceMock(Injector.class);
        // samplePacklet.setType(Packlet.PackletType.SERVICE_PACKLET);
        samplePacklet.setVersion("3.0.0");
        samplePacklet.setName("NIFI");
        samplePacklet.setDefinition("nifi.tar.gz");
        packletArrayList.add(samplePacklet);
        expect(injector.getInstance(MaintenanceStateHelper.class)).andReturn(null).atLeastOnce();
        expect(AmbariManagementControllerImplTest.ambariMetaInfo.getModules(mpackId)).andReturn(packletArrayList).atLeastOnce();
        replay(AmbariManagementControllerImplTest.ambariMetaInfo, injector);
        AmbariManagementController controller = new AmbariManagementControllerImpl(null, AmbariManagementControllerImplTest.clusters, injector);
        setAmbariMetaInfo(AmbariManagementControllerImplTest.ambariMetaInfo, controller);
        assertEquals(packletArrayList, controller.getModules(mpackId));
    }
}
