package com.jrxna.mongoose.model;

public class SiteConfig {
    private String siteName = "JRXNA";
    private String domain = "jrxna.com";
    private String subdomain = "mongoose";
    private String logoPath = "assets/images/JRXNALogoSmall.png";
    private String faviconPath = "assets/images/JRXNAFavicon.png";
    
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    
    public String getSubdomain() { return subdomain; }
    public void setSubdomain(String subdomain) { this.subdomain = subdomain; }
    
    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
    
    public String getFaviconPath() { return faviconPath; }
    public void setFaviconPath(String faviconPath) { this.faviconPath = faviconPath; }
    
    public String getFullDomain() {
        return subdomain + "." + domain;
    }
}