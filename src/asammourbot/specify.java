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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class specify {

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

    public static void run() throws IOException, ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {

        Wiki wiki = new Wiki("ar.wikipedia.org");
        Map langs = new HashMap();
        langs.put("fa", "-خرد");

        langs.put("en", "-stub");
        langs.put("sv", "stub");
        langs.put("tr", "-taslak");
        langs.put("ru", "-stub");
        langs.put("pt", "Esboço-");
        langs.put("ja", "-stub");
        langs.put("da", "stub");
        langs.put("ko", "토막글/");
        langs.put("uk", "-stub");

        Iterator entries = langs.entrySet().iterator();
        while (entries.hasNext()) {
            Entry thisEntry = (Entry) entries.next();
            Object key = thisEntry.getKey();
            Object value = thisEntry.getValue();
            List pages = getSqlRecords("select p.page_title, l2.ll_title\n"
                    + "from page p\n"
                    + "inner join categorylinks\n"
                    + "on cl_from = p.page_id\n"
                    + "inner join langlinks l1\n"
                    + "on l1.ll_from = p.page_id\n"
                    + "inner join " + key + "wiki_p.page p2\n"
                    + "on p2.page_title = replace(l1.ll_title,\" \", \"_\")\n"
                    + "inner join " + key + "wiki_p.templatelinks tl\n"
                    + "on tl.tl_from = p2.page_id \n"
                    + "inner join " + key + "wiki_p.page p3\n"
                    + "on p3.page_title = tl.tl_title\n"
                    + "inner join " + key + "wiki_p.langlinks l2\n"
                    + "on l2.ll_from = p3.page_id\n"
                    + "where cl_to = \"مقالات_بذور_عامة\"\n"
                    + "and tl.tl_title like \"%" + value + "%\"\n"
                    + "and tl.tl_from_namespace = 0\n"
                    + "and tl.tl_namespace = 10\n"
                    + "and l1.ll_lang = \"" + key + "\"\n"
                    + "and p2.page_namespace = 0\n"
                    + "and p2.page_is_redirect = 0\n"
                    + "and p3.page_is_redirect = 0\n"
                    + "and p3.page_namespace = 10\n"
                    + "and l2.ll_lang = \"ar\"\n"
                    + "and l2.ll_title like \"%بذرة%\";");

            for (Object tmp : pages) {

                String title = tmp.toString().split(",,,,,,,")[0];
                String stub = tmp.toString().split(",,,,,,,")[1].replace("قالب:", "");
                String content = wiki.getPageText(title);
                content = content.replace("{{بذرة}}", "{{" + stub + "}}");
                content = content.replace("{{ بذرة }}", "{{" + stub + "}}");
                content = content.replace("{{بذرة }}", "{{" + stub + "}}");
                content = content.replace("{{ بذرة}}", "{{" + stub + "}}");
                content = content.replace("{{بذرة|}}", "{{" + stub + "}}");
                content = content.replace("{{بذرة| }}", "{{" + stub + "}}");

                Tead t = new Tead(title, content, "روبوت:تخصيص البذرة : [[" + tmp.toString().split(",,,,,,,")[1] + "]]");
                t.start();
                Thread.sleep(1000);
            }
            pages.clear();
        }

    }
}
