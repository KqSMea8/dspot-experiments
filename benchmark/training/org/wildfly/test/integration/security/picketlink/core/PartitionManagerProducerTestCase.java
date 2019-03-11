/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.test.integration.security.picketlink.core;


import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;


/**
 *
 *
 * @author Pedro Igor
 */
@RunWith(Arquillian.class)
public class PartitionManagerProducerTestCase {
    public static final String CONFIGURATION_NAME = "partition.manager.producer";

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials credentials;

    @Test
    @InSequence(1)
    public void testProducedPartitionManagerInstance() {
        Assert.assertEquals(1, this.partitionManager.getConfigurations().size());
        IdentityConfiguration configuration = this.partitionManager.getConfigurations().iterator().next();
        Assert.assertEquals(PartitionManagerProducerTestCase.CONFIGURATION_NAME, configuration.getName());
    }

    @Test
    @InSequence(2)
    public void testAuthentication() {
        User user = new User("johny");
        this.identityManager.add(user);
        Password password = new Password("abcd1234");
        this.identityManager.updateCredential(user, password);
        Role role = new Role("admin");
        this.identityManager.add(role);
        BasicModel.grantRole(this.relationshipManager, user, role);
        this.credentials.setUserId("johny");
        this.credentials.setPassword("abcd1234");
        this.identity.login();
        Assert.assertTrue(this.identity.isLoggedIn());
        user = BasicModel.getUser(this.identityManager, "johny");
        role = BasicModel.getRole(this.identityManager, "admin");
        Assert.assertTrue(BasicModel.hasRole(this.relationshipManager, user, role));
    }

    static class PartitionManagerProducer {
        @Produces
        @PicketLink
        public PartitionManager getPartitionManager() {
            IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
            builder.named(PartitionManagerProducerTestCase.CONFIGURATION_NAME).stores().file().preserveState(false).supportAllFeatures();
            DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
            return partitionManager;
        }
    }
}
