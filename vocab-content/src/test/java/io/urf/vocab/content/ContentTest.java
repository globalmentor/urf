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

import static com.globalmentor.net.ContentType.*;
import static com.globalmentor.text.Text.PLAIN_CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.*;

import com.globalmentor.net.ContentType;

import io.urf.vocab.content.Content;

/**
 * Tests of the {@link Content} vocabulary.
 * @author Garret Wilson
 *
 */
public class ContentTest {

	@Test
	public void testTagFromCharset() {
		assertThat(Content.Tag.fromCharset(US_ASCII), is(Content.NAMESPACE.resolve("Charset#US-ASCII")));
		assertThat(Content.Tag.fromCharset(ISO_8859_1), is(Content.NAMESPACE.resolve("Charset#ISO-8859-1")));
		assertThat(Content.Tag.fromCharset(UTF_8), is(Content.NAMESPACE.resolve("Charset#UTF-8")));
	}

	@Test
	public void testTagToCharset() {
		assertThat(Content.Tag.toCharset(Content.NAMESPACE.resolve("Charset#US-ASCII")), is(US_ASCII));
		assertThat(Content.Tag.toCharset(Content.NAMESPACE.resolve("Charset#ISO-8859-1")), is(ISO_8859_1));
		assertThat(Content.Tag.toCharset(Content.NAMESPACE.resolve("Charset#UTF-8")), is(UTF_8));
	}

	@Test
	public void testTagFromMediaType() {
		assertThat(Content.Tag.fromMediaType(PLAIN_CONTENT_TYPE), is(Content.NAMESPACE.resolve("MediaType#text%2Fplain")));
		assertThat(Content.Tag.fromMediaType(APPLICATION_OCTET_STREAM_CONTENT_TYPE), is(Content.NAMESPACE.resolve("MediaType#application%2Foctet-stream")));
		assertThat(Content.Tag.fromMediaType(ContentType.of("application/ld+json")), is(Content.NAMESPACE.resolve("MediaType#application%2Fld%2Bjson")));
		assertThat(Content.Tag.fromMediaType(ContentType.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
				is(Content.NAMESPACE.resolve("MediaType#application%2Fvnd.openxmlformats-officedocument.wordprocessingml.document")));
		assertThat(Content.Tag.fromMediaType(ContentType.of("text/html")), is(Content.NAMESPACE.resolve("MediaType#text%2Fhtml")));
		assertThat(Content.Tag.fromMediaType(ContentType.of("text/html;charset=UTF-8")), is(Content.NAMESPACE.resolve("MediaType#text%2Fhtml%3Bcharset%3DUTF-8")));
	}

	@Test
	public void testTagToMediaType() {
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#text%2Fplain")), is(PLAIN_CONTENT_TYPE));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#application%2Foctet-stream")), is(APPLICATION_OCTET_STREAM_CONTENT_TYPE));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#application%2Fld%2Bjson")), is(ContentType.of("application/ld+json")));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#application%2Fvnd.openxmlformats-officedocument.wordprocessingml.document")),
				is(ContentType.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#text%2Fhtml")), is(ContentType.of("text/html")));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#text%2Fhtml%3Bcharset%3DUTF-8")), is(ContentType.of("text/html;charset=UTF-8")));
	}

}
