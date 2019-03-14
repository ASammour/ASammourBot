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
public class stubToPortal {

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
            String file = rs.getString("stub");

            records.add(firstName + ",,,,,,," + file);
        }
        return records;
    }

    public static void append(String page, String template, String summary) throws InterruptedException, IOException {
        String content = wiki.getPageText(page);

        if (!content.contains("{{شريط بوابات")) {
            if (content.contains("[[تصنيف:")) {
                content = content.replaceFirst("\\[\\[تصنيف\\:", template + "\n\n[[تصنيف:");
            } else {
                content = content + "\n\n" + template;
            }

            content = content.replace("{{مقالات بحاجة لشريط بوابات}}\n", "");
            Tead t = new Tead(page, content, summary);
            t.start();

            while (t.isAlive()) {
                Thread.sleep(5000);
            }
        }

    }

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        List pages = getSqlRecords("select page_title, (select replace(cl_to, \"بذرة_\",\"\") from categorylinks where cl_from = page_id and cl_to like \"بذرة%\" limit 1) as \"stub\"\n"
                + "from page\n"
                + "where page_namespace = 0\n"
                + "and page_is_redirect = 0\n"
                + "and page_id in (select cl_from from categorylinks where cl_from = page_id and cl_to = \"مقالات_بحاجة_لشريط_بوابات\")\n"
                + "and page_id in (select cl_from from categorylinks where cl_from = page_id and cl_to like \"بذرة%\")\n"
                + "and (select replace(cl_to, \"بذرة_\",\"\") from categorylinks where cl_from = page_id and cl_to like \"بذرة%\" limit 1) in (select page_title from\n"
                + "                                                                                                                 page \n"
                + "                                                                                                                 where page_namespace = 100\n"
                + "                                                                                                                 and page_is_redirect = 0\n"
                + "                                                                                                                 );");

        for (Object tmp : pages) {
            String title = tmp.toString().split(",,,,,,,")[0];
            String stub = tmp.toString().split(",,,,,,,")[1];

            append (title, "{{شريط بوابات|"+stub.replace("_", " ")+"}}", "روبوت:إضافة شريط بوابات من قالب البذرة +[[بوابة:"+stub+"]]");
            
        }
    }
}
