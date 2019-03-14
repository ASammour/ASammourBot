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

import static asammourbot.addLinks.getRegexRecords;
import static asammourbot.tagger.getDate;
import static asammourbot.tagger.wiki;
import java.io.IOException;
import java.util.ArrayList;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class addPortals {

    public static void append(String page, String template, String summary) throws InterruptedException, IOException {
        String content = wiki.getPageText(page);

        if (!content.contains("{{شريط بوابات")) {
            if (content.contains("[[تصنيف:")) {
                content = content.replaceFirst("\\[\\[تصنيف\\:", template + "\n\n[[تصنيف:");
            } else {
                content = content + "\n\n" + template;
            }

            content = content.replace("{{مقالات بحاجة لشريط بوابات}}\n", "");
            Tead t = new Tead(page, content, summary);
            t.start();

            while (t.isAlive()) {
                Thread.sleep(5000);
            }
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        String[] pages = wiki.getCategoryMembers("مقالات بحاجة لشريط بوابات", false, 0);
        for (String tmp : pages) {
            String x = "{{شريط بوابات";

            String content = wiki.getPageText(tmp);
            String [] cats = wiki.getCategories(tmp, false, true);
            for (String tmp1:cats){
                String [] members = wiki.getCategoryMembers(tmp1, false, 0);
                for (String tmp2:members){
                }
            }
            
            System.out.println(x);
        }
    }
}



/*for (String tmp : pages) {
            String x = "{{شريط بوابات";
            String[] cats = wiki.getCategories(tmp, false, true);
            String content = wiki.getPageText(tmp);
            for (String tmp1 : cats) {
                tmp1 = tmp1.replace("تصنيف:", "");
                if (wiki.exists(tmp1.split("-------"))[0]) {
                    String[] portals = wiki.getCategories(tmp1, false, false);
                    for (String tmp2 : portals) {
                        if (tmp2.contains("/مقالات متعلقة") && !tmp2.contains("أعلام") 
                                && content.contains(tmp2.replace("تصنيف:بوابة ", "").replace("/مقالات متعلقة", ""))
                                && !x.contains(tmp2.replace("تصنيف:بوابة ", "").replace("/مقالات متعلقة", ""))) {
                            x = x + "|" + tmp2.replace("تصنيف:بوابة ", "").replace("/مقالات متعلقة", "");
                        }
                    }
                }
            }
            x = x+"}}";
            if (!x.equals("{{شريط بوابات}}")) {
                append(tmp, x, "روبوت:إضافة شريط بوابات من تصنيفات المقالة "+x);
                
            }
        }*/
