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
import static java.sql.DriverManager.println;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class quarry {
    static  Wiki wiki = new Wiki("ar.wikipedia.org");

    public static String getQuarry(String title, String content) throws InterruptedException, IOException {
        String template = "";
        String urlRegex = "\\{\\{استعلام\\|[0-9]{1,7}\\}\\}";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(content);

        while (urlMatcher.find()) {
            String url = urlMatcher.group();
            template = url;
        }
        return template;
    }

    public static String getQuery(String url) throws InterruptedException, IOException {
        String query = "";
        Document document = Jsoup.connect("https://quarry.wmflabs.org/query/" + url).followRedirects(false).get();
        query = document.getElementById("code").text();
        query = query.substring(query.indexOf(";") + 1);

        return query;
    }

    public static ArrayList getSqlRecords(String query, String title, String quarryNumber) throws ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, IOException, LoginException, FailedLoginException, InterruptedException {
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
        write (rs,title, quarryNumber);

        return records;
    }

    public static void write(ResultSet rs, String title, String quarryNumber) throws IOException, SQLException, FailedLoginException, LoginException, InterruptedException {
        String finals = "";
        ResultSetMetaData md = rs.getMetaData();
        int count = md.getColumnCount();
        finals = finals +"<table border=1>\n";
        finals = finals +"<tr>";
        for (int i = 1; i <= count; i++) {
            finals = finals +"<th>";
            finals = finals +md.getColumnLabel(i);
        }
        finals = finals +"</tr>";
        while (rs.next()) {
            finals = finals +"<tr>";
            for (int i = 1; i <= count; i++) {
                finals = finals +"<td>";
                finals = finals +rs.getString(i);
            }
            finals = finals +"</tr>";
        }
        finals = finals +"</table>";
        Tead t = new Tead (title, "{{استعلام|"+quarryNumber+"}}\n\nآخر تحديث كان بواسطة: --~~~~\n\n"+finals,"روبوت:تحديث");
        t.start();
        Thread.sleep(1000);
    }

    public static void run() throws IOException, InterruptedException, ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, LoginException {
        
        String[] pages = wiki.whatTranscludesHere("قالب:استعلام", Wiki.ALL_NAMESPACES);

        for (String tmp : pages) {
            String content = wiki.getPageText(tmp);
            String template = getQuarry(tmp, content).replace("{{استعلام|", "").replace("}}", "");
            if (!template.equals("")) {
                getSqlRecords(getQuery(template),tmp, template);
            }
        }

    }
}
