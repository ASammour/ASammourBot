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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class deadEnd {

    public static List<String> getBalancedSubstrings(String s, Character markStart,
            Character markEnd, Boolean includeMarkers) {
        List<String> subTreeList = new ArrayList<String>();
        int level = 0;
        int lastOpenDelimiter = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == markStart) {
                level++;
                if (level == 1) {
                    lastOpenDelimiter = (includeMarkers ? i : i + 1);
                }
            } else if (c == markEnd) {
                if (level == 1) {
                    subTreeList.add(s.substring(lastOpenDelimiter, (includeMarkers ? i + 1 : i)));
                }
                if (level > 0) {
                    level--;
                }
            }
        }
        return subTreeList;
    }

    public static boolean isAllowed(String start, String end, String content) {
        int x = 0;
        Pattern p = Pattern.compile("\\" + start);
        Matcher m = p.matcher(content);
        while (m.find()) {
            x++;
        }

        int y = 0;
        p = Pattern.compile(end);
        m = p.matcher(content);
        while (m.find()) {
            y++;
        }
        return x == y;
    }

    public static void sort(String[][] search) {
        ArrayList results = new ArrayList();

        for (String[] search1 : search) {
            results.add(search1[0]);
        }

        Comparator c = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return Integer.compare(s1.length(), s2.length());
            }
        };

        Collections.sort(results, c);
    }

    public static void main(String[] args) throws IOException, FailedLoginException, LoginException {
        Wiki wiki = new Wiki("ar.wikipedia.org");
        wiki.login("ASammourBot", "crome801501101");

        String[] pages = wiki.getCategoryMembers("جميع مقالات النهاية المسدودة", false, 0);
        
        
        for (String t : pages) {

            String content = wiki.getPageText(t);
            String links = "";

            ArrayList records = new ArrayList();
            String[][] search = wiki.search("morelike:" + t, 0);
            sort(search);

            for (String[] search1 : search) {
                if (content.contains(" " + search1[0] + " ")
                        || content.contains(" ال" + search1[0] + " ")) {

                    if (!links.contains(search1[0])) {
                        links = links + ": [[" + search1[0] + "]]";
                    }

                    content = content.replace(" " + search1[0] + " ", " [[" + search1[0] + "]] ");
                    content = content.replace(" ال" + search1[0] + " ", " [[ال" + search1[0] + "]] ");

                }
                //wiki.edit(t, content, "روبوت:إضافة وصلات داخلية (" + links + ")", true, true, -2, null);
            }

            System.out.println(content);

        }
    }
}
