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
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URI;
import java.time.Year;
import java.util.*;

import javax.annotation.*;

import org.junit.*;

import io.urf.URF;
import io.urf.model.*;

/**
 * Test of parsing TURF documents with {@link TurfParser} into a simple graph using {@link SimpleGraphUrfProcessor}.
 * 
 * @author Garret Wilson
 */
public class TurfParserTest {

	/**
	 * Loads and parses the indicated TURF document resource.
	 * @implSpec The default implementation defaults to {@link #parse(InputStream)}.
	 * @param testResourceName The name of the TURF document resource for testing, relative to {@link TurfTestResources}.
	 * @return The list of TURF document roots parsed.
	 */
	protected List<Object> parseTestResource(@Nonnull final String testResourceName) throws IOException {
		try (final InputStream inputStream = TurfTestResources.class.getResourceAsStream(testResourceName)) {
			return parse(inputStream);
		}
	}

	/**
	 * Loads and parses the indicated TURF document resource.
	 * @param inputStream The input stream containing the TURF document for testing.
	 * @return The list of TURF document roots parsed.
	 */
	protected List<Object> parse(@Nonnull final InputStream inputStream) throws IOException {
		//TODO improve for multiple roots; update comments
		return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream);
	}

	/** @see TurfTestResources#OK_NAMESPACES_RESOURCE_NAMES */
	@Test
	public void testOkNamespaces() throws IOException {
		for(final String okNamespacesResourceName : OK_NAMESPACES_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okNamespacesResourceName).stream().findAny().map(ObjectUrfResource::unwrap); //TODO require no more than one resource
			final UrfObject urfObject = object.map(UrfObject.class::cast).orElseThrow(AssertionError::new);
			assertThat(okNamespacesResourceName, urfObject.getTag(), isPresentAndIs(URI.create("https://example.com/foo.bar")));
			assertThat(okNamespacesResourceName, urfObject.getPropertyValue(URF.AD_HOC_NAMESPACE.resolve("foo")), isPresentAndIs("bar"));
			assertThat(okNamespacesResourceName, urfObject.getPropertyValue(URI.create("http://purl.org/dc/elements/1.1/title")),
					isPresentAndIs("An Example Resource"));
			assertThat(okNamespacesResourceName, urfObject.getPropertyValue(URI.create("http://purl.org/dc/elements/1.1/creator")), isPresentAndIs("Jane Doe"));
			assertThat(okNamespacesResourceName, urfObject.getPropertyValue(URI.create("http://purl.org/dc/elements/1.1/date")), isPresentAndIs(Year.of(2018)));
			final UrfObject maker = urfObject.getPropertyValue(URI.create("http://xmlns.com/foaf/0.1/maker")).map(UrfObject.class::cast)
					.orElseThrow(AssertionError::new);
			assertThat(okNamespacesResourceName, maker.getTypeTag(), isPresentAndIs(URI.create("http://xmlns.com/foaf/0.1/Person")));
			assertThat(okNamespacesResourceName, maker.getPropertyValue(URI.create("http://xmlns.com/foaf/0.1/firstName")), isPresentAndIs("Jane"));
			assertThat(okNamespacesResourceName, maker.getPropertyValue(URI.create("http://xmlns.com/foaf/0.1/lastName")), isPresentAndIs("Doe"));
			assertThat(okNamespacesResourceName, maker.getPropertyValue(URI.create("http://xmlns.com/foaf/0.1/homepage")),
					isPresentAndIs(URI.create("https://janedoe.example.com/")));
		}
	}

	/** @see TurfTestResources#OK_ROOTS_WHITESPACE_RESOURCE_NAME */
	@Test
	public void testOkRoots() throws IOException {
		for(final String okRootResourceName : asList(OK_ROOTS_WHITESPACE_RESOURCE_NAME)) { //TODO add non-whitespace variation
			final List<Object> roots = parseTestResource(okRootResourceName);
			assertThat(roots, hasSize(8)); //TODO fix when references are brought back

			final UrfObject object1 = (UrfObject)roots.get(0);
			assertThat(object1.getTag(), isPresentAndIs(URI.create("https://example.com/object1")));
			assertThat(object1.getPropertyValue("info"), isPresentAndIs("first"));

			final UrfObject object2 = (UrfObject)roots.get(1);
			assertThat(object2.getTag(), isPresentAndIs(URI.create("https://example.com/object2")));
			assertThat(object2.getPropertyValue("info"), isPresentAndIs("second"));
			final UrfObject object2Stuff = object2.getPropertyValue("extra").map(UrfObject.class::cast).orElseThrow(AssertionError::new);
			assertThat(object2Stuff.getTypeTag(), isPresentAndIs(URF.Handle.toTag("Stuff")));
			assertThat(object2Stuff.getPropertyValue("test"), isPresentAndIs(222));

			final UrfObject object3 = (UrfObject)roots.get(2);
			assertThat(object3.getTag(), isPresentAndIs(URI.create("https://example.com/object3")));
			assertThat(object3.getPropertyValue("info"), isPresentAndIs("third"));
			final UrfObject object3Stuff = object3.getPropertyValue("extra").map(UrfObject.class::cast).orElseThrow(AssertionError::new);
			assertThat(object3Stuff.getTypeTag(), isPresentAndIs(URF.Handle.toTag("Stuff")));
			assertThat(object3Stuff.getPropertyValue("test"), isPresentAndIs(333));

			final UrfObject object4 = (UrfObject)roots.get(3);
			assertThat(object4.getTag(), isEmpty());
			assertThat(object4.getPropertyValue("info"), isPresentAndIs("fourth"));

			final UrfObject object5 = (UrfObject)roots.get(4);
			assertThat(object5.getTag(), isPresentAndIs(URI.create("https://example.com/object5")));
			assertThat(object5.getPropertyValue("info"), isPresentAndIs("fifth"));

			final UrfObject object6 = (UrfObject)roots.get(5);
			assertThat(object6.getTag(), isEmpty());
			assertThat(object6.getPropertyValue("info"), isPresentAndIs("fourth"));

			assertThat(roots.get(6), is("foobar"));

			final UrfObject object7 = (UrfObject)roots.get(7);
			assertThat(object7.getTag(), isEmpty());
			assertThat(object7.getPropertyValue("info"), isPresentAndIs("eighth"));
		}
	}

}
