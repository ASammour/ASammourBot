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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class testClosing {

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

    public static ArrayList getRegexRecords(String regex) throws IOException {
        ArrayList records = new ArrayList();
        String[][] search = wiki.search(regex, 0);

        for (String[] search1 : search) {
            records.add(search1[0] + ">>>" + search1[1]);
        }

        return records;
    }

    public static String getDate() {
        SimpleDateFormat ar = new SimpleDateFormat("MMMM yyyy", new Locale("ar-EG"));
        Date date = new Date();
        return ar.format(date);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getDate());
        Wiki wiki = new Wiki("ar.wikipedia.org");
        ArrayList x = getRegexRecords("incategory:\"جميع مقالات البذور\"");

        for (Object tmp : x) {
            String title = tmp.toString().split(">>>")[0];
            String size = tmp.toString().split(">>>")[1];
            if (Integer.parseInt(size) > 500) {
                System.out.println(title);
            }
        }

    }
}
