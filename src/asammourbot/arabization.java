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
import java.util.List;
import java.util.Properties;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class arabization {

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
            String from = rs.getString("pl_title");
            String to = rs.getString("ll_title");

            records.add(firstName + ",,,,,,," + from + ",,,,,,," + to);
        }
        return records;
    }

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        Wiki wiki = new Wiki("ar.wikipedia.org");
        List pages = getSqlRecords("select p.page_title, pl.pl_title, ell.ll_title\n"
                + "from page p\n"
                + "inner join pagelinks pl\n"
                + "on pl.pl_from = p.page_id\n"
                + "inner join enwiki_p.page ep\n"
                + "on pl.pl_title = ep.page_title\n"
                + "inner join enwiki_p.langlinks ell\n"
                + "on ep.page_id = ell.ll_from\n"
                + "where p.page_is_redirect = 0\n"
                + "and p.page_namespace = 0\n"
                + "and ell.ll_lang like \"ar\"\n"
                + "and pl.pl_from_namespace = 0\n"
                + "and pl.pl_namespace = 0\n"
                + "and ep.page_namespace = 0\n"
                + "and ep.page_is_redirect = 0\n"
                + "and pl.pl_title not in (select page_title from page where page_title = pl.pl_title and\n"
                + "                                       page_namespace = 0)\n"
                + "and p.page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = p.page_id)\n"
                + ";");

        for (Object tmp : pages) {
            if (tmp.toString().split(",,,,,,,").length == 3) {

                String title = tmp.toString().split(",,,,,,,")[0];
                String from = tmp.toString().split(",,,,,,,")[1];
                String to = tmp.toString().split(",,,,,,,")[2];

                String content = wiki.getPageText(title);
                if (content.contains("[[" + from) || content.contains("[[" + from.replace("_", " "))) {

                    content = content.replace("[[" + from+"]]", "[[" + to+"]]");
                    content = content.replace("[[" + from.replace("_", " ")+"]]", "[[" + to+"]]");

                    Tead t = new Tead(title, content, "روبوت:تعريب (" + from + "->[[" + to + "]])");
                    t.start();
                    while (t.isAlive()) {
                        Thread.sleep(1000);
                    }
                }
            }
        }
    }
}
