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
import static io.urf.surf.SurfTestResources.OK_LABELS_RESOURCE_NAME;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import org.junit.Test;

import io.urf.URF;
import io.urf.model.*;
import io.urf.surf.AbstractSimpleGraphSurfParserTest;
import io.urf.surf.SurfTestResources;
import junit.framework.AssertionFailedError;

/**
 * Test of parsing SURF documents with {@link TurfParser} into a simple graph using {@link SimpleGraphUrfProcessor}.
 * 
 * @author Garret Wilson
 */
public class TurfParserSurfTest extends AbstractSimpleGraphSurfParserTest<UrfObject> {

	@Override
	protected Optional<Object> parseTestResource(InputStream inputStream) throws IOException {
		return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().findAny(); //TODO require at most one
	}

	@Override
	protected Class<UrfObject> getSurfObjectClass() {
		return UrfObject.class;
	}

	@Override
	public Optional<String> getTypeHandle(final UrfObject urfObject) {
		return urfObject.getTypeTag().flatMap(URF.Handle::findFromTag);
	}

	@Override
	protected int getPropertyCount(final UrfObject urfObject) {
		return urfObject.getPropertyCount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getPropertyValue(final UrfObject urfObject, final String propertyHandle) {
		return (Optional<T>)urfObject.findPropertyValueByHandle(propertyHandle);
	}

	//#labels

	/** @see SurfTestResources#OK_LABELS_RESOURCE_NAME */
	@Test
	public void testOkLabels() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_LABELS_RESOURCE_NAME)) {
			final SimpleGraphUrfProcessor processor = new SimpleGraphUrfProcessor();
			final TurfParser<List<Object>> parser = new TurfParser<>(processor);
			final UrfObject root = (UrfObject)parser.parseDocument(inputStream).stream().findAny().get(); //TODO require at most one
			//|root|
			final Optional<Object> rootAliased = processor
					.findDeclaredObject(parser.findResourceByAlias("root").map(UrfReference.class::cast).flatMap(UrfReference::getTag).get());
			assertThat(rootAliased, isPresentAnd(sameInstance(root)));
			//TODO circular references: assertThat(root.getPropertyValue("self"), isPresentAnd(sameInstance(root)));
			//|number|
			final Object foo = root.findPropertyValueByHandle("foo").orElseThrow(AssertionFailedError::new);
			assertThat(foo, is(123L));

			final Optional<Object> numberAliased = parser.findResourceByAlias("number").map(ObjectUrfResource::unwrap);
			assertThat(numberAliased, isPresentAnd(sameInstance(foo)));
			//|test|
			final Object value = root.findPropertyValueByHandle("value").orElseThrow(AssertionFailedError::new);
			assertThat(value, is(false));
			final Optional<Object> testAliased = parser.findResourceByAlias("test").map(ObjectUrfResource::unwrap);
			assertThat(testAliased, isPresentAnd(sameInstance(value)));
			//|object|
			final UrfObject thing = (UrfObject)root.findPropertyValueByHandle("thing").orElseThrow(AssertionFailedError::new);
			assertThat(thing.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/example/Type")));
			final Optional<Object> objectAliased = processor
					.findDeclaredObject(parser.findResourceByAlias("object").map(UrfReference.class::cast).flatMap(UrfReference::getTag).get());
			assertThat(objectAliased, isPresentAnd(sameInstance(thing)));
			//|"foo"|*Bar
			final UrfObject foobar = (UrfObject)root.findPropertyValueByHandle("foobar").orElseThrow(AssertionFailedError::new);
			assertThat(foobar.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/Bar")));
			assertThat(foobar.getId(), isPresentAndIs("foo"));
			assertThat(foobar.findPropertyValueByHandle("prop"), isPresentAndIs("val"));
			//TODO circular references: assertThat(foobar.getPropertyValue("self"), isPresentAnd(sameInstance(foobar)));
			final Optional<Object> foobarIded = processor.findDeclaredObjectByTypeId("Bar", "foo");
			assertThat(foobarIded, isPresentAnd(sameInstance(foobar)));
			//list elements
			final List<?> stuff = (List<?>)thing.findPropertyValueByHandle("stuff").orElseThrow(AssertionFailedError::new);
			assertThat(stuff, hasSize(4));
			assertThat(stuff.get(0), is("one"));
			assertThat(stuff.get(1), is(123L));
			assertThat(numberAliased, isPresentAnd(sameInstance(stuff.get(1))));
			assertThat(stuff.get(2), is("three"));
			final Object stuffElement4 = stuff.get(3);
			assertThat(stuffElement4, instanceOf(getSurfObjectClass()));
			final UrfObject exampleThing = (UrfObject)stuffElement4;
			assertThat(exampleThing.getTypeTag(), isPresentAndIs(URI.create("https://urf.name/example/Thing")));
			assertThat(exampleThing.getTag(), isPresentAndIs(URI.create("http://example.com/thing")));
			assertThat(exampleThing.findPropertyValueByHandle("name"), isPresentAndIs("Example Thing"));
			final Optional<Object> exampleThingTagged = processor.findDeclaredObject(URI.create("http://example.com/thing"));
			assertThat(exampleThingTagged, isPresentAnd(sameInstance(exampleThing)));
			//map values
			final Map<?, ?> map = (Map<?, ?>)root.findPropertyValueByHandle("map").orElseThrow(AssertionFailedError::new);
			//TODO circular references: assertThat(map.get(0), is(sameInstance(map))); //the map has itself for a value
			assertThat(map.get(1L), is("one"));
			assertThat(map.get(2L), is(sameInstance(numberAliased.get())));
			assertThat(map.get(4L), is(sameInstance(foobar)));
			assertThat(map.get(99L), is(sameInstance(exampleThingTagged.get())));
			assertThat(map.get(100L), is(sameInstance(objectAliased.get())));
			//set members
			@SuppressWarnings("unchecked")
			final Set<Object> set = (Set<Object>)root.findPropertyValueByHandle("set").orElseThrow(AssertionFailedError::new);
			assertThat(set, hasSize(5));
			assertThat(set, hasItem(123L));
			assertThat(set, hasItem(false));
			final Optional<Object> newAliased = processor
					.findDeclaredObject(parser.findResourceByAlias("newThing").map(UrfReference.class::cast).flatMap(UrfReference::getTag).get());
			final UrfObject newAliasedResource = (UrfObject)newAliased.orElseThrow(AssertionFailedError::new);
			assertThat(getTypeHandle(newAliasedResource), isPresentAndIs("example-Thing"));
			assertThat(getPropertyValue(newAliasedResource, "description"), isPresentAndIs("a new thing"));
			final Optional<Object> anotherAliased = processor
					.findDeclaredObject(parser.findResourceByAlias("another").map(UrfReference.class::cast).flatMap(UrfReference::getTag).get());
			final UrfObject anotherAliasedResource = (UrfObject)anotherAliased.orElseThrow(AssertionFailedError::new);
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
