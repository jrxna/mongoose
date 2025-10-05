package com.jrxna.mongoose.model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class MarkdownFile {
    private String title;
    private LocalDate date;
    private String content;
    private String htmlContent;
    private String filePath;
    private String outputPath;
    private List<TOCItem> tableOfContents = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getOutputPath() { return outputPath; }
    public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
    
    public List<TOCItem> getTableOfContents() { return tableOfContents; }
    public void setTableOfContents(List<TOCItem> tableOfContents) { this.tableOfContents = tableOfContents; }
}