package com.jrxna.mongoose.service;

import com.jrxna.mongoose.model.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TemplateService {

    private final SiteConfig config = new SiteConfig();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private final String pageTemplate;

    public TemplateService() {
        try (InputStream is = getClass().getResourceAsStream("/templates/page.html")) {
            if (is == null) {
                throw new RuntimeException("Template file not found: /templates/page.html");
            }
            this.pageTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template", e);
        }
    }

    public String generatePage(MarkdownFile file, List<Section> sections) {
        // Calculate relative path to root based on file depth
        String pathToRoot = calculatePathToRoot(file.getOutputPath());

        // Generate sidebar HTML
        String sidebarHtml = generateSidebar(file, sections);

        // Generate TOC HTML
        String tocHtml = generateTOC(file);

        // Replace placeholders in template
        String html = pageTemplate
                .replace("{{PAGE_TITLE}}", escapeHtml(file.getTitle()))
                .replace("{{SITE_NAME}}", escapeHtml(config.getSiteName()))
                .replace("{{PATH_TO_ROOT}}", pathToRoot)
                .replace("{{PAGE_DATE}}", file.getDate().format(DATE_FORMATTER))
                .replace("{{SIDEBAR_CONTENT}}", sidebarHtml)
                .replace("{{PAGE_CONTENT}}", file.getHtmlContent())
                .replace("{{TOC_CONTENT}}", tocHtml);

        return html;
    }

    private String generateSidebar(MarkdownFile file, List<Section> sections) {
        StringBuilder html = new StringBuilder();
        // Find which section contains the current file
        Section currentSection = null;
        for (Section section : sections) {
            if (section.getFiles().contains(file)) {
                currentSection = section;
                break;
            }
        }

        // Special case: if this is index.html, open the first section
        if (file.getOutputPath().equals("index.html") && !sections.isEmpty()) {
            currentSection = sections.get(0);
        }

        // Generate sidebar sections
        for (Section section : sections) {
            html.append("<div class=\"sidebar-section\">\n");
            // Add 'open' attribute if this section contains the current file
            if (section.equals(currentSection)) {
                html.append("    <details open>\n");
            } else {
                html.append("    <details>\n");
            }
            html.append("        <summary class=\"sidebar-title\">")
                    .append(escapeHtml(section.getTitle()))
                    .append("</summary>\n");
            html.append("        <ul class=\"note-list\">\n");

            for (MarkdownFile sectionFile : section.getFiles()) {
                // Compare by filePath instead of object equality
                String activeClass = sectionFile.getFilePath().equals(file.getFilePath()) ? " active" : "";
                String relativePath = calculateRelativePath(file.getOutputPath(), sectionFile.getOutputPath());
                html.append("            <li class=\"note-item\"><a href=\"")
                        .append(escapeHtml(relativePath))
                        .append("\" class=\"note-link")
                        .append(activeClass)
                        .append("\">")
                        .append(escapeHtml(sectionFile.getTitle()))
                        .append("</a></li>\n");
            }

            html.append("        </ul>\n");
            html.append("    </details>\n");
            html.append("</div>\n");
        }

        return html.toString();
    }

    private String generateTOC(MarkdownFile file) {
        StringBuilder html = new StringBuilder();
        if (!file.getTableOfContents().isEmpty()) {
            html.append("<ul class=\"toc-list\">\n");
            for (TOCItem item : file.getTableOfContents()) {
                html.append("    <li class=\"toc-item\"><a href=\"#")
                        .append(escapeHtml(item.getId()))
                        .append("\" class=\"toc-link ")
                        .append(escapeHtml(item.getLevelClass()))
                        .append("\">")
                        .append(escapeHtml(item.getText()))
                        .append("</a></li>\n");
            }
            html.append("</ul>\n");
        }

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Calculate path to root based on file depth
    private String calculatePathToRoot(String outputPath) {
        long depth = outputPath.chars().filter(ch -> ch == '/').count();
        if (depth == 0) {
            return "";
        }
        return "../".repeat((int) depth);
    }

    private String calculateRelativePath(String fromPath, String toPath) {
        // Get directory parts
        String[] fromParts = fromPath.contains("/")
                ? fromPath.substring(0, fromPath.lastIndexOf('/')).split("/")
                : new String[0];
        String[] toParts = toPath.contains("/")
                ? toPath.substring(0, toPath.lastIndexOf('/')).split("/")
                : new String[0];

        String toFile = toPath.contains("/")
                ? toPath.substring(toPath.lastIndexOf('/') + 1)
                : toPath;

        // Same directory
        if (fromParts.length == toParts.length &&
                (fromParts.length == 0 || java.util.Arrays.equals(fromParts, toParts))) {
            return toFile;
        }

        // From root to subdirectory
        if (fromParts.length == 0) {
            return toPath;
        }

        // From subdirectory to root
        if (toParts.length == 0) {
            return "../" + toPath;
        }

        // Different subdirectories - go up to root, then down
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fromParts.length; i++) {
            result.append("../");
        }
        result.append(toPath);

        return result.toString();
    }
}