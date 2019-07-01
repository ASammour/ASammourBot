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

import static asammourbot.tagger.getSqlRecords;
import static asammourbot.tagger.prepend;
import static asammourbot.tagger.remove;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;

/**
 *
 * @author ASammour
 */
public class orphan {

    static Thread x;

    public static void run() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException, InterruptedException {

        try {
            ArrayList addOrphan = getSqlRecords("SELECT page_title,(select count(distinct pl_from) \n"
                    + "                from pagelinks \n"
                    + "                where pl_from_namespace = 0 \n"
                    + "                and pl_title in (\n"
                    + "                       select page_title from redirect inner join page on rd_from = page_id where page_namespace = 0 and rd_title = p.page_title\n"
                    + "                and rd_namespace = 0)\n"
                    + "                and pl_namespace = 0\n"
                    + "                and pl_from in (select page_id\n"
                    + "                                from page\n"
                    + "                                where page_id = pl_from\n"
                    + "                                and page_namespace = 0\n"
                    + "                                and page_is_redirect = 0)\n"
                    + "                and pl_from not in (select (pl_from)\n"
                    + "                from pagelinks \n"
                    + "                where pl_from_namespace = 0 \n"
                    + "                and pl_title = page_title\n"
                    + "                and pl_namespace = 0\n"
                    + "                and pl_from in (select page_id\n"
                    + "                                from page\n"
                    + "                                where page_id = pl_from\n"
                    + "                                and page_namespace = 0\n"
                    + "                                and page_is_redirect = 0))\n"
                    + "               and pl_from <> page_id   \n"
                    + "               )\n"
                    + "				+\n"
                    + "				(select count(distinct pl_from)\n"
                    + "                from pagelinks \n"
                    + "                where pl_from_namespace = 0 \n"
                    + "                and pl_title = page_title\n"
                    + "                and pl_namespace = 0\n"
                    + "                and pl_from in (select page_id\n"
                    + "                                from page\n"
                    + "                                where page_id = pl_from\n"
                    + "                                and page_namespace = 0\n"
                    + "                                and page_is_redirect = 0)\n"
                    + "                 and pl_from <> page_id \n"
                    + "               )\n"
                    + "               as counts\n"
                    + "FROM page p\n"
                    + "where page_namespace = 0\n"
                    + "and page_is_redirect = 0\n"
                    + "and page_id  not in (select cl_from from categorylinks where cl_to like \"%جميع_المقالات_اليتيمة%\" and cl_from = page_id)\n"
                    + "and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                    + "having counts < 3;");

            prepend(addOrphan, "{{يتيمة|تاريخ=", "إضافة [[قالب:يتيمة]]", false);
        } catch (InterruptedException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            ArrayList removeOrphan = getSqlRecords("SELECT page_title,(select count(distinct pl_from) \n"
                    + "                from pagelinks \n"
                    + "                where pl_from_namespace = 0 \n"
                    + "                and pl_title in (\n"
                    + "                       select page_title from redirect inner join page on rd_from = page_id where page_namespace = 0 and rd_title = p.page_title\n"
                    + "                and rd_namespace = 0)\n"
                    + "                and pl_namespace = 0\n"
                    + "                and pl_from in (select page_id\n"
                    + "                                from page\n"
                    + "                                where page_id = pl_from\n"
                    + "                                and page_namespace = 0\n"
                    + "                                and page_is_redirect = 0)\n"
                    + "                and pl_from not in (select (pl_from)\n"
                    + "                from pagelinks \n"
                    + "                where pl_from_namespace = 0 \n"
                    + "                and pl_title = page_title\n"
                    + "                and pl_namespace = 0\n"
                    + "                and pl_from in (select page_id\n"
                    + "                                from page\n"
                    + "                                where page_id = pl_from\n"
                    + "                                and page_namespace = 0\n"
                    + "                                and page_is_redirect = 0))\n"
                    + "               and pl_from <> page_id   \n"
                    + "               )\n"
                    + "				+\n"
                    + "				(select count(distinct pl_from)\n"
                    + "                from pagelinks \n"
                    + "                where pl_from_namespace = 0 \n"
                    + "                and pl_title = page_title\n"
                    + "                and pl_namespace = 0\n"
                    + "                and pl_from in (select page_id\n"
                    + "                                from page\n"
                    + "                                where page_id = pl_from\n"
                    + "                                and page_namespace = 0\n"
                    + "                                and page_is_redirect = 0)\n"
                    + "                 and pl_from <> page_id \n"
                    + "               )\n"
                    + "               as counts\n"
                    + "FROM page p\n"
                    + "where page_namespace = 0\n"
                    + "and page_is_redirect = 0\n"
                    + "and page_id  in (select cl_from from categorylinks where cl_to like \"%جميع_المقالات_اليتيمة%\" and cl_from = page_id)\n"
                    + "and page_id not in (select cl_from from categorylinks where cl_to like \"%صفحات_توضيح%\" and cl_from = page_id)\n"
                    + "having counts >= 3;");

            remove(removeOrphan, "\\{\\{يتيمة\\|.{2,20}\\}\\}\n", "إزالة [[قالب:يتيمة]]");
        } catch (InterruptedException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(orphan.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
