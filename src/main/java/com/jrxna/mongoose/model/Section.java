package com.jrxna.mongoose.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Section {
    private String title;
    private List<MarkdownFile> files = new ArrayList<>();
    private boolean isOpen = false;
    
    public Section(String title) {
        this.title = title;
    }
    
    public void addFile(MarkdownFile file) {
        this.files.add(file);
    }
}