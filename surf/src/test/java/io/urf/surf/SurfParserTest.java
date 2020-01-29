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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import javax.annotation.*;

import org.junit.Test;

import junit.framework.AssertionFailedError;

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
			final Object foo = root.getPropertyValue("foo").orElseThrow(AssertionFailedError::new);
			assertThat(foo, is(123L));
			final Optional<Object> numberAliased = surfParser.findResourceByAlias("number");
			assertThat(numberAliased, isPresentAnd(sameInstance(foo)));
			//|test|
			final Object value = root.getPropertyValue("value").orElseThrow(AssertionFailedError::new);
			assertThat(value, is(false));
			final Optional<Object> testAliased = surfParser.findResourceByAlias("test");
			assertThat(testAliased, isPresentAnd(sameInstance(value)));
			//|object|
			final SurfObject thing = (SurfObject)root.getPropertyValue("thing").orElseThrow(AssertionFailedError::new);
			assertThat(thing.getTypeHandle(), isPresentAndIs("example-Type"));
			final Optional<Object> objectAliased = surfParser.findResourceByAlias("object");
			assertThat(objectAliased, isPresentAnd(sameInstance(thing)));
			//|"foo"|*Bar
			final SurfObject foobar = (SurfObject)root.getPropertyValue("foobar").orElseThrow(AssertionFailedError::new);
			assertThat(foobar.getTypeHandle(), isPresentAndIs("Bar"));
			assertThat(foobar.getId(), isPresentAndIs("foo"));
			assertThat(foobar.getPropertyValue("prop"), isPresentAndIs("val"));
			//TODO circular references: assertThat(foobar.getPropertyValue("self"), isPresentAnd(sameInstance(foobar)));
			final Optional<SurfObject> foobarIded = surfParser.findObjectById("Bar", "foo");
			assertThat(foobarIded, isPresentAnd(sameInstance(foobar)));
			//list elements
			final List<?> stuff = (List<?>)thing.getPropertyValue("stuff").orElseThrow(AssertionFailedError::new);
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
			final Map<?, ?> map = (Map<?, ?>)root.getPropertyValue("map").orElseThrow(AssertionFailedError::new);
			//TODO circular references: assertThat(map.get(0), is(sameInstance(map))); //the map has itself for a value
			assertThat(map.get(1L), is("one"));
			assertThat(map.get(2L), is(sameInstance(numberAliased.get())));
			assertThat(map.get(4L), is(sameInstance(foobar)));
			assertThat(map.get(99L), is(sameInstance(exampleThingTagged.get())));
			assertThat(map.get(100L), is(sameInstance(objectAliased.get())));
			//set members
			@SuppressWarnings("unchecked")
			final Set<Object> set = (Set<Object>)root.getPropertyValue("set").orElseThrow(AssertionFailedError::new);
			assertThat(set, hasSize(5));
			assertThat(set, hasItem(123L));
			assertThat(set, hasItem(false));
			final Optional<Object> newAliased = surfParser.findResourceByAlias("newThing");
			final SurfObject newAliasedResource = (SurfObject)newAliased.orElseThrow(AssertionFailedError::new);
			assertThat(getTypeHandle(newAliasedResource), isPresentAndIs("example-Thing"));
			assertThat(getPropertyValue(newAliasedResource, "description"), isPresentAndIs("a new thing"));
			final Optional<Object> anotherAliased = surfParser.findResourceByAlias("another");
			final SurfObject anotherAliasedResource = (SurfObject)anotherAliased.orElseThrow(AssertionFailedError::new);
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
