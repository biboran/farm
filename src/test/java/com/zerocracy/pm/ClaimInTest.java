/**
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.pm;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Test case for {@link ClaimIn}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ClaimInTest {

    @Test
    public void readsParts() throws Exception {
        final ClaimIn claim = new ClaimIn(
            new XMLDocument(
                "<claim id='1'><author>yegor256</author></claim>"
            ).nodes("/claim").get(0)
        );
        MatcherAssert.assertThat(
            claim.author(),
            Matchers.equalTo("yegor256")
        );
    }

    @Test
    public void buildsClaimOut() throws Exception {
        final ClaimIn claim = new ClaimIn(
            new XMLDocument(
                "<claim id='1'><author>jeff</author><token>X</token></claim>"
            ).nodes("/claim ").get(0)
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Xembler(
                    new Directives().add("claims").append(
                        claim.reply("hello")
                    )
                ).xmlQuietly()
            ),
            XhtmlMatchers.hasXPaths(
                "/claims/claim[type='Notify']",
                "/claims/claim[token='X']",
                "/claims/claim/params/param[@name='cause']",
                "/claims/claim/params/param[@name='message' and .='hello']"
            )
        );
    }

}
