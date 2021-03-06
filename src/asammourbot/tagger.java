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

/**
 *
 * @author ASammour
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import static asammourbot.tagger.getRegexRecords;
import static asammourbot.tagger.prepend;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.jsoup.Jsoup;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

/**
 *
 * @author ASammour
 */
public class tagger {

    static Wiki wiki = new Wiki("ar.wikipedia.org");

    static String mainsummary = "روبوت: ";

    static Set pages = new HashSet();

    public static String getDate() {
        SimpleDateFormat ar = new SimpleDateFormat("MMMM", new Locale("ar"));
        Date date = new Date();
        return ar.format(date) + " " + Calendar.getInstance().get(Calendar.YEAR);
    }

    public static String getDate2(Date d) {
        SimpleDateFormat ar = new SimpleDateFormat("MMMM", new Locale("ar"));
        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        Date current = c.getTime();
        return ar.format(current) + " " + year.format(current);
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

    public static ArrayList getRegexRecords(String regex) throws IOException {
        ArrayList records = new ArrayList();
        String[][] search = wiki.search(regex, 0);

        for (String[] search1 : search) {
            records.add(search1[0]);
        }

        return records;
    }

    public static void prepend(ArrayList pages, String template, String summary, boolean createionDate) throws InterruptedException, IOException {
        for (Object tmp : pages) {
            String content = wiki.getPageText(tmp.toString());
            if (!content.contains(template.replace("{{", "").replace("}}", ""))) {
                if (createionDate) {
                    content = template + getDate2(wiki.getFirstRevision(tmp.toString()).getTimestamp().getTime()) + "}}\n" + content;
                } else {
                    content = template + getDate() + "}}\n" + content;
                }

                Tead t = new Tead(tmp.toString(), content, mainsummary + summary);
                t.start();
                Thread.sleep(2000);
            }

        }
    }

    public static void append(ArrayList pages, String template, String summary, boolean hasDate) throws InterruptedException, IOException {
        if (hasDate) {
            template = template + "|تاريخ=" + getDate() + "}}";
        } else {
            template = template + "}}";
        }

        for (Object tmp : pages) {
            String content = wiki.getPageText(tmp.toString());

            if (!content.contains(template.replace("{{", "").replace("}}", "")) && !template.endsWith("|}}")) {
                if (content.contains("[[تصنيف:")) {
                    content = content.replaceFirst("\\[\\[تصنيف\\:", template + "\n\n[[تصنيف:");
                } else {
                    content = content + "\n\n" + template;
                }

                Tead t = new Tead(tmp.toString(), content, mainsummary + summary);
                t.start();
                Thread.sleep(2000);
            }

        }
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
                Tead t = new Tead(tmp.toString(), content, mainsummary + summary);
                t.start();
                Thread.sleep(2000);
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

    public static ArrayList checkNewDeath(ArrayList pages) throws IOException, InterruptedException {
        ArrayList newList = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -10);

        for (Object tmp : pages) {
            Revision rev = wiki.getTopRevision(tmp.toString());
            Wiki.Revision ourRevision = wiki.getTopRevision(tmp.toString());

            try {
                while (rev.getText().contains("{{وفاة حديثة}}") && !rev.isNew()) {
                    ourRevision = rev;
                    rev = rev.getPrevious();
                }
            } catch (Exception e) {

            }
            if (ourRevision.getTimestamp().before(calendar)) {
                ArrayList a = new ArrayList();
                a.add(tmp.toString());
                remove(a, "\\{\\{وفاة حديثة\\}\\}\n", "إزالة [[قالب:وفاة حديثة]] بعد مرور 10 أيام");
                remove(a, "\\[\\[تصنيف:وفيات حديثة\\]\\]\n", "إزالة [[تصنيف:وفيات حديثة]] بعد مرور 10 أيام");
                a.clear();
            }
        }
        return newList;
    }

    public static ArrayList checkEditing(ArrayList pages) throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -2);
        System.out.println(calendar.getTime());
        ArrayList newList = new ArrayList();
        for (Object tmp : pages) {
            Revision rev = wiki.getTopRevision(tmp.toString());
            while (rev.isBot()) {
                rev = rev.getPrevious();
            }
            if (rev.getTimestamp().before(calendar)) {
                //newList.add (tmp.toString());
            }

        }
        return newList;
    }

    public static ArrayList checkDevelopment(ArrayList pages) throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -60);
        System.out.println(calendar.getTime());
        ArrayList newList = new ArrayList();
        for (Object tmp : pages) {
            Revision rev = wiki.getTopRevision(tmp.toString());
            while (rev.isBot()) {
                rev = rev.getPrevious();
            }
            if (rev.getTimestamp().before(calendar)) {
                newList.add(tmp.toString());
            }

        }
        return newList;
    }

    public static ArrayList checkDis(ArrayList pages) throws IOException, InterruptedException {
        ArrayList newList = new ArrayList();
        for (Object tmp : pages) {
            String content = wiki.getPageText(tmp.toString());
            content = content.replaceAll("\\{\\{بذرة.{1,40}\\}\\}\n", "");
            content = content.replaceAll("\\{\\{مقالة غير مراجعة.{1,40}\\}\\}\n", "");
            content = content.replaceAll("\\{\\{مصدر.{1,40}\\}\\}\n", "");
            content = content.replaceAll("\\{\\{نهاية مسدودة.{1,40}\\}\\}\n", "");
            content = content.replaceAll("\\{\\{شريط بوابات.{1,80}\\}\\}\n", "");
            content = content.replaceAll("\\{\\{وصلات قليلة.{1,40}\\}\\}\n", "");
            content = content.replaceAll("\\{\\{يتيمة.{1,40}\\}\\}\n", "");
            Tead t = new Tead(tmp.toString(), content, mainsummary + " إزالة قوالب صيانة من صفحة توضيح");
            t.start();
            Thread.sleep(2000);
        }
        return newList;
    }

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, FailedLoginException, LoginException, InterruptedException {

        ArrayList development = checkDevelopment(getRegexRecords("hastemplate:\"تطوير مقالة\""));
        remove(development, "\\{\\{تطوير مقالة\\}\\}", "إزالة [[قالب:تطوير مقالة]] بعد مرور أكثر من شهرين على آخر تعديل");
        remove(development, "\\{\\{تحت الإنشاء\\}\\}", "إزالة [[قالب:تطوير مقالة]] بعد مرور أكثر من شهرين على آخر تعديل");
        remove(development, "\\{\\{المقالة قيد الإنشاء\\}\\}", "إزالة [[قالب:تطوير مقالة]]  بعد مرور أكثر من شهرين على آخر تعديل");
        remove(development, "\\{\\{قيد التطوير\\}\\}", "إزالة [[قالب:تطوير مقالة]]  بعد مرور أكثر من شهرين على آخر تعديل");
        remove(development, "\\{\\{تطوير مقال\\}\\}", "إزالة [[قالب:تطوير مقالة]]  بعد مرور أكثر من شهرين على آخر تعديل");

        ArrayList addSource = getRegexRecords("-insource:/\\<ref/ -incategory:\"صفحات بها مراجع ويكي بيانات\" -hastemplate:\"مصدر\" -hastemplate:\"مصدر وحيد\" -hastemplate:\"مصادر أكثر\" -incategory:\"مرجع من ويكي بيانات\" -hastemplate:\"سيرة شخصية غير موثقة\" -hastemplate:\"جرايز\" -incategory:\"بوابة تقويم/مقالات متعلقة\" -hastemplate:\"Sfn\" -incategory:\"صفحات توضيح\"");
        prepend(addSource, "{{مصدر|تاريخ=", "إضافة [[قالب:مصدر]]", false);

        ArrayList removeSource = getRegexRecords("insource:/\\<ref\\>.{5,}\\<\\/ref\\>/ hastemplate:\"مصدر\"");
        removeSource.addAll(getRegexRecords("incategory:\"صفحات بها مراجع ويكي بيانات\" hastemplate:\"مصدر\""));
        removeSource.addAll(getRegexRecords("incategory:\"مرجع من ويكي بيانات\" hastemplate:\"مصدر\""));

        remove(removeSource, "\\{\\{مصدر\\|.{1,}\\}\\}\n", "إزالة [[قالب:مصدر]]");

        ArrayList deadButAlive = getRegexRecords("incategory:\"أشخاص على قيد الحياة\" incategory:\"وفيات 2019\"");
        remove(deadButAlive, "\\[\\[تصنيف:أشخاص على قيد الحياة\\]\\]\n", "إزالة [[تصنيف:أشخاص على قيد الحياة]] من شخصيات متوفاة");

        ArrayList editing = checkEditing(getRegexRecords("hastemplate:\"تحرر\""));
        remove(editing, "\\{\\{تحرر\\}\\}", "إزالة [[قالب:تحرر]] بعد مرور 7 أيام على آخر تعديل");
        remove(editing, "\\{\\{يحرر\\}\\}", "إزالة [[قالب:تحرر]] بعد مرور 7 أيام على آخر تعديل");
        remove(editing, "\\{\\{تحرير كثيف\\}\\}", "إزالة [[قالب:تحرر]] بعد مرور 7 أيام على آخر تعديل");

        ArrayList disTemplate = getSqlRecords("select page_title\n"
                + "from page\n"
                + "where page_namespace = 0\n"
                + "and page_is_redirect = 0\n"
                + "and page_title like \"%(توضيح)\"\n"
                + "and page_id not in (select cl_from from categorylinks where cl_from = page_id and cl_to like \"صفحات_توضيح\");");

        append(disTemplate, "{{توضيح", " إضافة [[قالب:توضيح]] لصفحة توضيح", false);

        ArrayList newDeath = new ArrayList();
        newDeath.addAll(Arrays.asList(wiki.getCategoryMembers("تصنيف:وفيات حديثة", false, 0)));
        checkNewDeath(newDeath);

        /**
         * استعلام لجلب المالات غير المراجعة والتي بحاجة لوسم مقالة غير مراجعة
         */
        ArrayList addApproved = getSqlRecords("select page_title from page\n"
                + "where page.page_is_redirect = 0\n"
                + "and page.page_namespace = 0\n"
                + "and page_id not in (select fp_page_id from flaggedpages where fp_page_id = page_id)\n"
                + "and page_id not in (select cl_from from categorylinks where cl_to like \"جميع_المقالات_غير_المراجعة\");");

        /**
         * إضافة الوسم إلى قائمة المقالات السابقة يتم تأريخ القالب بتاريخ إنشاء
         * المقالة وليس بالتاريخ الحالي
         */
        prepend(addApproved, "{{مقالة غير مراجعة|تاريخ=", "إضافة [[قالب:مقالة غير مراجعة]]", true);
        /**
         * قائمة المقالات المراجعة التي بحاجة لإزالة وسم مقالة غير مراجعة منها
         *
         */
        ArrayList removeApproved = getSqlRecords("select page_title from page\n"
                + "where page.page_is_redirect = 0\n"
                + "and page.page_namespace = 0\n"
                + "and page_id in (select fp_page_id from flaggedpages where fp_page_id = page_id)\n"
                + "and page_id in (select cl_from from categorylinks where cl_to like \"جميع_المقالات_غير_المراجعة\");");
        /**
         * إزالة وسم مقالة غير مراجعة من المقالات السابقة
         *
         */
        remove(removeApproved, "\\{\\{مقالة غير مراجعة.{2,25}\\}\\}\n", "إزالة [[قالب:مقالة غير مراجعة]]");

        /**
         * إضافة قالب مقالات بحاجة لشريط بوابات للمقالات التي بحاجة إلى شريط
         * بوابات مستثنى منها المقالات التي تحتوي قالب لا لشريط البوابات
         */
        ArrayList addPortal = getRegexRecords("-hastemplate:\"شريط بوابات\" -hastemplate:\"مقالات بحاجة لشريط بوابات\" -incategory:\"صفحات توضيح|مقالات بحاجة لشريط بوابات\" -intitle:\"(توضيح)\" -hastemplate:\"لا لشريط البوابات\"");

        /**
         * إضافة القالب إلى الصفجات السابقة في أسفل الصفحة
         */
        append(addPortal, "{{مقالات بحاجة لشريط بوابات", "إضافة [[قالب:مقالات بحاجة لشريط بوابات]]", false);

        /**
         * جلب المقالات التي تحتوي شريط بوابات ولكنها تحتوي قالب مقالات بحاجة
         * لشريط بوابات
         *
         */
        ArrayList remoevPortals = getRegexRecords("hastemplate:\"شريط بوابات\" incategory:\"مقالات بحاجة لشريط بوابات\" -incategory:\"صفحات توضيح\" -intitle:\"(توضيح)\"");
        /**
         * إزالة قالب مقالات بحاجة لشريط بوابات من المقالات السابقة
         */
        remove(remoevPortals, "\\[\\[تصنيف:مقالات بحاجة لشريط بوابات\\]\\]\n", "إزالة [[تصنيف:مقالات بحاجة لشريط بوابات]]");
        /**
         * جلب المقالات التي تحتوي شريط بوابات ولكنها تحتوي تصنيف مقالات بحاجة
         * لشريط بوابات
         */
        ArrayList remoevPortals1 = getRegexRecords("hastemplate:\"شريط بوابات\" hastemplate:\"مقالات بحاجة لشريط بوابات\" -incategory:\"صفحات توضيح\" -intitle:\"(توضيح)\"");
        /**
         * إزالة التصنيف من المقالات السابقة
         */
        remove(remoevPortals1, "\\{\\{مقالات بحاجة لشريط بوابات\\}\\}\n", "إزالة [[قالب:مقالات بحاجة لشريط بوابات]]");

        ArrayList addNoCats = getSqlRecords("select p2.page_title from page p2 \n"
                + "where p2.page_id not in (select cl_from \n"
                + "                      from categorylinks\n"
                + "                      inner join page p\n"
                + "                      on p.page_title = cl_to\n"
                + "                      and p.page_namespace = 14\n"
                + "                      where ((p.page_id not in (select cl_from from categorylinks  where cl_to = 'تصنيفات_مخفية')) or cl_to like \"%أشخاص_على_قيد_الحياة%\")\n"
                + "                      )\n"
                + "and p2.page_namespace = 0\n"
                + "and p2.page_is_redirect = 0\n"
                + "and p2.page_id not in (select cl_from from categorylinks where cl_to like \"%جميع_المقالات_غير_المصنفة%\" and cl_from = p2.page_id)\n"
                + "and p2.page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = p2.page_id);");
        append(addNoCats, "{{غير مصنفة", "إضافة [[قالب:غير مصنفة]]", true);

        ArrayList removeNoCats = getSqlRecords("select p2.page_title from page p2 \n"
                + "where p2.page_id in (select cl_from \n"
                + "                      from categorylinks\n"
                + "                      inner join page p\n"
                + "                      on p.page_title = cl_to\n"
                + "                      and p.page_namespace = 14\n"
                + "                      where ((p.page_id not in (select cl_from from categorylinks  where cl_to = 'تصنيفات_مخفية')) or cl_to like \"%أشخاص_على_قيد_الحياة%\")\n"
                + "                      )\n"
                + "and p2.page_namespace = 0\n"
                + "and p2.page_is_redirect = 0\n"
                + "and p2.page_id in (select cl_from from categorylinks where cl_to like \"%جميع_المقالات_غير_المصنفة%\" and cl_from = p2.page_id)\n"
                + "and p2.page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = p2.page_id);");

        remove(removeNoCats, "\\{\\{غير مصنفة.{2,25}\\}\\}\n", "إزالة [[قالب:غير مصنفة]]");
        remove(removeNoCats, "\\{\\{بذرة غير مصنفة.{2,25}\\}\\}\n", "إزالة [[قالب:بذرة غير مصنفة]]");

        ArrayList addStub = getSqlRecords("select page_title \n"
                + "from page\n"
                + "where page_is_redirect = 0\n"
                + "and page_namespace = 0\n"
                + "and page_id not in (select cl_from from categorylinks where cl_to = \"جميع_مقالات_البذور\" and cl_from = page_id)\n"
                + "and page_id not in (select cl_from from categorylinks where cl_to = \"صفحات_توضيح\" and cl_from = page_id)\n"
                + "and page_id not in (select cl_from from categorylinks where cl_to = \"صفحات_للحذف_السريع\" and cl_from = page_id)\n"
                + "and page_len < 1500;");
        append(addStub, "{{بذرة", "إضافة [[قالب:بذرة]]", false);


    }
}
