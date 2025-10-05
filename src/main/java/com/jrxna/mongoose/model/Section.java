package com.jrxna.mongoose.model;

import java.util.List;
import java.util.ArrayList;

public class Section {
    private String title;
    private List<MarkdownFile> files = new ArrayList<>();
    private boolean isOpen = false;
    
    public Section() {}
    
    public Section(String title) {
        this.title = title;
    }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public List<MarkdownFile> getFiles() { return files; }
    public void setFiles(List<MarkdownFile> files) { this.files = files; }
    
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }
    
    public void addFile(MarkdownFile file) {
        this.files.add(file);
    }
}