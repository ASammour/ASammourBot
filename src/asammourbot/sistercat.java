package asammourbot;


import asammourbot.Tead;
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
import java.util.Properties;

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

/**
 *
 * @author ASammour
 */
public class sistercat {
    
    
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

 
    
    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
                ArrayList wiktionary = getSqlRecords("select concat(\"تصنيف:\",page_title) as page_title from  \n"
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
                + "                                                    limit 100;");

        append(wiktionary, "{{روابط شقيقة", "إضافة [[ويكيبيديا:مشاريع شقيقة|روابط شقيقة]]", false);

        

        ArrayList wikibooks = getSqlRecords("select concat(\"تصنيف:\",page_title) as page_title from\n"
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
                + "                                                    limit 100;");

        append(wikibooks, "{{روابط شقيقة", "إضافة [[ويكيبيديا:مشاريع شقيقة|روابط شقيقة]]", false);

        ArrayList commons = getSqlRecords("select concat(\"تصنيف:\",page_title) as page_title from  \n"
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
                + " limit 100;");
        append(commons, "{{روابط شقيقة", "إضافة [[ويكيبيديا:مشاريع شقيقة|روابط شقيقة]]", false);

        ArrayList wikinews = getSqlRecords("select concat(\"تصنيف:\",page_title) as page_title from  \n"
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
                + "                                                    limit 100;");

        append(wikinews, "{{روابط شقيقة", "إضافة [[ويكيبيديا:مشاريع شقيقة|روابط شقيقة]]", false);

        ArrayList wikiquote = getSqlRecords("select concat(\"تصنيف:\",page_title) as page_title from  \n"
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
                + "                                                    limit 100;");

        append(wikiquote, "{{روابط شقيقة", "إضافة [[ويكيبيديا:مشاريع شقيقة|روابط شقيقة]]", false);

        ArrayList wikisource = getSqlRecords("select concat(\"تصنيف:\",page_title) as page_title from  \n"
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
                + "                                                    limit 100;");

        append(wikisource, "{{روابط شقيقة", "إضافة [[ويكيبيديا:مشاريع شقيقة|روابط شقيقة]]", false);

    }
}
