package com.github.miho73.ipu.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Controller("CommonControl")
public class CommonControl {
    private String sitemap;
    private String robotTxt;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${config.sitemap.path}") String sitemapPath;
    @Value("${config.robot.path}") String robotTxtPath;

    @PostConstruct
    public void initCommonControl() throws FileNotFoundException {
        LOGGER.debug("Read sitemap file from \""+sitemapPath+"\"");
        LOGGER.debug("Read robot.txt file from \""+robotTxtPath+"\"");
        sitemap = readFromFile(sitemapPath);
        LOGGER.debug("sitemap loaded");
        robotTxt = readFromFile(robotTxtPath);
        LOGGER.debug("robot.txt loaded");
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

    @GetMapping(value = "sitemap.xml", produces = "text/xml; charset=utf-8")
    @ResponseBody
    public String sitemap() {
        return sitemap;
    }

    @GetMapping(value = "robots.txt", produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String robot() {
        return robotTxt;
    }
}
