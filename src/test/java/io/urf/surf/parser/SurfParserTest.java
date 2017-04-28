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
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

import static io.urf.surf.test.SurfTestResources.*;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.globalmentor.java.Bytes.*;

import org.junit.*;

import io.urf.surf.test.SurfTestResources;
import junit.framework.AssertionFailedError;

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

	//TODO add test for bad document with content after root resource; make sure this prohibition gets into the spec 

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

	//##Boolean

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

	//##character

	/** @see SurfTestResources#OK_CHARACTERS_RESOURCE_NAME */
	@Test
	public void testOkCharacters() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_CHARACTERS_RESOURCE_NAME)) {
			final SurfResource resource = (SurfResource)new SurfParser().parse(inputStream).get();
			assertThat(resource.getPropertyValue("foo"), hasValue('|'));
			assertThat(resource.getPropertyValue("quote"), hasValue('"'));
			assertThat(resource.getPropertyValue("apostrophe"), hasValue('\''));
			assertThat(resource.getPropertyValue("backslash"), hasValue('\\'));
			assertThat(resource.getPropertyValue("solidus"), hasValue('/'));
			assertThat(resource.getPropertyValue("ff"), hasValue('\f'));
			assertThat(resource.getPropertyValue("lf"), hasValue('\n'));
			assertThat(resource.getPropertyValue("cr"), hasValue('\r'));
			assertThat(resource.getPropertyValue("tab"), hasValue('\t'));
			assertThat(resource.getPropertyValue("vtab"), hasValue('\u000B'));
			assertThat(resource.getPropertyValue("devanagari-ma"), hasValue('\u092E'));
			assertThat(resource.getPropertyValue("devanagari-maEscaped"), hasValue('\u092E'));
			/*TODO fix for supplementary characters
			assertThat(resource.getPropertyValue("tearsOfJoy"), hasValue(Character.toChars(0x1F602)));
			assertThat(resource.getPropertyValue("tearsOfJoyEscaped"), hasValue(Character.toChars(0x1F602)));
			*/
		}
	}

	//TODO add bad tests with empty character
	//TODO add bad tests with control characters
	//TODO add bad tests to prevent escaping normal characters 
	//TODO add bad tests with invalid surrogate character sequences

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
			assertThat(resource.getPropertyValue("empty"), hasValue(""));
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
			assertThat(resource.getPropertyValue("devanagari-maEscaped"), hasValue("\u092E"));
			assertThat(resource.getPropertyValue("tearsOfJoy"), hasValue(String.valueOf(Character.toChars(0x1F602))));
			assertThat(resource.getPropertyValue("tearsOfJoyEscaped"), hasValue(String.valueOf(Character.toChars(0x1F602))));
		}
	}

	//TODO add bad tests with control characters
	//TODO add bad tests to prevent escaping normal characters 
	//TODO add bad tests with invalid surrogate character sequences

	//##temporal

	/** @see SurfTestResources#OK_TEMPORALS_RESOURCE_NAME */
	@Test
	public void testOkTemporals() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_TEMPORALS_RESOURCE_NAME)) {
			final SurfResource resource = (SurfResource)new SurfParser().parse(inputStream).get();
			assertThat(resource.getPropertyValue("instant"), hasValue(Instant.parse("2017-02-12T23:29:18.829Z")));
			assertThat(resource.getPropertyValue("zonedDateTime"), hasValue(ZonedDateTime.parse("2017-02-12T15:29:18.829-08:00[America/Los_Angeles]")));
			assertThat(resource.getPropertyValue("offsetDateTime"), hasValue(OffsetDateTime.parse("2017-02-12T15:29:18.829-08:00")));
			assertThat(resource.getPropertyValue("offsetTime"), hasValue(OffsetTime.parse("15:29:18.829-08:00")));
			assertThat(resource.getPropertyValue("localDateTime"), hasValue(LocalDateTime.parse("2017-02-12T15:29:18.829")));
			assertThat(resource.getPropertyValue("localDate"), hasValue(LocalDate.parse("2017-02-12")));
			assertThat(resource.getPropertyValue("localTime"), hasValue(LocalTime.parse("15:29:18.829")));
			assertThat(resource.getPropertyValue("yearMonth"), hasValue(YearMonth.parse("2017-02")));
			assertThat(resource.getPropertyValue("monthDay"), hasValue(MonthDay.parse("--02-12")));
			assertThat(resource.getPropertyValue("year"), hasValue(Year.parse("2017")));
		}
	}

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

	//#map

	/** @see SurfTestResources#OK_MAPS_RESOURCE_NAME */
	@Test
	public void testOkMaps() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_MAPS_RESOURCE_NAME)) {
			final Optional<Object> object = new SurfParser().parse(inputStream);
			assertThat(object, isPresent());
			assertThat(object, hasValue(instanceOf(Map.class)));
			final Map<?, ?> map = (Map<?, ?>)object.get();
			assertThat(map.size(), is(7));
			assertThat(map.get("foo"), is("bar"));
			assertThat(map.get(123), is("number"));
			assertThat(map.get(false), is("Boolean"));
			assertThat(map.get(true), is("Boolean"));
			assertThat(map.get(Arrays.asList(1, 2, 3)), is(new BigDecimal("1.23")));
			final Map<Object, Object> pingPong = new HashMap<>();
			pingPong.put("ping", Arrays.asList('p', 'o', 'n', 'g'));
			assertThat(map.get("map"), is(pingPong));
			assertThat(map.get(new HashSet<Object>(Arrays.asList("foo", false))), is(true));
		}
	}

	/** @see SurfTestResources#OK_MAP_EMPTY_RESOURCE_NAMES */
	@Test
	public void testOkMapEmpty() throws IOException {
		for(final String okMapEmptyResourceName : OK_MAP_EMPTY_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okMapEmptyResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okMapEmptyResourceName, object, isPresent());
				assertThat(okMapEmptyResourceName, object, hasValue(instanceOf(Map.class)));
				final Map<?, ?> map = (Map<?, ?>)object.get();
				assertThat(map.size(), is(0));
			}
		}
	}

	/** @see SurfTestResources#OK_MAP_ONE_ENTRY_RESOURCE_NAMES */
	@Test
	public void testOkMapOneEntry() throws IOException {
		for(final String okMapOneEntryResourceName : OK_MAP_ONE_ENTRY_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okMapOneEntryResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okMapOneEntryResourceName, object, isPresent());
				assertThat(okMapOneEntryResourceName, object, hasValue(instanceOf(Map.class)));
				final Map<?, ?> map = (Map<?, ?>)object.get();
				assertThat(map.size(), is(1));
				assertThat(okMapOneEntryResourceName, map.containsKey("I"), is(true));
				assertThat(okMapOneEntryResourceName, map.get("I"), is("one"));
				assertThat(okMapOneEntryResourceName, map.containsKey("II"), is(false));
			}
		}
	}

	/** @see SurfTestResources#OK_MAP_TWO_ENTRIES_RESOURCE_NAMES */
	@Test
	public void testOkMapTwoEntries() throws IOException {
		for(final String okMapTwoEntriesResourceName : OK_MAP_TWO_ENTRIES_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okMapTwoEntriesResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okMapTwoEntriesResourceName, object, isPresent());
				assertThat(okMapTwoEntriesResourceName, object, hasValue(instanceOf(Map.class)));
				final Map<?, ?> map = (Map<?, ?>)object.get();
				assertThat(map.size(), is(2));
				assertThat(okMapTwoEntriesResourceName, map.containsKey("I"), is(true));
				assertThat(okMapTwoEntriesResourceName, map.get("I"), is("one"));
				assertThat(okMapTwoEntriesResourceName, map.containsKey("II"), is(true));
				assertThat(okMapTwoEntriesResourceName, map.get("II"), is("two"));
				assertThat(okMapTwoEntriesResourceName, map.containsKey("III"), is(false));
			}
		}
	}

	//##set

	/** @see SurfTestResources#OK_SET_NO_ITEMS_RESOURCE_NAMES */
	@Test
	public void testOkSetNoItems() throws IOException {
		for(final String okSetNoItemsResourceName : OK_SET_NO_ITEMS_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okSetNoItemsResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okSetNoItemsResourceName, object, isPresent());
				assertThat(okSetNoItemsResourceName, object, hasValue(instanceOf(Set.class)));
				final Set<?> set = (Set<?>)object.get();
				assertThat(okSetNoItemsResourceName, set, hasSize(0));
			}
		}
	}

	/** @see SurfTestResources#OK_SET_ONE_ITEM_RESOURCE_NAMES */
	@Test
	public void testOkSetOneItem() throws IOException {
		for(final String okSetOneItemResourceName : OK_SET_ONE_ITEM_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okSetOneItemResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okSetOneItemResourceName, object, isPresent());
				assertThat(okSetOneItemResourceName, object, hasValue(instanceOf(Set.class)));
				final Set<?> set = (Set<?>)object.get();
				assertThat(okSetOneItemResourceName, set, equalTo(new HashSet<Object>(asList("one"))));
			}
		}
	}

	/** @see SurfTestResources#OK_SET_TWO_ITEMS_RESOURCE_NAMES */
	@Test
	public void testOkSetTwoItems() throws IOException {
		for(final String okSetTwoItemsResourceName : OK_SET_TWO_ITEMS_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okSetTwoItemsResourceName)) {
				final Optional<Object> object = new SurfParser().parse(inputStream);
				assertThat(okSetTwoItemsResourceName, object, isPresent());
				assertThat(okSetTwoItemsResourceName, object, hasValue(instanceOf(Set.class)));
				final Set<?> set = (Set<?>)object.get();
				assertThat(okSetTwoItemsResourceName, set, hasSize(2));
				assertThat(okSetTwoItemsResourceName, set, equalTo(new HashSet<Object>(asList("one", "two"))));
			}
		}
	}

	//TODO create tests for duplicate items and double list item separators

	//#labels

	/** @see SurfTestResources#OK_LABELS_RESOURCE_NAME */
	@Test
	public void testOkLabels() throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(OK_LABELS_RESOURCE_NAME)) {
			final SurfParser surfParser = new SurfParser();
			final SurfResource root = (SurfResource)surfParser.parse(inputStream).get();
			//|root|
			final Optional<Object> rootLabeled = surfParser.getResourceByLabel("root");
			assertThat(rootLabeled, hasValue(sameInstance(root)));
			//|number|
			final Object foo = root.getPropertyValue("foo").orElseThrow(AssertionFailedError::new);
			assertThat(foo, is(123));
			final Optional<Object> numberLabeled = surfParser.getResourceByLabel("number");
			assertThat(numberLabeled, hasValue(sameInstance(foo)));
			//|test|
			final Object value = root.getPropertyValue("value").orElseThrow(AssertionFailedError::new);
			assertThat(value, is(false));
			final Optional<Object> testLabeled = surfParser.getResourceByLabel("test");
			assertThat(testLabeled, hasValue(sameInstance(value)));
			//|object|
			final SurfResource thing = (SurfResource)root.getPropertyValue("thing").orElseThrow(AssertionFailedError::new);
			//TODO assert type of thing
			final Optional<Object> objectLabeled = surfParser.getResourceByLabel("object");
			assertThat(objectLabeled, hasValue(sameInstance(thing)));
			//list elements
			final List<?> stuff = (List<?>)thing.getPropertyValue("stuff").orElseThrow(AssertionFailedError::new);
			assertThat(stuff, contains("one", 123, "three"));
			assertThat(numberLabeled, hasValue(sameInstance(stuff.get(1))));
			//map values TODO implement map label tests
			//			final Map<?, ?> map = (Map<?, ?>)root.getPropertyValue("map").orElseThrow(AssertionFailedError::new);
			//			assertThat(map.get(1), is)
			//set members
			@SuppressWarnings("unchecked")
			final Set<Object> set = (Set<Object>)root.getPropertyValue("set").orElseThrow(AssertionFailedError::new);
			assertThat(set, hasSize(5));
			assertThat(set, hasItem(123));
			assertThat(set, hasItem(false));
			final Optional<Object> newLabeled = surfParser.getResourceByLabel("new");
			final SurfResource newLabeledResource = (SurfResource)newLabeled.orElseThrow(AssertionFailedError::new);
			assertThat(newLabeledResource.getPropertyValue("description"), hasValue("a new thing"));
			final Optional<Object> anotherLabeled = surfParser.getResourceByLabel("another");
			final SurfResource anotherLabeledResource = (SurfResource)anotherLabeled.orElseThrow(AssertionFailedError::new);
			assertThat(anotherLabeledResource.getPropertyValue("description"), hasValue("yet another thing"));
			assertThat(set, hasItem(sameInstance(numberLabeled.get())));
			assertThat(set, hasItem(sameInstance(testLabeled.get())));
			assertThat(set, hasItem(sameInstance(objectLabeled.get())));
			assertThat(set, hasItem(sameInstance(newLabeledResource)));
			assertThat(set, hasItem(sameInstance(anotherLabeledResource)));
		}
	}

	//TODO create test with bad labels, such as labels with whitespace, labels for null, and redefined labels
}
