/*
 * Copyright © 2018 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.turf;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static io.urf.turf.TurfTestResources.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URI;
import java.time.Year;
import java.util.Optional;

import javax.annotation.*;

import org.junit.Test;

import io.urf.URF;
import io.urf.model.*;

/**
 * Test of parsing TURF documents with {@link TurfParser} into a simple graph using {@link SimpleGraphUrfProcessor}.
 * 
 * @author Garret Wilson
 */
public class TurfParserTest {

	/**
	 * Loads and parses an object graph by parsing the indicated TURF document resource.
	 * @implSpec The default implementation defaults to {@link #parseTestResource(InputStream)}.
	 * @param testResourceName The name of the TURF document resource for testing, relative to {@link TurfTestResources}.
	 * @return The optional resource instance parsed from the named TURF document resource.
	 */
	protected Optional<Object> parseTestResource(@Nonnull final String testResourceName) throws IOException {
		try (final InputStream inputStream = TurfTestResources.class.getResourceAsStream(testResourceName)) {
			return parseTestResource(inputStream);
		}
	}

	/**
	 * Loads and parses an object graph by parsing the indicated TURF document resource.
	 * @param inputStream The input stream containing the TURF document for testing.
	 * @return The optional resource instance parsed from the given TURF document input stream.
	 */
	protected Optional<Object> parseTestResource(@Nonnull final InputStream inputStream) throws IOException {
		return new TurfParser(new SimpleGraphUrfProcessor()).parse(inputStream)
				//map any object wrappers to their wrapped objects TODO make sure the wrappers have no description
				.map(object -> object instanceof ObjectUrfResource ? ((ObjectUrfResource<?>)object).getObject() : object);
	}

	@Test
	public void testOkNamespaces() throws IOException {
		final Optional<Object> object = parseTestResource(OK_NAMESPACES);
		final UrfObject urfObject = object.map(UrfObject.class::cast).orElseThrow(AssertionError::new);
		assertThat(urfObject.getTag(), isPresentAndIs(URI.create("https://example.com/foo.bar")));
		assertThat(urfObject.getPropertyValue(URF.AD_HOC_NAMESPACE.resolve("foo")), isPresentAndIs("bar"));
		assertThat(urfObject.getPropertyValue(URI.create("http://purl.org/dc/elements/1.1/title")), isPresentAndIs("An Example Resource"));
		assertThat(urfObject.getPropertyValue(URI.create("http://purl.org/dc/elements/1.1/creator")), isPresentAndIs("Jane Doe"));
		assertThat(urfObject.getPropertyValue(URI.create("http://purl.org/dc/elements/1.1/date")), isPresentAndIs(Year.of(2018)));
		final UrfObject maker = urfObject.getPropertyValue(URI.create("http://xmlns.com/foaf/0.1/maker")).map(UrfObject.class::cast)
				.orElseThrow(AssertionError::new);
		assertThat(maker.getPropertyValue(URI.create("http://xmlns.com/foaf/0.1/firstName")), isPresentAndIs("Jane"));
		assertThat(maker.getPropertyValue(URI.create("http://xmlns.com/foaf/0.1/lastName")), isPresentAndIs("Doe"));
		assertThat(maker.getPropertyValue(URI.create("http://xmlns.com/foaf/0.1/homepage")), isPresentAndIs(URI.create("https://janedoe.example.com/")));
	}

}
