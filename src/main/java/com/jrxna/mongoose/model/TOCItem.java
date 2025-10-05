package com.jrxna.mongoose.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TOCItem {
    private String id;
    private String text;
    private int level; // 2 for h2, 3 for h3, etc.
    
    public String getLevelClass() {
        return "level-" + level;
    }
}