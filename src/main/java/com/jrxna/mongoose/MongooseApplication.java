package com.jrxna.mongoose;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.MutableAttributes;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

@SpringBootApplication
public class MongooseApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MongooseApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: java -jar mongoose.jar <input_folder> <output_folder> <domain_name>");
            System.exit(1);
        }

        String inputFolderPath = args[0];
        String outputFolderPath = args[1];
        String domainName = args[2];

        Path inputPath = Paths.get(inputFolderPath);
        Path outputPath = Paths.get(outputFolderPath);

        if (!Files.exists(inputPath) || !Files.isDirectory(inputPath)) {
            System.out.println("Invalid input folder.");
            System.exit(1);
        }
        Files.createDirectories(outputPath);

        Files.write(outputPath.resolve("CNAME"), domainName.getBytes());

        MutableDataSet options = new MutableDataSet();
        options.set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "language-");
        HtmlRenderer renderer = HtmlRenderer.builder(options)
                .attributeProviderFactory(new AttributeProviderFactory() {
                    @Override
                    public AttributeProvider apply(LinkResolverContext context) {
                        return new CustomAttributeProvider();
                    }

                    @Override
                    public Set<Class<?>> getAfterDependents() { return null; }
                    @Override
                    public Set<Class<?>> getBeforeDependents() { return null; }
                    @Override
                    public boolean affectsGlobalScope() { return false; }
                })
                .build();

        Parser parser = Parser.builder(options).build();

        String templateFolderPath = "static-site-template";
        Path templatePath = Paths.get(templateFolderPath);

        // about.md
        Path aboutMdPath = inputPath.resolve("about.md");
        String aboutContent = Files.exists(aboutMdPath) ? new String(Files.readAllBytes(aboutMdPath)) : "";
        Node aboutDocument = parser.parse(aboutContent);
        String aboutHtml = renderer.render(aboutDocument);
        aboutHtml = applyLineNumbers(aboutHtml); // Apply line numbers to home page content as well

        // projects.md
        Path projectsMdPath = inputPath.resolve("projects.md");
        String projectsContent = Files.exists(projectsMdPath) ? new String(Files.readAllBytes(projectsMdPath)) : "";
        List<Map<String, String>> projectLinks = parseTitleAndSummary(projectsContent);

        StringBuilder projectsHtmlBuilder = new StringBuilder();
        for (int i = 0; i < projectLinks.size(); i++) {
            Map<String, String> project = projectLinks.get(i);
            projectsHtmlBuilder.append("<h3><a href=\"")
                    .append(project.get("url"))
                    .append("\" target=\"_blank\">")
                    .append(project.get("title"))
                    .append("</a></h3>\n");
            if (!project.get("summary").isEmpty()) {
                projectsHtmlBuilder.append("<p class=\"summary\">")
                        .append(project.get("summary"))
                        .append("</p>\n");
            }
            if (i < projectLinks.size() - 1) {
                projectsHtmlBuilder.append("<hr>\n");
            }
        }
        String projectsHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("projects.html")));
        String projectsHtmlOutput = projectsHtmlTemplate.replace("{{projects}}", projectsHtmlBuilder.toString());
        projectsHtmlOutput = applyLineNumbers(projectsHtmlOutput); // Apply line numbers to projects page
        Files.write(outputPath.resolve("projects.html"), projectsHtmlOutput.getBytes());

        // posts
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

                    String title = extractTitle(postContent);
                    String summary = extractSummary(postContent);

                    // Remove summary from body
                    String filteredPostHtml = removeSummaryLineFromHtml(postHtml, summary);
                    // We'll apply line numbers after we finalize the HTML for individual posts
                    // Actually let's just do it now, then also do final line numbers after all replacements
                    filteredPostHtml = applyLineNumbers(filteredPostHtml);

                    Map<String, String> postData = new HashMap<>();
                    postData.put("fileName", baseName + ".html");
                    postData.put("htmlContent", filteredPostHtml);
                    postData.put("title", title);
                    postData.put("summary", summary);

                    postsData.add(postData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String indexHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("index.html")));
        String blogHtmlTemplate = new String(Files.readAllBytes(templatePath.resolve("blog.html")));
        String indexHtmlOutput = indexHtmlTemplate.replace("{{content}}", aboutHtml);
        indexHtmlOutput = applyLineNumbers(indexHtmlOutput); // line numbers on index page too if any code

        // blog listing
        StringBuilder postsLinksBuilder = new StringBuilder();
        for (int i = 0; i < postsData.size(); i++) {
            Map<String, String> pd = postsData.get(i);
            postsLinksBuilder.append("<h3><a href=\"posts/")
                    .append(pd.get("fileName"))
                    .append("\">")
                    .append(pd.get("title"))
                    .append("</a></h3>\n");
            if (!pd.get("summary").isEmpty()) {
                postsLinksBuilder.append("<p class=\"summary\">")
                        .append(pd.get("summary"))
                        .append("</p>\n");
            }
            if (i < postsData.size() - 1) {
                postsLinksBuilder.append("<hr>\n");
            }
        }

        String blogHtmlOutput = blogHtmlTemplate.replace("{{posts}}", postsLinksBuilder.toString());
        blogHtmlOutput = applyLineNumbers(blogHtmlOutput); // line numbers on blog listing if code present

        Files.write(outputPath.resolve("index.html"), indexHtmlOutput.getBytes());
        Files.write(outputPath.resolve("blog.html"), blogHtmlOutput.getBytes());

        copyStaticAssets(templatePath, outputPath);

        Path outputPostsPath = outputPath.resolve("posts");
        Files.createDirectories(outputPostsPath);

        List<String> pages = new ArrayList<>();
        pages.add("index.html");
        pages.add("projects.html");
        pages.add("blog.html");

        // Individual post pages: summary in meta tags only
        for (Map<String, String> postData : postsData) {
            String postFileName = postData.get("fileName");
            String postHtmlContent = postData.get("htmlContent");
            String postTitle = postData.get("title");
            String postSummary = postData.get("summary");

            String postTemplate = new String(Files.readAllBytes(templatePath.resolve("post.html")));
            String postHtmlOutput = postTemplate
                    .replace("{{content}}", postHtmlContent)
                    .replace("{{title}}", postTitle)
                    .replace("{{summary}}", postSummary);

            // Ensure line numbers applied again (redundant but safe)
            postHtmlOutput = applyLineNumbers(postHtmlOutput);

            Path postOutputPath = outputPostsPath.resolve(postFileName);
            Files.write(postOutputPath, postHtmlOutput.getBytes());

            pages.add("posts/" + postFileName);
        }

        createSitemap(outputPath, pages, domainName);

        System.out.println("Mongoose Site Generated.");
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

    private String extractSummary(String markdownContent) {
        try (Scanner scanner = new Scanner(markdownContent)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("Summary:")) {
                    return line.substring("Summary:".length()).trim();
                }
            }
        }
        return "";
    }

    private List<Map<String, String>> parseTitleAndSummary(String content) {
        List<Map<String, String>> items = new ArrayList<>();
        String[] lines = content.split("\n");

        String currentTitle = null;
        String currentUrl = null;
        String currentSummary = "";

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("[") && trimmed.contains("](")) {
                Matcher m = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)").matcher(trimmed);
                if (m.find()) {
                    currentTitle = m.group(1);
                    currentUrl = m.group(2);
                    currentSummary = "";
                }
            } else if (trimmed.startsWith("Summary:")) {
                currentSummary = trimmed.substring("Summary:".length()).trim();
                if (currentTitle != null && currentUrl != null) {
                    Map<String, String> item = new HashMap<>();
                    item.put("title", currentTitle);
                    item.put("url", currentUrl);
                    item.put("summary", currentSummary);
                    items.add(item);

                    currentTitle = null;
                    currentUrl = null;
                    currentSummary = "";
                }
            }
        }

        return items;
    }

    private String removeSummaryLineFromHtml(String postHtml, String summary) {
        if (summary.isEmpty()) {
            return postHtml;
        }
        return postHtml.replaceAll("<p>Summary:.*?</p>", "");
    }

    private String applyLineNumbers(String html) {
        // Regex to find code blocks:
        // <pre><code class="(language-[^"]+)">
        // Replace with:
        // <pre class=" line-numbers $1"><code class="$1 line-numbers $1">
        Pattern p = Pattern.compile("<pre><code class=\"(language-[^\"]+)\">");
        Matcher m = p.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String langClass = m.group(1);
            // Insert the exact older format
            // The older snippet had a space before "line-numbers"
            // We'll replicate that exactly:
            // <pre class=" line-numbers language-XXX"><code class="language-XXX line-numbers language-XXX">
            m.appendReplacement(sb, "<pre class=\" line-numbers " + langClass + "\"><code class=\"" + langClass + " line-numbers " + langClass + "\">");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private void copyStaticAssets(Path templatePath, Path outputPath) throws IOException {
        Path sourceStylesPath = templatePath.resolve("styles.css");
        Path targetStylesPath = outputPath.resolve("styles.css");
        Files.copy(sourceStylesPath, targetStylesPath, StandardCopyOption.REPLACE_EXISTING);

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

    private void createSitemap(Path outputPath, List<String> pages, String domainName) throws IOException {
        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        for (String page : pages) {
            sitemap.append("  <url>\n");
            sitemap.append("    <loc>").append(domainName).append("/").append(page).append("</loc>\n");
            sitemap.append("  </url>\n");
        }

        sitemap.append("</urlset>");

        Files.write(outputPath.resolve("sitemap.xml"), sitemap.toString().getBytes());
    }

    static class CustomAttributeProvider implements AttributeProvider {
        @Override
        public void setAttributes(Node node, AttributablePart part, MutableAttributes attributes) {
            if (node instanceof FencedCodeBlock) {
                // no changes needed here
            }
        }
    }
}
