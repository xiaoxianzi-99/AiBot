package com.pei.service;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.Arrays;

public class MarkdownUtil {
    // 该工具类依赖 flexmark 库，如果在 IDE 中仍然显示无法解析，请先执行 Maven Reload 或 mvn compile 以下载依赖
    private static final MutableDataSet OPTIONS = new MutableDataSet()
            .set(HtmlRenderer.SOFT_BREAK, "<br />")
            .set(HtmlRenderer.HARD_BREAK, "<br />")
            .set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create(),
                    TaskListExtension.create(),
                    StrikethroughExtension.create(),
                    AutolinkExtension.create(),
                    EmojiExtension.create()
            ));
    private static final Parser PARSER = Parser.builder(OPTIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

    public static String toHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) return "";
        return RENDERER.render(PARSER.parse(markdown));
    }
}
