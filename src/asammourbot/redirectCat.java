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

import static asammourbot.redirects.getKeyFromValue;
import static asammourbot.tagger.getDate;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author ASammour
 */
public class redirectCat {

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
            String namespace = rs.getString("page_namespace");

            records.add(firstName + ",,,,," + namespace);
        }
        return records;
    }

    public static void append(ArrayList pages, String template) throws InterruptedException, IOException {

        for (Object tmp : pages) {
            String title = tmp.toString().split(",,,,,")[0];
            String namespace = tmp.toString().split(",,,,,")[1];
            title = (getKeyFromValue(wiki.getNamespaces(), Integer.parseInt(namespace)).toString().trim() + ":" + title).replace("^:", "");

            String content = wiki.getPageText(title);

            if (!content.contains(template.replace("{{", "").replace("}}", ""))) {

                content = content + "\n\n" + template;

                Tead t = new Tead(title, content, "روبوت: تصنيف التحويلات " + template);
                t.start();
                Thread.sleep(2000);
            }

        }
    }

    public static Object getKeyFromValue(LinkedHashMap hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        ArrayList tashkeel = getSqlRecords("select page_title, page_namespace from page\n"
                + "where page_is_redirect = 1\n"
                + "and (page_title like \"%ّ%\" \n"
                + "     or page_title like \"%ُ%\"\n"
                + "     or page_title like \"%ً%\"\n"
                + "     or page_title like\"%َ%\" \n"
                + "     or page_title like \"%ٍ%\" \n"
                + "     or page_title like \"%ْ%\" \n"
                + "     or page_title like \"%ِ%\"\n"
                + "     or page_title like \"%ٍ%\")\n"
                + "and page_id not in (select cl_from from categorylinks where cl_from = page_id and cl_to = \"تحويلات_بعلامات_تشكيل\") limit 100;");
        
        append(tashkeel, "{{تحويلة بعلامات تشكيل}}");

        ArrayList hindi = getSqlRecords("select page_title, page_namespace from page\n"
                + "where page_is_redirect = 1\n"
                + "and (page_title like \"%١%\" \n"
                + "     or page_title like \"%٢%\" \n"
                + "     or page_title like \"%٣%\" \n"
                + "     or page_title like \"%٤%\" \n"
                + "     or page_title like \"%٥%\"  \n"
                + "     or page_title like \"%٦%\"  \n"
                + "     or page_title like \"%٧%\" \n"
                + "     or page_title like \"%٨%\"\n"
                + "	 or page_title like \"%٩%\"\n"
                + "     or page_title like \"%٠%\")\n"
                + "and page_id not in (select cl_from from categorylinks where cl_from = page_id and cl_to = \"تحويلات_الأرقام_الهندية_إلى_الأرقام_العربية\") limit 100;");

        append(hindi, "{{تحويلة من الأرقام الهندية}}");

        ArrayList spell = getSqlRecords("select page_title, page_namespace from page\n"
                + "where page_is_redirect = 1\n"
                + "and page_id not in (select cl_from from categorylinks where cl_from = page_id and cl_to = \"تحويلات_أخطاء_إملائية\")\n"
                + "and (page_title = (select replace(rd_title,\"أ\",\"ا\") from redirect where rd_from = page_id and rd_namespace = page_namespace)\n"
                + "    or page_title = (select replace(rd_title,\"إ\",\"ا\") from redirect where rd_from = page_id and rd_namespace = page_namespace)\n"
                + "    or page_title = (select replace(rd_title,\"آ\",\"ا\") from redirect where rd_from = page_id and rd_namespace = page_namespace)) limit 100;");

        append(spell, "{{تحويلة خطأ إملائي}}");

        ArrayList female = getSqlRecords("select page_title, page_namespace from page\n"
                + "where page_is_redirect = 1\n"
                + "and page_id not in (select cl_from from categorylinks where cl_from = page_id and cl_to = \"تحويلات_إلى_اسم_مؤنث\")\n"
                + "and (page_title = (select replace(rd_title,\"ية\",\"\") from redirect where rd_from = page_id and rd_namespace = page_namespace and rd_interwiki = \"\"))\n"
                + "limit 100;");

        append(female, "{{تحويلة إلى اسم مؤنث}}");

        ArrayList al = getSqlRecords("select page_title, page_namespace from page\n"
                + "where page_is_redirect = 1\n"
                + "and page_id not in (select cl_from from categorylinks where cl_from = page_id and cl_to = \"تحويلات_إلى_ال_التعريف\")\n"
                + "and (page_title = (select replace(rd_title,\"ال\",\"\") from redirect where rd_from = page_id and rd_namespace = page_namespace and rd_interwiki = \"\")) limit 100;");

        append(al, "{{تحويلة تعريف}}");

        ArrayList notal = getSqlRecords("select page_title, page_namespace from page\n"
                + "where page_is_redirect = 1\n"
                + "and page_id not in (select cl_from from categorylinks where cl_from = page_id and cl_to = \"تحويلات_ال_التعريف\")\n"
                + "and (page_title = (select concat(\"ال\",rd_title) from redirect where rd_from = page_id and rd_namespace = page_namespace and rd_interwiki = \"\"))\n"
                + "limit 100;");

        append(notal, "{{تحويلة من ال التعريف}}");

    }

}
