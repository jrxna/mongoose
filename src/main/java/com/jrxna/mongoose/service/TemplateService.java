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
        StringBuilder html = new StringBuilder();
        
        html.append(generateHead(file));
        html.append("<body>\n");
        html.append("  <div class=\"page-wrapper\">\n");
        html.append("    <div class=\"container\">\n");
        html.append(generateHeader());
        html.append(generateSidebar(sections, file));
        html.append(generateMainContent(file));
        html.append(generateTOC(file));
        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }

    private String generateHead(MarkdownFile file) {
        return String.format("""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>%s | %s</title>
    <link rel="icon" type="image/png" href="%s">
    %s
</head>
""", file.getTitle(), config.getSiteName(), config.getFaviconPath(), generateStyles());
    }

    private String generateStyles() {
        return """
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        :root {
            --primary: #0071C5;
            --bg-primary: #0a0e17;
            --text-primary: #f5f6fa;
            --text-muted: #b0b3b8;
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
        
        .page-wrapper { width: 100%; min-height: 100vh; }
        .container { display: grid; min-height: 100vh; }
        
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
            background: var(--bg-primary);
        }
        
        .logo { display: flex; align-items: center; }
        
        .tutorials-link {
            color: var(--text-primary);
            text-decoration: none;
            transition: all 0.2s;
        }
        
        .tutorials-link:hover { color: var(--primary); }
        
        .sidebar {
            background: var(--bg-primary);
            scrollbar-width: none;
            -ms-overflow-style: none;
        }
        
        .sidebar::-webkit-scrollbar { display: none; }
        
        .sidebar-title {
            color: var(--text-primary);
            padding: 12px 24px;
            cursor: pointer;
            user-select: none;
            transition: all 0.2s;
        }
        
        .sidebar-title:hover { color: var(--primary); }
        
        details summary::-webkit-details-marker,
        details summary::marker { display: none; }
        
        details[open] .sidebar-title {
            background: var(--primary);
            color: var(--text-primary);
        }
        
        .note-list { list-style: none; }
        
        .note-link {
            display: block;
            padding: 12px 24px;
            color: var(--text-primary);
            text-decoration: none;
            transition: all 0.2s;
            border-left: 5px solid transparent;
        }
        
        .note-link:hover { color: var(--primary); }
        .note-link.active {
            color: var(--text-primary);
            border-left-color: var(--primary);
        }
        
        .main-content { background: var(--bg-primary); }
        .content-header { margin-bottom: 12px; }
        .page-title { margin-top: 0; color: var(--primary); }
        .page-meta { color: var(--text-muted); font-size: 12px; }
        
        .content-body h1, .content-body h2, .content-body h3 {
            margin-bottom: 12px;
            color: var(--primary);
        }
        
        .content-body p {
            margin-bottom: 12px;
            color: var(--text-primary);
        }
        
        .content-body ul, .content-body ol {
            margin: 0px 0px 16px 32px;
        }
        
        .content-body li { color: var(--text-primary); }
        
        .content-body code {
            background: var(--bg-primary);
            padding: 2px 6px;
            border-radius: 4px;
            border: 1px solid var(--border);
        }
        
        .content-body blockquote {
            border-left: 5px solid var(--primary);
            padding-left: 12px;
            margin: 24px 0;
            color: var(--text-primary);
            font-style: italic;
        }
        
        .toc { background: var(--bg-primary); }
        .toc-list { list-style: none; }
        
        .toc-link {
            display: block;
            color: var(--text-muted);
            text-decoration: none;
            font-size: 12px;
            padding-bottom: 12px;
            transition: color 0.2s;
        }
        
        .toc-link:hover { color: var(--primary); }
        .toc-link.level-2 { padding-left: 0; }
        .toc-link.level-3 { padding-left: 12px; }
        
        @media (min-width: 1280px) {
            .page-wrapper { width: 1280px; margin: 0 auto; }
            
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
            .container {
                grid-template: "main" auto "toc" auto / 1fr;
                padding-top: 60px;
            }
            
            .sidebar { display: none; }
            
            .main-content {
                grid-area: main;
                padding: 32px 24px;
            }
            
            .toc {
                grid-area: toc;
                padding: 24px;
                border-top: 1px solid var(--border);
            }
        }
    </style>
""";
    }

    private String generateHeader() {
        return String.format("""
      <header class="header">
        <div style="width:50px"></div>
        <div class="logo">
          <img src="%s" alt="%s Logo" style="height:30px; vertical-align:middle;">
        </div>
        <a href="#" class="tutorials-link">Tutorials</a>
      </header>
""", config.getLogoPath(), config.getSiteName());
    }

    private String generateSidebar(List<Section> sections, MarkdownFile currentFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("      <nav class=\"sidebar\">\n");
        
        for (Section section : sections) {
            sb.append("        <div class=\"sidebar-section\">\n");
            sb.append("          <details>\n");
            sb.append(String.format("            <summary class=\"sidebar-title\">%s</summary>\n", 
                section.getTitle()));
            sb.append("            <ul class=\"note-list\">\n");
            
            for (MarkdownFile file : section.getFiles()) {
                String activeClass = file.equals(currentFile) ? " active" : "";
                sb.append(String.format("              <li class=\"note-item\"><a href=\"%s\" class=\"note-link%s\">%s</a></li>\n",
                    file.getOutputPath(), activeClass, file.getTitle()));
            }
            
            sb.append("            </ul>\n");
            sb.append("          </details>\n");
            sb.append("        </div>\n");
        }
        
        sb.append("      </nav>\n");
        return sb.toString();
    }

    private String generateMainContent(MarkdownFile file) {
        String formattedDate = file.getDate().format(DATE_FORMATTER);
        
        return String.format("""
      <main class="main-content">
        <div class="content-header">
          <h1 class="page-title">%s</h1>
          <div class="page-meta">%s</div>
        </div>
        <div class="content-body">
          %s
        </div>
      </main>
""", file.getTitle(), formattedDate, file.getHtmlContent());
    }

    private String generateTOC(MarkdownFile file) {
        if (file.getTableOfContents().isEmpty()) {
            return "      <aside class=\"toc\"></aside>\n";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("      <aside class=\"toc\">\n");
        sb.append("        <ul class=\"toc-list\">\n");
        
        for (TOCItem item : file.getTableOfContents()) {
            sb.append(String.format(
                "          <li class=\"toc-item\"><a href=\"#%s\" class=\"toc-link %s\">%s</a></li>\n",
                item.getId(), item.getLevelClass(), item.getText()
            ));
        }
        
        sb.append("        </ul>\n");
        sb.append("      </aside>\n");
        
        return sb.toString();
    }
}