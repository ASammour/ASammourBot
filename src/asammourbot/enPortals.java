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
public class enPortals {

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
        List pages = getSqlRecords("select p1.page_title, group_concat(ll2.ll_title SEPARATOR '***') AS ll_title\n"
                + "from page p1\n"
                + "inner join langlinks ll1\n"
                + "on ll1.ll_from = p1.page_id\n"
                + "inner join enwiki_p.page p2\n"
                + "on p2.page_title = replace(ll1.ll_title,\" \",\"_\")\n"
                + "inner join enwiki_p.page p3\n"
                + "on p3.page_title = p2.page_title\n"
                + "inner join enwiki_p.pagelinks pl1\n"
                + "on pl1.pl_from = p3.page_id\n"
                + "inner join enwiki_p.page p4\n"
                + "on p4.page_title = pl1.pl_title\n"
                + "inner join enwiki_p.langlinks ll2\n"
                + "on ll2.ll_from = p4.page_id\n"
                + "where p1.page_namespace = 0\n"
                + "and p1.page_is_redirect = 0\n"
                + "and ll1.ll_lang = \"en\"\n"
                + "and p2.page_namespace = 0\n"
                + "and p2.page_is_redirect = 0\n"
                + "and p3.page_is_redirect = 0\n"
                + "and p3.page_namespace = 1\n"
                + "and pl1.pl_namespace = 100\n"
                + "and p4.page_is_redirect = 0\n"
                + "and p4.page_namespace = 100\n"
                + "and ll2.ll_lang = \"ar\"\n"
                + "and p1.page_id not in (select cl_from from categorylinks where cl_to = \"صفحات_توضيح\")\n"
                + "and p1.page_id not in (select cl_from from categorylinks where cl_to = concat(\"بوابة_\",replace(replace(ll2.ll_title,\"بوابة:\",\"\"),\" \",\"_\"),\"/مقالات_متعلقة\"))\n"
                + "group by p1.page_title;");

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
            if (!summary.equals("")) {

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
                Tead t = new Tead(title, content, "روبوت: إضافة بوابات معادلة من المقابل الإنجليزي " + summary);
                t.start();
                Thread.sleep(1000);
            }
        }
    }
}
