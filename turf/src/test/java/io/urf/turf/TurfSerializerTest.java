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
import static java.util.AbstractMap.SimpleImmutableEntry;
import static java.util.Arrays.*;
import static java.util.stream.StreamSupport.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.net.URI;
import java.time.Year;
import java.util.*;

import javax.annotation.*;

import org.junit.jupiter.api.*;

import com.globalmentor.vocab.*;

import io.urf.URF;
import io.urf.URF.Tag;
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
	 * @implNote This implementation does not yet deep comparison of n-ary property values (i.e. n-ary property values must be value objects for comparison).
	 * @param reason additional information about the error.
	 * @param object1 The first URF object graph to test.
	 * @param object2 The URF object graph to test with the first.
	 */
	protected void assertGraphsEqual(@Nonnull final String reason, @Nonnull final UrfObject object1, @Nonnull final UrfObject object2) {
		assertThat(reason + ": tag", object1.getTag(), is(object2.getTag()));
		assertThat(reason + ": type", object1.getTypeTag(), is(object2.getTypeTag()));
		assertThat(reason + ": property count", object1.getPropertyCount(), is(object2.getPropertyCount()));
		assertThat(reason + ": property value count", object1.getPropertyValueCount(), is(object2.getPropertyValueCount()));

		//TODO make this entire graph comparison more efficient; this was just quick-and-dirty

		//test the non n-ary properties
		object1.getProperties().forEach(property -> {
			final URI propertyTag = property.getKey();
			if(!Tag.isNary(propertyTag)) {
				final Object propertyValue1 = property.getValue();
				final Object propertyValue2 = object2.findPropertyValue(propertyTag)
						.orElseThrow(() -> new AssertionError(reason + ": missing property value " + propertyTag));
				//TODO add convenience method for creating a property tag reference to provide more tag/typeTag info
				assertGraphsEqual(reason + ", " + propertyTag, propertyValue1, propertyValue2);
			}
		});

		//test the n-ary properties
		//TODO get the property tags in a more efficient way; add to UrfObject API
		stream(object1.getProperties().spliterator(), false).map(Map.Entry::getKey).filter(Tag::isNary).forEach(naryPropertyTag -> {
			assertThat(reason + ", " + naryPropertyTag, object1.getPropertyValues(naryPropertyTag), is(object2.getPropertyValues(naryPropertyTag)));
		});
	}

	private static final URI CC_NAMESPACE = URI.create("http://creativecommons.org/ns#");
	private static final URI DC_NAMESPACE = URI.create("http://purl.org/dc/terms/");
	private static final URI OG_NAMESPACE = URI.create("http://ogp.me/ns#");
	private static final URI EG_NAMESPACE = URI.create("https://example.com/ns/");

	/** @see TurfSerializer#discoverVocabulary(URI) */
	@Test
	public void testDiscoverVocabulary() {
		final TurfSerializer serializer = new TurfSerializer();
		serializer.setDiscoverVocabularies(true);
		//The Creative Commons and Open Graph namespaces will be incorrectly detected by URF for now,
		//because they don't follow the URF namespace convention.
		serializer.discoverVocabulary(VocabularyTerm.of(CC_NAMESPACE, "permits").toURI()); //not an URF namespace
		serializer.discoverVocabulary(VocabularyTerm.of(DC_NAMESPACE, "creator").toURI());
		serializer.discoverVocabulary(VocabularyTerm.of(OG_NAMESPACE, "title").toURI()); //not an URF namespace
		serializer.discoverVocabulary(VocabularyTerm.of(DC_NAMESPACE, "creator").toURI()); //duplicate
		serializer.discoverVocabulary(VocabularyTerm.of(EG_NAMESPACE, "FooBar").toURI());
		final VocabularyRegistrar vocabularyRegistrar = serializer.getVocabularyRegistrar();
		assertThat(vocabularyRegistrar.getRegisteredPrefixesByVocabulary(),
				containsInAnyOrder(new SimpleImmutableEntry<>(URI.create("http://creativecommons.org/"), "ns1"), new SimpleImmutableEntry<>(DC_NAMESPACE, "terms"),
						new SimpleImmutableEntry<>(URI.create("http://ogp.me/"), "ns2"), new SimpleImmutableEntry<>(EG_NAMESPACE, "ns")));
	}

	/** @see TurfSerializer#discoverVocabulary(URI) */
	@Test
	public void testDiscoverVocabularyWithKnownVocabularies() {
		final VocabularyRegistry knownVocabularies = VocabularyRegistry.builder().registerPrefix("dc", DC_NAMESPACE).registerPrefix("eg", EG_NAMESPACE).build();
		final TurfSerializer serializer = new TurfSerializer(knownVocabularies);
		//The Creative Commons and Open Graph namespaces will be incorrectly detected by URF for now,
		//because they don't follow the URF namespace convention.
		serializer.discoverVocabulary(VocabularyTerm.of(CC_NAMESPACE, "permits").toURI()); //not an URF namespace
		serializer.discoverVocabulary(VocabularyTerm.of(DC_NAMESPACE, "creator").toURI());
		serializer.discoverVocabulary(VocabularyTerm.of(OG_NAMESPACE, "title").toURI()); //not an URF namespace
		serializer.discoverVocabulary(VocabularyTerm.of(DC_NAMESPACE, "creator").toURI()); //duplicate
		serializer.discoverVocabulary(VocabularyTerm.of(EG_NAMESPACE, "FooBar").toURI());
		final VocabularyRegistrar vocabularyRegistrar = serializer.getVocabularyRegistrar();
		assertThat(vocabularyRegistrar.getRegisteredPrefixesByVocabulary(),
				containsInAnyOrder(new SimpleImmutableEntry<>(URI.create("http://creativecommons.org/"), "ns1"), new SimpleImmutableEntry<>(DC_NAMESPACE, "dc"),
						new SimpleImmutableEntry<>(URI.create("http://ogp.me/"), "ns2"), new SimpleImmutableEntry<>(EG_NAMESPACE, "eg")));
	}

	/** @see TurfSerializer#discoverVocabularies(Object) */
	@Test
	public void testDiscoverVocabulariesFromResourceType() {
		final UrfObject urfObject = new UrfObject(URI.create("https://example.com/test1/tag"), URI.create("https://example.com/test2/Foo"));
		urfObject.setPropertyValueByHandle("foo", "bar");
		final UrfObject nestedUrfObject = new UrfObject(null, URI.create("https://example.com/test3/Bar"));
		urfObject.setPropertyValueByHandle("nested", nestedUrfObject);
		final TurfSerializer serializer = new TurfSerializer();
		serializer.discoverVocabularies(urfObject);
		final VocabularyRegistrar vocabularyRegistrar = serializer.getVocabularyRegistrar();
		//in the current implementation the tag is not discovered as a vocabulary
		assertThat(vocabularyRegistrar.getRegisteredPrefixesByVocabulary(),
				containsInAnyOrder(new SimpleImmutableEntry<>(URI.create("https://example.com/test2/"), "test2"),
						new SimpleImmutableEntry<>(URI.create("https://example.com/test3/"), "test3")));
	}

	/** @see TurfSerializer#discoverVocabularies(Object) */
	@Test
	public void testDiscoverVocabulariesFromPropertyTags() {
		final UrfObject urfObject = new UrfObject();
		urfObject.setPropertyValue(URI.create("https://example.com/test1/prop1"), "value1");
		final UrfObject nestedUrfObject = new UrfObject();
		nestedUrfObject.setPropertyValue(URI.create("https://example.com/test2/prop2"), "value2");
		urfObject.setPropertyValueByHandle("nested", nestedUrfObject);
		final TurfSerializer serializer = new TurfSerializer();
		serializer.discoverVocabularies(urfObject);
		final VocabularyRegistrar vocabularyRegistrar = serializer.getVocabularyRegistrar();
		assertThat(vocabularyRegistrar.getRegisteredPrefixesByVocabulary(),
				containsInAnyOrder(new SimpleImmutableEntry<>(URI.create("https://example.com/test1/"), "test1"),
						new SimpleImmutableEntry<>(URI.create("https://example.com/test2/"), "test2")));
	}

	/** @see TurfSerializer#discoverVocabularies(Object) */
	@Test
	public void testDiscoverVocabulariesInList() {
		final UrfObject urfObject1 = new UrfObject(null, URI.create("https://example.com/test1/Foo"));
		final UrfObject urfObject2 = new UrfObject(null, URI.create("https://example.com/test2/Bar"));
		final UrfObject urfObject3 = new UrfObject(null, URI.create("https://example.com/test3/FooBar"));
		urfObject1.setPropertyValueByHandle("prop", urfObject2);
		urfObject1.setPropertyValueByHandle("more", asList(urfObject3)); //nested list
		final List<Object> list = asList("foo", urfObject1, "bar");
		final TurfSerializer serializer = new TurfSerializer();
		serializer.discoverVocabularies(list);
		final VocabularyRegistrar vocabularyRegistrar = serializer.getVocabularyRegistrar();
		assertThat(vocabularyRegistrar.getRegisteredPrefixesByVocabulary(),
				containsInAnyOrder(new SimpleImmutableEntry<>(URI.create("https://example.com/test1/"), "test1"),
						new SimpleImmutableEntry<>(URI.create("https://example.com/test2/"), "test2"),
						new SimpleImmutableEntry<>(URI.create("https://example.com/test3/"), "test3")));
	}

	/** @see TurfSerializer#discoverVocabularies(Object) */
	@Test
	public void testDiscoverVocabulariesInMap() {
		final UrfObject rootObject = new UrfObject(null, URI.create("https://example.com/test1/FooBar"));
		final UrfObject fooObject = new UrfObject(null, URI.create("https://example.com/test2/Foo"));
		final UrfObject barObject = new UrfObject(null, URI.create("https://example.com/test3/Bar"));
		final Map<Object, Object> map = new HashMap<>();
		map.put("foo", "bar");
		map.put(fooObject, barObject);
		rootObject.setPropertyValueByHandle("more", map);
		final TurfSerializer serializer = new TurfSerializer();
		serializer.discoverVocabularies(rootObject);
		final VocabularyRegistrar vocabularyRegistrar = serializer.getVocabularyRegistrar();
		assertThat(vocabularyRegistrar.getRegisteredPrefixesByVocabulary(),
				containsInAnyOrder(new SimpleImmutableEntry<>(URI.create("https://example.com/test1/"), "test1"),
						new SimpleImmutableEntry<>(URI.create("https://example.com/test2/"), "test2"),
						new SimpleImmutableEntry<>(URI.create("https://example.com/test3/"), "test3")));
	}

	/**
	 * @see TurfSerializer#setDiscoverVocabularies(boolean)
	 * @see TurfSerializer#serializeDocument(Object)
	 */
	@Test
	public void testSerializeDocumentWithoutVocabularyDiscovery() throws IOException {
		final UrfObject urfObject = new UrfObject(null, URI.create("https://example.com/test/FooBar"));
		urfObject.setPropertyValue(URI.create("https://example.com/foo/bar"), "example");
		final TurfSerializer turfSerializer = new TurfSerializer();
		turfSerializer.setDiscoverVocabularies(false);
		turfSerializer.setFormatted(false);
		final String serialization = turfSerializer.serializeDocument(urfObject);
		assertThat(serialization, is("*|<https://example.com/test/FooBar>|:|<https://example.com/foo/bar>|=\"example\";"));
	}

	/**
	 * @see TurfSerializer#setDiscoverVocabularies(boolean)
	 * @see TurfSerializer#serializeDocument(Object)
	 */
	@Test
	public void testSerializeDocumentDiscoveringResourceTypeVocabulary() throws IOException {
		final UrfObject urfObject = new UrfObject(null, URI.create("https://example.com/test/FooBar"));
		final TurfSerializer turfSerializer = new TurfSerializer();
		turfSerializer.setDiscoverVocabularies(true);
		turfSerializer.setFormatted(false);
		final String serialization = turfSerializer.serializeDocument(urfObject);
		assertThat(serialization, is("===>urf:space-test=<https://example.com/test/>;<*test/FooBar"));
	}

	/**
	 * @see TurfSerializer#setDiscoverVocabularies(boolean)
	 * @see TurfSerializer#serializeDocument(Object)
	 */
	@Test
	public void testSerializeDocumentDiscoveringPropertyVocabulary() throws IOException {
		final UrfObject urfObject = new UrfObject();
		urfObject.setPropertyValue(URI.create("https://example.com/foo/bar"), "example");
		final TurfSerializer turfSerializer = new TurfSerializer();
		turfSerializer.setDiscoverVocabularies(true);
		turfSerializer.setFormatted(false);
		final String serialization = turfSerializer.serializeDocument(urfObject);
		assertThat(serialization, is("===>urf:space-foo=<https://example.com/foo/>;<*:foo/bar=\"example\";"));
	}

	/** @see TurfSerializer#serializeTagReference(Appendable, URI) */
	@Test
	public void testSerializeTagReference() throws IOException {
		final VocabularyManager aliases = new VocabularyManager();
		aliases.registerVocabulary(URI.create("https://example.com/fake/"), "fake");
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/"), aliases).toString(), is("|<https://urf.name/>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/Example"), aliases).toString(), is("Example"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/Example+"), aliases).toString(), is("Example+"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/Example#foo"), aliases).toString(), is("Example#foo"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/Example#123"), aliases).toString(), is("Example#123"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/Example#foo:bar"), aliases).toString(),
				is("|<https://urf.name/Example#foo:bar>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/test"), aliases).toString(), is("test"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/test+"), aliases).toString(), is("test+"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/test/"), aliases).toString(),
				is("|<https://urf.name/test/>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/false"), aliases).toString(),
				is("|<https://urf.name/false>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/true"), aliases).toString(),
				is("|<https://urf.name/true>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/foo/bar"), aliases).toString(), is("foo-bar"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/foo/bar+"), aliases).toString(), is("foo-bar+"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/foo/bar/"), aliases).toString(),
				is("|<https://urf.name/foo/bar/>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/foo/bar/Example"), aliases).toString(),
				is("foo-bar-Example"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/foo/bar/Example+"), aliases).toString(),
				is("foo-bar-Example+"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://urf.name/foo/bar/Example#test123"), aliases).toString(),
				is("foo-bar-Example#test123"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/Example"), aliases).toString(),
				is("fake/Example"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/Example+"), aliases).toString(),
				is("fake/Example+"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/Example#foo"), aliases).toString(),
				is("fake/Example#foo"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/Example#123"), aliases).toString(),
				is("fake/Example#123"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/Example#foo:bar"), aliases).toString(),
				is("|<https://example.com/fake/Example#foo:bar>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/test"), aliases).toString(), is("fake/test"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/test+"), aliases).toString(), is("fake/test+"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/Example"), aliases).toString(),
				is("fake/Example"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/Example+"), aliases).toString(),
				is("fake/Example+"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/foo/bar"), aliases).toString(),
				is("|<https://example.com/fake/foo/bar>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/foo/bar+"), aliases).toString(),
				is("|<https://example.com/fake/foo/bar+>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/foo/bar/Example"), aliases).toString(),
				is("|<https://example.com/fake/foo/bar/Example>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/foo/bar/Example+"), aliases).toString(),
				is("|<https://example.com/fake/foo/bar/Example+>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/foo/bar/Example#foo"), aliases).toString(),
				is("|<https://example.com/fake/foo/bar/Example#foo>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/foo/bar/Example#123"), aliases).toString(),
				is("|<https://example.com/fake/foo/bar/Example#123>|"));
		assertThat(TurfSerializer.serializeTagReference(new StringBuilder(), URI.create("https://example.com/fake/foo/bar/Example#foo:bar"), aliases).toString(),
				is("|<https://example.com/fake/foo/bar/Example#foo:bar>|"));
	}

	//#handles

	/** @see TurfTestResources#OK_OBJECT_HANDLE_RESOURCE_NAMES */
	@Test
	public void testObjectHandle() throws IOException {
		for(final String okObjectHandleResourceName : OK_OBJECT_HANDLE_RESOURCE_NAMES) {
			final UrfObject urfObject = new UrfObject(URI.create("https://urf.name/foo"));
			urfObject.setPropertyValueByHandle("example", "test");
			for(final boolean formatted : asList(false, true)) {
				final TurfSerializer serializer = new TurfSerializer();
				serializer.setFormatted(formatted);
				final String serialization = serializer.serializeDocument(urfObject);
				assertGraphsEqual(okObjectHandleResourceName, parse(serialization), parseTestResource(okObjectHandleResourceName));
			}
		}
	}

	/** @see TurfTestResources#OK_OBJECT_HANDLE_TYPE_RESOURCE_NAME */
	@Test
	public void testObjectHandleType() throws IOException {
		final UrfObject urfObject = new UrfObject(URI.create("https://urf.name/foo"), URI.create("https://urf.name/Bar"));
		urfObject.setPropertyValueByHandle("example", "test");
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_OBJECT_HANDLE_TYPE_RESOURCE_NAME, parse(serialization), parseTestResource(OK_OBJECT_HANDLE_TYPE_RESOURCE_NAME));
		}
	}

	//##potentially ambiguous handles

	/** @see TurfTestResources#OK_HANDLE_AMBIGUOUS_PROPERTY_RESOURCE_NAME */
	@Test
	public void testHandleAmbiguousProperty() throws IOException {
		final UrfObject urfObject = new UrfObject(URI.create("https://urf.name/foo"), URI.create("https://urf.name/Bar"));
		urfObject.setPropertyValueByHandle("true", "test");
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_HANDLE_AMBIGUOUS_PROPERTY_RESOURCE_NAME, parse(serialization), parseTestResource(OK_HANDLE_AMBIGUOUS_PROPERTY_RESOURCE_NAME));
		}
	}

	/** @see TurfTestResources#OK_HANDLE_AMBIGUOUS_TAG_RESOURCE_NAME */
	@Test
	public void testHandleAmbiguousTag() throws IOException {
		final UrfObject urfObject = new UrfObject(URI.create("https://urf.name/false"), URI.create("https://urf.name/Bar"));
		urfObject.setPropertyValueByHandle("example", "test");
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_HANDLE_AMBIGUOUS_TAG_RESOURCE_NAME, parse(serialization), parseTestResource(OK_HANDLE_AMBIGUOUS_TAG_RESOURCE_NAME));
		}
	}

	/** @see TurfTestResources#OK_HANDLE_AMBIGUOUS_TYPE_RESOURCE_NAME */
	@Test
	public void testHandleAmbiguousType() throws IOException {
		final UrfObject urfObject = new UrfObject(URI.create("https://urf.name/foo"), URI.create("https://urf.name/true"));
		urfObject.setPropertyValueByHandle("example", "test");
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_HANDLE_AMBIGUOUS_TYPE_RESOURCE_NAME, parse(serialization), parseTestResource(OK_HANDLE_AMBIGUOUS_TYPE_RESOURCE_NAME));
		}
	}

	/** @see TurfTestResources#OK_HANDLE_AMBIGUOUS_VALUE_RESOURCE_NAME */
	@Test
	public void testHandleAmbiguousValue() throws IOException {
		final UrfObject urfObject = new UrfObject(URI.create("https://urf.name/foo"), URI.create("https://urf.name/Bar"));
		urfObject.setPropertyValueByHandle("example", new UrfObject(URI.create("https://urf.name/false")));
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_HANDLE_AMBIGUOUS_VALUE_RESOURCE_NAME, parse(serialization), parseTestResource(OK_HANDLE_AMBIGUOUS_VALUE_RESOURCE_NAME));
		}
	}

	//TODO add serializer tests for IDs

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
		object1.setPropertyValueByHandle("info", "first");

		final UrfObject object2 = new UrfObject(URI.create("https://example.com/object2"));
		object2.setPropertyValueByHandle("info", "second");
		final UrfObject object2Stuff = new UrfObject(null, "Stuff");
		object2Stuff.setPropertyValueByHandle("test", 222);
		object2.setPropertyValueByHandle("extra", object2Stuff);

		final UrfObject object3 = new UrfObject(URI.create("https://example.com/object3"));
		object3.setPropertyValueByHandle("info", "third");
		final UrfObject object3Stuff = new UrfObject(null, URF.Handle.toTag("Stuff"));
		object3Stuff.setPropertyValueByHandle("test", 333);
		object3.setPropertyValueByHandle("extra", object3Stuff);

		final UrfObject object4 = new UrfObject();
		object4.setPropertyValueByHandle("info", "fourth");

		final UrfObject object5 = new UrfObject(URI.create("https://example.com/object5"), "foo-Bar");
		object5.setPropertyValueByHandle("info", "fifth");

		final UrfObject object6 = new UrfObject();
		object6.setPropertyValueByHandle("info", "fourth");

		final String string = "foobar";

		final UrfObject object7 = new UrfObject();
		object7.setPropertyValueByHandle("info", "eighth");

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
		assertThat(new TurfSerializer().serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example#foo"), null, false).toString(),
				is("Example#foo"));
		assertThat(new TurfSerializer().serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example#123"), null, false).toString(),
				is("Example#123"));
		//TODO add test for an ID that might be a valid name yet still need encoding
		assertThat(new TurfSerializer().registerNamespace(URI.create("https://example.com/fake/"), "fake")
				.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/fake/Example#foo"), null, false).toString(), is("fake/Example#foo"));

		//|"id"|*Type
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example#foo"), URI.create("https://urf.name/Example"), false).toString(),
				is("|\"foo\"|*Example"));
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example#123"), URI.create("https://urf.name/Example"), false).toString(),
				is("|\"123\"|*Example"));
		assertThat(
				new TurfSerializer().registerNamespace(URI.create("https://example.com/fake/"), "fake").serializeObjectReference(new StringBuilder(),
						URI.create("https://example.com/fake/Example#foo"), URI.create("https://example.com/fake/Example"), false).toString(),
				is("|\"foo\"|*fake/Example"));
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
		assertThat(new TurfSerializer().serializeObjectReference(new StringBuilder(), URI.create("https://urf.name/Example#foo:bar"), null, false).toString(),
				is("|<https://urf.name/Example#foo:bar>|")); //ID prevents a valid name, therefore there can be no handle
		assertThat(
				new TurfSerializer()
						.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test"), URI.create("https://urf.name/SomeType"), false).toString(),
				is("|<https://example.com/Test>|"));
		assertThat(
				new TurfSerializer()
						.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test"), URI.create("https://urf.name/SomeType"), true).toString(),
				is("|<https://example.com/Test>|*SomeType"));
		assertThat(new TurfSerializer()
				.serializeObjectReference(new StringBuilder(), URI.create("https://example.com/Test#foo"), URI.create("https://urf.name/SomeType"), true).toString(),
				is("|<https://example.com/Test#foo>|*SomeType"));
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

	//#short-hand property object descriptions

	/** @see TurfTestResources#OK_PROPERTY_OBJECT_DESCRIPTIONS_RESOURCE_NAMES */
	@Test
	public void testPropertyObjectDescriptions() throws IOException {
		for(final String okPropertyObjectDescriptionsResourceName : OK_PROPERTY_OBJECT_DESCRIPTIONS_RESOURCE_NAMES) {
			final UrfObject urfObject = new UrfObject();
			final UrfObject exampleObject = new UrfObject();
			exampleObject.setPropertyValueByHandle("foo", "bar");
			exampleObject.setPropertyValueByHandle("test", 123L);
			urfObject.setPropertyValueByHandle("example", exampleObject);
			final UrfObject otherObject = new UrfObject();
			otherObject.setPropertyValueByHandle("one", true);
			urfObject.setPropertyValueByHandle("other", otherObject);
			for(final boolean shortPropertyObjectDescriptions : asList(false, true)) {
				for(final boolean formatted : asList(false, true)) {
					final TurfSerializer serializer = new TurfSerializer();
					serializer.setShortPropertyObjectDescriptions(shortPropertyObjectDescriptions);
					serializer.setFormatted(formatted);
					final String serialization = serializer.serializeDocument(urfObject);
					assertGraphsEqual(okPropertyObjectDescriptionsResourceName, parse(serialization), parseTestResource(okPropertyObjectDescriptionsResourceName));
				}
			}
		}
	}

	//#n-ary properties

	/** @see TurfTestResources#OK_NARY_ONE_PROPERTY_ONE_VALUE_RESOURCE_NAME */
	@Test
	public void testOkNaryOnePropertyOneValue() throws IOException {
		final UrfObject urfObject = new UrfObject();
		urfObject.addPropertyValueByHandle("many+", "example");
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_NARY_ONE_PROPERTY_ONE_VALUE_RESOURCE_NAME, parse(serialization), parseTestResource(OK_NARY_ONE_PROPERTY_ONE_VALUE_RESOURCE_NAME));
		}
	}

	/** @see TurfTestResources#OK_NARY_ONE_PROPERTY_TWO_VALUES_RESOURCE_NAME */
	@Test
	public void testOkNaryOnePropertyTwoValues() throws IOException {
		final UrfObject urfObject = new UrfObject();
		urfObject.addPropertyValueByHandle("many+", "example");
		urfObject.addPropertyValueByHandle("many+", "test");
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_NARY_ONE_PROPERTY_TWO_VALUES_RESOURCE_NAME, parse(serialization), parseTestResource(OK_NARY_ONE_PROPERTY_TWO_VALUES_RESOURCE_NAME));
		}
	}

	/** @see TurfTestResources#OK_NARY_ONE_PROPERTY_THREE_VALUES_RESOURCE_NAME */
	@Test
	public void testOkNaryOnePropertyThreeValues() throws IOException {
		final UrfObject urfObject = new UrfObject();
		urfObject.addPropertyValueByHandle("many+", "example");
		urfObject.addPropertyValueByHandle("many+", "test");
		urfObject.addPropertyValueByHandle("many+", Year.of(1999));
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_NARY_ONE_PROPERTY_THREE_VALUES_RESOURCE_NAME, parse(serialization),
					parseTestResource(OK_NARY_ONE_PROPERTY_THREE_VALUES_RESOURCE_NAME));
		}
	}

	/** @see TurfTestResources#OK_NARY_TWO_PROPERTIES_RESOURCE_NAME */
	@Test
	public void testOkNaryTwoProperties() throws IOException {
		final UrfObject urfObject = new UrfObject();
		urfObject.addPropertyValueByHandle("many+", "example");
		urfObject.addPropertyValueByHandle("many+", "test");
		urfObject.addPropertyValueByHandle("many+", Year.of(1999));
		urfObject.addPropertyValueByHandle("nary+", "first");
		urfObject.addPropertyValueByHandle("nary+", "second");
		urfObject.addPropertyValueByHandle("nary+", "third");
		urfObject.addPropertyValueByHandle("nary+", "fourth");
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			assertGraphsEqual(OK_NARY_TWO_PROPERTIES_RESOURCE_NAME, parse(serialization), parseTestResource(OK_NARY_TWO_PROPERTIES_RESOURCE_NAME));
		}
	}

	/** @see TurfTestResources#OK_NARY_MIXED_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkNaryMixedProperties() throws IOException {
		final UrfObject urfObject = new UrfObject();
		urfObject.setPropertyValueByHandle("foo", "bar");
		urfObject.addPropertyValueByHandle("many+", "example");
		urfObject.addPropertyValueByHandle("many+", "test");
		urfObject.addPropertyValueByHandle("many+", Year.of(1999));
		urfObject.addPropertyValueByHandle("nary+", "first");
		urfObject.addPropertyValueByHandle("nary+", "second");
		urfObject.addPropertyValueByHandle("nary+", "third");
		urfObject.addPropertyValueByHandle("nary+", "fourth");
		for(final boolean formatted : asList(false, true)) {
			final TurfSerializer serializer = new TurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serializeDocument(urfObject);
			for(final String okNaryMixedPropertiesResourceName : OK_NARY_MIXED_PROPERTIES_RESOURCE_NAMES) {
				assertGraphsEqual(okNaryMixedPropertiesResourceName, parse(serialization), parseTestResource(okNaryMixedPropertiesResourceName));
			}
		}
	}

}
