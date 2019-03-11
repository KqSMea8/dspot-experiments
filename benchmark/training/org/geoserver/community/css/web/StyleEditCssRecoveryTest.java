/**
 * (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.community.css.web;


import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.Styles;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.geoserver.wms.web.data.StyleEditPage;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for recovering CSS styles generated by the old (pre-pluggable styles) CSS extension. That
 * extension extension output styles based on generated SLD files, without saving a reference to the
 * user's CSS input, and as a result the new style editor treats them as SLD.
 */
public class StyleEditCssRecoveryTest extends GeoServerWicketTestSupport {
    String oldCssStyle = "OldCssStyle";

    String oldCssStyleWithFormatSLD = "OldCssStyle_Format_Set_To_SLD";

    String oldCssStyleWithSLDManuallyEdited = "OldCssStyle_SLD_Manually_Edited";

    Catalog catalog;

    /**
     * Test recovery of a CSS Style generated by the old CSS Extension, when the StyleInfo has no
     * declared <format> and its filename points to a derived SLD.
     */
    @Test
    public void testRecoverLostCssStyle() throws Exception {
        StyleInfo styleInfo = catalog.getStyleByName(oldCssStyle);
        StyleEditPage edit = new StyleEditPage(styleInfo);
        tester.startPage(edit);
        tester.assertRenderedPage(StyleEditPage.class);
        tester.assertNoErrorMessage();
        // Assert that the page displays the format as css
        tester.assertModelValue("styleForm:context:panel:format", "css");
        // Assert that the editor text area contains css
        String editorContents = ((String) (tester.getComponentFromLastRenderedPage("styleForm:styleEditor:editorContainer:editorParent:editor").getDefaultModelObject()));
        Styles.handler("css").parse(editorContents, null, null, null);
        // Assert that the catalog's StyleInfo is now a css style
        StyleInfo si = catalog.getStyleByName(oldCssStyle);
        Assert.assertEquals("css", si.getFormat());
        Assert.assertEquals(((oldCssStyle) + ".css"), si.getFilename());
    }

    /**
     * Test recovery of a CSS Style generated by the old CSS Extension, when the StyleInfo declares
     * <format>sld</format> and its filename points to a derived SLD.
     */
    @Test
    public void testRecoverLostCssStyleWithFormatSetToSLD() throws Exception {
        StyleInfo styleInfo = catalog.getStyleByName(oldCssStyleWithFormatSLD);
        StyleEditPage edit = new StyleEditPage(styleInfo);
        tester.startPage(edit);
        tester.assertRenderedPage(StyleEditPage.class);
        tester.assertNoErrorMessage();
        // Assert that the page displays the format as css
        tester.assertModelValue("styleForm:context:panel:format", "css");
        // Assert that the editor text area contains css
        String editorContents = ((String) (tester.getComponentFromLastRenderedPage("styleForm:styleEditor:editorContainer:editorParent:editor").getDefaultModelObject()));
        Styles.handler("css").parse(editorContents, null, null, null);
        // Assert that the catalog's StyleInfo is now a css style
        StyleInfo si = catalog.getStyleByName(oldCssStyleWithFormatSLD);
        Assert.assertEquals("css", si.getFormat());
        Assert.assertEquals(((oldCssStyleWithFormatSLD) + ".css"), si.getFilename());
    }

    /**
     * Test that the recovery code does not overwrite generated SLD styles if they were subsequently
     * edited.
     */
    @Test
    public void testIgnoreCssStyleIfSLDWasEdited() throws Exception {
        StyleInfo styleInfo = catalog.getStyleByName(oldCssStyleWithSLDManuallyEdited);
        StyleEditPage edit = new StyleEditPage(styleInfo);
        tester.startPage(edit);
        tester.assertRenderedPage(StyleEditPage.class);
        tester.assertNoErrorMessage();
        // Assert that the page displays the format as SLD
        tester.assertModelValue("styleForm:context:panel:format", "sld");
        // Assert that the editor text area contains SLD
        String editorContents = ((String) (tester.getComponentFromLastRenderedPage("styleForm:styleEditor:editorContainer:editorParent:editor").getDefaultModelObject()));
        Styles.handler("sld").parse(editorContents, null, null, null);
        // Assert that the catalog's StyleInfo is still a SLD style
        StyleInfo si = catalog.getStyleByName(oldCssStyleWithSLDManuallyEdited);
        Assert.assertEquals("sld", si.getFormat());
        Assert.assertEquals(((oldCssStyleWithSLDManuallyEdited) + ".sld"), si.getFilename());
    }
}
