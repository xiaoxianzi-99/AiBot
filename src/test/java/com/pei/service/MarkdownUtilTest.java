package com.pei.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MarkdownUtil}
 */
class MarkdownUtilTest {

    @Test
    void toHtml_nullInput_returnsEmptyString() {
        assertEquals("", MarkdownUtil.toHtml(null));
    }

    @Test
    void toHtml_emptyInput_returnsEmptyString() {
        assertEquals("", MarkdownUtil.toHtml(""));
    }

    @Test
    void toHtml_plainText_wrapsInParagraph() {
        String result = MarkdownUtil.toHtml("Hello World");
        assertTrue(result.contains("Hello World"));
        assertTrue(result.contains("<p>"));
    }

    @Test
    void toHtml_boldText_rendersStrongTag() {
        String result = MarkdownUtil.toHtml("**bold**");
        assertTrue(result.contains("<strong>bold</strong>"));
    }

    @Test
    void toHtml_italicText_rendersEmTag() {
        String result = MarkdownUtil.toHtml("*italic*");
        assertTrue(result.contains("<em>italic</em>"));
    }

    @Test
    void toHtml_h1Header_rendersH1Tag() {
        String result = MarkdownUtil.toHtml("# Heading 1");
        assertTrue(result.contains("<h1>"));
        assertTrue(result.contains("Heading 1"));
    }

    @Test
    void toHtml_h2Header_rendersH2Tag() {
        String result = MarkdownUtil.toHtml("## Heading 2");
        assertTrue(result.contains("<h2>"));
        assertTrue(result.contains("Heading 2"));
    }

    @Test
    void toHtml_unorderedList_rendersUlAndLiTags() {
        String result = MarkdownUtil.toHtml("- item 1\n- item 2");
        assertTrue(result.contains("<ul>"));
        assertTrue(result.contains("<li>"));
        assertTrue(result.contains("item 1"));
        assertTrue(result.contains("item 2"));
    }

    @Test
    void toHtml_orderedList_rendersOlAndLiTags() {
        String result = MarkdownUtil.toHtml("1. first\n2. second");
        assertTrue(result.contains("<ol>"));
        assertTrue(result.contains("<li>"));
        assertTrue(result.contains("first"));
        assertTrue(result.contains("second"));
    }

    @Test
    void toHtml_inlineCode_rendersCodeTag() {
        String result = MarkdownUtil.toHtml("`System.out.println()`");
        assertTrue(result.contains("<code>"));
        assertTrue(result.contains("System.out.println()"));
    }

    @Test
    void toHtml_codeBlock_rendersPreAndCodeTags() {
        String result = MarkdownUtil.toHtml("```\nint x = 1;\n```");
        assertTrue(result.contains("<pre>"));
        assertTrue(result.contains("<code>"));
        assertTrue(result.contains("int x = 1;"));
    }

    @Test
    void toHtml_strikethroughText_rendersDelTag() {
        String result = MarkdownUtil.toHtml("~~deleted~~");
        assertTrue(result.contains("<del>deleted</del>") || result.contains("<s>deleted</s>"));
    }

    @Test
    void toHtml_blockquote_rendersBlockquoteTag() {
        String result = MarkdownUtil.toHtml("> quoted text");
        assertTrue(result.contains("<blockquote>"));
        assertTrue(result.contains("quoted text"));
    }

    @Test
    void toHtml_table_rendersTableTag() {
        String markdown = "| A | B |\n|---|---|\n| 1 | 2 |";
        String result = MarkdownUtil.toHtml(markdown);
        assertTrue(result.contains("<table>"));
        assertTrue(result.contains("<th>"));
        assertTrue(result.contains("<td>"));
    }

    @Test
    void toHtml_multipleCallsReturnConsistentResults() {
        String input = "**test**";
        String result1 = MarkdownUtil.toHtml(input);
        String result2 = MarkdownUtil.toHtml(input);
        assertEquals(result1, result2);
    }
}
