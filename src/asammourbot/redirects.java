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
public class redirects {

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
            String redirect = rs.getString("rd_title");
            String firstName = rs.getString("page_title");
            String file = rs.getString("page_namespace");

            records.add(firstName + ",,,,,,," + file + ",,,,,,," + redirect);
        }
        return records;
    }

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        List broken = getSqlRecords("select rd_title, page_title, page_namespace from redirect \n"
                + "inner join page \n"
                + "on page_id =rd_from\n"
                + "where rd_title not in (select page_title from page where page_title= rd_title and rd_namespace = page_namespace)\n"
                + "and page_is_redirect = 1\n"
                + "and rd_namespace >= 0\n"
                + "and rd_interwiki = \"\"\n"
                + "limit 100;");

        List doub = getSqlRecords("select rd_title, page_title, page_namespace from redirect \n"
                + "inner join page \n"
                + "on page_id =rd_from\n"
                + "where rd_title in (select page_title from page where page_title= rd_title and rd_namespace = page_namespace and page_is_redirect = 1)\n"
                + "and page_is_redirect = 1\n"
                + "and rd_namespace >= 0\n"
                + "and rd_interwiki = \"\"\n"
                + "limit 100;");

        for (Object tmp : broken) {
            String title = tmp.toString().split(",,,,,,,")[1];
            String namespace = tmp.toString().split(",,,,,,,")[2];
            String redirect = tmp.toString().split(",,,,,,,")[0];
            String content = wiki.getPageText(title);
            title = wiki.getNamespaces().get(Integer.parseInt(namespace)) + ":" + title;
            Tead t = new Tead(title, "{{شطب|وسم آلي لتحويلة مكسورة}}\n" + content, "روبوت:تحويلة مكسورة");
            t.start();
            Thread.sleep(1000);
        }

        for (Object tmp : doub) {
            String title = tmp.toString().split(",,,,,,,")[1];
            String namespace = tmp.toString().split(",,,,,,,")[2];
            String redirect = wiki.resolveRedirect(tmp.toString().split(",,,,,,,")[0]);
            String content = wiki.getPageText(title);
            title = wiki.getNamespaces().get(Integer.parseInt(namespace)) + ":" + title;
            content = content.replace("[[" + tmp.toString().split(",,,,,,,")[2] + "]]", "[[" + redirect + "]]");
            Tead t = new Tead(title, content, "روبوت:إصلاح وصلة مزدوجة");
            t.start();
            Thread.sleep(1000);

        }
    }
}
