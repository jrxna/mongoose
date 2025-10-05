package com.jrxna.mongoose.model;

public class TOCItem {
    private String id;
    private String text;
    private int level;
    
    public TOCItem() {}
    
    public TOCItem(String id, String text, int level) {
        this.id = id;
        this.text = text;
        this.level = level;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public String getLevelClass() {
        return "level-" + level;
    }
}