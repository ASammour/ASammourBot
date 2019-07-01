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

import java.io.IOException;
import java.sql.SQLException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.wikipedia.Wiki;

/**
 *
 * @author ASammour
 */
public class main {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, SQLException, IllegalAccessException, IOException, LoginException, FailedLoginException, InterruptedException {
        String param = args[0];

        if (param.equals("quarry")) {
            quarry.run();
        }

        if (param.equals("orphan")) {
            orphan.run();
        }
        
        if (param.equals("nocats")) {
            noCats.run();
        }
        
        if (param.equals("addcats")) {
            addCats.run();
        }
        
        if (param.equals("tagger")) {
            //addTitleURL.run();
            moveToData.run();
            redirects.run();
            fixPortals.run();
            tagger.run();
            
        }

        if (param.equals("redcat")) {
            redCats.run();
        }
        
        
        if (param.equals("redportal")) {
            redPortals.run();
        }

        if (param.equals("redfile")) {
            removeRedFiles.run();
        }

        if (param.equals("catarticle")) {
            catarticle.run();
        }
        
        if (param.equals("catcat")) {
            catcat.run();
            //addCats.run();
            //noCats.run();
            //cat.run();
        }
        
        if (param.equals("cat")) {
            cat.run();
        }
        
        if (param.equals("sisterarticle")) {
            sisterarticle.run();
        }
        
        if (param.equals("sistercat")) {
            sistercat.run();
        }

        if (param.equals("specify")) {
            specify.run();
        }

        if (param.equals("stubtoportal")) {
            stubToPortal.run();
        }

        if (param.equals("portaltostub")) {
            portalToStub.run();
        }

        if (param.equals("arabtemplate")) {
            arabtemplate.run();
        }

        if (param.equals("arabarticle")) {
            arabarticle.run();
        }
        
        if (param.equals("frportals")) {
            frPortals.run();
        }
        
        if (param.equals("enportals")) {
            enPortals.run();
        }

        if (param.equals("url")) {
            addTitleURL.run();
        }
        
        if (param.equals("redirectcat")) {
            redirectCat.run();
        }
        
        if (param.equals("nocats")) {
            noCats.run();
        }
        
        if (param.equals("addcats")) {
            addCats.run();
        }
    }
}
