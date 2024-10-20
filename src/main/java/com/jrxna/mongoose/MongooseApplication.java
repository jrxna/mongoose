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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class MongooseApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MongooseApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Step 1: Get input and output folder paths
        if (args.length < 2) {
            System.out.println("Please provide the input and output folder paths.");
            System.exit(1);
        }
        String inputFolderPath = args[0];
        String outputFolderPath = args[1];
        Path inputPath = Paths.get(inputFolderPath);
        Path outputPath = Paths.get(outputFolderPath);

        if (!Files.exists(inputPath) || !Files.isDirectory(inputPath)) {
            System.out.println("The provided input path is not a valid directory.");
            System.exit(1);
        }
        Files.createDirectories(outputPath);

        // Initialize Markdown parser
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // Prepare HTML templates
        String templateFolderPath = "static-site-template";
        Path templatePath = Paths.get(templateFolderPath);

        // Define the output directory path
        Files.createDirectories(outputPath);

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

        // Parse projects.md to extract links and descriptions
        List<Map<String, String>> projectLinks = new ArrayList<>();
        Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)(?::\\s*(.*))?");
        Matcher matcher = linkPattern.matcher(projectsContent);
        while (matcher.find()) {
            String projectName = matcher.group(1);
            String projectUrl = matcher.group(2);
            String projectDescription = matcher.group(3) != null ? matcher.group(3) : "";
            Map<String, String> projectData = new HashMap<>();
            projectData.put("name", projectName);
            projectData.put("url", projectUrl);
            projectData.put("description", projectDescription);
            projectLinks.add(projectData);
        }

        // Generate HTML list of projects with descriptions
        StringBuilder projectsHtmlBuilder = new StringBuilder();
        projectsHtmlBuilder.append("<ul>\n");
        for (Map<String, String> project : projectLinks) {
            projectsHtmlBuilder.append("  <li><a href=\"")
                    .append(project.get("url"))
                    .append("\" target=\"_blank\">")
                    .append(project.get("name"))
                    .append("</a>");
            if (!project.get("description").isEmpty()) {
                projectsHtmlBuilder.append(": ").append(project.get("description"));
            }
            projectsHtmlBuilder.append("</li>\n");
        }
        projectsHtmlBuilder.append("</ul>");
        String projectsLinksHtml = projectsHtmlBuilder.toString();

        // Read the projects.html template
        String projectsHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("projects.html")));

        // Replace {{projects}} placeholder in projects.html template
        String projectsHtmlOutput = projectsHtmlTemplate.replace("{{projects}}", projectsLinksHtml);

        // Write projects.html to the output directory
        Files.write(outputPath.resolve("projects.html"), projectsHtmlOutput.getBytes());

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

        // Read other HTML templates
        String indexHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("index.html")));
        String blogHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("blog.html")));

        // Replace placeholders with content
        String indexHtmlOutput = indexHtmlTemplate.replace("{{content}}", aboutHtml);

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
        Files.write(outputPath.resolve("index.html"), indexHtmlOutput.getBytes());
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
            String postTemplate = "<!DOCTYPE html>\n"
                    + "<html lang=\"en\">\n"
                    + "<head>\n"
                    + "    <meta charset=\"UTF-8\">\n"
                    + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                    + "    <title>" + postData.get("title") + "</title>\n"
                    + "    <link rel=\"stylesheet\" href=\"../styles.css\">\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "    <!-- Header -->\n"
                    + "    <header class=\"header\">\n"
                    + "        <a href=\"../index.html\">\n"
                    + "            <img src=\"../images/JRXNA.png\" alt=\"Website Logo\" class=\"logo\">\n"
                    + "        </a>\n"
                    + "    </header>\n"
                    + "    <!-- Navigation Bar -->\n"
                    + "    <nav class=\"navbar\">\n"
                    + "        <a href=\"../index.html\">About</a>\n"
                    + "        <a href=\"../blog.html\">Blog</a>\n"
                    + "        <a href=\"../projects.html\">Projects</a>\n"
                    + "    </nav>\n"
                    + "    <main class=\"content\">\n"
                    + "        {{content}}\n"
                    + "    </main>\n"
                    + "    <!-- Footer -->\n"
                    + "    <footer class=\"footer\">\n"
                    + "        <p>&copy; Joel Rego</p>\n"
                    + "    </footer>\n"
                    + "</body>\n"
                    + "</html>";

            String postHtmlOutput = postTemplate.replace("{{content}}", postHtmlContent);

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
        String[] assetFolders = { "fonts", "images" };
        for (String folderName : assetFolders) {
            Path sourceFolder = templatePath.resolve(folderName);
            Path targetFolder = outputPath.resolve(folderName);
            copyFolder(sourceFolder, targetFolder);
        }
    }

    private static void copyFolder(Path source, Path target) throws IOException {
        if (Files.exists(source)) {
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
}
