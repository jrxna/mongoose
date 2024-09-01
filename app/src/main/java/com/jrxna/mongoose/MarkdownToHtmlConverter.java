/*
 * This source file was generated by the Gradle 'init' task
 */
package com.jrxna.mongoose;

import org.commonmark.parser.Parser;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

public class MarkdownToHtmlConverter {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java MarkdownToHtmlConverter <input-folder>");
            System.exit(1);
        }
        
        Path inputDir = Paths.get(args[0]);
        Path contentDir = inputDir.resolve("content");
        Path outputDir = contentDir; // Output in the same directory
        
        if (!Files.exists(contentDir) || !Files.isDirectory(contentDir)) {
            System.err.println("Content directory does not exist or is not a directory.");
            System.exit(1);
        }
        
        processMarkdownFiles(contentDir, outputDir);
    }

    private static void processMarkdownFiles(Path contentDir, Path outputDir) throws IOException {
        // Process home/index.md
        Path homeDir = contentDir.resolve("home");
        processMarkdownFile(homeDir.resolve("home.md"), outputDir.resolve("index.html"));
        
        // Process about/about.md
        Path aboutDir = contentDir.resolve("about");
        processMarkdownFile(aboutDir.resolve("about.md"), outputDir.resolve("about.html"));
        
        // Process blog/*.md
        Path blogDir = contentDir.resolve("blog");
        try (Stream<Path> paths = Files.walk(blogDir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".md"))
                 .forEach(p -> {
                     try {
                         String fileName = p.getFileName().toString().replace(".md", ".html");
                         Path outputFile = outputDir.resolve("blog").resolve(fileName);
                         processMarkdownFile(p, outputFile);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 });
        }
        
        // Copy CSS file
        Files.copy(Paths.get("src/main/resources/styles.css"), outputDir.resolve("styles.css"), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void processMarkdownFile(Path markdownFile, Path outputHtmlFile) throws IOException {
        if (Files.notExists(markdownFile)) {
            System.err.println("Markdown file not found: " + markdownFile);
            return;
        }
        
        String markdown = new String(Files.readAllBytes(markdownFile));
        
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);
        
        String htmlWithCss = wrapInHtml(html);
        
        Files.createDirectories(outputHtmlFile.getParent());
        Files.write(outputHtmlFile, htmlWithCss.getBytes());
    }
    
    private static String wrapInHtml(String bodyContent) {
        String cssLink = "<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">";
        String htmlTemplate = "<!DOCTYPE html><html><head><title>Static Site</title>%s</head><body>%s</body></html>";
        return String.format(htmlTemplate, cssLink, bodyContent);
    }
}
