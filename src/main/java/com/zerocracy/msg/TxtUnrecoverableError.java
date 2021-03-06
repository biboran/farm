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
package com.zerocracy.msg;

import com.zerocracy.farm.props.Props;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.Text;
import org.cactoos.io.BytesOf;
import org.cactoos.text.JoinedText;
import org.cactoos.text.TextOf;

/**
 * Error message for unrecoverable failure.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.17
 */
public final class TxtUnrecoverableError implements Text {

    /**
     * Error.
     */
    private final Throwable err;

    /**
     * Props.
     */
    private final Props props;

    /**
     * Ctor.
     * @param error Error
     * @param pps Props
     */
    public TxtUnrecoverableError(final Throwable error, final Props pps) {
        this.err = error;
        this.props = pps;
    }

    @Override
    public String asString() throws IOException {
        return new JoinedText(
            "",
            "There is an unrecoverable failure on my side.",
            " Please, submit it",
            " [here](https://github.com/zerocracy/farm/issues):",
            "\n\n```\n",
            StringUtils.abbreviate(
                new TextOf(
                    new BytesOf(this.err)
                ).asString(),
                // @checkstyle MagicNumber (1 line)
                1000
            ),
            "\n```\n\n",
            String.format(
                "[%s](https://github.com/zerocracy/farm/tree/%1$s)",
                this.props.get("//build/version", "")
            ),
            " /cc @yegor256"
        ).asString();
    }
}
