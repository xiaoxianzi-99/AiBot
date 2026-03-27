package com.pei.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MarkdownTemplate}
 */
class MarkdownTemplateTest {

    @Test
    void wrap_nullInput_producesValidHtml() {
        String result = MarkdownTemplate.wrap(null);
        assertTrue(result.startsWith("<html>"));
        assertTrue(result.endsWith("</html>"));
        assertTrue(result.contains("<body>"));
        assertTrue(result.contains("</body>"));
    }

    @Test
    void wrap_emptyInput_producesValidHtml() {
        String result = MarkdownTemplate.wrap("");
        assertTrue(result.startsWith("<html>"));
        assertTrue(result.contains("<body></body>"));
    }

    @Test
    void wrap_htmlFragment_embedsFragmentInBody() {
        String fragment = "<p>Hello World</p>";
        String result = MarkdownTemplate.wrap(fragment);
        assertTrue(result.contains("<body><p>Hello World</p></body>"));
    }

    @Test
    void wrap_includesCharsetMetaTag() {
        String result = MarkdownTemplate.wrap("test");
        assertTrue(result.contains("charset=\"utf-8\""));
    }

    @Test
    void wrap_includesStyleTag() {
        String result = MarkdownTemplate.wrap("test");
        assertTrue(result.contains("<style>"));
        assertTrue(result.contains("</style>"));
    }

    @Test
    void wrap_includesViewportMetaTag() {
        String result = MarkdownTemplate.wrap("test");
        assertTrue(result.contains("viewport"));
    }

    @Test
    void wrap_cssContainsFontFamily() {
        String result = MarkdownTemplate.wrap("test");
        assertTrue(result.contains("font-family"));
    }

    @Test
    void wrap_cssContainsCodeBlockStyling() {
        String result = MarkdownTemplate.wrap("test");
        assertTrue(result.contains("pre"));
        assertTrue(result.contains("code"));
    }
}
