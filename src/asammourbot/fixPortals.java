/* 
 * The MIT License
 *
 * Copyright 2019 ASammour.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package asammourbot;

import static asammourbot.tagger.wiki;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class fixPortals {

    public static void main(String[] args) throws IOException, InterruptedException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        ArrayList portalToNav = new ArrayList();

        portalToNav.addAll(Arrays.asList(wiki.whatTranscludesHere("قالب:بوابة", 14)));

        portalToNav(portalToNav);
       
    }

    public static void portalToNav(ArrayList pages) throws IOException, InterruptedException {
        String finalText = "{{شريط بوابات";

        for (Object tmp : pages) {
            String content = wiki.getPageText(tmp.toString());
            
            content = content.replace("{{Portal", "{{بوابة");
            content = content.replace("{{portal", "{{بوابة");
            
            String[] portals = getPortals(content);
            String[] portalsNav = getPortalsNav(content);

            if (portals.length != 0) {

                for (String portal : portals) {
                    if (!Arrays.asList(portalsNav).contains(portal) && !portal.equals("")) {
                        finalText = finalText + "|" + portal;
                    }
                }

                for (String navPortal : portalsNav) {
                    if (!finalText.contains(navPortal)) {
                        finalText = finalText + "|" + navPortal;
                    }
                }

                finalText = finalText + "}}";

                content = remove(content, "\\{\\{بوابة\\|.{2,}\\}\\}\n");
                content = remove(content, "\\{\\{شريط بوابات\\|.{2,}\\}\\}\n");
                content = remove(content, "\\{\\{مقالات بحاجة لشريط بوابات\\}\\}\n");
                append(tmp.toString(), content, finalText, "روبوت: استبدال [[قالب:بوابة]] بشريط بوابات (" + finalText + ")");
                finalText = "{{شريط بوابات";
                Arrays.fill(portals, null);
            }
        }

    }

    public static String[] getPortalsNav(String content) {
        String[] portals = {};
        Pattern pattern = Pattern.compile("\\{\\{شريط بوابات\\|.{2,}\\}\\}", Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(content);

        while (urlMatcher.find()) {
            String url = urlMatcher.group().replace("{{شريط بوابات", "").replace("}}", "");
            portals = url.split("\\|");
        }
        return portals;
    }

    public static String[] getPortals(String content) {
        String[] portals = {};
        Pattern pattern = Pattern.compile("\\{\\{بوابة\\|.{2,}\\}\\}", Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(content);

        while (urlMatcher.find()) {
            String url = urlMatcher.group().replace("{{بوابة", "").replace("}}", "");
            portals = url.split("\\|");
        }
        return portals;
    }

    public static String remove(String content, String regex) throws InterruptedException, IOException {
        String urlRegex = regex;
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(content);

        while (urlMatcher.find()) {
            String url = urlMatcher.group();
            content = content.replace(url, "");
        }
        return content;

    }

    public static void append(String title, String content, String template, String summary) throws InterruptedException, IOException {

        if (!content.contains(template.replace("{{", "").replace("}}", ""))) {
            if (content.contains("[[تصنيف:")) {

                content = content.replaceFirst("\\[\\[تصنيف\\:", template + "\n\n[[تصنيف:");
            } else {
                content = content + template;
            }

            Tead t = new Tead(title, content, summary);
            t.start();
            Thread.sleep(2000);
        }

    }
}
