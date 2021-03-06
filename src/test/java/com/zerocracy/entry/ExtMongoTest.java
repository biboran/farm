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
package com.zerocracy.entry;

import com.jcabi.xml.XML;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.pm.Footprint;
import com.zerocracy.pmo.Pmo;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link ExtMongo}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ExtMongoTest {

    @Test
    public void createsMongo() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Project project = new Pmo(farm);
        new ClaimOut().type("hello").postTo(project);
        final XML xml = new Claims(project).iterate().iterator().next();
        final MongoClient mongo = new ExtMongo(farm).value();
        final String pid = "12MONGO89";
        try (final Footprint footprint = new Footprint(mongo, pid)) {
            footprint.open(xml);
            footprint.close(xml);
            MatcherAssert.assertThat(
                footprint.collection().find(Filters.eq("project", pid)),
                Matchers.iterableWithSize(1)
            );
        }
    }

    @Test
    public void worksInMultipleThreads() throws Exception {
        try (final Farm farm = new SyncFarm(new PropsFarm(new FkFarm()))) {
            final Project project = new Pmo(farm);
            final String pid = "123456799";
            MatcherAssert.assertThat(
                inc -> {
                    final MongoClient mongo = new ExtMongo(farm).value();
                    try (final Footprint footprint =
                        new Footprint(mongo, pid)) {
                        new ClaimOut().type("hello dude").postTo(project);
                        final XML xml = new Claims(project)
                            .iterate().iterator().next();
                        footprint.open(xml);
                        footprint.close(xml);
                        return footprint.collection()
                            .find(Filters.eq("project", pid))
                            .iterator().hasNext();
                    }
                },
                new RunsInThreads<>(new AtomicInteger())
            );
        }
    }
}
