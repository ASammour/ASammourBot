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

import static asammourbot.deadEnd.wiki;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class addStub {

    static Wiki wiki = new Wiki("ar.wikipedia.org");

    public static void main(String[] args) throws IOException, FailedLoginException {
        wiki.login("ASammourBot", "crome801501101");

        String[] pages = wiki.getCategoryMembers("مقالات بذور عامة", false, 0);

        for (String t : pages) {
            ArrayList x = new ArrayList ();
            
            String content = wiki.getPageText(t);

            String links = "";

            ArrayList records = new ArrayList();
            String[][] search = wiki.search("morelike:" + t, 0);

            for (String[] search1 : search) {
                String title = search1[0];
                String con = wiki.getPageText(title);
                String stub = getStub(con);
                if (!stub.equals("")){
                    if (content.contains(stub.replace("بذرة", "").trim())){
                        
                    }
                }
                break;
                //wiki.edit(t, content, "روبوت:إضافة وصلات داخلية (" + links + ")", true, true, -2, null);
            }

        }

    }

    public static String getStub(String content) {
        String stub = "";
        Pattern pattern = Pattern.compile("\\{\\{بذرة .{2,20}\\}\\}", Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(content);

        while (urlMatcher.find()) {
            String url = urlMatcher.group();
            stub = url;
        }
        return stub;
    }
}
