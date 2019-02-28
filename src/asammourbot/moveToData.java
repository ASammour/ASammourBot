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
import javax.security.auth.login.LoginException;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class moveToData {

    public static ArrayList getRegexRecords(String regex, int namespace) throws IOException {
        ArrayList records = new ArrayList();
        String[][] search = wiki.search(regex, namespace);

        for (String[] search1 : search) {
            records.add(search1[0]);
        }

        Wiki wiki = new Wiki("arwikipedia.org");
        return records;
    }

    public static void main(String[] args) throws IOException, LoginException, InterruptedException {
        String[] langs = {"fa","en", "fr", "de", "pt", "ru", "ja", "es", "he", "it", "tr", "da", "pl", "uk", "ko", "cs", "ceb", "sv", "nl"};

        for (String tmp : langs) {
            ArrayList pages = getRegexRecords("insource:/\\[\\[" + tmp + ":/", 0);
            for (Object page : pages) {
                String wikibase = wiki.getData(page.toString());

                String opposite = (wiki.getOpposite(wikibase, tmp));
                System.out.println(opposite);
                if (!opposite.trim().equals("")) {
                    String content = wiki.getPageText(page.toString());
                    content = content.replaceAll("\\[\\[" + tmp+".{1,}\\]\\]", "");
                    System.out.println(content);
                    Tead t = new Tead (page.toString(), content, "روبوت:إزالة وصلة لغات قديمة ("+"[[" + tmp + ":" + opposite + "]])");
                    t.start();
                    while (t.isAlive()){
                        Thread.sleep(100);
                    }
                }
            }
        }
    }
}
