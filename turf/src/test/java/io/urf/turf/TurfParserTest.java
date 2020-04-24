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
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.net.URI;
import java.time.Year;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.*;

import org.junit.jupiter.api.*;

import com.globalmentor.io.Filenames;
import com.globalmentor.net.ContentType;

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
	 * @implSpec The default implementation delegates to {@link #parseTestResource(String, ContentType)} using a media type based upon the resource extension.
	 * @param testResourceName The name of the TURF document resource for testing, relative to {@link TurfTestResources}.
	 * @return The list of TURF document roots parsed.
	 * @throws IllegalArgumentException if the test resource name has no filename extension or it is for an unrecognized type.
	 * @see TURF#CONTENT_TYPE
	 * @see TURF#PROPERTIES_CONTENT_TYPE
	 */
	protected List<Object> parseTestResource(@Nonnull final String testResourceName) throws IOException {
		final ContentType contentType = Filenames.findExtension(testResourceName).map(extension -> {
			switch(extension) {
				case TURF.FILENAME_EXTENSION:
					return TURF.CONTENT_TYPE;
				case TURF.PROPERTIES_FILENAME_EXTENSION:
					return TURF.PROPERTIES_CONTENT_TYPE;
				default:
					throw new IllegalArgumentException(String.format("Test resource name `%s` has unrecognized filename extension `%s`.", testResourceName, extension));
			}
		}).orElseThrow(() -> new IllegalArgumentException(String.format("Test resource name `%s` missing filename extension.", testResourceName)));
		return parseTestResource(testResourceName, contentType);
	}

	/**
	 * Loads and parses the indicated TURF document resource.
	 * @implSpec The default implementation delegates to {@link #parse(InputStream, ContentType)}.
	 * @param testResourceName The name of the TURF document resource for testing, relative to {@link TurfTestResources}.
	 * @param contentType The Internet media type of the TURF document being parsed, or <code>null</code> if the TURF variant should be automatically detected.
	 * @return The list of TURF document roots parsed.
	 */
	protected List<Object> parseTestResource(@Nonnull final String testResourceName, @Nullable final ContentType contentType) throws IOException {
		try (final InputStream inputStream = TurfTestResources.class.getResourceAsStream(testResourceName)) {
			return parse(inputStream, contentType);
		}
	}

	/**
	 * Loads and parses the indicated TURF document resource.
	 * @param inputStream The input stream containing the TURF document for testing.
	 * @param contentType The Internet media type of the TURF document being parsed, or <code>null</code> if the TURF variant should be automatically detected.
	 * @return The list of TURF document roots parsed.
	 */
	protected List<Object> parse(@Nonnull final InputStream inputStream, @Nullable final ContentType contentType) throws IOException {
		return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream, contentType);
	}

	//# handles

	/** @see TurfTestResources#OK_OBJECT_HANDLE_RESOURCE_NAMES */
	@Test
	public void testObjectHandle() throws IOException {
		for(final String okObjectHandleResourceName : OK_OBJECT_HANDLE_RESOURCE_NAMES) {
			final UrfObject urfObject = (UrfObject)parseTestResource(okObjectHandleResourceName).stream().findAny()
					.orElseThrow(() -> new AssertionError(okObjectHandleResourceName));
			assertThat(okObjectHandleResourceName, urfObject.getTag(), isPresentAndIs(URI.create("https://urf.name/foo")));
			assertThat(okObjectHandleResourceName, urfObject.getTypeTag(), not(isPresent()));
			assertThat(okObjectHandleResourceName, urfObject.findPropertyValueByHandle("example"), isPresentAndIs("test"));
		}
	}

	/** @see TurfTestResources#OK_OBJECT_HANDLE_TYPE_RESOURCE_NAME */
	@Test
	public void testObjectHandleType() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_OBJECT_HANDLE_TYPE_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);
		assertThat(urfObject.getTag(), isPresentAndIs(URI.create("https://urf.name/foo")));
		assertThat(urfObject.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/Bar")));
		assertThat(urfObject.findPropertyValueByHandle("example"), isPresentAndIs("test"));
	}

	//## potentially ambiguous handles

	/** @see TurfTestResources#OK_HANDLE_AMBIGUOUS_PROPERTY_RESOURCE_NAME */
	@Test
	public void testHandleAmbiguousProperty() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_HANDLE_AMBIGUOUS_PROPERTY_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);
		assertThat(urfObject.getTag(), isPresentAndIs(URI.create("https://urf.name/foo")));
		assertThat(urfObject.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/Bar")));
		assertThat(urfObject.findPropertyValueByHandle("true"), isPresentAndIs("test"));
	}

	/** @see TurfTestResources#OK_HANDLE_AMBIGUOUS_TAG_RESOURCE_NAME */
	@Test
	public void testHandleAmbiguousTag() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_HANDLE_AMBIGUOUS_TAG_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);
		assertThat(urfObject.getTag(), isPresentAndIs(URI.create("https://urf.name/false")));
		assertThat(urfObject.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/Bar")));
		assertThat(urfObject.findPropertyValueByHandle("example"), isPresentAndIs("test"));
	}

	/** @see TurfTestResources#OK_HANDLE_AMBIGUOUS_TYPE_RESOURCE_NAME */
	@Test
	public void testHandleAmbiguousType() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_HANDLE_AMBIGUOUS_TYPE_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);
		assertThat(urfObject.getTag(), isPresentAndIs(URI.create("https://urf.name/foo")));
		assertThat(urfObject.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/true")));
		assertThat(urfObject.findPropertyValueByHandle("example"), isPresentAndIs("test"));
	}

	/** @see TurfTestResources#OK_HANDLE_AMBIGUOUS_VALUE_RESOURCE_NAME */
	@Test
	public void testHandleAmbiguousValue() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_HANDLE_AMBIGUOUS_VALUE_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);
		assertThat(urfObject.getTag(), isPresentAndIs(URI.create("https://urf.name/foo")));
		assertThat(urfObject.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/Bar")));
		final UrfObject value = urfObject.findPropertyValueByHandle("example").map(UrfObject.class::cast).orElseThrow(AssertionError::new);
		assertThat(value.getTag(), isPresentAndIs(URI.create("https://urf.name/false")));
	}

	//TODO add bad tests that include both a tag label and a handle; a tag label and a handle; and an alias and a handle (if we continue not to support that) 

	//# IDs

	/** @see TurfTestResources#OK_IDS_RESOURCE_NAME */
	@Test
	public void testIds() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_IDS_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);

		final UrfObject asTag = (UrfObject)urfObject.findPropertyValueByHandle("asTag").orElseThrow(AssertionError::new);
		assertThat(asTag.getTag(), isPresentAndIs(URI.create("https://example.com/Foo#bar1")));
		assertThat(asTag.findPropertyValueByHandle("test"), isPresentAndIs("first"));

		final UrfObject asBareTag = (UrfObject)urfObject.findPropertyValueByHandle("asBareTag").orElseThrow(AssertionError::new);
		assertThat(asBareTag.getTag(), isPresentAndIs(URI.create("https://example.com/Foo#bar2")));
		assertThat(asBareTag.findPropertyValueByHandle("test"), isEmpty());

		final UrfObject asWithTypeTag = (UrfObject)urfObject.findPropertyValueByHandle("asTagWithTypeTag").orElseThrow(AssertionError::new);
		assertThat(asWithTypeTag.getTag(), isPresentAndIs(URI.create("https://example.com/Foo#bar3")));
		assertThat(asWithTypeTag.getTypeTag(), isPresentAndIs(URI.create("https://example.com/Foo")));
		assertThat(asWithTypeTag.findPropertyValueByHandle("test"), isPresentAndIs("third"));

		final UrfObject asLabelAndTag = (UrfObject)urfObject.findPropertyValueByHandle("asLabelAndTag").orElseThrow(AssertionError::new);
		assertThat(asLabelAndTag.getTag(), isPresentAndIs(URI.create("https://example.com/Foo#bar4")));
		assertThat(asLabelAndTag.getTypeTag(), isPresentAndIs(URI.create("https://example.com/Foo")));
		assertThat(asLabelAndTag.findPropertyValueByHandle("test"), isPresentAndIs("fourth"));

		final UrfObject asLabelAndHandle = (UrfObject)urfObject.findPropertyValueByHandle("asLabelAndHandle").orElseThrow(AssertionError::new);
		assertThat(asLabelAndHandle.getTag(), isPresentAndIs(URI.create("https://urf.name/Foo#bar5")));
		assertThat(asLabelAndHandle.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/Foo")));
		assertThat(asLabelAndHandle.findPropertyValueByHandle("test"), isPresentAndIs("fifth"));

		final UrfObject asLabelAndNamespaceHandle = (UrfObject)urfObject.findPropertyValueByHandle("asLabelAndNamespaceHandle").orElseThrow(AssertionError::new);
		assertThat(asLabelAndNamespaceHandle.getTag(), isPresentAndIs(URI.create("https://example.com/one/two/Foo#bar6")));
		assertThat(asLabelAndNamespaceHandle.getTypeTag(), isPresentAndIs(URI.create("https://example.com/one/two/Foo")));
		assertThat(asLabelAndNamespaceHandle.findPropertyValueByHandle("test"), isPresentAndIs("sixth"));

		final UrfObject asTagAndNamespaceHandle = (UrfObject)urfObject.findPropertyValueByHandle("asTagAndNamespaceHandle").orElseThrow(AssertionError::new);
		assertThat(asTagAndNamespaceHandle.getTag(), isPresentAndIs(URI.create("https://example.com/one/two/Foo#bar7")));
		assertThat(asTagAndNamespaceHandle.getTypeTag(), isPresentAndIs(URI.create("https://example.com/one/two/Foo")));
		assertThat(asTagAndNamespaceHandle.findPropertyValueByHandle("test"), isPresentAndIs("seventh"));

		final UrfObject asHandle = (UrfObject)urfObject.findPropertyValueByHandle("asHandle").orElseThrow(AssertionError::new);
		assertThat(asHandle.getTag(), isPresentAndIs(URI.create("https://urf.name/Foo#bar8")));
		assertThat(asHandle.findPropertyValueByHandle("test"), isPresentAndIs("eighth"));

		final UrfObject asNamespaceHandle = (UrfObject)urfObject.findPropertyValueByHandle("asNamespaceHandle").orElseThrow(AssertionError::new);
		assertThat(asNamespaceHandle.getTag(), isPresentAndIs(URI.create("https://example.com/one/two/Foo#bar9")));
		assertThat(asNamespaceHandle.findPropertyValueByHandle("test"), isPresentAndIs("ninth"));

	}

	//TODO add bad ID test with conflicting type, e.g. 	|<https://example.com/Foo#test>|*|<https://example.com/Bar>|

	//# namespaces

	/** @see TurfTestResources#OK_NAMESPACES_RESOURCE_NAMES */
	@Test
	public void testOkNamespaces() throws IOException {
		for(final String okNamespacesResourceName : OK_NAMESPACES_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okNamespacesResourceName).stream().findAny(); //TODO require no more than one resource
			final UrfObject urfObject = object.map(UrfObject.class::cast).orElseThrow(() -> new AssertionError(okNamespacesResourceName));
			assertThat(okNamespacesResourceName, urfObject.getPropertyCount(), is(5));
			assertThat(okNamespacesResourceName, urfObject.getTag(), isPresentAndIs(URI.create("https://example.com/foo.bar")));
			assertThat(okNamespacesResourceName, urfObject.findPropertyValue(URF.AD_HOC_NAMESPACE.resolve("foo")), isPresentAndIs("bar"));
			assertThat(okNamespacesResourceName, urfObject.findPropertyValue(URI.create("http://purl.org/dc/elements/1.1/title")),
					isPresentAndIs("An Example Resource"));
			assertThat(okNamespacesResourceName, urfObject.findPropertyValue(URI.create("http://purl.org/dc/elements/1.1/creator")), isPresentAndIs("Jane Doe"));
			assertThat(okNamespacesResourceName, urfObject.findPropertyValue(URI.create("http://purl.org/dc/elements/1.1/date")), isPresentAndIs(Year.of(2018)));
			final UrfObject maker = urfObject.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/maker")).map(UrfObject.class::cast)
					.orElseThrow(() -> new AssertionError(okNamespacesResourceName));
			assertThat(okNamespacesResourceName, maker.getTypeTag(), isPresentAndIs(URI.create("http://xmlns.com/foaf/0.1/Person")));
			assertThat(okNamespacesResourceName, maker.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/firstName")), isPresentAndIs("Jane"));
			assertThat(okNamespacesResourceName, maker.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/lastName")), isPresentAndIs("Doe"));
			assertThat(okNamespacesResourceName, maker.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/homepage")),
					isPresentAndIs(URI.create("https://janedoe.example.com/")));
		}
	}

	//# roots

	/** @see TurfTestResources#OK_ROOTS_WHITESPACE_RESOURCE_NAME */
	@Test
	public void testOkRoots() throws IOException {
		for(final String okRootResourceName : asList(OK_ROOTS_WHITESPACE_RESOURCE_NAME)) { //TODO add non-whitespace variation
			final List<Object> roots = parseTestResource(okRootResourceName);
			assertThat(okRootResourceName, roots, hasSize(8)); //TODO fix when references are brought back

			final UrfObject object1 = (UrfObject)roots.get(0);
			assertThat(okRootResourceName, object1.getTag(), isPresentAndIs(URI.create("https://example.com/object1")));
			assertThat(okRootResourceName, object1.findPropertyValueByHandle("info"), isPresentAndIs("first"));

			final UrfObject object2 = (UrfObject)roots.get(1);
			assertThat(okRootResourceName, object2.getTag(), isPresentAndIs(URI.create("https://example.com/object2")));
			assertThat(okRootResourceName, object2.findPropertyValueByHandle("info"), isPresentAndIs("second"));
			final UrfObject object2Stuff = object2.findPropertyValueByHandle("extra").map(UrfObject.class::cast)
					.orElseThrow(() -> new AssertionError(okRootResourceName));
			assertThat(okRootResourceName, object2Stuff.getTypeTag(), isPresentAndIs(URF.Handle.toTag("Stuff")));
			assertThat(okRootResourceName, object2Stuff.findPropertyValueByHandle("test"), isPresentAndIs(222L));

			final UrfObject object3 = (UrfObject)roots.get(2);
			assertThat(okRootResourceName, object3.getTag(), isPresentAndIs(URI.create("https://example.com/object3")));
			assertThat(okRootResourceName, object3.findPropertyValueByHandle("info"), isPresentAndIs("third"));
			final UrfObject object3Stuff = object3.findPropertyValueByHandle("extra").map(UrfObject.class::cast)
					.orElseThrow(() -> new AssertionError(okRootResourceName));
			assertThat(okRootResourceName, object3Stuff.getTypeTag(), isPresentAndIs(URF.Handle.toTag("Stuff")));
			assertThat(okRootResourceName, object3Stuff.findPropertyValueByHandle("test"), isPresentAndIs(333L));

			final UrfObject object4 = (UrfObject)roots.get(3);
			assertThat(okRootResourceName, object4.getTag(), isEmpty());
			assertThat(okRootResourceName, object4.findPropertyValueByHandle("info"), isPresentAndIs("fourth"));

			final UrfObject object5 = (UrfObject)roots.get(4);
			assertThat(okRootResourceName, object5.getTag(), isPresentAndIs(URI.create("https://example.com/object5")));
			assertThat(okRootResourceName, object5.getTypeTag(), isPresentAndIs(URF.Handle.toTag("foo-Bar")));
			assertThat(okRootResourceName, object5.findPropertyValueByHandle("info"), isPresentAndIs("fifth"));

			final UrfObject object6 = (UrfObject)roots.get(5);
			assertThat(okRootResourceName, object6.getTag(), isEmpty());
			assertThat(okRootResourceName, object6.findPropertyValueByHandle("info"), isPresentAndIs("fourth"));

			assertThat(okRootResourceName, roots.get(6), is("foobar"));

			final UrfObject object7 = (UrfObject)roots.get(7);
			assertThat(okRootResourceName, object7.getTag(), isEmpty());
			assertThat(okRootResourceName, object7.findPropertyValueByHandle("info"), isPresentAndIs("eighth"));
		}
	}

	//# properties files

	/**
	 * Tests parsing of TURF documents along with equivalent TURF Properties documents.
	 * @see TurfTestResources#OK_PROPERTIES_RESOURCE_NAMES
	 */
	@Test
	public void testOkProperties() throws IOException {
		for(final String okPropertiesResourceName : OK_PROPERTIES_RESOURCE_NAMES) {
			final UrfObject urfObject = (UrfObject)parseTestResource(okPropertiesResourceName).stream().findAny()
					.orElseThrow(() -> new AssertionError(okPropertiesResourceName));
			assertThat(urfObject.getTag(), isEmpty());
			assertThat(urfObject.getTypeTag(), isEmpty());
			assertThat(okPropertiesResourceName, urfObject.getPropertyCount(), is(4));
			assertThat(okPropertiesResourceName, urfObject.findPropertyValueByHandle("foo"), isPresentAndIs("bar"));
			assertThat(okPropertiesResourceName, urfObject.findPropertyValueByHandle("test"), isPresentAndIs(123L));
			assertThat(okPropertiesResourceName, urfObject.findPropertyValueByHandle("opposites"),
					isPresentAndIs(Stream.of(new SimpleImmutableEntry<>("in", "out"), new SimpleImmutableEntry<>("up", "down"))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
			assertThat(okPropertiesResourceName, urfObject.findPropertyValueByHandle("other"), isPresentAnd(instanceOf(UrfObject.class)));
			final UrfObject otherObject = urfObject.findPropertyValueByHandle("other").map(UrfObject.class::cast)
					.orElseThrow(() -> new AssertionError(okPropertiesResourceName));
			assertThat(okPropertiesResourceName, otherObject.findPropertyValueByHandle("one"), isPresentAndIs(true));
		}
	}

	/** @see TurfTestResources#OK_PROPERTIES_NAMESPACES_ALIASES_RESOURCE_NAME */
	@Test
	public void testOkPropertiesNamespacesAliases() throws IOException {
		final Optional<Object> object = parseTestResource(OK_PROPERTIES_NAMESPACES_ALIASES_RESOURCE_NAME).stream().findAny();
		final UrfObject urfObject = object.map(UrfObject.class::cast).orElseThrow(AssertionError::new);
		assertThat(urfObject.getTag(), isEmpty());
		assertThat(urfObject.getTypeTag(), isEmpty());
		assertThat(urfObject.getPropertyCount(), is(5));
		assertThat(urfObject.findPropertyValue(URF.AD_HOC_NAMESPACE.resolve("foo")), isPresentAndIs("bar"));
		assertThat(urfObject.findPropertyValue(URI.create("http://purl.org/dc/elements/1.1/title")), isPresentAndIs("An Example Resource"));
		assertThat(urfObject.findPropertyValue(URI.create("http://purl.org/dc/elements/1.1/creator")), isPresentAndIs("Jane Doe"));
		assertThat(urfObject.findPropertyValue(URI.create("http://purl.org/dc/elements/1.1/date")), isPresentAndIs(Year.of(2018)));
		final UrfObject maker = urfObject.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/maker")).map(UrfObject.class::cast)
				.orElseThrow(AssertionError::new);
		assertThat(maker.getTypeTag(), isPresentAndIs(URI.create("http://xmlns.com/foaf/0.1/Person")));
		assertThat(maker.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/firstName")), isPresentAndIs("Jane"));
		assertThat(maker.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/lastName")), isPresentAndIs("Doe"));
		assertThat(maker.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/homepage")), isPresentAndIs(URI.create("https://janedoe.example.com/")));
	}

	//# short-hand property object descriptions

	/** @see TurfTestResources#OK_PROPERTY_OBJECT_DESCRIPTIONS_RESOURCE_NAMES */
	@Test
	public void testPropertyObjectDescriptions() throws IOException {
		for(final String okPropertyObjectDescriptionsResourceName : OK_PROPERTY_OBJECT_DESCRIPTIONS_RESOURCE_NAMES) {
			final UrfObject urfObject = (UrfObject)parseTestResource(okPropertyObjectDescriptionsResourceName).stream().findAny()
					.orElseThrow(() -> new AssertionError(okPropertyObjectDescriptionsResourceName));
			assertThat(okPropertyObjectDescriptionsResourceName, urfObject.getPropertyCount(), is(2));
			assertThat(okPropertyObjectDescriptionsResourceName, urfObject.findPropertyValueByHandle("example"), isPresentAnd(instanceOf(UrfObject.class)));
			final UrfObject exampleObject = urfObject.findPropertyValueByHandle("example").map(UrfObject.class::cast)
					.orElseThrow(() -> new AssertionError(okPropertyObjectDescriptionsResourceName));
			assertThat(okPropertyObjectDescriptionsResourceName, exampleObject.findPropertyValueByHandle("foo"), isPresentAndIs("bar"));
			assertThat(okPropertyObjectDescriptionsResourceName, exampleObject.findPropertyValueByHandle("test"), isPresentAndIs(123L));
			assertThat(okPropertyObjectDescriptionsResourceName, urfObject.findPropertyValueByHandle("other"), isPresentAnd(instanceOf(UrfObject.class)));
			final UrfObject otherObject = urfObject.findPropertyValueByHandle("other").map(UrfObject.class::cast)
					.orElseThrow(() -> new AssertionError(okPropertyObjectDescriptionsResourceName));
			assertThat(okPropertyObjectDescriptionsResourceName, otherObject.findPropertyValueByHandle("one"), isPresentAndIs(true));
		}
	}

	//# n-ary properties

	/** @see TurfTestResources#OK_NARY_ONE_PROPERTY_ONE_VALUE_RESOURCE_NAME */
	@Test
	public void testOkNaryOnePropertyOneValue() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_NARY_ONE_PROPERTY_ONE_VALUE_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);
		assertThat(urfObject.getPropertyCount(), is(1));
		assertThat(urfObject.getPropertyValueCount(), is(1));
		assertThat(urfObject.getPropertyValuesByHandle("many+"), containsInAnyOrder("example"));
	}

	/** @see TurfTestResources#OK_NARY_ONE_PROPERTY_TWO_VALUES_RESOURCE_NAME */
	@Test
	public void testOkNaryOnePropertyTwoValues() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_NARY_ONE_PROPERTY_TWO_VALUES_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);
		assertThat(urfObject.getPropertyCount(), is(1));
		assertThat(urfObject.getPropertyValueCount(), is(2));
		assertThat(urfObject.getPropertyValuesByHandle("many+"), containsInAnyOrder("example", "test"));
	}

	/** @see TurfTestResources#OK_NARY_ONE_PROPERTY_THREE_VALUES_RESOURCE_NAME */
	@Test
	public void testOkNaryOnePropertyThreeValues() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_NARY_ONE_PROPERTY_THREE_VALUES_RESOURCE_NAME).stream().findAny()
				.orElseThrow(AssertionError::new);
		assertThat(urfObject.getPropertyCount(), is(1));
		assertThat(urfObject.getPropertyValueCount(), is(3));
		assertThat(urfObject.getPropertyValuesByHandle("many+"), containsInAnyOrder("example", "test", Year.of(1999)));
	}

	/** @see TurfTestResources#OK_NARY_TWO_PROPERTIES_RESOURCE_NAME */
	@Test
	public void testOkNaryTwoProperties() throws IOException {
		final UrfObject urfObject = (UrfObject)parseTestResource(OK_NARY_TWO_PROPERTIES_RESOURCE_NAME).stream().findAny().orElseThrow(AssertionError::new);
		assertThat(urfObject.getPropertyCount(), is(2));
		assertThat(urfObject.getPropertyValueCount(), is(7));
		assertThat(urfObject.getPropertyValuesByHandle("many+"), containsInAnyOrder("example", "test", Year.of(1999)));
		assertThat(urfObject.getPropertyValuesByHandle("nary+"), containsInAnyOrder("first", "second", "third", "fourth"));
	}

	/** @see TurfTestResources#OK_NARY_MIXED_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkNaryMixedProperties() throws IOException {
		for(final String okNamespacesResourceName : OK_NARY_MIXED_PROPERTIES_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okNamespacesResourceName).stream().findAny(); //TODO require no more than one resource
			final UrfObject urfObject = object.map(UrfObject.class::cast).orElseThrow(() -> new AssertionError(okNamespacesResourceName));
			assertThat(urfObject.getPropertyCount(), is(3));
			assertThat(urfObject.getPropertyValueCount(), is(8));
			assertThat(urfObject.findPropertyValueByHandle("foo"), isPresentAndIs("bar"));
			assertThat(urfObject.getPropertyValuesByHandle("many+"), containsInAnyOrder("example", "test", Year.of(1999)));
			assertThat(urfObject.getPropertyValuesByHandle("nary+"), containsInAnyOrder("first", "second", "third", "fourth"));
		}
	}

}
