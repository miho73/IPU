package com.github.miho73.ipu.library;

import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Converters {
    public String convertDiff(String code) {
        return switch (code) {
            case "unse" -> "Not rated";
            case "unra" -> "Unrated";
            case "broz" -> "Bronze";
            case "silv" -> "Silver";
            case "gold" -> "Gold";
            case "sapp" -> "Sapphire";
            case "ruby" -> "Ruby";
            case "diam" -> "Diamond";
            default -> "Unset";
        };
    }

    public String convertDiffColor(String code) {
        return switch (code) {
            case "unse" -> "#000";
            case "unra" -> "#555";
            case "broz" -> "#987b43";
            case "silv" -> "#C0C0C0";
            case "gold" -> "#FFD700";
            case "sapp" -> "#0F52BA";
            case "ruby" -> "#e0115f";
            case "diam" -> "#7BC3D4";
            default -> "#000";
        };
    }

    public int[] cutTable = {0, 3000, 15000, 40000, 80000, 150000, 200000, 300000};

    public Map<Integer, String> codeTableRl = new HashMap<>() {{
        put(1, "Unrated");
        put(2, "Bronze");
        put(3, "Silver");
        put(4, "Gold");
        put(5, "Sapphire");
        put(6, "Ruby");
        put(7, "Diamond");
        put(8, "Infinity");
    }};

    public Map<Integer, String> codeTable = new HashMap<>() {{
        put(1, "unra");
        put(2, "broz");
        put(3, "silv");
        put(4, "gold");
        put(5, "sapp");
        put(6, "ruby");
        put(7, "diam");
        put(8, "redd");
    }};

    public Map<String, String> Subj = new HashMap<>() {{
        put("alge", "대수");
        put("numb", "정수");
        put("comb", "조합");
        put("geom", "기하");
        put("phys", "물리");
        put("chem", "화학");
        put("biol", "생물");
        put("eart", "지구");
    }};

    public String convertSubj(String code) {
        return Subj.getOrDefault(code, "미분류");
    }

    public int getLevelCode(int exp) {
        int levelCode = 8;
        for(int i=1; i<=7; i++) {
            if(exp < cutTable[i]) {
                levelCode = i;
                break;
            }
        }
        return levelCode;
    }
}
