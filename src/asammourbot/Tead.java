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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class Tead extends Thread {

    Wiki wiki = new Wiki("ar.wikipedia.org");

    @Override
    public void run() {
        ArrayList cred = new ArrayList();

        try {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader("cred.data"));
                try {
                    String x;
                    while ((x = br.readLine()) != null) {
                        // Printing out each line in the file
                        cred.add(x.replaceAll("[^\\p{Graph}\n\r\t ]", ""));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                System.out.println(e);
                e.printStackTrace();
            }

            /**
             * القيام بالتعديل إذا كان تاريخ إنشاء المقالة قبل 12 ساعة من تاريخ
             * التعديل
             */
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, -5);
            Wiki.Revision firstRev = wiki.getFirstRevision(page);

            if (firstRev.getTimestamp().before(calendar) 
                    && !content.contains("</pages>")
                    && wiki.getPageInfo(page).get("exists").equals(true)
                    && wiki.getPageInfo("مستخدم:"+cred.get(0).toString()+"/إيقاف").get("exists").equals(true)
                    && wiki.getPageText("مستخدم:"+cred.get(0).toString()+"/إيقاف").equals("نعم")) {
                
                wiki.login(cred.get(0).toString(), cred.get(1).toString());
                wiki.edit(page, content.replaceAll("(\r?\n){3,}", "\n\n"), summary.replace("روبوت:", "روبوت ("+new version ().getCurrentVersion()+"): "), true, true, -2, null);
            }
            else{
                System.out.println("تم إيقاف عمل البوت، أو أن الصفحة محمية، أو أن خطأ ما قد حدث****");
            }
        } catch (IOException | LoginException ex) {
            Logger.getLogger(Tead.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    String page;
    String summary;
    String content;

    public Tead(String page, String content, String summary) {
        this.page = page;
        this.summary = summary;
        this.content = content;
    }

}
