

package com.squareup.javapoet;


public class AmplUtilTest {
    @org.junit.Test
    public void characterLiteral() {
        org.junit.Assert.assertEquals("a", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('a'));
        org.junit.Assert.assertEquals("b", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('b'));
        org.junit.Assert.assertEquals("c", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('c'));
        org.junit.Assert.assertEquals("%", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('%'));
        org.junit.Assert.assertEquals("\\b", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\b'));
        org.junit.Assert.assertEquals("\\t", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\t'));
        org.junit.Assert.assertEquals("\\n", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\n'));
        org.junit.Assert.assertEquals("\\f", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\f'));
        org.junit.Assert.assertEquals("\\r", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\r'));
        org.junit.Assert.assertEquals("\"", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('"'));
        org.junit.Assert.assertEquals("\\\'", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\''));
        org.junit.Assert.assertEquals("\\\\", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\\'));
        org.junit.Assert.assertEquals("\\u0000", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes(' '));
        org.junit.Assert.assertEquals("\\u0007", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes(''));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('?'));
        org.junit.Assert.assertEquals("\\u007f", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes(''));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u00bf'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u00ff'));
        org.junit.Assert.assertEquals("\\u0000", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes(' '));
        org.junit.Assert.assertEquals("\\u0001", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes(''));
        org.junit.Assert.assertEquals("\\u0002", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes(''));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u20ac'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u2603'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u2660'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u2663'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u2665'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u2666'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u2735'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\u273a'));
        org.junit.Assert.assertEquals("?", com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes('\uff0f'));
    }

    @org.junit.Test
    public void stringLiteral() {
        stringLiteral("abc");
        stringLiteral("????");
        stringLiteral("\u20ac\\t@\\t$", "\u20ac\t@\t$", " ");
        stringLiteral("abc();\\n\"\n  + \"def();", "abc();\ndef();", " ");
        stringLiteral("This is \\\"quoted\\\"!", "This is \"quoted\"!", " ");
        stringLiteral("e^{i\\\\pi}+1=0", "e^{i\\pi}+1=0", " ");
    }

    void stringLiteral(java.lang.String string) {
        stringLiteral(string, string, " ");
    }

    void stringLiteral(java.lang.String expected, java.lang.String value, java.lang.String indent) {
        org.junit.Assert.assertEquals((("\"" + expected) + "\""), com.squareup.javapoet.Util.stringLiteralWithDoubleQuotes(value, indent));
    }
}

