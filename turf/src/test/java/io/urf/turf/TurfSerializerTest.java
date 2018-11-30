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

import static io.urf.turf.TurfTestResources.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
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
 * Test of {@link TurfSerializer}.
 * <p>
 * Rather than testing exact serializations (which would make the tests tedious and brittle), most of the tests here re-parse the serializations and compare
 * them to parsed test serializations provided in {@link TurfTestResources}.
 * </p>
 * @author Garret Wilson
 */
public class TurfSerializerTest {

	/**
	 * Loads and parses an object graph by parsing the indicated TURF document resource.
	 * @implSpec The default implementation defaults to {@link #parse(InputStream)}.
	 * @param testResourceName The name of the TURF document resource for testing, relative to {@link TurfTestResources}.
	 * @return The optional resource instance parsed from the named TURF document resource.
	 */
	protected Optional<Object> parseTestResource(@Nonnull final String testResourceName) throws IOException {
		try (final InputStream inputStream = TurfTestResources.class.getResourceAsStream(testResourceName)) {
			return parse(inputStream);
		}
	}

	/**
	 * Loads and parses an object graph by parsing the indicated TURF document resource.
	 * @param inputStream The input stream containing the TURF document for testing.
	 * @return The optional resource instance parsed from the given TURF document input stream.
	 */
	protected Optional<Object> parse(@Nonnull final InputStream inputStream) throws IOException {
		return new TurfParser(new SimpleGraphUrfProcessor()).parseDocument(inputStream).map(ObjectUrfResource::unwrap);
	}

	/**
	 * Loads and parses an object graph by parsing the indicated TURF document resource.
	 * @param string The string containing the TURF document for testing.
	 * @return The optional resource instance parsed from the given TURF document input stream.
	 */
	protected Optional<Object> parse(@Nonnull final String string) throws IOException {
		try (final InputStream inputStream = new ByteArrayInputStream(string.getBytes(TURF.DEFAULT_CHARSET))) {
			return parse(inputStream);
		}
	}

	/**
	 * Compares two URF object graphs for equality.
	 * @implNote This implementation doesn't yet support collections.
	 * @param reason additional information about the error
	 * @param object1 The first URF object graph to test.
	 * @param object2 The URF object graph to test with the first.
	 */
	protected void assertGraphsEqual(@Nonnull final String reason, @Nonnull final Object object1, @Nonnull final Object object2) { //TODO move to UrfTests; create analogous version for SURF parser
		if(object1 instanceof UrfObject && object2 instanceof UrfObject) {
			assertGraphsEqual(reason, (UrfObject)object1, (UrfObject)object2);
		} else { //TODO add support for collections
			assertThat(reason, object1, is(object2)); //assume these are value objects to compare (or not even the same type of object)
		}
	}

	/**
	 * Compares two URF object graphs for equality.
	 * @param reason additional information about the error
	 * @param object1 The first URF object graph to test.
	 * @param object2 The URF object graph to test with the first.
	 */
	protected void assertGraphsEqual(@Nonnull final String reason, @Nonnull final UrfObject object1, @Nonnull final UrfObject object2) {
		assertThat(reason + ": tag", object1.getTag(), is(object2.getTag()));
		assertThat(reason + ": type", object1.getTypeTag(), is(object2.getTypeTag()));
		assertThat(reason + ": property count", object1.getPropertyCount(), is(object2.getPropertyCount()));
		object1.getProperties().forEach(property -> {
			final URI propertyTag = property.getKey();
			final Object propertyValue1 = property.getValue();
			final Object propertyValue2 = object2.getPropertyValue(propertyTag)
					.orElseThrow(() -> new AssertionError(reason + ": missing property value " + propertyTag));
			//TODO add convenience method for creating a property tag reference to provide more tag/typeTag info
			assertGraphsEqual(reason + ", " + propertyTag, propertyValue1, propertyValue2);
		});
	}

	/** @see TurfTestResources#OK_NAMESPACES_RESOURCE_NAMES */
	@Test
	public void testOkNamespaces() throws IOException {
		final UrfObject urfObject = new UrfObject(URI.create("https://example.com/foo.bar"));
		urfObject.setPropertyValue(URF.AD_HOC_NAMESPACE.resolve("foo"), "bar");
		urfObject.setPropertyValue(URI.create("http://purl.org/dc/elements/1.1/title"), "An Example Resource");
		urfObject.setPropertyValue(URI.create("http://purl.org/dc/elements/1.1/creator"), "Jane Doe");
		urfObject.setPropertyValue(URI.create("http://purl.org/dc/elements/1.1/date"), Year.of(2018));
		final UrfObject maker = new UrfObject(null, URI.create("http://xmlns.com/foaf/0.1/Person"));
		maker.setPropertyValue(URI.create("http://xmlns.com/foaf/0.1/firstName"), "Jane");
		maker.setPropertyValue(URI.create("http://xmlns.com/foaf/0.1/lastName"), "Doe");
		maker.setPropertyValue(URI.create("http://xmlns.com/foaf/0.1/homepage"), URI.create("https://janedoe.example.com/"));
		urfObject.setPropertyValue(URI.create("http://xmlns.com/foaf/0.1/maker"), maker);
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.registerNamespace(URI.create("http://purl.org/dc/elements/1.1/"), "dc");
			serializer.registerNamespace(URI.create("http://xmlns.com/foaf/0.1/"), "foaf");
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			for(final String okObjectNoPropertiesResourceName : OK_NAMESPACES_RESOURCE_NAMES) {
				assertGraphsEqual(okObjectNoPropertiesResourceName, parse(serialization).orElseThrow(AssertionError::new),
						parseTestResource(okObjectNoPropertiesResourceName).orElseThrow(AssertionError::new));
			}
		}
	}

	/* TODO create test for namespace aliases
			final TurfSerializer serializer = new TurfSerializer();
			serializer.registerNamespace(URI.create("http://purl.org/dc/elements/1.1/"), "dc");
			serializer.registerNamespace(URI.create("http://xmlns.com/foaf/0.1/"), "foaf");
			serializer.setFormatted(true);
			System.out.println(serializer.serializeDocument(urfObject));
	 */
}
