/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.urf.vocab.content;

import static java.nio.charset.StandardCharsets.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.nio.charset.Charset;

import org.junit.jupiter.api.*;

import io.urf.vocab.content.Content;

/**
 * Tests of the {@link Content} vocabulary.
 * @author Garret Wilson
 *
 */
public class ContentTest {

	//content-Charset

	/** @see Content.Tag#fromCharset(Charset) */
	@Test
	public void testTagFromCharset() {
		assertThat(Content.Tag.fromCharset(US_ASCII), is(Content.NAMESPACE.resolve("Charset#US-ASCII")));
		assertThat(Content.Tag.fromCharset(ISO_8859_1), is(Content.NAMESPACE.resolve("Charset#ISO-8859-1")));
		assertThat(Content.Tag.fromCharset(UTF_8), is(Content.NAMESPACE.resolve("Charset#UTF-8")));
	}

	/** @see Content.Tag#toCharset(URI) */
	@Test
	public void testTagToCharset() {
		assertThat(Content.Tag.toCharset(Content.NAMESPACE.resolve("Charset#US-ASCII")), is(US_ASCII));
		assertThat(Content.Tag.toCharset(Content.NAMESPACE.resolve("Charset#ISO-8859-1")), is(ISO_8859_1));
		assertThat(Content.Tag.toCharset(Content.NAMESPACE.resolve("Charset#UTF-8")), is(UTF_8));
	}

}
