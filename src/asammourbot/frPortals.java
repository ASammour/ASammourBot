package asammourbot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.wikipedia.Wiki;

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
/**
 *
 * @author ASammour
 */
public class frPortals {

    static Wiki wiki = new Wiki("ar.wikipedia.org");

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
            String file = rs.getString("ll_title");

            records.add(firstName + ",,,,,,," + file);
        }
        return records;
    }

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        List pages = getSqlRecords("SELECT p.page_title,\n"
                + "       group_concat(el.ll_title SEPARATOR '***') AS ll_title\n"
                + "FROM page p\n"
                + "INNER JOIN langlinks l1 ON l1.ll_from = p.page_id\n"
                + "INNER JOIN frwiki_p.page frpage ON (frpage.page_title) = (replace(l1.ll_title, \" \", \"_\"))\n"
                + "INNER JOIN frwiki_p.templatelinks frtemp ON frtemp.tl_from = frpage.page_id\n"
                + "INNER JOIN frwiki_p.page p2 ON (p2.page_title) = (replace(frtemp.tl_title, \"Portail_\", \"\"))\n"
                + "INNER JOIN frwiki_p.langlinks el ON p2.page_id = el.ll_from\n"
                + "INNER JOIN page p3 ON p3.page_title = replace(replace(el.ll_title, \"بوابة:\", \"\"), \" \", \"_\")\n"
                + "WHERE p.page_is_redirect = 0\n"
                + "  AND p.page_namespace =0\n"
                + "  AND frpage.page_namespace = 0\n"
                + "  AND frpage.page_is_redirect = 0\n"
                + "  AND p2.page_is_redirect = 0\n"
                + "  AND p2.page_namespace = 100\n"
                + "  AND p3.page_namespace = 100\n"
                + "  AND p3.page_is_redirect = 0\n"
                + "  AND frtemp.tl_from_namespace = 0\n"
                + "  AND frtemp.tl_namespace = 10\n"
                + "  AND el.ll_lang LIKE \"ar\"\n"
                + "  AND frtemp.tl_title LIKE \"Portail%\"\n"
                + "  AND p.page_id NOT IN\n"
                + "    (SELECT cl_from\n"
                + "     FROM categorylinks\n"
                + "     WHERE cl_to = concat(\"بوابة_\", p3.page_title, \"/مقالات_متعلقة\"))\n"
                + "  AND l1.ll_lang LIKE \"fr\"\n"
                + "  AND p.page_title NOT IN\n"
                + "    (SELECT cl_from\n"
                + "     FROM categorylinks\n"
                + "     WHERE cl_to LIKE \"%صفحات_توضيح%\"\n"
                + "       AND cl_from = p.page_title)\n"
                + "  AND p.page_title NOT IN\n"
                + "    (SELECT cl_from\n"
                + "     FROM categorylinks\n"
                + "     WHERE cl_to LIKE \"%الصفحات_التي_لا_تقبل_ربط_البوابات_المعادل%\"\n"
                + "       AND cl_from = p.page_title)\n"
                + "GROUP BY p.page_title;");

        for (Object tmp : pages) {
            String summary = "";
            String title = tmp.toString().split(",,,,,,,")[0];
            String[] portals = tmp.toString().split(",,,,,,,")[1].split("\\*\\*\\*");
            String portalsText = "";
            String content = wiki.getPageText(title);

            for (String tmp1 : portals) {
                if (!tmp1.contains("مثلية") && !tmp1.contains("إرهاب") && !content.contains("|" + tmp1.replace("بوابة:", "")) && !content.contains("لا لربط البوابات")) {
                    portalsText = portalsText + "|" + tmp1.replace("بوابة:", "");
                    summary = summary + ": [[" + tmp1 + "]]";
                }
            }

            if (content.contains("{{شريط بوابات")) {
                content = content.replace("{{شريط بوابات|", "{{شريط بوابات" + portalsText + "|");
            } else if (content.contains("{{شريط البوابات")) {
                content = content.replace("{{شريط البوابات|", "{{شريط البوابات" + portalsText + "|");
            } else {
                if (content.contains("[[تصنيف:")) {
                    content = content.replaceFirst("\\[\\[تصنيف\\:", "{{شريط بوابات" + portalsText + "}}\n\n[[تصنيف:");
                } else {
                    content = content + "\n\n" + "{{شريط بوابات" + portalsText + "}}";
                }
            }
            Tead t = new Tead(title, content, "روبوت: إضافة بوابات معادلة من المقابل الفرنسي " + summary);
            t.start();
            Thread.sleep(1000);
        }
    }
}
