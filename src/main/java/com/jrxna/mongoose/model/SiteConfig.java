package com.jrxna.mongoose.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiteConfig {
    private String siteName = "JRXNA";
    private String domain = "jrxna.com";
    private String subdomain = "mongoose";
    private String logoPath = "assets/images/JRXNALogoSmall.png";
    private String faviconPath = "assets/images/JRXNAFavicon.png";
    
    public String getFullDomain() {
        return subdomain + "." + domain;
    }
}