package com.jrxna.mongoose;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class MongooseApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MongooseApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Step 1: Get input folder path
        if (args.length == 0) {
            System.out.println("Please provide the input folder path.");
            System.exit(1);
        }
        String inputFolderPath = args[0];
        Path inputPath = Paths.get(inputFolderPath);

        if (!Files.exists(inputPath) || !Files.isDirectory(inputPath)) {
            System.out.println("The provided input path is not a valid directory.");
            System.exit(1);
        }

        // Initialize Markdown parser
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // Read and parse about.md
        Path aboutMdPath = inputPath.resolve("about.md");
        String aboutContent = "";
        if (Files.exists(aboutMdPath)) {
            aboutContent = new String(Files.readAllBytes(aboutMdPath));
        } else {
            System.out.println("about.md not found in the input folder.");
        }
        Node aboutDocument = parser.parse(aboutContent);
        String aboutHtml = renderer.render(aboutDocument);

        // Read and parse projects.md
        Path projectsMdPath = inputPath.resolve("projects.md");
        String projectsContent = "";
        if (Files.exists(projectsMdPath)) {
            projectsContent = new String(Files.readAllBytes(projectsMdPath));
        } else {
            System.out.println("projects.md not found in the input folder.");
        }
        Node projectsDocument = parser.parse(projectsContent);
        String projectsHtml = renderer.render(projectsDocument);

        // Read and parse posts
        Path postsFolderPath = inputPath.resolve("posts");
        List<Map<String, String>> postsData = new ArrayList<>();

        if (Files.exists(postsFolderPath) && Files.isDirectory(postsFolderPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(postsFolderPath, "*.md")) {
                for (Path entry : stream) {
                    String postContent = new String(Files.readAllBytes(entry));
                    Node postDocument = parser.parse(postContent);
                    String postHtml = renderer.render(postDocument);

                    String fileName = entry.getFileName().toString();
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

                    Map<String, String> postData = new HashMap<>();
                    postData.put("fileName", baseName + ".html");
                    postData.put("htmlContent", postHtml);
                    postData.put("title", extractTitle(postContent));
                    postsData.add(postData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("posts directory not found or is not a directory.");
        }

        // Prepare HTML templates
        String templateFolderPath = "static-site-template";
        Path templatePath = Paths.get(templateFolderPath);

        String indexHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("index.html")));
        String projectsHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("projects.html")));
        String blogHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("blog.html")));

        // Replace placeholders with content
        String indexHtmlOutput = indexHtmlTemplate.replace("{{content}}", aboutHtml);
        String projectsHtmlOutput = projectsHtmlTemplate.replace("{{content}}", projectsHtml);

        // Generate posts links for blog.html
        StringBuilder postsLinksBuilder = new StringBuilder();
        postsLinksBuilder.append("<ul>\n");
        for (Map<String, String> postData : postsData) {
            String postFileName = postData.get("fileName");
            String postTitle = postData.get("title");

            postsLinksBuilder.append("  <li><a href=\"posts/")
                    .append(postFileName)
                    .append("\">")
                    .append(postTitle)
                    .append("</a></li>\n");
        }
        postsLinksBuilder.append("</ul>");
        String postsLinksHtml = postsLinksBuilder.toString();
        String blogHtmlOutput = blogHtmlTemplate.replace("{{posts}}", postsLinksHtml);

        // Write output files
        String outputFolderPath = "output";
        Path outputPath = Paths.get(outputFolderPath);
        Files.createDirectories(outputPath);

        Files.write(outputPath.resolve("index.html"), indexHtmlOutput.getBytes());
        Files.write(outputPath.resolve("projects.html"), projectsHtmlOutput.getBytes());
        Files.write(outputPath.resolve("blog.html"), blogHtmlOutput.getBytes());

        // Copy static assets
        copyStaticAssets(templatePath, outputPath);

        // Write individual post files
        Path outputPostsPath = outputPath.resolve("posts");
        Files.createDirectories(outputPostsPath);

        for (Map<String, String> postData : postsData) {
            String postFileName = postData.get("fileName");
            String postHtmlContent = postData.get("htmlContent");

            // Use the blog.html template for post pages
            String postTemplate = new String(Files.readAllBytes(templatePath.resolve("blog.html")));
            String postHtmlOutput = postTemplate.replace("{{posts}}", postHtmlContent);

            Path postOutputPath = outputPostsPath.resolve(postFileName);
            Files.write(postOutputPath, postHtmlOutput.getBytes());
        }

        System.out.println("Site generated successfully.");
    }

    private String extractTitle(String markdownContent) {
        try (Scanner scanner = new Scanner(markdownContent)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("#")) {
                    return line.replaceAll("#+", "").trim();
                }
            }
        }
        return "Untitled";
    }

    private void copyStaticAssets(Path templatePath, Path outputPath) throws IOException {
        // Copy styles.css
        Path sourceStylesPath = templatePath.resolve("styles.css");
        Path targetStylesPath = outputPath.resolve("styles.css");
        Files.copy(sourceStylesPath, targetStylesPath, StandardCopyOption.REPLACE_EXISTING);

        // Copy asset directories
        String[] assetFolders = {"fonts", "images"};
        for (String folderName : assetFolders) {
            Path sourceFolder = templatePath.resolve(folderName);
            Path targetFolder = outputPath.resolve(folderName);
            copyFolder(sourceFolder, targetFolder);
        }
    }

    private static void copyFolder(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectory(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
