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

import static asammourbot.portalToStub.getSqlRecords;
import static asammourbot.tagger.append;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.security.auth.login.LoginException;
import org.jsoup.Jsoup;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class cat {

    public static List getPortals(String query) throws ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, IOException {

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
            String file = rs.getString("portals");

            records.add(firstName + ",,,,,,," + file);
        }
        return records;
    }

    public static ArrayList getSqlRecords(String query) throws ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, IOException {
        ArrayList records = new ArrayList();
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
            records.add(firstName);
        }
        return records;
    }

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException, LoginException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        ArrayList pages = getSqlRecords("select concat(\"تصنيف:\",p.page_title) as page_title from page p\n"
                + "inner join page p1\n"
                + "on p1.page_title = p.page_title\n"
                + "where p.page_namespace = 14\n"
                + "and p.page_is_redirect = 0\n"
                + "and p1.page_namespace = 0\n"
                + "and p1.page_is_redirect = 0\n"
                + "and p.page_id not in (select pl_from from pagelinks where pl_from_namespace = 14 and pl_namespace = 0)\n"
                + "and p.page_id not in (select cl_from from categorylinks where cl_from = p.page_id and cl_to = \"تحويلات_تصنيفات_ويكيبيديا\");");

        for (Object tmp : pages) {
            String con = wiki.getPageText(tmp.toString());
            con = "{{مزيد|" + tmp.toString().replace("تصنيف:", "").replace("_", " ") + "}}\n" + con;
            Tead t = new Tead(tmp.toString(), con, "روبوت:إضافة [[قالب:مزيد]]");
            t.start();
            while (t.isAlive()) {
                Thread.sleep(1000);
            }
        }

        List port = getPortals("select p1.page_title, group_concat(distinct ll2.ll_title) as \"portals\"\n"
                + "from page p1\n"
                + "inner join langlinks ll1\n"
                + "on ll1.ll_from = p1.page_id\n"
                + "inner join frwiki_p.page p2\n"
                + "on p2.page_title = replace(replace(ll1.ll_title,\" \",\"_\"),\"Catégorie:\",\"\")\n"
                + "inner join frwiki_p.pagelinks pl1\n"
                + "on pl1.pl_from = p2.page_id\n"
                + "inner join frwiki_p.page p3\n"
                + "on p3.page_title = pl1.pl_title\n"
                + "inner join frwiki_p.langlinks ll2\n"
                + "on ll2.ll_from = p3.page_id\n"
                + "where p1.page_namespace = 14\n"
                + "and p1.page_is_redirect = 0\n"
                + "and p2.page_namespace = 14\n"
                + "and p2.page_is_redirect = 0\n"
                + "and ll1.ll_lang = \"fr\"\n"
                + "and ll2.ll_lang = \"ar\"\n"
                + "and p3.page_namespace = 100\n"
                + "and p3.page_is_redirect = 0\n"
                + "and p1.page_id not in (select tl_from \n"
                + "                       from templatelinks \n"
                + "                       where tl_from = p1.page_id \n"
                + "                       and tl_from_namespace = 14 and tl_namespace = 10\n"
                + "                       and (tl_title in (\"شريط_بوابات\",\"بوابة\")))\n"
                + "and replace(replace(ll2.ll_title,\" \",\"_\"),\"بوابة:\",\"\") not in (select pl_title from pagelinks where pl_title = replace(replace(ll2.ll_title,\" \",\"_\"),\"بوابة:\",\"\")\n"
                + "                                         and pl_from = p1.page_id)\n"
                + "group by p1.page_title"
                + " limit 100;");

        for (Object tmp : port) {
            String portalsText = "";
            String title = "تصنيف:" + tmp.toString().split(",,,,,,,")[0];
            String portals = tmp.toString().split(",,,,,,,")[1];
            String content = wiki.getPageText(title);
            for (String tmp1 : portals.split(",")) {
                portalsText = portalsText + "|" + tmp1.replace("بوابة:", "");
            }
            if (content.contains("{{بوابة")) {
                content = content.replace("{{بوابة", "{{بوابة" + portalsText);

            } else if (content.contains("{{شريط بوابات")) {
                content = content.replace("{{شريط بوابات", "{{شريط بوابات" + portalsText);

            } else {
                content = "{{بوابة" + portalsText + "}}\n" + content;

            }

            Tead t = new Tead(title, content, "روبوت:إضافة بوابات من المقابل الفرنسي {{بوابة" + portalsText + "}}");
            t.start();
            while (t.isAlive()) {
                Thread.sleep(1000);
            }

        }

        List port2 = getPortals("select p.page_title, group_concat(replace(replace(c.cl_to,\"بوابة_\",\"\"),\"/مقالات_متعلقة\",\"\")) as \"portals\" from page p\n"
                + "inner join page p1\n"
                + "on p1.page_title = p.page_title\n"
                + "inner join categorylinks c\n"
                + "on c.cl_from = p1.page_id\n"
                + "where p.page_namespace = 14\n"
                + "and p.page_is_redirect = 0\n"
                + "and p1.page_namespace = 0\n"
                + "and p1.page_is_redirect = 0\n"
                + "and c.cl_to like \"%/مقالات_متعلقة\"\n"
                + "and p.page_id not in (select tl_from \n"
                + "                       from templatelinks \n"
                + "                       where tl_from = p.page_id \n"
                + "                       and tl_from_namespace = 14 and tl_namespace = 10\n"
                + "                       and (tl_title in (\"شريط_بوابات\",\"بوابة\")))\n"
                + "group by p1.page_title limit 100;");

        for (Object tmp : port2) {
            String portalsText = "";
            String title = "تصنيف:" + tmp.toString().split(",,,,,,,")[0];
            String portals = tmp.toString().split(",,,,,,,")[1];
            String content = wiki.getPageText(title);
            for (String tmp1 : portals.split(",")) {
                portalsText = portalsText + "|" + tmp1.replace("بوابة:", "").replace("_", " ");
            }
            if (content.contains("{{بوابة")) {
                content = content.replace("{{بوابة", "{{بوابة" + portalsText);

            } else if (content.contains("{{شريط بوابات")) {
                content = content.replace("{{شريط بوابات", "{{شريط بوابات" + portalsText);

            } else {
                content = "{{بوابة" + portalsText + "}}\n" + content;

            }

            Tead t = new Tead(title, content, "روبوت:إضافة بوابات من الصفحة الأم {{بوابة" + portalsText + "}}");
            t.start();
            while (t.isAlive()) {
                Thread.sleep(1000);
            }

        }

    }
}
