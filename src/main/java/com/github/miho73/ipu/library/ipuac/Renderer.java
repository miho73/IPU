package com.github.miho73.ipu.library.ipuac;

import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Renderer {
    /**   LINE REGEX   **/
    private final Pattern section      = Pattern.compile("^==\\s+(.*?)\\s+==$");

    private final String bold_n_italic = "[']{4}(.*?)[']{4}";
    private final String italic        = "[']{3}(.*?)[']{3}";
    private final String bold          = "[']{2}(.*?)[']{2}";
    private final String underline     = "[_]{2}(.*?)[_]{2}";
    private final String strike        = "[-]{2}(.*?)[-]{2}";
    private final String superscript   = "[\\^]{2}(.*?)[\\^]{2}";
    private final String subscript     = "[,]{2}(.*?)[,]{2}";

    private final Pattern color         = Pattern.compile("[{]{3}(.*?)\\s(.*?)[}]{3}");
    private final Pattern bgcolor       = Pattern.compile("[\\[]{3}(.*?)\\s(.*?)[\\]]{3}");
    private final Pattern font_size     = Pattern.compile("[{]{3}([+-]\\d+)\\s(.*?)[}]{3}");

    private final String  link_with_exp = "[\\[]{2}(.*?)[|](.*?)[\\]]{2}";
    private final String  link          = "[\\[]{2}(.*?)[\\]]{2}";
    private final Pattern image         = Pattern.compile("[\\[][{](.*?)[}][\\]]");
    private final Pattern imageWithArgs = Pattern.compile("[\\[][{](.*?)[|](.*?)[}][\\]]");

    private final Pattern func          = Pattern.compile("[\\[]([a-zA-Z]+?)[(](.*?)[)][\\]]");
    private final Pattern tableElement  = Pattern.compile("^[(]([|-]\\d+)[)](.*?)$");
    /********************/

    public String IPUACtoHTML(String ipuac) {
        String[] lines = ipuac.split("\n");

        Vector<String> finalDom = new Vector<>();

        HashMap<String, String> definitions = new HashMap<>();
        boolean quoteFlag = false, tableFlag = false, listFlag = false;
        int hide_code = 0;

        for (String line: lines) {
            StringBuilder html = new StringBuilder();
            int len = line.length();

            // 빈 줄(줄바꿈)인 경우 무시(인용, 테이블은 끊어줌)
            if(line.equals("")) {
                if(quoteFlag) {
                    finalDom.setElementAt(finalDom.elementAt(finalDom.size()-1)+"</blockquote>", finalDom.size()-1);
                    quoteFlag = false;
                }
                if(tableFlag) {
                    finalDom.setElementAt(finalDom.elementAt(finalDom.size()-1)+"</tbody></table>", finalDom.size()-1);
                    tableFlag = false;
                }
                if(listFlag) {
                    finalDom.setElementAt(finalDom.get(finalDom.size()-1)+"</ul>", finalDom.size()-1);
                    listFlag = false;
                }
                continue;
            }

            // 지시문 처리
            if(line.charAt(0) == '#') {
                String inst = line.substring(1, 4);
                String[] prop = line.substring(5, len).split("=");
                switch(inst) {
                    case "def":
                        definitions.put(prop[0], prop[1]);
                        break;
                    default:
                        html = new StringBuilder("<p class=\"error\">IPUAC 오류: 지시문 '" + inst + "을(를) 이해할 수 없습니다.</p>");
                        finalDom.add(html.toString());
                        break;
                }
                continue;
            }

            // HTML Injection 방지
            line = line.replaceAll("<", "&#60;")
                       .replaceAll(">", "&#62;");

            //매크로 치환
            line = line.replaceAll("\\[lf]", "<br>")
                       .replaceAll("---", "<hr class=\"prob-div-hr\">");

            // 텍스트 스타일 Regex로 검색 -> 치환
            line = line.replaceAll(bold_n_italic, "<span class=\"ac-bold ac-italic\">$1</span>")
                       .replaceAll(italic,        "<span class=\"ac-italic\">$1</span>")
                       .replaceAll(bold,          "<span class=\"ac-bold\">$1</span>")
                       .replaceAll(underline,     "<span class=\"ac-underline\">$1</span>")
                       .replaceAll(strike,        "<span class=\"ac-strike\">$1</span>")
                       .replaceAll(superscript,   "<sup class=\"ac-super\">$1</sup>")
                       .replaceAll(subscript,     "<sub class=\"ac-sub\">$1</sub>");

            //텍스트 크기 적용
            while(true) {
                Matcher matcher = font_size.matcher(line);
                if(matcher.find()) {
                    line = matcher.replaceFirst("<span style=\"font-size: "+Math.floor(Integer.parseInt(matcher.group(1))+11)/10+"em;\">"+matcher.group(2)+"</span>");
                }
                else break;
            }

            // 텍스트 & 배경 색상 적용
            while(true) {
                Matcher matcher = color.matcher(line);
                if(matcher.find()) {
                    line = matcher.replaceFirst("<span style=\"color: "+matcher.group(1)+";\">"+matcher.group(2)+"</span>");
                }
                else break;
            }
            while(true) {
                Matcher matcher = bgcolor.matcher(line);
                if(matcher.find()) {
                    line = matcher.replaceFirst("<span style=\"background-color: "+matcher.group(1)+";\">"+matcher.group(2)+"</span>");
                }
                else break;
            }

            // 하이퍼링크 적용
            line = line.replaceAll(link_with_exp, "<a href=\"$1\" class=\"ac-link\" target=\"_blank\">$2</a>")
                       .replaceAll(link,          "<a href=\"$1\" class=\"ac-link\" target=\"_blank\">$1</a>");

            // 이미지 적용
            while(true) {
                Matcher matcher = imageWithArgs.matcher(line);
                if(matcher.find()) {
                    String[] lstCfg = matcher.group(2).split("&");
                    if(matcher.group(1).startsWith("img:")) {
                        line = matcher.replaceFirst("<img src=\"/problem/lib/"+matcher.group(1).substring(4)+"\" class=\"ac-img\" "+String.join(" ", lstCfg)+">");
                    }
                    else line = matcher.replaceFirst("<img src=\""+matcher.group(1)+"\" class=\"ac-img\" "+String.join(" ", lstCfg)+">");
                }
                else break;
            }
            while(true) {
                Matcher matcher = image.matcher(line);
                if(matcher.find()) {
                    if(matcher.group(1).startsWith("img:")) {
                        line = matcher.replaceFirst("<img src=\"/problem/lib/"+matcher.group(1).substring(4)+"\" class=\"ac-img\">");
                    }
                    else line = matcher.replaceFirst("<img src=\""+matcher.group(1)+"\" class=\"ac-img\">");
                }
                else break;
            }

            // 함수 적용
            while(true) {
                Matcher matcher = func.matcher(line);
                if(matcher.find()) {
                    String arg = matcher.group(2);
                    switch (matcher.group(1).toLowerCase()) {
                        case "ytp" -> {
                            String[] argsy = arg.split(",");
                            line = matcher.replaceFirst("<iframe src=\"https://www.youtube.com/embed/" + argsy[0] + "\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen class=\"ac-ytp\" " + String.join(" ", argsy) + "></iframe>");
                        }
                        case "embed" -> {
                            String[] argse = arg.split(",");
                            line = matcher.replaceFirst("<iframe src=\"" + argse[0] + "\" class=\"ac-embed\" " + String.join(" ", argse) + "></iframe>");
                        }
                        // hide 함수는 바로 적용
                        case "hide" -> {
                            String[] argsh = arg.split(",");
                            line = "<button class=\"ac-hidden\" onclick=\"open(" + hide_code + ")\" id=\"ac-open-" + hide_code + "\">" + argsh[0] + "</button><div id=\"ac-hidden-" + hide_code + "\" class=\"ac-hidden-content\">" + argsh[1] + "</div>";
                            hide_code++;
                        }
                        default -> line = "<p class=\"error\">IPUAC 오류: 함수 '"+matcher.group(1).toLowerCase()+"을(를) 이해할 수 없습니다.</p>";
                    }
                }
                else break;
            }

            // Bullet list 바로 적용
            if(line.startsWith("*")) {
                html = new StringBuilder();
                if(!listFlag) {
                    html = new StringBuilder("<ul class=\"ac-ul\">");
                }
                html.append("<li class=\"ac-ul-li\">")
                    .append(line.substring(2));
                listFlag = true;
                finalDom.add(html.toString());
                continue;
            }
            // 이번줄은 리스트가 아니지만 저번이 리스트였다면 리스트 끝내줌
            else if(listFlag) {
                finalDom.setElementAt(finalDom.get(finalDom.size()-1)+"</ul>", finalDom.size()-1);
                listFlag = false;
            }

            // 테이블 바로 적용
            if(line.startsWith("||")) {
                html = new StringBuilder();
                //테이블이 처음 시작된 경우 테이블 열기
                if(!tableFlag) {
                    html = new StringBuilder("<table class=\"ac-table\"><tbody class=\"ac-tbody\">");
                }
                html = new StringBuilder(switch (definitions.getOrDefault("table-align", "0")) {
                    case "1" -> html + "<tr class=\"ac-left\">";
                    case "2" -> html + "<tr class=\"ac-center\">";
                    case "3" -> html + "<tr class=\"ac-right\">";
                    case "4" -> html + "<tr class=\"ac-stretch\">";
                    default -> html + "<tr>";
                });
                String[] inTable = line.substring(2, line.length()-2).split("[|]{2}");
                for (String td : inTable) {
                    Matcher matcher = tableElement.matcher(td);
                    if(matcher.find()) {
                        String inst = matcher.group(1);
                        if(inst.charAt(0)=='-') {
                            html.append("<td colspan=\"").append(inst.substring(1)).append("\">").append(matcher.group(2)).append("</td>");
                        }
                        else if(inst.charAt(0)=='|') {
                            html.append("<td rowspan=\"").append(inst.substring(1)).append("\">").append(matcher.group(2)).append("</td>");
                        }
                        else html.append("<td>").append(td).append("</td>");
                    }
                    else html.append("<td>").append(td).append("</td>");
                }
                html.append("</tr>");
                tableFlag = true;
                finalDom.add(html.toString());
                continue;
            }
            // 이번줄은 테이블이 아니지만 저번줄이 테이블이면 테이블 닫기
            else if(tableFlag) {
                finalDom.setElementAt(finalDom.get(finalDom.size()-1)+"</tbody></table>", finalDom.size()-1);
                tableFlag = false;
            }

            // 인용문 바로 적용
            if(line.charAt(0) == '!') {
                if(quoteFlag) {
                    html = new StringBuilder("<br>" + line.substring(1));
                }
                else html = new StringBuilder("<blockquote class=\"ac-quote\">" + line.substring(1));
                quoteFlag = true;
                finalDom.add(html.toString());
                continue;
            }
            // 이번줄은 인용이 아니지만 저번줄이 인용이면 저번 인용 닫기
            else if(quoteFlag) {
                finalDom.setElementAt(finalDom.elementAt(finalDom.size()-1)+"</blockquote>", finalDom.size()-1);
                quoteFlag = false;
            }

            // IPUAC -> HTML 교체 후 길이 업데이트
            len = line.length();

            // 섹션인 경우 바로 적용
            if(section.matcher(line).matches()) {
                html = new StringBuilder("<span class=\"prob-title ipuac\">" + line.substring(2, len - 2) + "</span><hr class=\"prob-hr ipuac\">");
                finalDom.add(html.toString());
                continue;
            }

            if(line.charAt(0) == '\\') {
                html = new StringBuilder(switch (line.charAt(1)) {
                    case '1' -> "<p class=\"ac-left ipuac\">" + line.substring(2, len) + "</p>";
                    case '2' -> "<p class=\"ac-center ipuac\">" + line.substring(2, len) + "</p>";
                    case '3' -> "<p class=\"ac-right ipuac\">" + line.substring(2, len) + "</p>";
                    case '4' -> "<p class=\"ac-stretch ipuac\">" + line.substring(2, len) + "</p>";
                    default -> "<p class=\"ipuac\">" + line + "</p>";
                });
            }
            else if(definitions.containsKey("text-align")) {
                html = new StringBuilder(switch (definitions.get("text-align")) {
                    case "1" -> "<p class=\"ac-left ipuac\">" + line + "</p>";
                    case "2" -> "<p class=\"ac-center ipuac\">" + line + "</p>";
                    case "3" -> "<p class=\"ac-right ipuac>" + line + "</p>";
                    case "4" -> "<p class=\"ac-stretch ipuac\">" + line + "</p>";
                    default -> "<p class=\"ipuac\">" + line + "</p>";
                });
            }
            else html = new StringBuilder("<p class=\"ipuac\">" + line + "</p>");
            finalDom.add(html.toString());
        }
        StringBuilder finalHtml = new StringBuilder();
        for (String line : finalDom) {
            finalHtml.append(line);
        }
        return finalHtml.toString();
    }
}
