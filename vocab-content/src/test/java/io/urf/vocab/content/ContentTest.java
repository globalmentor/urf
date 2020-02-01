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

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.net.ContentType.*;
import static com.globalmentor.text.Text.PLAIN_CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.nio.charset.Charset;

import org.junit.jupiter.api.*;

import com.globalmentor.net.ContentType;

import io.urf.model.*;
import io.urf.vocab.content.Content;

/**
 * Tests of the {@link Content} vocabulary.
 * @author Garret Wilson
 *
 */
public class ContentTest {

	//content-type

	/** @see Content#findContentType */
	@Test
	public void testFindContentType() {
		final UrfObject urfObject = new UrfObject();
		assertThat(Content.findContentType(urfObject), isEmpty());
		urfObject.setPropertyValue(Content.TYPE_PROPERTY_TAG, new UrfObject(Content.NAMESPACE.resolve("MediaType#text%2Fhtml%3Bcharset%3Dutf-8")));
		assertThat(Content.findContentType(urfObject), isPresentAndIs(ContentType.parse("text/html; charset=UTF-8")));
	}

	/** @see Content#setContentType(UrfResourceDescription, ContentType) */
	@Test
	public void testSetContentType() {
		final UrfObject urfObject = new UrfObject();
		Content.setContentType(urfObject, ContentType.parse("text/html; charset=UTF-8"));
		assertThat(urfObject.findPropertyValue(Content.TYPE_PROPERTY_TAG).map(UrfObject.class::cast).flatMap(UrfObject::getTag),
				isPresentAndIs(Content.NAMESPACE.resolve("MediaType#text%2Fhtml%3Bcharset%3Dutf-8")));
	}

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

	//content-MediaType

	/** @see Content.Tag#fromMediaType(ContentType) */
	@Test
	public void testTagFromMediaType() {
		assertThat(Content.Tag.fromMediaType(PLAIN_CONTENT_TYPE), is(Content.NAMESPACE.resolve("MediaType#text%2Fplain")));
		assertThat(Content.Tag.fromMediaType(APPLICATION_OCTET_STREAM_CONTENT_TYPE), is(Content.NAMESPACE.resolve("MediaType#application%2Foctet-stream")));
		assertThat(Content.Tag.fromMediaType(ContentType.parse("application/ld+json")), is(Content.NAMESPACE.resolve("MediaType#application%2Fld%2Bjson")));
		assertThat(Content.Tag.fromMediaType(ContentType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
				is(Content.NAMESPACE.resolve("MediaType#application%2Fvnd.openxmlformats-officedocument.wordprocessingml.document")));
		assertThat(Content.Tag.fromMediaType(ContentType.parse("text/html")), is(Content.NAMESPACE.resolve("MediaType#text%2Fhtml")));
		assertThat(Content.Tag.fromMediaType(ContentType.parse("text/html;charset=UTF-8")),
				is(Content.NAMESPACE.resolve("MediaType#text%2Fhtml%3Bcharset%3Dutf-8")));
	}

	/** @see Content.Tag#toMediaType(URI) */
	@Test
	public void testTagToMediaType() {
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#text%2Fplain")), is(PLAIN_CONTENT_TYPE));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#application%2Foctet-stream")), is(APPLICATION_OCTET_STREAM_CONTENT_TYPE));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#application%2Fld%2Bjson")), is(ContentType.parse("application/ld+json")));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#application%2Fvnd.openxmlformats-officedocument.wordprocessingml.document")),
				is(ContentType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document")));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#text%2Fhtml")), is(ContentType.parse("text/html")));
		assertThat(Content.Tag.toMediaType(Content.NAMESPACE.resolve("MediaType#text%2Fhtml%3Bcharset%3Dutf-8")), is(ContentType.parse("text/html;charset=UTF-8")));
	}

}
