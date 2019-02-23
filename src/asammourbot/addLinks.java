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

import static asammourbot.tagger.wiki;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author ASammour
 */
public class addLinks {
    
    
     public static ArrayList getRegexRecords(String regex) throws IOException {
        ArrayList records = new ArrayList();
        String[][] search = wiki.search(regex, 0);

        for (int i = 0; i < search.length; i++) {
            records.add(search[i][0]);
        }

        return records;
    }
     
     public static void main(String[] args) throws IOException {
        
         String[] titles = wiki.getCategoryMembers("تصنيف:جميع مقالات النهاية المسدودة", false, 0);
         for (String tmp: titles){
             String content = wiki.getPageText(tmp);
             ArrayList related = getRegexRecords("morelike:"+tmp);
             for (Object tmp1:related){
                 if (content.contains(tmp1.toString())){
                     System.out.println(tmp1);
                 }
             }
             
         }
    }
}
