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

import static asammourbot.portalToStub.getSqlRecords;
import static asammourbot.tagger.append;
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
import javax.security.auth.login.LoginException;
import org.jsoup.Jsoup;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class cat {

    public static List getPortals(String query) throws ClassNotFoundException, SQLException, FileNotFoundException, InstantiationException, IllegalAccessException, IOException {

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

    public static void insertSister(ArrayList pages, String summary, String className, String url, String project) throws IOException, LoginException, InterruptedException {
        for (Object tmp : pages) {
            tmp = ("تصنيف:" + tmp).toString();
            ArrayList newList = new ArrayList();

            String sister = getSister(tmp.toString(), className, url, project);
            if (!sister.equals("")) {
                newList.add(tmp.toString());
            }
            append(newList, sister, "إضافة [[ويكيبيديا:مشاريع شقيقة|" + summary + "]]", false);
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

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException, LoginException {
        Wiki wiki = new Wiki("ar.wikipedia.org");

        List port = getPortals("select p1.page_title, group_concat(distinct ll2.ll_title) as \"portals\"\n"
                + "from page p1\n"
                + "inner join langlinks ll1\n"
                + "on ll1.ll_from = p1.page_id\n"
                + "inner join frwiki_p.page p2\n"
                + "on p2.page_title = replace(replace(ll1.ll_title,\" \",\"_\"),\"Catégorie:\",\"\")\n"
                + "inner join frwiki_p.pagelinks pl1\n"
                + "on pl1.pl_from = p2.page_id\n"
                + "inner join frwiki_p.page p3\n"
                + "on p3.page_title = pl1.pl_title\n"
                + "inner join frwiki_p.langlinks ll2\n"
                + "on ll2.ll_from = p3.page_id\n"
                + "where p1.page_namespace = 14\n"
                + "and p1.page_is_redirect = 0\n"
                + "and p2.page_namespace = 14\n"
                + "and p2.page_is_redirect = 0\n"
                + "and ll1.ll_lang = \"fr\"\n"
                + "and ll2.ll_lang = \"ar\"\n"
                + "and p3.page_namespace = 100\n"
                + "and p3.page_is_redirect = 0\n"
                + "and replace(replace(ll2.ll_title,\" \",\"_\"),\"بوابة:\",\"\") not in (select pl_title from pagelinks where pl_title = replace(replace(ll2.ll_title,\" \",\"_\"),\"بوابة:\",\"\")\n"
                + "                                         and pl_from = p1.page_id)\n"
                + "group by p1.page_title limit 100;");

        for (Object tmp : port) {
            String portalsText = "";
            String title = "تصنيف:" + tmp.toString().split(",,,,,,,")[0];
            String portals = tmp.toString().split(",,,,,,,")[1];
            String content = wiki.getPageText(title);
            for (String tmp1 : portals.split(",")) {
                portalsText = portalsText + "|" + tmp1.replace("بوابة:", "");
            }
            if (content.contains("{{بوابة")) {
                content = content.replace("{{بوابة", "{{بوابة" + portalsText);

            } else if (content.contains("{{شريط بوابات")) {
                content = content.replace("{{شريط بوابات", "{{شريط بوابات" + portalsText);

            } else {
                content = "{{بوابة"+portalsText+"}}\n"+content;

            }

            Tead t = new Tead(title, content, "روبوت:إضافة بوابات {{بوابة" + portalsText + "}}");
            t.start();
            while (t.isAlive()) {
                Thread.sleep(1000);
            }

        }

        ArrayList wiktionary = getSqlRecords("select page_title from  \n"
                + "page\n"
                + "inner join page_props\n"
                + "on pp_page = page_id\n"
                + "where pp_propname like \"%wikibase_item%\"\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwiki%\"\n"
                + "                                                   )\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwiktionary%\"\n"
                + "                                                   ) \n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%Wiktionary%\")\n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%ويكاموس%\")\n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%شقيق%\")\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%تصنيفات_مخفية%\" and cl_from = page_id)\n"
                + "                                                   and page_namespace =14 \n"
                + "                                                   and page_is_redirect = 0\n"
                + "                                                   limit 10;");

        insertSister(wiktionary, "قالب:ويكاموس", "wb-otherproject-wiktionary", "https://ar.wiktionary.org/wiki/", "wiktionary");

        ArrayList pages = getSqlRecords("select p.page_title from page p\n"
                + "inner join page p1\n"
                + "on p1.page_title = p.page_title\n"
                + "where p.page_namespace = 14\n"
                + "and p.page_is_redirect = 0\n"
                + "and p1.page_namespace = 0\n"
                + "and p1.page_is_redirect = 0\n"
                + "and p.page_id not in (select pl_from from pagelinks where pl_from_namespace = 14 and pl_namespace = 0)\n"
                + "and p.page_id not in (select cl_from from categorylinks where cl_from = p.page_id and cl_to = \"تحويلات_تصنيفات_ويكيبيديا\") limit 10;");

        for (Object tmp : pages) {
            tmp = "تصنيف:" + tmp;
            String con = wiki.getPageText(tmp.toString());
            con = "{{مزيد|" + tmp.toString().replace("تصنيف:", "").replace("_", " ") + "}}\n" + con;
            Tead t = new Tead(tmp.toString(), con, "روبوت:إضافة [[قالب:مزيد]]");
            t.start();
            while (t.isAlive()) {
                Thread.sleep(1000);
            }
        }

        ArrayList wikibooks = getSqlRecords("select page_title from\n"
                + "page\n"
                + "inner join page_props\n"
                + "on pp_page = page_id\n"
                + "where pp_propname like \"%wikibase_item%\"\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwiki%\"\n"
                + "                                                   )\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwikibooks%\"\n"
                + "                                                   ) \n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%كتب%\")\n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%شقيق%\")\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%تصنيفات_مخفية%\" and cl_from = page_id)\n"
                + "                                                   and page_namespace =14 \n"
                + "                                                   and page_is_redirect = 0\n"
                + "                                                   limit 10;");

        insertSister(wikibooks, "قالب:ويكي الكتب", "wb-otherproject-wikibooks", "https://ar.wikibooks.org/wiki/", "wikibooks");

        ArrayList commons = getSqlRecords("select page_title from  \n"
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
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%commonswiki%\"\n"
                + "                                                   ) \n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%كومنز%\")\n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%شقيق%\")\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%تصنيفات_مخفية%\" and cl_from = page_id)\n"
                + "                                                   and page_namespace =14 \n"
                + "                                                   and page_is_redirect = 0\n"
                + "limit 10;");
        insertSister(commons, "قالب:كومنز", "wb-otherproject-commons", "https://commons.wikimedia.org/wiki/", "commons");

        ArrayList wikinews = getSqlRecords("select page_title from  \n"
                + "page\n"
                + "inner join page_props\n"
                + "on pp_page = page_id\n"
                + "where pp_propname like \"%wikibase_item%\"\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwiki%\"\n"
                + "                                                   )\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwikinews%\"\n"
                + "                                                   ) \n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%خبار%\")\n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%شقيق%\")\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%تصنيفات_مخفية%\" and cl_from = page_id)\n"
                + "                                                   and page_namespace =14 \n"
                + "                                                   and page_is_redirect = 0\n"
                + "                                                   limit 10;");

        insertSister(wikinews, "قالب:ويكي الأخبار", "wb-otherproject-wikinews", "https://ar.wikinews.org/wiki/", "wikinews");

        ArrayList wikiquote = getSqlRecords("select page_title from  \n"
                + "page\n"
                + "inner join page_props\n"
                + "on pp_page = page_id\n"
                + "where pp_propname like \"%wikibase_item%\"\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwiki%\"\n"
                + "                                                   )\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwikiquote%\"\n"
                + "                                                   ) \n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%اقتباس%\")\n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%شقيق%\")\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%تصنيفات_مخفية%\" and cl_from = page_id)\n"
                + "                                                   and page_namespace =14 \n"
                + "                                                   and page_is_redirect = 0\n"
                + "                                                   limit 10;");

        insertSister(wikiquote, "قالب:ويكي الاقتباس", "wb-otherproject-wikiquote", "https://ar.wikiquote.org/wiki/", "wikiquote");

        ArrayList wikisource = getSqlRecords("select page_title from  \n"
                + "page\n"
                + "inner join page_props\n"
                + "on pp_page = page_id\n"
                + "where pp_propname like \"%wikibase_item%\"\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwiki%\"\n"
                + "                                                   )\n"
                + "and TRIM(LEADING 'Q' FROM pp_value) in (select wikidatawiki_p.wb_items_per_site.ips_item_id\n"
                + "                                                   from wikidatawiki_p.wb_items_per_site\n"
                + "                                                   where wikidatawiki_p.wb_items_per_site.ips_item_id = TRIM(LEADING 'Q' FROM pp_value)\n"
                + "                                                   and wikidatawiki_p.wb_items_per_site.ips_site_id like \"%arwikisource%\"\n"
                + "                                                   ) \n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%مصدر%\")\n"
                + "                                                   and page_id not in (select tl_from from templatelinks where tl_from = page_id and tl_from_namespace = 14 and tl_title like \"%شقيق%\")\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                + "                                                   and page_id not in (select cl_from from categorylinks where cl_to like \"%تصنيفات_مخفية%\" and cl_from = page_id)\n"
                + "                                                   and page_namespace =14 \n"
                + "                                                   and page_is_redirect = 0\n"
                + "                                                   limit 10;");

        insertSister(wikisource, "قالب:ويكي مصدر", "wb-otherproject-wikisource", "https://ar.wikisource.org/wiki/", "wikisource");

    }
}
