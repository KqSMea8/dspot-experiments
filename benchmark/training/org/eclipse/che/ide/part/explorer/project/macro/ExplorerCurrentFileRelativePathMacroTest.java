/**
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.explorer.project.macro;


import ExplorerCurrentFileRelativePathMacro.KEY;
import com.google.common.base.Joiner;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


/**
 * Unit tests for the {@link ExplorerCurrentFileRelativePathMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class ExplorerCurrentFileRelativePathMacroTest extends AbstractExplorerMacroTest {
    private ExplorerCurrentFileRelativePathMacro provider;

    @Test
    public void testGetKey() throws Exception {
        Assert.assertSame(provider.getName(), KEY);
    }

    @Test
    public void getValue() throws Exception {
        initWithOneFile();
        provider.expand();
        Mockito.verify(promiseProvider).resolve(ArgumentMatchers.eq(AbstractExplorerMacroTest.FILE_1_PATH));
    }

    @Test
    public void getMultipleValues() throws Exception {
        initWithTwoFiles();
        provider.expand();
        Mockito.verify(promiseProvider).resolve(ArgumentMatchers.eq(Joiner.on(", ").join(AbstractExplorerMacroTest.FILE_1_PATH, AbstractExplorerMacroTest.FILE_2_PATH)));
    }

    @Test
    public void getEmptyValues() throws Exception {
        initWithNoFiles();
        provider.expand();
        Mockito.verify(promiseProvider).resolve(ArgumentMatchers.eq(""));
    }
}
