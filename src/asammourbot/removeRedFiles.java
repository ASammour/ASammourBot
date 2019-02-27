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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class removeRedFiles {

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
            String file = rs.getString("il_to");

            records.add(firstName + ",,,,,,," + file);
        }
        return records;
    }

    public static boolean containsOnce(String s, String sub) {
        boolean tf = false;
        s = s.replaceFirst(sub, "");
        tf = !s.contains(sub);
        return tf;
    }

    public static String getGallery(String content, String regex, String file) {

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(content);

        while (urlMatcher.find()) {
            String url = urlMatcher.group();
            String[] f = url.split("\n");
            for (String tmp : f) {
                if (tmp.contains(file)) {
                    content = content.replace(tmp + "\n", "");
                }
            }
        }
        return content;
    }

    public static String getInfoBox(String content, String file) {

        content = content.replace("ملف:" + file, "");
        content = content.replace("ملف: " + file, "");

        content = content.replace("File:" + file, "");
        content = content.replace("File: " + file, "");

        content = content.replace("file:" + file, "");
        content = content.replace("file: " + file, "");

        content = content.replace(file, "");

        return content;
    }

    public static String getSingle(String content, String file) {

        List<String> balanced = getBalancedSubstrings(content, '[', ']', true);

        for (String tmp : balanced) {
            if (tmp.toLowerCase().contains(file.toLowerCase())) {
                content = content.replace(tmp, "");
            }
        }
        return content;
    }

    public static List<String> getBalancedSubstrings(String s, Character markStart,
            Character markEnd, Boolean includeMarkers) {
        List<String> subTreeList = new ArrayList<String>();
        int level = 0;
        int lastOpenDelimiter = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == markStart) {
                level++;
                if (level == 1) {
                    lastOpenDelimiter = (includeMarkers ? i : i + 1);
                }
            } else if (c == markEnd) {
                if (level == 1) {
                    subTreeList.add(s.substring(lastOpenDelimiter, (includeMarkers ? i + 1 : i)));
                }
                if (level > 0) {
                    level--;
                }
            }
        }
        return subTreeList;
    }

    public static boolean isAllowed(String content) {
        boolean tf = false;
        int x = 0;
        int y = 0;

        Pattern p = Pattern.compile("\\[");
        Matcher m = p.matcher(content);
        while (m.find()) {
            x++;
        }

        p = Pattern.compile("\\]");
        m = p.matcher(content);
        while (m.find()) {
            y++;
        }

        if (x == y) {
            tf = true;
        }
        return tf;
    }

    public static void run() throws IOException, ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {
        System.out.println("dsadasda");
        Wiki wiki = new Wiki("ar.wikipedia.org");
        List pages = getSqlRecords("SELECT CONCAT(page_title) AS page_title, CONCAT(il_to) AS il_to\n"
                + "FROM page\n"
                + "JOIN imagelinks\n"
                + "ON page_id = il_from\n"
                + "WHERE (NOT EXISTS(\n"
                + "SELECT 1\n"
                + "FROM image\n"
                + "WHERE img_name = il_to))\n"
                + "AND (NOT EXISTS(\n"
                + "SELECT\n"
                + "1\n"
                + "FROM commonswiki_p.page\n"
                + "WHERE page_title = il_to\n"
                + "AND page_namespace = 6))\n"
                + "AND page_namespace = 0;");
        
        pages.addAll(getSqlRecords("SELECT CONCAT(\"قالب:\",page_title) AS page_title, CONCAT(il_to) AS il_to\n"
                + "FROM page\n"
                + "JOIN imagelinks\n"
                + "ON page_id = il_from\n"
                + "WHERE (NOT EXISTS(\n"
                + "SELECT 1\n"
                + "FROM image\n"
                + "WHERE img_name = il_to))\n"
                + "AND (NOT EXISTS(\n"
                + "SELECT\n"
                + "1\n"
                + "FROM commonswiki_p.page\n"
                + "WHERE page_title = il_to\n"
                + "AND page_namespace = 6))\n"
                + "AND page_namespace = 10;"));

        pages.addAll(getSqlRecords("SELECT CONCAT(\"وحدة:\",page_title) AS page_title, CONCAT(il_to) AS il_to\n"
                + "FROM page\n"
                + "JOIN imagelinks\n"
                + "ON page_id = il_from\n"
                + "WHERE (NOT EXISTS(\n"
                + "SELECT 1\n"
                + "FROM image\n"
                + "WHERE img_name = il_to))\n"
                + "AND (NOT EXISTS(\n"
                + "SELECT\n"
                + "1\n"
                + "FROM commonswiki_p.page\n"
                + "WHERE page_title = il_to\n"
                + "AND page_namespace = 6))\n"
                + "AND page_namespace = 828;"));
        
        for (Object tmp : pages) {
            String title = tmp.toString().split(",,,,,,,")[0];
            String file = tmp.toString().split(",,,,,,,")[1];

            String content = wiki.getPageText(title);
            if (!content.contains(file)) {
                file = file.replace("_", " ");
            }
            if (!content.toLowerCase().replace("_", " ").contains(file.toLowerCase().replace("_", " "))) {
                continue;
            }
            if (containsOnce(content, file) && isAllowed(content)) {
                content = getGallery(content, "\\<gallery[\\s\\S]{1,}\\<\\/gallery\\>", file);
                content = getSingle(content, file);
                content = getInfoBox(content, file);

                if (!content.toLowerCase().replace("_", " ").contains(file.toLowerCase().replace("_", " "))) {
                    Tead tead = new Tead(title, content, "روبوت:إزالة ملف غير موجود (" + file + ")");
                    tead.start();
                    while (tead.isAlive()) {
                        Thread.sleep(1000);
                    }
                }

            }

        }

    }

}
