package com.jrxna.mongoose;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class MongooseApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MongooseApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
    }
}
