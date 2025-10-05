package com.jrxna.mongoose.service;

import com.jrxna.mongoose.model.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SiteGeneratorService {

    @Autowired
    private MarkdownParserService markdownParser;
    
    @Autowired
    private TemplateService templateService;

    public void generateSite(String inputPath, String outputPath) throws IOException {
        Path inputDir = Paths.get(inputPath);
        Path outputDir = Paths.get(outputPath);

        if (!Files.exists(inputDir) || !Files.isDirectory(inputDir)) {
            throw new IOException("Input directory does not exist: " + inputPath);
        }

        Files.createDirectories(outputDir);

        System.out.println("📖 Scanning markdown files...");
        
        List<Section> sections = scanDirectory(inputDir);
        
        System.out.println("   Found " + sections.size() + " sections");
        int totalFiles = sections.stream()
            .mapToInt(s -> s.getFiles().size())
            .sum();
        System.out.println("   Found " + totalFiles + " markdown files");

        System.out.println("\n🔄 Parsing markdown and generating HTML...");
        
        for (Section section : sections) {
            for (MarkdownFile file : section.getFiles()) {
                markdownParser.parseMarkdown(file);
            }
        }

        System.out.println("   Parsed " + totalFiles + " files");

        System.out.println("\n📝 Generating pages...");
        
        for (Section section : sections) {
            for (MarkdownFile file : section.getFiles()) {
                String html = templateService.generatePage(file, sections);
                Path outputFile = outputDir.resolve(file.getOutputPath());
                Files.createDirectories(outputFile.getParent());
                Files.writeString(outputFile, html);
            }
        }

        System.out.println("   Generated " + totalFiles + " pages");

        System.out.println("\n📦 Copying assets...");
        copyAssets(outputDir);
        
        if (!sections.isEmpty() && !sections.get(0).getFiles().isEmpty()) {
            MarkdownFile firstFile = sections.get(0).getFiles().get(0);
            String indexHtml = templateService.generatePage(firstFile, sections);
            Files.writeString(outputDir.resolve("index.html"), indexHtml);
            System.out.println("   Created index.html");
        }
    }

    private List<Section> scanDirectory(Path root) throws IOException {
        List<Section> sections = new ArrayList<>();
        
        List<Path> sectionDirs = Files.list(root)
            .filter(Files::isDirectory)
            .sorted()
            .collect(Collectors.toList());

        for (Path sectionDir : sectionDirs) {
            String sectionName = formatSectionName(sectionDir.getFileName().toString());
            Section section = new Section(sectionName);

            List<Path> markdownFiles = Files.list(sectionDir)
                .filter(p -> p.toString().endsWith(".md"))
                .sorted()
                .collect(Collectors.toList());

            for (Path mdFile : markdownFiles) {
                MarkdownFile file = new MarkdownFile();
                file.setFilePath(mdFile.toString());
                
                String relativePath = root.relativize(mdFile).toString();
                String outputPath = relativePath.replace(".md", ".html");
                file.setOutputPath(outputPath);
                
                section.addFile(file);
            }

            if (!section.getFiles().isEmpty()) {
                sections.add(section);
            }
        }

        List<Path> rootFiles = Files.list(root)
            .filter(p -> p.toString().endsWith(".md"))
            .sorted()
            .collect(Collectors.toList());

        if (!rootFiles.isEmpty()) {
            Section rootSection = new Section("Documentation");
            for (Path mdFile : rootFiles) {
                MarkdownFile file = new MarkdownFile();
                file.setFilePath(mdFile.toString());
                file.setOutputPath(mdFile.getFileName().toString().replace(".md", ".html"));
                rootSection.addFile(file);
            }
            sections.add(0, rootSection);
        }

        return sections;
    }

    private String formatSectionName(String dirName) {
        return Arrays.stream(dirName.split("[-_]"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
            .collect(Collectors.joining(" "));
    }

    private void copyAssets(Path outputDir) throws IOException {
        Path assetsDir = outputDir.resolve("assets");
        Files.createDirectories(assetsDir);
        Files.createDirectories(assetsDir.resolve("images"));
        
        System.out.println("   Created assets directory structure");
    }
}