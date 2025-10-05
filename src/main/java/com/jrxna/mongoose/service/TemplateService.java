package com.jrxna.mongoose.service;

import com.jrxna.mongoose.model.*;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TemplateService {

    private final SiteConfig config = new SiteConfig();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public String generatePage(MarkdownFile file, List<Section> sections) {
        // Calculate relative path to root based on file depth
        String pathToRoot = calculatePathToRoot(file.getOutputPath());

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>""").append(file.getTitle()).append(" | ").append(config.getSiteName()).append("""
                </title>
                    <link rel="icon" type="image/png" href=\"""").append(pathToRoot).append("""
                assets/images/JRXNAFavicon.png">
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }

                        :root {
                            --primary: #0071C5;
                            --bg-primary: #0a0e17;
                            --text-primary: #f5f6fa;
                            --text-muted: #b0b3b8;
                            --success: #2ea043;
                            --error: #f85149;
                            --warning: #f0b323;
                            --border: #2a2f3e;
                        }

                        body {
                            font-family: Arial, Helvetica, sans-serif;
                            background: var(--bg-primary);
                            color: var(--text-primary);
                            font-size: 14px;
                            line-height: 1.6;
                            display: flex;
                            justify-content: center;
                        }

                        .page-wrapper {
                            width: 100%;
                            min-height: 100vh;
                        }

                        .container {
                            display: grid;
                            min-height: 100vh;
                        }

                        .header {
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            padding: 0 24px;
                            z-index: 100;
                            position: fixed;
                            inset: 0;
                            height: 60px;
                            border: 1px solid var(--border);
                            border-top: none;
                        }

                        .header,
                        .sidebar {
                            background: var(--bg-primary);
                        }

                        .main-content,
                        .toc {
                            background: var(--bg-primary);
                        }

                        .logo {
                            display: flex;
                            align-items: center;
                        }

                        .tutorials-link {
                            color: var(--text-primary);
                            text-decoration: none;
                            transition: all 0.2s;
                        }

                        .tutorials-link:hover,
                        .menu-toggle-label:hover {
                            color: var(--primary);
                        }

                        .menu-toggle-label {
                            color: var(--text-primary);
                            display: none;
                        }

                        .menu-toggle {
                            display: none;
                        }

                        .sidebar-title {
                            color: var(--text-primary);
                            padding: 12px 24px;
                            cursor: pointer;
                            user-select: none;
                            transition: all 0.2s;
                        }

                        .sidebar-title:hover {
                            color: var(--primary);
                        }

                        details summary::-webkit-details-marker,
                        details summary::marker {
                            display: none;
                        }

                        details[open] .sidebar-title {
                            background: var(--primary);
                            color: var(--text-primary);
                        }

                        .note-list {
                            list-style: none;
                        }

                        .note-link {
                            display: block;
                            padding: 12px 24px;
                            color: var(--text-primary);
                            text-decoration: none;
                            transition: all 0.2s;
                            border-left: 5px solid transparent;
                        }

                        .note-link:hover {
                            color: var(--primary);
                        }

                        .note-link.active {
                            color: var(--text-primary);
                            border-left-color: var(--primary);
                        }

                        .content-header {
                            margin-bottom: 12px;
                        }

                        .page-title {
                            margin-top: 0;
                            color: var(--primary);
                        }

                        .page-meta {
                            color: var(--text-muted);
                            font-size: 12px;
                        }

                        h1,
                        h2,
                        h3 {
                            margin-bottom: 12px;
                            color: var(--primary);
                        }

                        .content-body p {
                            margin-bottom: 12px;
                            color: var(--text-primary);
                        }

                        .content-body ul,
                        .content-body ol {
                            margin: 0px 0px 16px 32px;
                        }

                        .content-body li {
                            color: var(--text-primary);
                        }

                        .content-body code {
                            background: var(--bg-primary);
                            padding: 2px 6px;
                            border-radius: 4px;
                            font-family: 'JetBrains';
                            border: 1px solid var(--border);
                        }

                        .content-body blockquote {
                            border-left: 5px solid var(--primary);
                            padding-left: 12px;
                            margin: 24px 0;
                            color: var(--text-primary);
                            font-style: italic;
                        }

                        .toc-list {
                            list-style: none;
                        }

                        .toc-link {
                            display: block;
                            color: var(--text-muted);
                            text-decoration: none;
                            font-size: 12px;
                            padding-bottom: 12px;
                            transition: color 0.2s;
                        }

                        .toc-link:hover {
                            color: var(--primary);
                        }

                        .toc-link.level-2 {
                            padding-left: 0;
                        }

                        .toc-link.level-3 {
                            padding-left: 12px;
                        }

                        .sidebar::-webkit-scrollbar,
                        .main-content::-webkit-scrollbar,
                        .toc::-webkit-scrollbar {
                            display: none;
                        }

                        .sidebar,
                        .main-content,
                        .toc {
                            scrollbar-width: none;
                            -ms-overflow-style: none;
                        }

                        @media (min-width: 1280px) {
                            .page-wrapper {
                                width: 1280px;
                                margin: 0 auto;
                            }

                            .container {
                                grid-template: "sidebar main toc" 1fr / 320px 640px 320px;
                            }

                            .header {
                                left: 50%;
                                transform: translateX(-50%);
                                width: 1280px;
                            }

                            .sidebar {
                                grid-area: sidebar;
                                padding: 84px 0 0;
                                overflow-y: auto;
                                height: 100vh;
                                border-left: 1px solid var(--border);
                                border-right: 1px solid var(--border);
                            }

                            .main-content {
                                grid-area: main;
                                padding: 84px 24px 24px;
                                overflow-y: auto;
                                height: 100vh;
                            }

                            .toc {
                                grid-area: toc;
                                padding: 84px 24px 24px;
                                overflow-y: auto;
                                height: 100vh;
                                border-left: 1px solid var(--border);
                                border-right: 1px solid var(--border);
                            }
                        }

                        @media (max-width: 1280px) {
                            .page-wrapper {
                                border-left: 1px solid var(--border);
                                border-right: 1px solid var(--border);
                            }

                            .container {
                                grid-template: "main" auto "toc" auto / 1fr;
                                padding-top: 60px;
                            }

                            .header {
                                display: grid;
                                grid-template-columns: 50px 1fr auto;
                                align-items: center;
                            }

                            .logo {
                                justify-content: center;
                            }

                            .menu-toggle-label {
                                display: block;
                            }

                            .sidebar {
                                position: fixed;
                                top: 60px;
                                left: 0;
                                width: 280px;
                                height: calc(100vh - 60px);
                                transform: translateX(-100%);
                                transition: transform 0.3s;
                                z-index: 150;
                                border-right: 1px solid var(--border);
                                overflow-y: auto;
                                padding: 24px 0;
                            }

                            #menu-toggle:checked~.sidebar {
                                transform: translateX(0);
                            }

                            .overlay {
                                display: none;
                                position: fixed;
                                top: 60px;
                                left: 0;
                                right: 0;
                                bottom: 0;
                                background: rgba(0, 0, 0, 0.5);
                                z-index: 149;
                                cursor: pointer;
                            }

                            #menu-toggle:checked~.overlay {
                                display: block;
                            }

                            .main-content {
                                grid-area: main;
                                padding: 32px 24px;
                                overflow-y: visible;
                                height: auto;
                            }

                            .toc {
                                grid-area: toc;
                                padding: 24px;
                                height: auto;
                                overflow-y: visible;
                                border-top: 1px solid var(--border);
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="page-wrapper">
                        <div class="container">
                            <input type="checkbox" id="menu-toggle" class="menu-toggle">
                            <header class="header">
                                <label for="menu-toggle" class="menu-toggle-label">Notes</label>
                                <div class="logo">
                                    <a href="https://jrxna.com">
                                        <img src=\"""").append(pathToRoot)
                .append("""
                        assets/images/JRXNALogoSmall.png" alt="JRXNA Logo" style="height:30px; vertical-align:middle;">
                                            </a>
                                        </div>
                                        <a href="https://youtube.com/@jrxna" target="_blank" class="tutorials-link">Tutorials</a>
                                    </header>
                                    <label for="menu-toggle" class="overlay"></label>
                                    <nav class="sidebar">
                        """);

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
            html.append("                <div class=\"sidebar-section\">\n");
            // Add 'open' attribute if this section contains the current file
            if (section.equals(currentSection)) {
                html.append("                    <details open>\n");
            } else {
                html.append("                    <details>\n");
            }
            html.append("                        <summary class=\"sidebar-title\">").append(section.getTitle())
                    .append("</summary>\n");
            html.append("                        <ul class=\"note-list\">\n");

            for (MarkdownFile sectionFile : section.getFiles()) {
                // Compare by filePath instead of object equality
                String activeClass = sectionFile.getFilePath().equals(file.getFilePath()) ? " active" : "";
                String relativePath = calculateRelativePath(file.getOutputPath(), sectionFile.getOutputPath());
                html.append("                            <li class=\"note-item\"><a href=\"")
                        .append(relativePath)
                        .append("\" class=\"note-link")
                        .append(activeClass)
                        .append("\">")
                        .append(sectionFile.getTitle())
                        .append("</a></li>\n");
            }

            html.append("                        </ul>\n");
            html.append("                    </details>\n");
            html.append("                </div>\n");
        }

        html.append("""
                </nav>
                <main class="main-content">
                    <div class="content-header">
                        <h1 class="page-title">""").append(file.getTitle()).append("""
                </h1>
                                    <div class="page-meta">""").append(file.getDate().format(DATE_FORMATTER)).append("""
                </div>
                                </div>
                                <div class="content-body">
                """).append(file.getHtmlContent()).append("""
                                </div>
                            </main>
                            <aside class="toc">
                """);

        // Generate TOC
        if (!file.getTableOfContents().isEmpty()) {
            html.append("                <ul class=\"toc-list\">\n");
            for (TOCItem item : file.getTableOfContents()) {
                html.append("                    <li class=\"toc-item\"><a href=\"#")
                        .append(item.getId())
                        .append("\" class=\"toc-link ")
                        .append(item.getLevelClass())
                        .append("\">")
                        .append(item.getText())
                        .append("</a></li>\n");
            }
            html.append("                </ul>\n");
        }

        html.append("""
                            </aside>
                        </div>
                    </div>
                </body>
                </html>""");

        return html.toString();
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