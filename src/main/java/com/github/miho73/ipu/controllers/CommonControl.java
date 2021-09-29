package com.github.miho73.ipu.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Controller("CommonControl")
public class CommonControl {
    private final String sitemap;
    private final String robotTxt;

    @Value("${config.sitemap.path}") private String sitemapPath;
    @Value("${config.robot.path}") private String robotTxtPath;

    public CommonControl() throws FileNotFoundException {
        sitemap = readFromFile(sitemapPath);
        robotTxt = readFromFile(robotTxtPath);
    }

    private String readFromFile(String path) throws FileNotFoundException {
        File read = new File(path);
        Scanner reader = new Scanner(read);
        StringBuilder content = new StringBuilder();
        while (reader.hasNextLine()) {
            content.append(reader.nextLine());
        }
        reader.close();
        return content.toString();
    }

    @GetMapping(value = "sitemap.xml", produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String sitemap() {
        return sitemap;
    }

    @GetMapping(value = "robots.txt", produces = "text/xml; charset=utf-8")
    @ResponseBody
    public String robot() {
        return robotTxt;
    }
}
