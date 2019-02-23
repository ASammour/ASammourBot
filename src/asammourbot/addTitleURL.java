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

import static asammourbot.tagger.getRegexRecords;
import static asammourbot.tagger.wiki;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.jsoup.Jsoup;

/**
 *
 * @author ASammour
 */
public class addTitleURL {

    public static void checkNoTitle(ArrayList pages) throws IOException, InterruptedException, FailedLoginException, LoginException {
        for (Object tmp : pages) {
            String content = wiki.getPageText(tmp.toString());
            Pattern pattern = Pattern.compile("\\<ref\\>\\[[^ ]{15,}\\]\\<\\/ref\\>", Pattern.CASE_INSENSITIVE);
            Matcher urlMatcher = pattern.matcher(content);

            while (urlMatcher.find()) {
                String url = urlMatcher.group();
                String withoutRef = url.replace("<ref>[", "").replace("]</ref>", "");

                org.jsoup.nodes.Document document = null;

                try {
                    document = Jsoup.connect(withoutRef).get();
                } catch (Exception e) {

                }
                if (document != null) {
                    if (!document.title().toLowerCase().contains("move") 
                            && !document.title().toLowerCase().contains("delete")
                            && !document.title().toLowerCase().contains("perment")
                            && !document.title().toLowerCase().contains("object")
                            && !document.title().toLowerCase().contains("error")
                            && !document.title().toLowerCase().contains("error")
                            && !document.title().toLowerCase().contains("fail")){
                        
                    }
                    content = content.replace(url, url.replace("]</ref>", " "+document.title()+"]</ref>"));
                    content = decodeUrl(content);
                    wiki.login("ASammourBot", "crome801501101");
                    wiki.edit(tmp.toString(), content, "روبوت:إضافة عنوان آلي لرابط بدون عنوان", true, true, -2, null);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, LoginException {
        checkNoTitle(getRegexRecords("insource:/\\<ref\\>\\[[^ ]{15,}\\]\\<\\/ref\\>/"));

    }

    public static String decodeUrl(String content) {
        content = content.replace("%D8%A7", "ا");
        content = content.replace("%D9%B1", "ٱ");
        content = content.replace("%D8%A5", "إ");
        content = content.replace("%D8%A3", "أ");
        content = content.replace("%D8%A2", "آ");
        content = content.replace("%D8%A1", "ء");
        content = content.replace("%D9%94", " ٔ");
        content = content.replace("%D9%B4", "ٴ");
        content = content.replace("%D9%95", "ٕ");
        content = content.replace("%D8%A6", "ئ");
        content = content.replace("%D8%A4", "ؤ");
        content = content.replace("%D8%A8", "ب");
        content = content.replace("%D8%AA", "ت");
        content = content.replace("%D8%A9", "ة");
        content = content.replace("%D8%AB", "ث");
        content = content.replace("%D8%AC", "ج");
        content = content.replace("%D8%AD", "ح");
        content = content.replace("%D8%AE", "خ");
        content = content.replace("%D8%AF", "د");
        content = content.replace("%D8%B0", "ذ");
        content = content.replace("%D8%B1", "ر");
        content = content.replace("%D8%B2", "ز");
        content = content.replace("%D8%B3", "س");
        content = content.replace("%D8%B4", "ش");
        content = content.replace("%D8%B5", "ص");
        content = content.replace("%D8%B6", "ض");
        content = content.replace("%D8%B7", "ط");
        content = content.replace("%D8%B8", "ظ");
        content = content.replace("%D8%B9", "ع");
        content = content.replace("%D8%BA", "غ");
        content = content.replace("%D9%81", "ف");
        content = content.replace("%D9%82", "ق");
        content = content.replace("%D9%83", "ك");
        content = content.replace("%DA%A9", "ک");
        content = content.replace("%D9%84", "ل");
        content = content.replace("%D9%85", "م");
        content = content.replace("%D9%86", "ن");
        content = content.replace("%D9%87", "ه");
        content = content.replace("%D9%87%E2%80%8D", "ه‍");
        content = content.replace("%D9%88", "و");
        content = content.replace("%D9%8A", "ي");
        content = content.replace("%D9%89", "ى");
        content = content.replace("%D9%80", "ـ");
        content = content.replace("%D9%AA", "٪");
        content = content.replace("%D9%AD", "٭");
        content = content.replace("%D8%8C", "،");
        content = content.replace("%D9%A0", "٠");
        content = content.replace("%D9%A1", "١");
        content = content.replace("%D9%A2", "٢");
        content = content.replace("%D9%A3", "٣");
        content = content.replace("%D9%A4", "٤");
        content = content.replace("%D9%A5", "٥");
        content = content.replace("%D9%A6", "٦");
        content = content.replace("%D9%A7", "٧");
        content = content.replace("%D9%A8", "٨");
        content = content.replace("%D9%A9", "٩");
        return content;
    }

}
