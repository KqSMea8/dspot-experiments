/**
 * Copyright (C) 2018 Ryszard Wi?niewski <brut.alll@gmail.com>
 *  Copyright (C) 2018 Connor Tumbleson <connor.tumbleson@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package brut.androlib.decode;


import brut.androlib.BaseTest;
import brut.androlib.TestUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Connor Tumbleson <connor.tumbleson@gmail.com>
 */
public class ExternalEntityTest extends BaseTest {
    @Test
    public void doctypeTest() throws IOException {
        String expected = TestUtils.replaceNewlines(("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + ((("<manifest android:versionCode=\"1\" android:versionName=\"1.0\" android:compileSdkVersion=\"23\" android:compileSdkVersionCodename=\"6.0-2438415\" " + "hardwareAccelerated=\"true\" package=\"com.ibotpeaches.doctype\" platformBuildVersionCode=\"24\" platformBuildVersionName=\"6.0-2456767\"  ") + "xmlns:android=\"http://schemas.android.com/apk/res/android\">    <supports-screens android:anyDensity=\"true\" android:smallScreens=\"true\" ") + "android:normalScreens=\"true\" android:largeScreens=\"true\" android:resizeable=\"true\" android:xlargeScreens=\"true\" /></manifest>")));
        byte[] encoded = Files.readAllBytes(Paths.get((((((BaseTest.sTestOrigDir) + (File.separator)) + "output") + (File.separator)) + "AndroidManifest.xml")));
        String obtained = TestUtils.replaceNewlines(new String(encoded));
        Assert.assertEquals(expected, obtained);
    }
}
