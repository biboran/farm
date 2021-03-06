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
package com.zerocracy.farm;

import com.jcabi.s3.Ocket;
import com.jcabi.s3.fake.FkOcket;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link S3Item}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class S3ItemTest {

    @Test
    public void modifiesFiles() throws Exception {
        final Ocket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket", "roles.xml"
        );
        final Path temp = Files.createTempFile("", "");
        try (final Item item = new S3Item(ocket, temp)) {
            new Xocument(item).bootstrap("pm/staff/roles");
            new Xocument(item).modify(
                new Directives().xpath("/roles")
                    .add("person")
                    .attr("id", "yegor256")
                    .add("role").set("ARC")
            );
        }
        try (final Item item = new S3Item(ocket, temp)) {
            MatcherAssert.assertThat(
                new Xocument(item).xpath("/roles/text()"),
                Matchers.not(Matchers.emptyIterable())
            );
        }
    }

    @Test
    public void closesNonExistingFiles() throws Exception {
        final Ocket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket-66", "orders.xml"
        );
        try (final Item item = new S3Item(ocket)) {
            item.path();
        }
    }

    @Test
    public void closesExistingFiles() throws Exception {
        final Ocket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket-69", "bans.xml"
        );
        try (final Item item = new S3Item(ocket)) {
            Files.write(item.path(), "".getBytes());
        }
    }

    @Test
    public void refreshesFilesOnServer() throws Exception {
        final FkOcket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket-1", "wbs.xml"
        );
        final Path temp = Files.createTempFile("", "");
        final String before;
        try (final Item item = new S3Item(ocket, temp)) {
            new Xocument(item).bootstrap("pm/scope/wbs");
            before = new Xocument(item).toString();
            new Xocument(item).modify(
                new Directives().xpath("/wbs")
                    .add("job")
                    .attr("id", "gh:yegor256/pdd#1")
                    .add("role").set("DEV").up()
                    .add("created").set("2016-12-29T09:03:21.684Z")
            );
        }
        new Ocket.Text(ocket).write(before);
        Files.setLastModifiedTime(
            ocket.file().toPath(),
            FileTime.fromMillis(Long.MAX_VALUE)
        );
        try (final Item item = new S3Item(ocket, temp)) {
            MatcherAssert.assertThat(
                new Xocument(item).nodes("/wbs[not(job)]"),
                Matchers.not(Matchers.emptyIterable())
            );
        }
    }

}
