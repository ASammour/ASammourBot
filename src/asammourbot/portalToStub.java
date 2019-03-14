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
public class portalToStub {

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

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        List pages = getSqlRecords("SELECT page_title,\n"
                + "\n"
                + "  (SELECT replace(replace(cl_to, \"بوابة_\", \"\"), \"/مقالات_متعلقة\", \"\")\n"
                + "   FROM categorylinks\n"
                + "   WHERE cl_from = page_id\n"
                + "     AND cl_to LIKE\"%/مقالات_متعلقة\"\n"
                + "   LIMIT 1) AS \"stub\"\n"
                + "FROM page\n"
                + "INNER JOIN categorylinks cl1 ON cl1.cl_from = page_id\n"
                + "WHERE cl1.cl_to = \"مقالات_بذور_عامة\"\n"
                + "  AND\n"
                + "    (SELECT concat(\"بذرة_\", replace(replace(cl_to, \"بوابة_\", \"\"), \"/مقالات_متعلقة\", \"\"))\n"
                + "     FROM categorylinks\n"
                + "     WHERE cl_from = page_id\n"
                + "       AND cl_to LIKE\"%/مقالات_متعلقة\"\n"
                + "     LIMIT 1) IN\n"
                + "    (SELECT page_title\n"
                + "     FROM page\n"
                + "     WHERE page_namespace = 10\n"
                + "     );");

        for (Object tmp : pages) {
            String title = tmp.toString().split(",,,,,,,")[0];
            String stub = tmp.toString().split(",,,,,,,")[1];
            String content = wiki.getPageText(title);
            content = content.replace("{{بذرة}}", "{{بذرة " + stub.replace("_"," ") + "}}");

            Tead t = new Tead(title, content, "روبوت:تخصيص البذرة {{بذرة " + stub + "}}");
            t.start();
            Thread.sleep(1000);
        }
    }
}
