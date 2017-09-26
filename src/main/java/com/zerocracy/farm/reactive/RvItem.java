/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.farm.reactive;

import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import java.io.IOException;
import java.nio.file.Path;
import lombok.EqualsAndHashCode;
import org.cactoos.Proc;
import org.cactoos.func.IoCheckedProc;

/**
 * Reactive item.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
@EqualsAndHashCode(of = "origin")
final class RvItem implements Item {

    /**
     * Original item.
     */
    private final Item origin;

    /**
     * The spin.
     */
    private final Proc<?> flush;

    /**
     * Ctor.
     * @param item Original item
     * @param proc Spin
     */
    RvItem(final Item item, final Proc<?> proc) {
        this.origin = item;
        this.flush = proc;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        return this.origin.path();
    }

    @Override
    public void close() throws IOException {
        final int total = new Xocument(this.path())
            .nodes("/claims/claim").size();
        this.origin.close();
        if (total > 0) {
            new IoCheckedProc<>(this.flush).exec(null);
        }
    }

}
