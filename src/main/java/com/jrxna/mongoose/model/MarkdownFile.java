package com.jrxna.mongoose.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarkdownFile {
    private String title;
    private LocalDate date;
    private String content;
    private String htmlContent;
    private String filePath;
    private String outputPath;
    private List<TOCItem> tableOfContents = new ArrayList<>();
}