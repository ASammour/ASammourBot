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
import java.util.Properties;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class redPortals {

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
            String file = rs.getString("portals");

            records.add(firstName + ",,,,,,," + file);
        }
        return records;
    }

    public static void run() throws IOException, ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {

        Wiki wiki = new Wiki("ar.wikipedia.org");

        List pages = getSqlRecords("select page_title, group_concat(cl_to) as \"portals\"\n"
                + "from categorylinks \n"
                + "inner join page\n"
                + "on page_id = cl_from\n"
                + "where cl_to like \"بوابة%/مقالات_متعلقة\"\n"
                + "and cl_to not in (select page_title from page where page_is_redirect = 0 and page_namespace = 14)\n"
                + "and page_namespace = 0\n"
                + "and page_is_redirect = 0\n"
                + "group by page_title;");

        for (Object tmp : pages) {
            String portalsText = "";
            String title = tmp.toString().split(",,,,,,,")[0];
            String[] portals = tmp.toString().split(",,,,,,,")[1].split(",");
            String content = wiki.getPageText(title);
            for (String tmp1 : portals) {
                String portal = tmp1.replace("/مقالات_متعلقة", "")
                        .replace("بوابة_", "")
                        .replace("_", " ");
                content = content.replace("|" + portal + "|", "|");
                content = content.replace("|" + portal + "}}", "}}");
                content = content.replace("{{شريط بوابات}}", "");
                portalsText = portalsText + " :[[" + tmp1.replace("/مقالات_متعلقة", "") + "]]";
            }
            Tead t = new Tead(title, content, "روبوت:إزالة بوابات غير موجودة" + portalsText);
            t.start();
            Thread.sleep(1000);
            
        }

    }
}
