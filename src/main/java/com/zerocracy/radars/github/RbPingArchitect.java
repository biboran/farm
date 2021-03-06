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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.SoftException;
import com.zerocracy.pm.staff.Roles;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import javax.json.JsonObject;

/**
 * Ping architect on new issues.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
public final class RbPingArchitect implements Rebound {

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Issue.Smart issue = new Issue.Smart(
            new IssueOfEvent(github, event)
        );
        final Roles roles = new Roles(new GhProject(farm, issue.repo()));
        final String author = new Issue.Smart(issue).author()
            .login().toLowerCase(Locale.ENGLISH);
        String answer;
        try {
            roles.bootstrap();
            final Collection<String> arcs = roles.findByRole("ARC");
            if (arcs.isEmpty()) {
                answer = "No architects here";
            } else if (arcs.contains(author)) {
                answer = "The architect is speaking";
            } else {
                final String intro = String.join(", @", arcs);
                if (issue.isPull()) {
                    new ThrottledComments(issue.comments()).post(
                        new Par(
                            "@%s please, pay attention to this pull request"
                        ).say(intro)
                    );
                } else {
                    new ThrottledComments(issue.comments()).post(
                        new Par(
                            "@%s please, pay attention to this issue"
                        ).say(intro)
                    );
                }
                answer = String.format("Architects notified: %s", arcs);
            }
        } catch (final SoftException ex) {
            if ("yegor256".equals(author)) {
                answer = "It's a ticket from @yegor256";
            } else {
                new ThrottledComments(issue.comments()).post(
                    new Par(
                        "@%s I'm not managing this repo, remove the",
                        "[webhook](https://github.com/%s/settings/hooks)",
                        "or contact me in [Slack](http://www.zerocracy.com)",
                        "//cc @yegor256"
                    ).say(author, issue.repo().coordinates())
                );
                answer = "This repo is not managed";
            }
        }
        return answer;
    }
}
