/*
 * Copyright © 2016-2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.surf;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;

import static io.urf.surf.SurfTestResources.OK_LABELS_RESOURCE_NAME;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import javax.annotation.*;

import org.junit.jupiter.api.*;

import com.globalmentor.io.ParseUnexpectedDataException;
import com.globalmentor.io.function.*;

/**
 * Tests of {@link SurfParser}.
 * @author Garret Wilson
 */
public class SurfParserTest extends AbstractSimpleGraphSurfParserTest<SurfObject> {

	/**
	 * {@inheritDoc}
	 * @see SurfParser#parse(InputStream)
	 */
	@Override
	protected Optional<Object> parseTestResource(@Nonnull final InputStream inputStream) throws IOException {
		return new SurfParser().parse(inputStream);
	}

	@Override
	protected Class<SurfObject> getSurfObjectClass() {
		return SurfObject.class;
	}

	@Override
	public Optional<String> getTypeHandle(final SurfObject surfObject) {
		return surfObject.getTypeHandle();
	}

	@Override
	protected int getPropertyCount(final SurfObject surfObject) {
		return surfObject.getPropertyCount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getPropertyValue(final SurfObject surfObject, final String propertyHandle) {
		return (Optional<T>)surfObject.getPropertyValue(propertyHandle);
	}

	//parser

	/**
	 * Invokes the {@link SurfParser#parseSequence(Reader, char, IOConsumer)} for testing.
	 * @param <T> The type of sequence item being parsed.
	 * @param input Test data to be parsed.
	 * @param sequenceEnd The character expected to end the sequence.
	 * @param itemParser The strategy for parsing each item in the sequence.
	 * @return A list of items parsed from the sequence.
	 * @throws IOException if there was some error reading the input.
	 */
	protected static <T> List<T> parseSequence(@Nonnull final String input, final char sequenceEnd, @Nonnull final IOFunction<Reader, T> itemParser)
			throws IOException {
		final List<T> items = new ArrayList<T>();
		SurfParser.parseSequence(new StringReader(input), sequenceEnd, reader -> items.add(itemParser.apply(reader)));
		return items;
	}

	@Test
	public void testParseSequence() throws IOException {
		assertThat(parseSequence("true}", '}', SurfParser::parseBoolean), contains(true));
		assertThat(parseSequence("true, false}", '}', SurfParser::parseBoolean), contains(true, false));
		assertThat(parseSequence("true ", '}', SurfParser::parseBoolean), contains(true));
		assertThat(parseSequence("true\n", '}', SurfParser::parseBoolean), contains(true));
		assertThat(parseSequence("true, false", '}', SurfParser::parseBoolean), contains(true, false));
		assertThat(parseSequence("true false}", '}', SurfParser::parseBoolean), contains(true)); //trailing data ignored
		assertThat(parseSequence("123, 234, 345}", '}', SurfParser::parseNumber), contains(123L, 234L, 345L));
		assertThat(parseSequence("123,234,345}", '}', SurfParser::parseNumber), contains(123L, 234L, 345L));
		assertThrows(ParseUnexpectedDataException.class, () -> parseSequence("123,,234,345}", '}', SurfParser::parseNumber));
		assertThrows(ParseUnexpectedDataException.class, () -> parseSequence("123\n,\n\t\n,234,345}", '}', SurfParser::parseNumber));
		assertThrows(ParseUnexpectedDataException.class, () -> parseSequence("123,234,\n\n\n,345}", '}', SurfParser::parseNumber));
		assertThat(parseSequence("123\n234\n345}", '}', SurfParser::parseNumber), contains(123L, 234L, 345L));
		assertThat(parseSequence("123,234\n345}", '}', SurfParser::parseNumber), contains(123L, 234L, 345L));
		assertThat(parseSequence("123\n234,345}", '}', SurfParser::parseNumber), contains(123L, 234L, 345L));
		assertThat(parseSequence("123\n\n\n234  \n\n \t\n345}", '}', SurfParser::parseNumber), contains(123L, 234L, 345L));
		assertThat(parseSequence("123\n\t, \n 234 \n,345}", '}', SurfParser::parseNumber), contains(123L, 234L, 345L));
	}

	/** @see SurfParser#skipSequenceDelimiters(Reader) */
	@Test
	public void testSkipSequenceDelimiters() throws IOException {
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("")), isEmpty());
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("   ")), isEmpty());
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("\t")), isEmpty());
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("!foo")), isEmpty());
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("!foo,")), isEmpty()); //line comment hides comma
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("\t !foo")), isEmpty());
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("foo")), isEmpty());
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("foo !bar")), isEmpty());
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("\n")), isPresentAndIs(false));
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("   \n")), isPresentAndIs(false));
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("\t\n")), isPresentAndIs(false));
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("!foo\n")), isPresentAndIs(false));
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("\t!foo\n")), isPresentAndIs(false));
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader(",")), isPresentAndIs(true));
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("   ,")), isPresentAndIs(true));
		assertThat(SurfParser.skipSequenceDelimiters(new StringReader("\t,")), isPresentAndIs(true));
	}

	//documents

	//#labels

	/** @see SurfTestResources#OK_LABELS_RESOURCE_NAME */
	@Test
	public void testOkLabels() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_LABELS_RESOURCE_NAME)) {
			final SurfParser surfParser = new SurfParser();
			final SurfObject root = (SurfObject)surfParser.parse(inputStream).get();
			//|root|
			final Optional<Object> rootAliased = surfParser.findResourceByAlias("root");
			assertThat(rootAliased, isPresentAnd(sameInstance(root)));
			//TODO circular references: assertThat(root.getPropertyValue("self"), isPresentAnd(sameInstance(root)));
			//|number|
			final Object foo = root.getPropertyValue("foo").orElseThrow(AssertionError::new);
			assertThat(foo, is(123L));
			final Optional<Object> numberAliased = surfParser.findResourceByAlias("number");
			assertThat(numberAliased, isPresentAnd(sameInstance(foo)));
			//|test|
			final Object value = root.getPropertyValue("value").orElseThrow(AssertionError::new);
			assertThat(value, is(false));
			final Optional<Object> testAliased = surfParser.findResourceByAlias("test");
			assertThat(testAliased, isPresentAnd(sameInstance(value)));
			//|object|
			final SurfObject thing = (SurfObject)root.getPropertyValue("thing").orElseThrow(AssertionError::new);
			assertThat(thing.getTypeHandle(), isPresentAndIs("example-Type"));
			final Optional<Object> objectAliased = surfParser.findResourceByAlias("object");
			assertThat(objectAliased, isPresentAnd(sameInstance(thing)));
			//|"foo"|*Bar
			final SurfObject foobar = (SurfObject)root.getPropertyValue("foobar").orElseThrow(AssertionError::new);
			assertThat(foobar.getTypeHandle(), isPresentAndIs("Bar"));
			assertThat(foobar.getId(), isPresentAndIs("foo"));
			assertThat(foobar.getPropertyValue("prop"), isPresentAndIs("val"));
			//TODO circular references: assertThat(foobar.getPropertyValue("self"), isPresentAnd(sameInstance(foobar)));
			final Optional<SurfObject> foobarIded = surfParser.findObjectById("Bar", "foo");
			assertThat(foobarIded, isPresentAnd(sameInstance(foobar)));
			//list elements
			final List<?> stuff = (List<?>)thing.getPropertyValue("stuff").orElseThrow(AssertionError::new);
			assertThat(stuff, hasSize(4));
			assertThat(stuff.get(0), is("one"));
			assertThat(stuff.get(1), is(123L));
			assertThat(numberAliased, isPresentAnd(sameInstance(stuff.get(1))));
			assertThat(stuff.get(2), is("three"));
			final Object stuffElement4 = stuff.get(3);
			assertThat(stuffElement4, instanceOf(getSurfObjectClass()));
			final SurfObject exampleThing = (SurfObject)stuffElement4;
			assertThat(exampleThing.getTypeHandle(), isPresentAndIs("example-Thing"));
			assertThat(exampleThing.getTag(), isPresentAndIs(URI.create("http://example.com/thing")));
			assertThat(exampleThing.getPropertyValue("name"), isPresentAndIs("Example Thing"));
			final Optional<SurfObject> exampleThingTagged = surfParser.findObjectByTag(URI.create("http://example.com/thing"));
			assertThat(exampleThingTagged, isPresentAnd(sameInstance(exampleThing)));
			//map values
			final Map<?, ?> map = (Map<?, ?>)root.getPropertyValue("map").orElseThrow(AssertionError::new);
			//TODO circular references: assertThat(map.get(0), is(sameInstance(map))); //the map has itself for a value
			assertThat(map.get(1L), is("one"));
			assertThat(map.get(2L), is(sameInstance(numberAliased.get())));
			assertThat(map.get(4L), is(sameInstance(foobar)));
			assertThat(map.get(99L), is(sameInstance(exampleThingTagged.get())));
			assertThat(map.get(100L), is(sameInstance(objectAliased.get())));
			//set members
			@SuppressWarnings("unchecked")
			final Set<Object> set = (Set<Object>)root.getPropertyValue("set").orElseThrow(AssertionError::new);
			assertThat(set, hasSize(5));
			assertThat(set, hasItem(123L));
			assertThat(set, hasItem(false));
			final Optional<Object> newAliased = surfParser.findResourceByAlias("newThing");
			final SurfObject newAliasedResource = (SurfObject)newAliased.orElseThrow(AssertionError::new);
			assertThat(getTypeHandle(newAliasedResource), isPresentAndIs("example-Thing"));
			assertThat(getPropertyValue(newAliasedResource, "description"), isPresentAndIs("a new thing"));
			final Optional<Object> anotherAliased = surfParser.findResourceByAlias("another");
			final SurfObject anotherAliasedResource = (SurfObject)anotherAliased.orElseThrow(AssertionError::new);
			assertThat(getPropertyValue(anotherAliasedResource, "description"), isPresentAndIs("yet another thing"));
			assertThat(set, hasItem(sameInstance(numberAliased.get())));
			assertThat(set, hasItem(sameInstance(testAliased.get())));
			//TODO circular references: assertThat(set, hasItem(sameInstance(root))); //the set contains the root
			assertThat(set, hasItem(sameInstance(objectAliased.get())));
			assertThat(set, hasItem(sameInstance(newAliasedResource)));
			assertThat(set, hasItem(sameInstance(anotherAliasedResource)));
			//TODO circular references: assertThat(set, hasItem(sameInstance(set))); //the set contains itself
		}
	}

}
