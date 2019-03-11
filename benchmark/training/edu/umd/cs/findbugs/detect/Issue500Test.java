/**
 * Contributions to SpotBugs
 * Copyright (C) 2018, William R. Price
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.umd.cs.findbugs.detect;


import edu.umd.cs.findbugs.AbstractIntegrationTest;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcher;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcherBuilder;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author William R. Price
 */
public class Issue500Test extends AbstractIntegrationTest {
    @Test
    public void test() {
        performAnalysis("lambdas/Issue500.class");
        BugInstanceMatcher bugMatcher = new BugInstanceMatcherBuilder().build();
        Assert.assertThat(getBugCollection(), containsExactly(0, bugMatcher));
    }
}
