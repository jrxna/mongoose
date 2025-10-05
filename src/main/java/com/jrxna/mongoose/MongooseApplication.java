package com.jrxna.mongoose;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.jrxna.mongoose.service.SiteGeneratorService;

@SpringBootApplication
public class MongooseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongooseApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(SiteGeneratorService generator) {
        return args -> {
            if (args.length < 1) {
                System.out.println("Usage: mongoose <input-directory> [output-directory]");
                System.out.println("\nOptions:");
                System.out.println("  input-directory   Path to folder containing markdown files");
                System.out.println("  output-directory  Path for generated site (default: ./output)");
                System.exit(1);
            }

            String inputPath = args[0];
            String outputPath = args.length > 1 ? args[1] : "./output";

            System.out.println("🦡 Mongoose Static Site Generator");
            System.out.println("=====================================");
            System.out.println("Input:  " + inputPath);
            System.out.println("Output: " + outputPath);
            System.out.println();

            try {
                generator.generateSite(inputPath, outputPath);
                System.out.println("\n✅ Site generated successfully!");
                System.out.println("📁 Open " + outputPath + "/index.html to view your site");
            } catch (Exception e) {
                System.err.println("\n❌ Error generating site: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        };
    }
}