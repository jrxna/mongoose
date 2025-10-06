package com.jrxna.mongoose.service;

import com.jrxna.mongoose.model.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

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

        // Parse all markdown files first to get dates
        for (Section section : sections) {
            for (MarkdownFile file : section.getFiles()) {
                markdownParser.parseMarkdown(file);
            }
        }

        System.out.println("   Parsed " + totalFiles + " files");

        System.out.println("\n📊 Sorting by date...");

        // Sort files within each section (oldest first)
        for (Section section : sections) {
            section.getFiles().sort((f1, f2) -> f1.getDate().compareTo(f2.getDate()));
        }

        // Sort sections by newest date in each section (newest sections first)
        sections.sort((s1, s2) -> {
            LocalDate newest1 = s1.getFiles().stream()
                    .map(MarkdownFile::getDate)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.MIN);
            LocalDate newest2 = s2.getFiles().stream()
                    .map(MarkdownFile::getDate)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.MIN);
            return newest2.compareTo(newest1); // Reverse order - newest first
        });

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

            MarkdownFile indexFile = new MarkdownFile();
            indexFile.setTitle(firstFile.getTitle());
            indexFile.setDate(firstFile.getDate());
            indexFile.setContent(firstFile.getContent());
            indexFile.setHtmlContent(firstFile.getHtmlContent());
            indexFile.setTableOfContents(firstFile.getTableOfContents());
            indexFile.setFilePath(firstFile.getFilePath()); // ADD THIS
            indexFile.setOutputPath("index.html");

            String indexHtml = templateService.generatePage(indexFile, sections);
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
            String originalName = sectionDir.getFileName().toString();
            String displayName = formatSectionName(originalName);
            String urlSafeName = toUrlSafe(originalName);

            Section section = new Section(displayName);

            List<Path> markdownFiles = Files.list(sectionDir)
                    .filter(p -> p.toString().endsWith(".md"))
                    .sorted()
                    .collect(Collectors.toList());

            for (Path mdFile : markdownFiles) {
                MarkdownFile file = new MarkdownFile();
                file.setFilePath(mdFile.toString());

                // Use URL-safe folder name in output path
                String fileName = mdFile.getFileName().toString().replace(".md", ".html");
                String outputPath = urlSafeName + "/" + fileName;
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
        Path assetsDir = outputDir.resolve("assets/images");
        Files.createDirectories(assetsDir);

        // Copy logo and favicon from resources
        try (var logoStream = getClass().getResourceAsStream("/assets/images/Logo.png")) {
            if (logoStream != null) {
                Files.copy(logoStream, assetsDir.resolve("Logo.png"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }

        try (var faviconStream = getClass().getResourceAsStream("/assets/images/Favicon.png")) {
            if (faviconStream != null) {
                Files.copy(faviconStream, assetsDir.resolve("Favicon.png"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }

        System.out.println("   Copied assets");
    }

    private String toUrlSafe(String name) {
        return name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "");
    }
}