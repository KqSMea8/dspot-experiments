/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.status;


import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class StatusUtilTest {
    Context context = new ContextBase();

    StatusUtil statusUtil = new StatusUtil(context);

    @Test
    public void emptyStatusListShouldResultInNotFound() {
        Assert.assertEquals((-1), statusUtil.timeOfLastReset());
    }

    @Test
    public void withoutResetsStatusUtilShouldReturnNotFound() {
        context.getStatusManager().add(new InfoStatus("test", this));
        Assert.assertEquals((-1), statusUtil.timeOfLastReset());
    }

    @Test
    public void statusListShouldReturnLastResetTime() {
        context.getStatusManager().add(new InfoStatus("test", this));
        long resetTime = System.currentTimeMillis();
        context.getStatusManager().add(new InfoStatus(CoreConstants.RESET_MSG_PREFIX, this));
        context.getStatusManager().add(new InfoStatus("bla", this));
        Assert.assertTrue((resetTime <= (statusUtil.timeOfLastReset())));
    }
}
