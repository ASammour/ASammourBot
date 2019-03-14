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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.LoginException;
import org.jsoup.Jsoup;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class cat {

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


    public static void remove(ArrayList pages, String template, String summary) throws InterruptedException, IOException {
        for (Object tmp : pages) {
            String content = wiki.getPageText(tmp.toString());
            int before = content.length();
            String urlRegex = template;
            Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
            Matcher urlMatcher = pattern.matcher(content);

            while (urlMatcher.find()) {
                String url = urlMatcher.group();
                content = content.replace(url, "");
            }

            int after = content.length();

            if ((before - after) < 50 && after != before) {
            }
        }
    }

    public static void insertSister(ArrayList pages, String summary, String className, String url, String project) throws IOException, LoginException, InterruptedException {
        for (Object tmp : pages) {
            ArrayList newList = new ArrayList();

            String sister = getSister(tmp.toString(), className, url, project);
            if (!sister.equals("")) {
                newList.add(tmp.toString());
            }

        }
    }

    public static String getSister(String title, String className, String url, String project) throws IOException {
        String finalText = "";
        org.jsoup.nodes.Document document = Jsoup.connect("https://ar.wikipedia.org/wiki/" + title).get();
        String link = document.select("." + className + " > a").attr("href");
        link = link.replace(url, "");
        link = java.net.URLDecoder.decode(link, "UTF-8");

        if (project.equals("commons")) {
            if (link.contains("Category:")) {
                finalText = "{{تصنيف كومنز|" + link.replace("Category:", "");
            } else {
                finalText = "{{كومنز|" + link;
            }
        } else if (project.equals("wikinews")) {
            finalText = "{{ويكي الأخبار|" + link;
        } else if (project.equals("wikiquote")) {
            finalText = "{{ويكي الاقتباس|" + link;
        } else if (project.equals("wiktionary")) {
            finalText = "{{ويكاموس|" + link;
        } else if (project.equals("wikisource")) {
            finalText = "{{ويكي مصدر|" + link;
        } else if (project.equals("wikibooks")) {
            finalText = "{{ويكي الكتب|" + link;
        }
        return finalText.replace("_", " ");
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        ArrayList pages = getSqlRecords("select (page_title) from  \n"
                + "page\n"
                + "inner join page_props\n"
                + "on pp_page = page_id\n"
                + "where page_id not in (select arwiki_p.categorylinks.cl_from from arwiki_p.categorylinks where arwiki_p.categorylinks.cl_from = arwiki_p.page.page_id and arwiki_p.categorylinks.cl_to like \"%كومنز%\")\n"
                + "and pp_propname like \"%wikibase_item%\"\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwiki%\"\n"
                + "                                                   )\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and (wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwikinews%\"\n"
                + "                                                   ) \n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%أخبار%\")\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                + "                                                   and page_namespace =14\n"
                + "                                                   and page_is_redirect = 0;");
        
        

    }
}
