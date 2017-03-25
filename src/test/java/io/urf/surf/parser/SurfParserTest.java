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

package io.urf.surf.parser;

import static org.junit.Assert.*;

import java.io.*;
import java.util.List;
import java.util.Optional;

import static io.urf.surf.test.SurfTestResources.*;
import static org.hamcrest.Matchers.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import org.junit.*;

import io.urf.surf.test.SurfTestResources;

/**
 * Tests of {@link SurfParser}.
 * @author Garret Wilson
 */
public class SurfParserTest {

	//simple files

	/** @see SurfTestResources#OK_SIMPLE_RESOURCE_NAMES */
	@Test
	public void testOkSimpleResources() throws IOException {
		for(final String okSimpleResourceName : OK_SIMPLE_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okSimpleResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okSimpleResourceName, object, isPresent());
				assertThat(okSimpleResourceName, object, hasValue(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)object.get();
				assertThat(okSimpleResourceName, resource.getTypeName(), not(isPresent()));
				assertThat(okSimpleResourceName, resource.getPropertyCount(), is(0));
			}
		}
	}

	//objects

	/** @see SurfTestResources#OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkObjectNoProperties() throws IOException {
		for(final String okObjectNoPropertiesResourceName : OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okObjectNoPropertiesResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okObjectNoPropertiesResourceName, object, isPresent());
				assertThat(okObjectNoPropertiesResourceName, object, hasValue(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)object.get();
				assertThat(okObjectNoPropertiesResourceName, resource.getTypeName(), not(isPresent()));
				assertThat(okObjectNoPropertiesResourceName, resource.getPropertyCount(), is(0));
			}
		}
	}

	/** @see SurfTestResources#OK_OBJECT_ONE_PROPERTY_RESOURCE_NAMES */
	@Test
	public void testOkObjectOneProperty() throws IOException {
		for(final String okObjectOnePropertyResourceName : OK_OBJECT_ONE_PROPERTY_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okObjectOnePropertyResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okObjectOnePropertyResourceName, object, isPresent());
				assertThat(okObjectOnePropertyResourceName, object, hasValue(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)object.get();
				assertThat(okObjectOnePropertyResourceName, resource.getTypeName(), not(isPresent()));
				assertThat(okObjectOnePropertyResourceName, resource.getPropertyCount(), is(1));
				assertThat(okObjectOnePropertyResourceName, resource.getPropertyValue("one"), isPresent());
				assertThat(okObjectOnePropertyResourceName, resource.getPropertyValue("one"), hasValue("one"));
				assertThat(okObjectOnePropertyResourceName, resource.getPropertyValue("two"), not(isPresent()));
			}
		}
	}

	/** @see SurfTestResources#OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkObjectTwoProperties() throws IOException {
		for(final String okObjectTwoPropertiesResourceName : OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okObjectTwoPropertiesResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okObjectTwoPropertiesResourceName, object, isPresent());
				assertThat(okObjectTwoPropertiesResourceName, object, hasValue(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)object.get();
				assertThat(okObjectTwoPropertiesResourceName, resource.getTypeName(), not(isPresent()));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyCount(), is(2));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("one"), isPresent());
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("one"), hasValue("one"));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("two"), isPresent());
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("two"), hasValue("two"));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("three"), not(isPresent()));
			}
		}
	}

	//lists

	/** @see SurfTestResources#OK_LIST_NO_ITEMS_RESOURCE_NAMES */
	@Test
	public void testOkListNoItems() throws IOException {
		for(final String okListNoItemsResourceName : OK_LIST_NO_ITEMS_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okListNoItemsResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okListNoItemsResourceName, object, isPresent());
				assertThat(okListNoItemsResourceName, object, hasValue(instanceOf(List.class)));
				final List<?> list = (List<?>)object.get();
				assertThat(okListNoItemsResourceName, list, hasSize(0));
			}
		}
	}

	/** @see SurfTestResources#OK_LIST_ONE_ITEM_RESOURCE_NAMES */
	@Test
	public void testOkListOneItem() throws IOException {
		for(final String okListOneItemResourceName : OK_LIST_ONE_ITEM_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okListOneItemResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okListOneItemResourceName, object, isPresent());
				assertThat(okListOneItemResourceName, object, hasValue(instanceOf(List.class)));
				final List<?> list = (List<?>)object.get();
				assertThat(okListOneItemResourceName, list, hasSize(1));
				assertThat(okListOneItemResourceName, list.get(0), is("one"));
			}
		}
	}

	/** @see SurfTestResources#OK_LIST_TWO_ITEMS_RESOURCE_NAMES */
	@Test
	public void testOkListTwoItems() throws IOException {
		for(final String okListTwoItemsResourceName : OK_LIST_TWO_ITEMS_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okListTwoItemsResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okListTwoItemsResourceName, object, isPresent());
				assertThat(okListTwoItemsResourceName, object, hasValue(instanceOf(List.class)));
				final List<?> list = (List<?>)object.get();
				assertThat(okListTwoItemsResourceName, list, hasSize(2));
				assertThat(okListTwoItemsResourceName, list.get(0), is("one"));
				assertThat(okListTwoItemsResourceName, list.get(1), is("two"));
			}
		}
	}

	//TODO create tests for bad properties, such as double list item separators

	//boolean

	/** @see SurfTestResources#OK_BOOLEAN_FALSE_RESOURCE_NAME */
	@Test
	public void testOkBooleanFalse() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_BOOLEAN_FALSE_RESOURCE_NAME)) {
			final Optional<Object> object = new SurfParser().parse(inputStream);
			assertThat(object, isPresent());
			assertThat(object, hasValue(Boolean.FALSE));
		}
	}

	/** @see SurfTestResources#OK_BOOLEAN_TRUE_RESOURCE_NAME */
	@Test
	public void testOkBooleanTrue() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_BOOLEAN_TRUE_RESOURCE_NAME)) {
			final Optional<Object> object = new SurfParser().parse(inputStream);
			assertThat(object, isPresent());
			assertThat(object, hasValue(Boolean.TRUE));
		}
	}

	//strings

	/** @see SurfTestResources#OK_STRING_FOOBAR_RESOURCE_NAME */
	@Test
	public void testOkStringFoobar() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_STRING_FOOBAR_RESOURCE_NAME)) {
			final Optional<Object> object = new SurfParser().parse(inputStream);
			assertThat(object, isPresent());
			assertThat(object, hasValue("foobar"));
		}
	}

	/** @see SurfTestResources#OK_STRINGS_RESOURCE_NAME */
	@Test
	public void testOkStrings() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_STRINGS_RESOURCE_NAME)) {
			final SurfResource resource = (SurfResource)new SurfParser().parse(inputStream).get();
			assertThat(resource.getPropertyValue("foo"), hasValue("bar"));
			assertThat(resource.getPropertyValue("quote"), hasValue("\""));
			assertThat(resource.getPropertyValue("backslash"), hasValue("\\"));
			assertThat(resource.getPropertyValue("solidus"), hasValue("/"));
			assertThat(resource.getPropertyValue("ff"), hasValue("\f"));
			assertThat(resource.getPropertyValue("lf"), hasValue("\n"));
			assertThat(resource.getPropertyValue("cr"), hasValue("\r"));
			assertThat(resource.getPropertyValue("tab"), hasValue("\t"));
			assertThat(resource.getPropertyValue("vtab"), hasValue("\u000B"));
			assertThat(resource.getPropertyValue("devanagari-ma"), hasValue("\u092E"));
			assertThat(resource.getPropertyValue("tearsOfJoy"), hasValue(String.valueOf(Character.toChars(0x1F602))));
		}
	}

	//TODO add bad tests to prevent escaping normal characters 
	//TODO add bad tests with invalid surrogate character sequences 

}
