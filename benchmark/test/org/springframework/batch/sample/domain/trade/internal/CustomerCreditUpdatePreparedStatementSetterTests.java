/**
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.sample.domain.trade.internal;


import CustomerCreditUpdatePreparedStatementSetter.FIXED_AMOUNT;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.Test;
import org.springframework.batch.sample.domain.trade.CustomerCredit;


/**
 *
 *
 * @author Dave Syer
 */
public class CustomerCreditUpdatePreparedStatementSetterTests {
    private CustomerCreditUpdatePreparedStatementSetter setter = new CustomerCreditUpdatePreparedStatementSetter();

    private CustomerCredit credit;

    private PreparedStatement ps;

    /* Test method for {@link org.springframework.batch.sample.domain.trade.internal.CustomerCreditUpdatePreparedStatementSetter#setValues(CustomerCredit, PreparedStatement) } */
    @Test
    public void testSetValues() throws SQLException {
        ps.setBigDecimal(1, credit.getCredit().add(FIXED_AMOUNT));
        ps.setLong(2, credit.getId());
        setter.setValues(credit, ps);
    }
}
