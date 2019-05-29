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
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */


/**
 * تقوم هذه المهمة بإضافة تصنيف للمقالة التي لها نفس العنوان
 * مثال
 * مقالة تاريخ مصر
 * يضيف لها 
 * تصنيف:تاريخ مصر
 * وهكذا
 * @author ASammour
 */
public class addCats {

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

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, FailedLoginException, InterruptedException {
        
        Wiki wiki = new Wiki ("ar.wikipedia.org");
        ArrayList pages = getSqlRecords("select p1.page_title\n"
                + "from page p1\n"
                + "inner join page p2\n"
                + "on p2.page_title = p1.page_title\n"
                + "where p1.page_namespace = 0\n"
                + "and p1.page_is_redirect = 0\n"
                + "and p2.page_namespace = 14\n"
                + "and p2.page_is_redirect = 0\n"
                + "and p1.page_id not in (select cl_from from categorylinks where cl_from = p1.page_id and cl_to = p2.page_title)\n"
                + "and p2.page_id not in (select cl_from from categorylinks where cl_from = p2.page_id and cl_to like \"تحويلات_تصنيفات_ويكيبيديا\")\n"
                + "and p1.page_title like \"%\\_%\";");

        for (Object tmp : pages) {
            String content = wiki.getPageText(tmp.toString());
            content = content +"\n[[تصنيف:"+tmp.toString().replace("_", " ")+"]]\n";
            content  = sortcats(content);
            Tead t = new Tead (tmp.toString(),content,"روبوت:إضافة تصنيف حسب العنوان +[[تصنيف:"+tmp.toString()+"]]");
            t.start();
            while (t.isAlive()){
                Thread.sleep(1000);
            }
        }
    }

}
