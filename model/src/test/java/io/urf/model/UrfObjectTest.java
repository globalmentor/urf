/*
 * Copyright © 2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.model;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.time.*;
import java.util.*;

import org.junit.*;

import io.urf.URF.Handle;

/**
 * Tests of {@link UrfObject}.
 * @author Garret Wilson
 */
public class UrfObjectTest {

	private static final URI TEST_PROPERTY_TAG = Handle.toTag("test");

	private static final URI TEST_MANY_PROPERTY_TAG = Handle.toTag("test+");

	@Test
	public void testGetPropertyValue() {
		final UrfObject urfObject = new UrfObject();
		urfObject.setPropertyValue(URI.create("https://example/joined"), LocalDate.of(2016, Month.JANUARY, 23));

		final Optional<Object> optionalJoined = urfObject.findPropertyValue(URI.create("https://example/joined"));
		assertThat(optionalJoined, isPresentAndIs(LocalDate.parse("2016-01-23")));
	}

	@Test
	public void testGetPropertyValueHandle() {
		final UrfObject urfObject = new UrfObject();
		urfObject.setPropertyValueByHandle("joined", LocalDate.of(2016, Month.JANUARY, 23));

		final Optional<Object> optionalJoined = urfObject.findPropertyValueByHandle("joined");
		assertThat(optionalJoined, isPresentAndIs(LocalDate.parse("2016-01-23")));
	}

	/**
	 * Tests adding and retrieving binary and n-ary properties using add/get methods.
	 * @see UrfObject#addPropertyValue(URI, Object)
	 * @see UrfObject#getPropertyValues(URI)
	 */
	@Test
	public void testAddGetNaryPropertyValues() {
		final UrfObject urfObject = new UrfObject();

		urfObject.addPropertyValue(TEST_MANY_PROPERTY_TAG, "foo");
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo"));

		urfObject.setPropertyValue(TEST_PROPERTY_TAG, "single");
		assertThat(urfObject.getPropertyValues(TEST_PROPERTY_TAG), containsInAnyOrder("single"));
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo"));

		urfObject.addPropertyValue(TEST_MANY_PROPERTY_TAG, "bar");
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo", "bar"));
		assertThat(urfObject.getPropertyValues(TEST_PROPERTY_TAG), containsInAnyOrder("single"));

		urfObject.addPropertyValue(TEST_MANY_PROPERTY_TAG, "baz");
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo", "bar", "baz"));
		assertThat(urfObject.getPropertyValues(TEST_PROPERTY_TAG), containsInAnyOrder("single"));

		assertThat(urfObject.getPropertyCount(), is(2));
		assertThat(urfObject.getPropertyValueCount(), is(4));
	}

	/**
	 * Tests setting and retrieving a n-ary property using normal set/find methods.
	 * @see UrfObject#setPropertyValue(URI, Object)
	 * @see UrfObject#findPropertyValue(URI)
	 */
	@Test
	public void testSetPropertyValue() {
		final UrfObject urfObject = new UrfObject();
		urfObject.setPropertyValue(TEST_MANY_PROPERTY_TAG, "foo");
		assertThat(urfObject.findPropertyValue(TEST_MANY_PROPERTY_TAG), isPresentAndIs("foo"));

		urfObject.setPropertyValue(TEST_PROPERTY_TAG, "single");
		assertThat(urfObject.findPropertyValue(TEST_PROPERTY_TAG), isPresentAndIs("single"));

		assertThat(urfObject.getPropertyCount(), is(2));
		assertThat(urfObject.getPropertyValueCount(), is(2));
		urfObject.addPropertyValue(TEST_MANY_PROPERTY_TAG, "bar");
		assertThat(urfObject.getPropertyCount(), is(2));
		assertThat(urfObject.getPropertyValueCount(), is(3));
		assertThat(urfObject.findPropertyValue(TEST_PROPERTY_TAG), isPresentAndIs("single"));

		urfObject.setPropertyValue(TEST_MANY_PROPERTY_TAG, "example");
		assertThat(urfObject.findPropertyValue(TEST_MANY_PROPERTY_TAG), isPresentAndIs("example"));
		assertThat(urfObject.getPropertyCount(), is(2));
		assertThat(urfObject.getPropertyValueCount(), is(2));
		assertThat(urfObject.findPropertyValue(TEST_PROPERTY_TAG), isPresentAndIs("single"));
	}

	/**
	 * Tests that trying to add two binary property values will throw an exception.
	 * @see UrfObject#addPropertyValue(URI, Object)
	 */
	@Test(expected = IllegalStateException.class)
	public void testAddTwoBinaryPropertyValues() {
		final UrfObject urfObject = new UrfObject();
		urfObject.addPropertyValue(TEST_PROPERTY_TAG, "foo");
		urfObject.addPropertyValue(TEST_PROPERTY_TAG, "bar");
	}

	/**
	 * Tests merging binary and n-ary properties.
	 * @see UrfObject#mergePropertyValue(URI, Object)
	 * @see UrfObject#getPropertyValues(URI)
	 */
	@Test
	public void testMergePropertyValue() {
		final UrfObject urfObject = new UrfObject();

		urfObject.mergePropertyValue(TEST_MANY_PROPERTY_TAG, "foo");
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo"));

		urfObject.mergePropertyValue(TEST_PROPERTY_TAG, "single");
		assertThat(urfObject.getPropertyValues(TEST_PROPERTY_TAG), containsInAnyOrder("single"));
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo"));

		urfObject.mergePropertyValue(TEST_MANY_PROPERTY_TAG, "bar");
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo", "bar"));
		assertThat(urfObject.getPropertyValues(TEST_PROPERTY_TAG), containsInAnyOrder("single"));

		urfObject.mergePropertyValue(TEST_PROPERTY_TAG, "uno");
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo", "bar"));
		assertThat(urfObject.getPropertyValues(TEST_PROPERTY_TAG), containsInAnyOrder("uno"));

		urfObject.mergePropertyValue(TEST_MANY_PROPERTY_TAG, "baz");
		assertThat(urfObject.getPropertyValues(TEST_MANY_PROPERTY_TAG), containsInAnyOrder("foo", "bar", "baz"));
		assertThat(urfObject.getPropertyValues(TEST_PROPERTY_TAG), containsInAnyOrder("uno"));

		assertThat(urfObject.getPropertyCount(), is(2));
		assertThat(urfObject.getPropertyValueCount(), is(4));
	}

}
