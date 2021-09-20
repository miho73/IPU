package com.github.miho73.ipu.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

@Service("DocsService")

@PropertySource("classpath:/properties/application.properties")
public class DocsService {
    @Value("${docs.mappingFile}") String mappingFile;

    private JSONObject mapping;
    private String prefix = "";

    @PostConstruct
    public void initDocsService() throws IOException {
        File read = new File(mappingFile);
        Scanner reader = new Scanner(read);
        StringBuilder mappingJson = new StringBuilder();
        while (reader.hasNextLine()) {
            mappingJson.append(reader.nextLine());
        }
        reader.close();
        mapping = new JSONObject(mappingJson.toString());
        if(mapping.has("prefix")) {
            prefix = mapping.getString("prefix");
        }
    }

    public String getMapping(String docsURL) {
        if(!mapping.has(docsURL)) return null;
        return prefix+mapping.getString(docsURL);
    }

    public void reloadMapping() throws IOException {
        prefix = "";
        initDocsService();
    }
}
