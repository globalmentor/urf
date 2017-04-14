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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.urf.surf.test.SurfTestResources.*;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.Matchers.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.globalmentor.java.Bytes.*;

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

	//#object

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

	//TODO add error tests of duplicated property names

	//#literals

	//##binary

	/** @see SurfTestResources#OK_BINARY_RESOURCE_NAME */
	@Test
	public void testOkBinary() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_BINARY_RESOURCE_NAME)) {
			final SurfResource resource = (SurfResource)new SurfParser().parse(inputStream).get();

			assertThat(resource.getPropertyValue("count"), hasValue(new byte[] {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99, (byte)0xaa,
					(byte)0xbb, (byte)0xcc, (byte)0xdd, (byte)0xee, (byte)0xff}));
			assertThat(resource.getPropertyValue("count_unpadded"), hasValue(new byte[] {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99,
					(byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd, (byte)0xee, (byte)0xff}));

			assertThat(resource.getPropertyValue("rfc4648Example1"), hasValue(new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03, (byte)0xd9, 0x7e}));
			assertThat(resource.getPropertyValue("rfc4648Example2"), hasValue(new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03, (byte)0xd9}));
			assertThat(resource.getPropertyValue("rfc4648Example2_unpadded"), hasValue(new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03, (byte)0xd9}));
			assertThat(resource.getPropertyValue("rfc4648Example3"), hasValue(new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03}));
			assertThat(resource.getPropertyValue("rfc4648Example3_unpadded"), hasValue(new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03}));

			assertThat(resource.getPropertyValue("rfc4648TestVector1"), hasValue(NO_BYTES));
			assertThat(resource.getPropertyValue("rfc4648TestVector2"), hasValue("f".getBytes(US_ASCII)));
			assertThat(resource.getPropertyValue("rfc4648TestVector3"), hasValue("fo".getBytes(US_ASCII)));
			assertThat(resource.getPropertyValue("rfc4648TestVector4"), hasValue("foo".getBytes(US_ASCII)));
			assertThat(resource.getPropertyValue("rfc4648TestVector5"), hasValue("foob".getBytes(US_ASCII)));
			assertThat(resource.getPropertyValue("rfc4648TestVector6"), hasValue("fooba".getBytes(US_ASCII)));
			assertThat(resource.getPropertyValue("rfc4648TestVector7"), hasValue("foobar".getBytes(US_ASCII)));
		}
	}

	//##boolean

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

	//##IRI

	/** @see SurfTestResources#OK_IRIS_RESOURCE_NAME */
	@Test
	public void testOkIris() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_IRIS_RESOURCE_NAME)) {
			final SurfResource resource = (SurfResource)new SurfParser().parse(inputStream).get();
			assertThat(resource.getPropertyValue("example"), hasValue(URI.create("http://www.example.com/")));
			assertThat(resource.getPropertyValue("iso_8859_1"), hasValue(URI.create("http://www.example.org/Dürst")));
			assertThat(resource.getPropertyValue("encodedForbidden"), hasValue(URI.create("http://xn--99zt52a.example.org/%E2%80%AE")));
		}
	}

	//TODO add tests for extended characters; bad IRIs (such as a non-absolute IRI); a test containing U+202E, as described in RFC 3987 3.2.1

	//##number

	//TODO create several single-number documents to test various number components ending the document, e.g. 123 and 123.456

	/** @see SurfTestResources#OK_NUMBERS_RESOURCE_NAME */
	@Test
	public void testOkNumbers() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_NUMBERS_RESOURCE_NAME)) {
			final SurfResource resource = (SurfResource)new SurfParser().parse(inputStream).get();
			assertThat(resource.getPropertyValue("zero"), hasValue(Integer.valueOf(0)));
			assertThat(resource.getPropertyValue("zeroFraction"), hasValue(Double.valueOf(0)));
			assertThat(resource.getPropertyValue("one"), hasValue(Integer.valueOf(1)));
			assertThat(resource.getPropertyValue("oneFraction"), hasValue(Double.valueOf(1)));
			assertThat(resource.getPropertyValue("integer"), hasValue(Integer.valueOf(123)));
			assertThat(resource.getPropertyValue("negative"), hasValue(Integer.valueOf(-123)));
			assertThat(resource.getPropertyValue("long"), hasValue(Long.valueOf(3456789123L)));
			assertThat(resource.getPropertyValue("fraction"), hasValue(Double.valueOf(12345.6789)));
			assertThat(resource.getPropertyValue("scientific1"), hasValue(Double.valueOf(1.23e+4)));
			assertThat(resource.getPropertyValue("scientific2"), hasValue(Double.valueOf(12.3e-4)));
			assertThat(resource.getPropertyValue("scientific3"), hasValue(Double.valueOf(-123.4e+5)));
			assertThat(resource.getPropertyValue("scientific4"), hasValue(Double.valueOf(-321.45e-12)));
			assertThat(resource.getPropertyValue("scientific5"), hasValue(Double.valueOf(45.67e+89)));
			//These BigDecimal tests require identical scale, which is why "$0.0" isn't compared to BigDecimal.ZERO.
			//If value equivalence regardless of scale is desired, use BigDecimal.compare().
			assertThat(resource.getPropertyValue("decimal"), hasValue(new BigDecimal("0.3")));
			assertThat(resource.getPropertyValue("money"), hasValue(new BigDecimal("1.23")));
			assertThat(resource.getPropertyValue("decimalZero"), hasValue(BigInteger.ZERO));
			assertThat(resource.getPropertyValue("decimalZeroFraction"), hasValue(new BigDecimal("0.0")));
			assertThat(resource.getPropertyValue("decimalOne"), hasValue(BigInteger.ONE));
			assertThat(resource.getPropertyValue("decimalOneFraction"), hasValue(new BigDecimal("1.0")));
			assertThat(resource.getPropertyValue("decimalInteger"), hasValue(new BigInteger("123")));
			assertThat(resource.getPropertyValue("decimalNegative"), hasValue(new BigInteger("-123")));
			assertThat(resource.getPropertyValue("decimalLong"), hasValue(new BigInteger("3456789123")));
			assertThat(resource.getPropertyValue("decimalFraction"), hasValue(new BigDecimal("12345.6789")));
			assertThat(resource.getPropertyValue("decimalScientific1"), hasValue(new BigDecimal("1.23e+4")));
			assertThat(resource.getPropertyValue("decimalScientific2"), hasValue(new BigDecimal("12.3e-4")));
			assertThat(resource.getPropertyValue("decimalScientific3"), hasValue(new BigDecimal("-123.4e+5")));
			assertThat(resource.getPropertyValue("decimalScientific4"), hasValue(new BigDecimal("-321.45e-12")));
			assertThat(resource.getPropertyValue("decimalScientific5"), hasValue(new BigDecimal("45.67e+89")));
		}
	}

	//##regular expression

	/** @see SurfTestResources#OK_REGULAR_EXPRESSIONS_RESOURCE_NAME */
	@Test
	public void testOkRegularExpressions() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_REGULAR_EXPRESSIONS_RESOURCE_NAME)) {
			final SurfResource resource = (SurfResource)new SurfParser().parse(inputStream).get();
			assertThat(resource.getPropertyValue("empty").map(Pattern.class::cast).map(Pattern::pattern), hasValue(""));
			assertThat(resource.getPropertyValue("abc").map(Pattern.class::cast).map(Pattern::pattern), hasValue("abc"));
			assertThat(resource.getPropertyValue("regexEscape").map(Pattern.class::cast).map(Pattern::pattern), hasValue("ab\\.c"));
			assertThat(resource.getPropertyValue("doubleBackslash").map(Pattern.class::cast).map(Pattern::pattern), hasValue("\\\\"));
			assertThat(resource.getPropertyValue("slash").map(Pattern.class::cast).map(Pattern::pattern), hasValue("/"));
		}
	}

	//TODO add bad tests with control characters

	//##string

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

	//TODO add bad tests with control characters
	//TODO add bad tests to prevent escaping normal characters 
	//TODO add bad tests with invalid surrogate character sequences

	//#collections

	//##list

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

}
