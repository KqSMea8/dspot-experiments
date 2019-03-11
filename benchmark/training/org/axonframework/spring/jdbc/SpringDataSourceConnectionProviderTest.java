/**
 * Copyright (c) 2010-2014. Axon Framework
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
package org.axonframework.spring.jdbc;


import java.sql.Connection;
import javax.sql.DataSource;
import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.transaction.Transaction;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;


@ContextConfiguration(classes = SpringDataSourceConnectionProviderTest.Context.class)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class SpringDataSourceConnectionProviderTest {
    private Connection mockConnection;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ConnectionProvider connectionProvider;

    private SpringTransactionManager springTransactionManager;

    @DirtiesContext
    @Transactional
    @Test
    public void testConnectionNotCommittedWhenTransactionScopeOutsideUnitOfWork() throws Exception {
        Mockito.when(dataSource.getConnection()).thenAnswer(( invocation) -> {
            Assert.fail("Should be using an already existing connection.");
            return null;
        });
        UnitOfWork<?> uow = DefaultUnitOfWork.startAndGet(null);
        Connection connection = connectionProvider.getConnection();
        connection.commit();
        uow.commit();
    }

    @Test
    public void testConnectionCommittedWhenTransactionScopeInsideUnitOfWork() throws Exception {
        Mockito.doAnswer(( invocation) -> {
            final Object spy = Mockito.spy(invocation.callRealMethod());
            mockConnection = ((Connection) (spy));
            return spy;
        }).when(dataSource).getConnection();
        UnitOfWork<?> uow = DefaultUnitOfWork.startAndGet(null);
        Transaction transaction = springTransactionManager.startTransaction();
        uow.onCommit(( u) -> transaction.commit());
        uow.onRollback(( u) -> transaction.rollback());
        Connection innerConnection = connectionProvider.getConnection();
        Assert.assertNotSame(innerConnection, mockConnection);
        innerConnection.commit();
        Mockito.verify(mockConnection, Mockito.never()).commit();
        uow.commit();
        Mockito.verify(mockConnection).commit();
    }

    @ImportResource("classpath:/META-INF/spring/db-context.xml")
    @Configuration
    public static class Context {
        @Bean
        public ConnectionProvider connectionProvider(DataSource dataSource) {
            return new org.axonframework.common.jdbc.UnitOfWorkAwareConnectionProviderWrapper(new SpringDataSourceConnectionProvider(dataSource));
        }
    }
}
