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
import java.util.Optional;

import static io.urf.surf.test.SurfTestResources.*;
import static org.hamcrest.Matchers.*;
import org.junit.*;

import io.urf.surf.test.SurfTestResources;

/**
 * Tests of {@link SurfParser}.
 * @author Garret Wilson
 */
public class SurfParserTest {

	//objects

	/** @see SurfTestResources#OK_SIMPLE_RESOURCE_NAMES */
	@Test
	public void testOkSimpleResources() throws IOException {
		for(final String okSimpleResourceName : OK_SIMPLE_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okSimpleResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okSimpleResourceName, object.isPresent(), is(true));
				assertThat(okSimpleResourceName, object.get(), is(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)object.get();
				assertThat(okSimpleResourceName, resource.getTypeName().isPresent(), is(false));
				assertThat(okSimpleResourceName, resource.getPropertyCount(), is(0));
			}
		}
	}

	/** @see SurfTestResources#OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkObjectNoProperties() throws IOException {
		for(final String okObjectNoPropertiesResourceName : OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okObjectNoPropertiesResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okObjectNoPropertiesResourceName, object.isPresent(), is(true));
				assertThat(okObjectNoPropertiesResourceName, object.get(), is(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)object.get();
				assertThat(okObjectNoPropertiesResourceName, resource.getTypeName().isPresent(), is(false));
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
				assertThat(okObjectOnePropertyResourceName, object.isPresent(), is(true));
				assertThat(okObjectOnePropertyResourceName, object.get(), is(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)object.get();
				assertThat(okObjectOnePropertyResourceName, resource.getTypeName().isPresent(), is(false));
				assertThat(okObjectOnePropertyResourceName, resource.getPropertyCount(), is(1));
				assertThat(okObjectOnePropertyResourceName, resource.getPropertyValue("one").isPresent(), is(true));
				assertThat(okObjectOnePropertyResourceName, resource.getPropertyValue("one").get(), is("one"));
				assertThat(okObjectOnePropertyResourceName, resource.getPropertyValue("two").isPresent(), is(false));
			}
		}
	}

	/** @see SurfTestResources#OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkObjectTwoProperties() throws IOException {
		for(final String okObjectTwoPropertiesResourceName : OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okObjectTwoPropertiesResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okObjectTwoPropertiesResourceName, object.isPresent(), is(true));
				assertThat(okObjectTwoPropertiesResourceName, object.get(), is(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)object.get();
				assertThat(okObjectTwoPropertiesResourceName, resource.getTypeName().isPresent(), is(false));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyCount(), is(2));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("one").isPresent(), is(true));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("one").get(), is("one"));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("two").isPresent(), is(true));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("two").get(), is("two"));
				assertThat(okObjectTwoPropertiesResourceName, resource.getPropertyValue("three").isPresent(), is(false));
			}
		}
	}

	//TODO create tests for bad properties, such as double list item separators

	/** @see SurfTestResources#OK_BOOLEAN_FALSE_RESOURCE_NAME */
	@Test
	public void testOkBooleanFalse() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_BOOLEAN_FALSE_RESOURCE_NAME)) {
			final Optional<Object> object = new SurfParser().parse(inputStream);
			assertThat(object.isPresent(), is(true));
			assertThat(object.get(), is(Boolean.FALSE));
		}
	}

	/** @see SurfTestResources#OK_BOOLEAN_TRUE_RESOURCE_NAME */
	@Test
	public void testOkBooleanTrue() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_BOOLEAN_TRUE_RESOURCE_NAME)) {
			final Optional<Object> object = new SurfParser().parse(inputStream);
			assertThat(object.isPresent(), is(true));
			assertThat(object.get(), is(Boolean.TRUE));
		}
	}

	/** @see SurfTestResources#OK_STRING_FOOBAR_RESOURCE_NAME */
	@Test
	public void testOkStringFoobar() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_STRING_FOOBAR_RESOURCE_NAME)) {
			final Optional<Object> object = new SurfParser().parse(inputStream);
			assertThat(object.isPresent(), is(true));
			assertThat(object.get(), is("foobar"));
		}
	}

}
