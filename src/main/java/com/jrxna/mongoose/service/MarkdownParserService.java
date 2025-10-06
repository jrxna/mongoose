package com.jrxna.mongoose.service;

import com.jrxna.mongoose.model.MarkdownFile;
import com.jrxna.mongoose.model.TOCItem;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MarkdownParserService {

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final Yaml yaml;

    public MarkdownParserService() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                AutolinkExtension.create()));

        // Enable heading anchor IDs
        options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        options.set(HtmlRenderer.RENDER_HEADER_ID, true);

        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
        this.yaml = new Yaml();
    }

    public void parseMarkdown(MarkdownFile file) throws IOException {
        String content = Files.readString(Paths.get(file.getFilePath()));

        // Extract frontmatter
        Map<String, Object> frontmatter = extractFrontmatter(content);

        // Set title and date from frontmatter or defaults
        if (frontmatter.containsKey("title")) {
            file.setTitle((String) frontmatter.get("title"));
        } else {
            file.setTitle(generateTitleFromFilename(file.getFilePath()));
        }

        if (frontmatter.containsKey("date")) {
            file.setDate(parseDate((String) frontmatter.get("date")));
        } else {
            file.setDate(LocalDate.now());
        }

        // Remove frontmatter from content
        String markdownContent = removeFrontmatter(content);

        // Remove first H1 title and Date line from content
        markdownContent = removeHeaderAndDate(markdownContent);

        file.setContent(markdownContent);

        // Parse markdown to HTML
        Document document = parser.parse(markdownContent);
        String html = renderer.render(document);
        file.setHtmlContent(html);

        // Generate table of contents
        file.setTableOfContents(generateTOC(document));
    }

    private Map<String, Object> extractFrontmatter(String content) {
        Pattern pattern = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String yamlContent = matcher.group(1);
            try {
                return yaml.load(yamlContent);
            } catch (Exception e) {
                System.err.println("Warning: Failed to parse frontmatter: " + e.getMessage());
            }
        }

        return new HashMap<>();
    }

    private String removeFrontmatter(String content) {
        return content.replaceFirst("^---\\s*\\n.*?\\n---\\s*\\n", "");
    }

    private String generateTitleFromFilename(String filepath) {
        String filename = Paths.get(filepath).getFileName().toString();
        filename = filename.replace(".md", "");

        return Arrays.stream(filename.split("[-_]"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .reduce((a, b) -> a + " " + b)
                .orElse("Untitled");
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private List<TOCItem> generateTOC(Document document) {
        List<TOCItem> items = new ArrayList<>();

        document.getDescendants().forEach(node -> {
            if (node instanceof com.vladsch.flexmark.ast.Heading) {
                com.vladsch.flexmark.ast.Heading heading = (com.vladsch.flexmark.ast.Heading) node;
                int level = heading.getLevel();

                if (level == 2 || level == 3) {
                    String text = heading.getText().toString();
                    String id = generateId(text);
                    items.add(new TOCItem(id, text, level));
                }
            }
        });

        return items;
    }

    private String generateId(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    private String removeHeaderAndDate(String content) {
        // Remove first H1 (# Title) and any following whitespace
        content = content.replaceFirst("^#\\s+.*?\\n+", "");

        // Remove any leading whitespace
        content = content.trim();

        // Remove Date line (Date: ...)
        content = content.replaceFirst("^Date:\\s*.*?\\n+", "");

        // Remove any remaining leading whitespace
        return content.trim() + "\n";
    }
}