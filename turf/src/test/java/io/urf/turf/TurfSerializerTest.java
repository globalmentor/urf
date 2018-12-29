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
import java.util.*;

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
		return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream);
	}

	/**
	 * Loads and parses the TURF document indicated by the given string.
	 * @param string The string containing the TURF document for testing.
	 * @return The list of TURF document roots parsed.
	 */
	protected List<Object> parse(@Nonnull final String string) throws IOException {
		try (final InputStream inputStream = new ByteArrayInputStream(string.getBytes(TURF.DEFAULT_CHARSET))) {
			return parse(inputStream);
		}
	}

	/**
	 * Compares two lists of URF object graphs for equality.
	 * @param reason additional information about the error.
	 * @param objects1 The first list of URF object graphs to test.
	 * @param objects2 The URF object graphs to test with the first, in the same order.
	 */
	protected void assertGraphsEqual(@Nonnull final String reason, @Nonnull final List<Object> objects1, @Nonnull final List<Object> objects2) { //TODO move to UrfTests; create analogous version for SURF parser
		assertThat(reason + ": number of roots", objects1, hasSize(objects2.size()));
		for(int i = 0, length = objects1.size(); i < length; i++) { //TODO use new zipping function when created
			assertGraphsEqual(reason, objects1.get(i), objects2.get(i));
		}
	}

	/**
	 * Compares two URF object graphs for equality.
	 * @implNote This implementation doesn't yet support collections.
	 * @param reason additional information about the error.
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
	 * @param reason additional information about the error.
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

	//#namespaces

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
			for(final boolean useAliases : asList(false, true)) {
				final TurfSerializer serializer = new TurfSerializer();
				if(useAliases) {
					serializer.registerNamespace(URI.create("http://purl.org/dc/elements/1.1/"), "dc");
					serializer.registerNamespace(URI.create("http://xmlns.com/foaf/0.1/"), "foaf");
				}
				serializer.setFormatted(formatted);
				final String serialization = serializer.serializeDocument(urfObject);
				for(final String okObjectNoPropertiesResourceName : OK_NAMESPACES_RESOURCE_NAMES) {
					assertGraphsEqual(okObjectNoPropertiesResourceName, parse(serialization), parseTestResource(okObjectNoPropertiesResourceName));
				}
			}
		}
	}

	//#roots

	/** @see TurfTestResources#OK_ROOTS_WHITESPACE_RESOURCE_NAME */
	@Test
	public void testOkRoots() throws IOException {
		final UrfObject object1 = new UrfObject(URI.create("https://example.com/object1"));
		object1.setPropertyValue("info", "first");

		final UrfObject object2 = new UrfObject(URI.create("https://example.com/object2"));
		object2.setPropertyValue("info", "second");
		final UrfObject object2Stuff = new UrfObject(null, "Stuff");
		object2Stuff.setPropertyValue("test", 222);
		object2.setPropertyValue("extra", object2Stuff);

		final UrfObject object3 = new UrfObject(URI.create("https://example.com/object3"));
		object3.setPropertyValue("info", "third");
		final UrfObject object3Stuff = new UrfObject(null, URF.Handle.toTag("Stuff"));
		object3Stuff.setPropertyValue("test", 333);
		object3.setPropertyValue("extra", object3Stuff);

		final UrfObject object4 = new UrfObject();
		object4.setPropertyValue("info", "fourth");

		final UrfObject object5 = new UrfObject(URI.create("https://example.com/object5"), "foo-Bar");
		object5.setPropertyValue("info", "fifth");

		final UrfObject object6 = new UrfObject();
		object6.setPropertyValue("info", "fourth");

		final String string = "foobar";

		final UrfObject object7 = new UrfObject();
		object7.setPropertyValue("info", "eighth");

		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(object1, object2, object3, object4, object5, object6, string, object7);
			for(final String okObjectNoPropertiesResourceName : asList(OK_ROOTS_WHITESPACE_RESOURCE_NAME)) { //TODO add non-whitespace variation
				assertGraphsEqual(okObjectNoPropertiesResourceName, parse(serialization), parseTestResource(okObjectNoPropertiesResourceName));
			}
		}
	}

	//#objects

	/** @see TurfSerializer#serializeObjectReference(Appendable, URI, URI, boolean) */
	@Test
	public void TestSerializeObjectReference() throws IOException {

		//Type#id
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example#foo"), URI.create("https://urf.name/Example"), false).toString(),
				is("Example#foo"));
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example#123"), URI.create("https://urf.name/Example"), false).toString(),
				is("Example#123"));
		assertThat(new TurfSerializer().registerNamespace(URI.create("https://example.com/fake/"), "fake").serializeObjectReference(new StringBuilder(),
				URI.create("https://example.com/fake/Example#foo"), URI.create("https://example.com/fake/Example"), false).toString(), is("fake/Example#foo"));

		//|"id"|*Type
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example#foo:bar"), URI.create("https://urf.name/Example"), false)
				.toString(), is("|\"foo:bar\"|*Example"));
		assertThat(
				new TurfSerializer().registerNamespace(URI.create("https://example.com/fake/"), "fake").serializeObjectReference(new StringBuilder(),
						URI.create("https://example.com/fake/Example#foo:bar"), URI.create("https://example.com/fake/Example"), false).toString(),
				is("|\"foo:bar\"|*fake/Example"));
		//|"id"|*|<typeTag>|
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test#foo:bar"), URI.create("https://example.com/Test"), false)
				.toString(), is("|\"foo:bar\"|*|<https://example.com/Test>|"));

		//handle*Type
		assertThat(
				new TurfSerializer()
						.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example"), URI.create("https://urf.name/SomeType"), false).toString(),
				is("Example"));
		assertThat(
				new TurfSerializer()
						.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example"), URI.create("https://urf.name/SomeType"), true).toString(),
				is("Example*SomeType"));
		assertThat(
				new TurfSerializer().registerNamespace(URI.create("https://example.com/fake/"), "fake").serializeObjectReference(new StringBuilder(),
						URI.create("https://example.com/fake/Example"), URI.create("https://example.com/fake/SomeType"), true).toString(),
				is("fake/Example*fake/SomeType"));
		//handle*|<typeTag>|
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example"), URI.create("https://example.com/SomeType"), false).toString(),
				is("Example"));
		assertThat(
				new TurfSerializer()
						.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example"), URI.create("https://example.com/SomeType"), true).toString(),
				is("Example*|<https://example.com/SomeType>|"));
		assertThat(new TurfSerializer().registerNamespace(URI.create("https://example.com/fake/"), "fake")
				.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/fake/Example"), URI.create("https://example.com/SomeType"), true)
				.toString(), is("fake/Example*|<https://example.com/SomeType>|"));

		//|<tag>|*Type
		assertThat(
				new TurfSerializer()
						.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test"), URI.create("https://urf.name/SomeType"), false).toString(),
				is("|<https://example.com/Test>|"));
		assertThat(
				new TurfSerializer()
						.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test"), URI.create("https://urf.name/SomeType"), true).toString(),
				is("|<https://example.com/Test>|*SomeType"));
		assertThat(new TurfSerializer().registerNamespace(URI.create("https://example.com/fake/"), "fake")
				.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test"), URI.create("https://example.com/fake/SomeType"), true)
				.toString(), is("|<https://example.com/Test>|*fake/SomeType"));
		//|<tag>|*|<typeTag>|
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test"), URI.create("https://example.com/SomeType"), false).toString(),
				is("|<https://example.com/Test>|"));
		assertThat(
				new TurfSerializer()
						.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test"), URI.create("https://example.com/SomeType"), true).toString(),
				is("|<https://example.com/Test>|*|<https://example.com/SomeType>|"));
	}

}
