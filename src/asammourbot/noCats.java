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

import static asammourbot.oppositeCats.addedCats;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class noCats {

    public static List getSqlRecords(String query) throws ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, IOException {

        List records = new ArrayList();
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Properties mycnf = new Properties();
        mycnf.load(new FileInputStream(System.getProperty("user.home") + "/replica.my.cnf"));
        String password = mycnf.getProperty("password");
        password = password.substring((password.startsWith("\"")) ? 1 : 0, password.length() - ((password.startsWith("\"")) ? 1 : 0));
        mycnf.put("password", password);
        mycnf.put("useOldUTF8Behavior", "true");
        mycnf.put("useUnicode", "true");
        mycnf.put("characterEncoding", "UTF-8");
        mycnf.put("connectionCollation", "utf8_general_ci");
        String url = "jdbc:mysql://arwiki.analytics.db.svc.eqiad.wmflabs:3306/arwiki_p";
        Connection conn = DriverManager.getConnection(url, mycnf);

        // create the java statement
        Statement st = conn.createStatement();

        // execute the query, and get a java resultset
        ResultSet rs = st.executeQuery(query);

        // iterate through the java resultset
        while (rs.next()) {
            String firstName = rs.getString("page_title");
            String file = rs.getString("cats");

            records.add(firstName + ",,,,,,," + file);
        }
        return records;
    }

    public static void run() throws IOException, FailedLoginException, LoginException, ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        String[] langs = {"fr", "de", "pt", "ru", "ja", "es", "he", "it","fa","tr","da","pl", "uk","ko","cs","ceb","sv","nl"};

        for (String tmp : langs) {
            List pages = getSqlRecords("select p.page_title, group_concat(p3.page_title SEPARATOR '***') as cats\n"
                    + "from page p\n"
                    + "inner join langlinks l1\n"
                    + "on l1.ll_from = p.page_id\n"
                    + "inner join " + tmp + "wiki_p.page\n"
                    + "on " + tmp + "wiki_p.page.page_title = replace(l1.ll_title, \" \", \"_\")\n"
                    + "inner join " + tmp + "wiki_p.categorylinks\n"
                    + "on " + tmp + "wiki_p.categorylinks.cl_from = " + tmp + "wiki_p.page.page_id\n"
                    + "inner join " + tmp + "wiki_p.page p2\n"
                    + "on p2.page_title = " + tmp + "wiki_p.categorylinks.cl_to\n"
                    + "inner join " + tmp + "wiki_p.langlinks el\n"
                    + "on p2.page_id  = el.ll_from\n"
                    + "inner join page p3\n"
                    + "on p3.page_title = replace(replace(el.ll_title, \"تصنيف:\",\"\"), \" \", \"_\")\n"
                    + "where p.page_is_redirect = 0\n"
                    + "and p.page_namespace = 0\n"
                    + "and " + tmp + "wiki_p.page.page_namespace = 0\n"
                    + "and " + tmp + "wiki_p.page.page_is_redirect = 0\n"
                    + "and p2.page_id not in (select " + tmp + "wiki_p.categorylinks.cl_from from " + tmp + "wiki_p.categorylinks  where " + tmp + "wiki_p.categorylinks.cl_to = (select SUBSTRING(replace(ll_title,\" \",\"_\"), POSITION(\":\" IN replace(ll_title,\" \",\"_\"))+1) from langlinks where ll_lang = \"" + tmp + "\" and ll_from = 308278))\n"
                    + "and p.page_id not in (select cl_from from categorylinks where cl_to = p3.page_title)\n"
                    + "and p3.page_id not in (select cl_from from categorylinks  where cl_to = 'تصنيفات_مخفية')\n"
                    + "and p3.page_id not in (select cl_from from categorylinks  where cl_to = 'أحداث_جارية')\n"
                    + "and p3.page_id not in (select cl_from from categorylinks  where cl_to = 'تحويلات_تصنيفات_ويكيبيديا')\n"
                    + "and p3.page_title not like \"صفحات_توضيح%\"\n"
                    + "and p.page_id not in (select cl_from from categorylinks  where cl_to = 'صفحات_لا_تقبل_التصنيف_المعادل')\n"
                    + "and p.page_id in (select cl_from from categorylinks  where cl_to = 'جميع_المقالات_غير_المصنفة')\n"
                    + "and p2.page_is_redirect = 0\n"
                    + "and p2.page_namespace = 14\n"
                    + "and p3.page_namespace = 14\n"
                    + "and p3.page_is_redirect = 0\n"
                    + "and l1.ll_lang like \"" + tmp + "\"\n"
                    + "and el.ll_lang like \"ar\"\n"
                    + "group by p.page_title;");
            for (Object page : pages) {
                String addedCats = "";
                String title = page.toString().split(",,,,,,,")[0];
                String[] cats = page.toString().split(",,,,,,,")[1].split("\\*\\*\\*");
                String content = wiki.getPageText(title);

                for (String cat : cats) {
                    System.out.println("********" + cat);
                    cat = cat.replace("_", " ");
                    addedCats = addedCats + "+ [[تصنيف:" + cat + "]] "; //ملخص التعديل
                    content = content + "\n\n" + "[[تصنيف:" + cat + "]]\n"; //إضافة إلى نص المقالة
                }

                Tead t = new Tead(title, sortcats(content), "روبوت:إضافة تصنيفات معادلة (" + tmp + ") لمقالة غير مصنفة " + addedCats);
                t.start();
                while (t.isAlive()) {
                    Thread.sleep(1000);
                }

            }

        }
    }

    public static String sortcats(String s) throws IOException, FailedLoginException {
        String tt = s;
        Set<String> treeSet = new TreeSet<String>();
        String urlRegex = "\\[\\[تصنيف:.{3,}\\]\\]";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(tt);
        String finaltext = "";

        while (urlMatcher.find()) {
            tt = tt.replace(urlMatcher.group(), ";;;;;;;");
            treeSet.add(urlMatcher.group());
        }

        for (Object tmp : treeSet) {
            finaltext = finaltext + tmp + "\n";
        }
        tt = tt.replace(";;;;;;;", "");
        tt = tt + finaltext;

        return tt;

    }
}
