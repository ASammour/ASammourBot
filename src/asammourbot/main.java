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

        if (param.equals("tagger")) {
            fixPortals.run();
            addTitleURL.run();
            tagger.run();
            moveToData.run();
            redirects.run();
        }

        if (param.equals("red")) {
            removeRedFiles.run();
            redCats.run();
            redPortals.run();
        }

        if (param.equals("cat")) {
            oppositeCats.run();
            addCats.run();
            noCats.run();
            cat.run();
        }

        if (param.equals("specify")) {
            specify.run();
            portalToStub.run();
            stubToPortal.run();
        }

        if (param.equals("arab")) {
            arabization.run();
        }

        if (param.equals("portals")) {
            frPortals.run();
            enPortals.run();
        }

    }
}
