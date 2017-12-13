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

package io.urf.surf;

import static org.junit.Assert.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.*;

import static com.globalmentor.java.Bytes.*;
import static io.urf.surf.SurfTestResources.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;

import com.globalmentor.itu.TelephoneNumber;
import com.globalmentor.java.CodePointCharacter;
import com.globalmentor.net.EmailAddress;

import io.clogr.Clogged;

/**
 * Tests of {@link SurfParser}.
 * <p>
 * Rather than testing exact serializations (which would make the tests tedious and brittle), most of the tests here re-parse the serializations and compare
 * them to parsed test serializations provided in {@link SurfTestResources}.
 * </p>
 * @author Garret Wilson
 */
public class SurfSerializerTest implements Clogged {

	/**
	 * Loads and parses a Java resource using {@link SurfParser}.
	 * @param testResourceName The name of the Java resource for testing, relative to {@link SurfParserTest}.
	 * @return The optional resource instance parsed from the named Java instance.
	 * @see SurfTestResources
	 * @see SurfParser#parse(InputStream)
	 */
	protected static Optional<Object> parseTestResource(@Nonnull final String testResourceName) throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(testResourceName)) {
			return new SurfParser().parse(inputStream);
		}
	}

	/** General test currently for manual verification of formatted serialization. */
	@Ignore
	@Test
	public void testSerialize() throws IOException {
		final SurfObject surfObject = new SurfObject(URI.create("urn:uuid:bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832"), "User");
		surfObject.setPropertyValue("firstName", "Jane");
		surfObject.setPropertyValue("lastName", "Doe");
		surfObject.setPropertyValue("fullName", "Jane Doe");
		final SurfObject name = new SurfObject();
		name.setPropertyValue("first", "Jane");
		name.setPropertyValue("last", "Doe");
		surfObject.setPropertyValue("name", name);
		//TODO finish

		final SurfSerializer serializer = new SurfSerializer();
		serializer.setFormatted(true);

		getLogger().debug(serializer.serialize(surfObject)); //currently for manual, visual inspection
	}

	/**
	 * Ensures that serializing to an output stream works correctly, and that the output is flushed to the output stream after serialization.
	 * @see SurfSerializer#serialize(OutputStream, Object)
	 */
	@Test
	public void testSerializeOutputStream() throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			//The SURF serializers internally may create a buffered writer which the caller does not have access to.
			//If the serializer does not flush this writer, the caller will never see the bytes appear in the original output stream.  
			new SurfSerializer().serialize(byteArrayOutputStream, new SurfObject());
		} finally {
			byteArrayOutputStream.close();
		}
		assertThat(byteArrayOutputStream.toString(SURF.CHARSET.name()), equalTo("*"));
	}

	//#objects

	@Test
	public void testOkEmptyObjects() throws IOException {
		assertThat(new SurfSerializer().serialize(new SurfObject()), equalTo("*"));
		assertThat(new SurfSerializer().serialize(new SurfObject("FooBar")), equalTo("*FooBar"));
	}

	/** @see SurfTestResources#OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkObjectNoProperties() throws IOException {
		final SurfObject surfObject = new SurfObject();
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			for(final String okObjectNoPropertiesResourceName : OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES) {
				assertThat(okObjectNoPropertiesResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okObjectNoPropertiesResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_OBJECT_ONE_PROPERTY_RESOURCE_NAMES */
	@Test
	public void testOkObjectOneProperty() throws IOException {
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("one", "one");
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			for(final String okObjectOnePropertyResourceName : OK_OBJECT_ONE_PROPERTY_RESOURCE_NAMES) {
				assertThat(okObjectOnePropertyResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okObjectOnePropertyResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkObjectTwoProperties() throws IOException {
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("one", "one");
		surfObject.setPropertyValue("two", "two");
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			for(final String okObjectTwoPropertiesResourceName : OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES) {
				assertThat(okObjectTwoPropertiesResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okObjectTwoPropertiesResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_OBJECT_TYPE_RESOURCE_NAMES */
	@Test
	public void testOkObjectType() throws IOException {
		final SurfObject surfObject = new SurfObject("example-FooBar");
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			for(final String okObjectTypeResourceName : OK_OBJECT_TYPE_RESOURCE_NAMES) {
				assertThat(okObjectTypeResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okObjectTypeResourceName)));
			}
		}
	}

	//#literals

	//##binary

	/** @see SurfTestResources#OK_BINARY_RESOURCE_NAME */
	@Test
	public void testOkBinary() throws IOException {

		//test serializing java.nio.ByteByffer separately, because the parser never sends that back
		final ByteBuffer countByteBuffer = ByteBuffer.wrap(new byte[] {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99, (byte)0xaa,
				(byte)0xbb, (byte)0xcc, (byte)0xdd, (byte)0xee, (byte)0xff});
		assertThat(new SurfSerializer().serialize(countByteBuffer), equalTo("%ABEiM0RVZneImaq7zN3u/w"));

		final SurfObject surfObject = new SurfObject();

		surfObject.setPropertyValue("count", new byte[] {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99, (byte)0xaa, (byte)0xbb, (byte)0xcc,
				(byte)0xdd, (byte)0xee, (byte)0xff});

		surfObject.setPropertyValue("rfc4648Example1", new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03, (byte)0xd9, 0x7e});
		surfObject.setPropertyValue("rfc4648Example2", new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03, (byte)0xd9});
		surfObject.setPropertyValue("rfc4648Example3", new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03});

		surfObject.setPropertyValue("rfc4648TestVector1", NO_BYTES);
		surfObject.setPropertyValue("rfc4648TestVector2", "f".getBytes(US_ASCII));
		surfObject.setPropertyValue("rfc4648TestVector3", "fo".getBytes(US_ASCII));
		surfObject.setPropertyValue("rfc4648TestVector4", "foo".getBytes(US_ASCII));
		surfObject.setPropertyValue("rfc4648TestVector5", "foob".getBytes(US_ASCII));
		surfObject.setPropertyValue("rfc4648TestVector6", "fooba".getBytes(US_ASCII));
		surfObject.setPropertyValue("rfc4648TestVector7", "foobar".getBytes(US_ASCII));

		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			/*TODO fix SurfObject equals() to take byte arrays into consideration 
			* final String serialization = serializer.serialize(surfObject);
			* assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_BINARY_RESOURCE_NAME)));
			*/
		}
	}

	//##Boolean

	@Test
	public void testOkBooleans() throws IOException {
		assertThat(new SurfSerializer().serialize(true), equalTo("true"));
		assertThat(new SurfSerializer().serialize(false), equalTo("false"));
	}

	/** @see SurfTestResources#OK_BOOLEAN_FALSE_RESOURCE_NAME */
	@Test
	public void testOkBooleanFalse() throws IOException {
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(Boolean.FALSE);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_BOOLEAN_FALSE_RESOURCE_NAME)));
		}
	}

	/** @see SurfTestResources#OK_BOOLEAN_TRUE_RESOURCE_NAME */
	@Test
	public void testOkBooleanTrue() throws IOException {
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(Boolean.TRUE);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_BOOLEAN_TRUE_RESOURCE_NAME)));
		}
	}

	//##character

	/** @see SurfTestResources#OK_CHARACTERS_RESOURCE_NAME */
	@Test
	public void testOkCharacters() throws IOException {
		//test serializing java.lang.Character separately, because the parser never sends that back
		assertThat(new SurfSerializer().serialize('X'), equalTo("'X'"));
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("foo", CodePointCharacter.of('|'));
		surfObject.setPropertyValue("quote", CodePointCharacter.of('"'));
		surfObject.setPropertyValue("apostrophe", CodePointCharacter.of('\''));
		surfObject.setPropertyValue("backslash", CodePointCharacter.of('\\'));
		surfObject.setPropertyValue("solidus", CodePointCharacter.of('/'));
		surfObject.setPropertyValue("backspace", CodePointCharacter.of('\b'));
		surfObject.setPropertyValue("ff", CodePointCharacter.of('\f'));
		surfObject.setPropertyValue("lf", CodePointCharacter.of('\n'));
		surfObject.setPropertyValue("cr", CodePointCharacter.of('\r'));
		surfObject.setPropertyValue("tab", CodePointCharacter.of('\t'));
		surfObject.setPropertyValue("vtab", CodePointCharacter.of('\u000B'));
		surfObject.setPropertyValue("devanagari-ma", CodePointCharacter.of('\u092E'));
		surfObject.setPropertyValue("devanagari-maEscaped", CodePointCharacter.of('\u092E'));
		surfObject.setPropertyValue("tearsOfJoy", CodePointCharacter.of(0x1F602));
		surfObject.setPropertyValue("tearsOfJoyEscaped", CodePointCharacter.of(0x1F602));
		assertThat(surfObject, equalTo(parseTestResource(OK_CHARACTERS_RESOURCE_NAME).get())); //verify the test data
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_CHARACTERS_RESOURCE_NAME)));
		}
	}

	//##email address

	/** @see SurfTestResources#OK_EMAIL_ADDRESSES_RESOURCE_NAME */
	@Test
	public void testOkEmailAddresses() throws IOException {
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("example", EmailAddress.fromString("jdoe@example.com"));
		surfObject.setPropertyValue("dot", EmailAddress.fromString("jane.doe@example.com"));
		surfObject.setPropertyValue("tag", EmailAddress.fromString("jane.doe+tag@example.com"));
		surfObject.setPropertyValue("dash", EmailAddress.fromString("jane.doe-foo@example.com"));
		surfObject.setPropertyValue("x", EmailAddress.fromString("x@example.com"));
		surfObject.setPropertyValue("dashedDomain", EmailAddress.fromString("foo-bar@strange-example.com"));
		surfObject.setPropertyValue("longTLD", EmailAddress.fromString("example@s.solutions"));
		assertThat(surfObject, equalTo(parseTestResource(OK_EMAIL_ADDRESSES_RESOURCE_NAME).get())); //verify the test data
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_EMAIL_ADDRESSES_RESOURCE_NAME)));
		}
	}

	//##IRI

	/** @see SurfTestResources#OK_IRIS_RESOURCE_NAME */
	@Test
	public void testOkIris() throws IOException {
		//test serializing java.netURL separately, because the parser never sends that back
		assertThat(new SurfSerializer().serialize(new URL("http://www.example.com/")), equalTo("<http://www.example.com/>"));
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("example", URI.create("http://www.example.com/"));
		surfObject.setPropertyValue("iso_8859_1", URI.create("http://www.example.org/Dürst"));
		surfObject.setPropertyValue("encodedForbidden", URI.create("http://xn--99zt52a.example.org/%E2%80%AE"));
		assertThat(surfObject, equalTo(parseTestResource(OK_IRIS_RESOURCE_NAME).get())); //verify the test data
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_IRIS_RESOURCE_NAME)));
		}
	}

	//TODO add IllegalArgumentException test for URL that isn't a valid IRI 

	//##number

	/** @see SurfTestResources#OK_NUMBERS_RESOURCE_NAME */
	@Test
	public void testOkNumbers() throws IOException {
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("zero", Integer.valueOf(0));
		surfObject.setPropertyValue("zeroFraction", Double.valueOf(0));
		surfObject.setPropertyValue("one", Integer.valueOf(1));
		surfObject.setPropertyValue("oneFraction", Double.valueOf(1));
		surfObject.setPropertyValue("integer", Integer.valueOf(123));
		surfObject.setPropertyValue("negative", Integer.valueOf(-123));
		surfObject.setPropertyValue("long", Long.valueOf(3456789123L));
		surfObject.setPropertyValue("fraction", Double.valueOf(12345.6789));
		surfObject.setPropertyValue("scientific1", Double.valueOf(1.23e+4));
		surfObject.setPropertyValue("scientific2", Double.valueOf(12.3e-4));
		surfObject.setPropertyValue("scientific3", Double.valueOf(-123.4e+5));
		surfObject.setPropertyValue("scientific4", Double.valueOf(-321.45e-12));
		surfObject.setPropertyValue("scientific5", Double.valueOf(45.67e+89));
		//These BigDecimal tests require identical scale, which is why "$0.0" isn't compared to BigDecimal.ZERO.
		surfObject.setPropertyValue("decimal", new BigDecimal("0.3"));
		surfObject.setPropertyValue("money", new BigDecimal("1.23"));
		surfObject.setPropertyValue("decimalZero", BigInteger.ZERO);
		surfObject.setPropertyValue("decimalZeroFraction", new BigDecimal("0.0"));
		surfObject.setPropertyValue("decimalOne", BigInteger.ONE);
		surfObject.setPropertyValue("decimalOneFraction", new BigDecimal("1.0"));
		surfObject.setPropertyValue("decimalInteger", new BigInteger("123"));
		surfObject.setPropertyValue("decimalNegative", new BigInteger("-123"));
		surfObject.setPropertyValue("decimalLong", new BigInteger("3456789123"));
		surfObject.setPropertyValue("decimalFraction", new BigDecimal("12345.6789"));
		surfObject.setPropertyValue("decimalScientific1", new BigDecimal("1.23e+4"));
		surfObject.setPropertyValue("decimalScientific2", new BigDecimal("12.3e-4"));
		surfObject.setPropertyValue("decimalScientific3", new BigDecimal("-123.4e+5"));
		surfObject.setPropertyValue("decimalScientific4", new BigDecimal("-321.45e-12"));
		surfObject.setPropertyValue("decimalScientific5", new BigDecimal("45.67e+89"));
		assertThat(surfObject, equalTo(parseTestResource(OK_NUMBERS_RESOURCE_NAME).get())); //verify the test data
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_NUMBERS_RESOURCE_NAME)));
		}
	}

	//##regular expression

	/** @see SurfTestResources#OK_REGULAR_EXPRESSIONS_RESOURCE_NAME */
	@Ignore //TODO enable when SurfObject support comparing pattern property values; see https://bugs.openjdk.java.net/browse/JDK-7163589
	@Test
	public void testOkRegularExpressions() throws IOException {
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("empty", Pattern.compile(""));
		surfObject.setPropertyValue("abc", Pattern.compile("abc"));
		surfObject.setPropertyValue("regexEscape", Pattern.compile("ab\\.c"));
		surfObject.setPropertyValue("doubleBackslash", Pattern.compile("\\\\"));
		surfObject.setPropertyValue("slash", Pattern.compile("/"));
		assertThat(surfObject, equalTo(parseTestResource(OK_REGULAR_EXPRESSIONS_RESOURCE_NAME).get())); //verify the test data
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_REGULAR_EXPRESSIONS_RESOURCE_NAME)));
		}
	}

	//##string

	/**
	 * @see SurfTestResources#OK_STRING_FOOBAR_RESOURCE_NAME
	 */
	@Test
	public void testOkStringFoobar() throws IOException {
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize("foobar");
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_STRING_FOOBAR_RESOURCE_NAME)));
		}
	}

	//TODO implement OK_STRINGS_RESOURCE_NAME

	//##telephone number

	/** @see SurfTestResources#OK_TELEPHONE_NUMBERS_RESOURCE_NAME */
	@Test
	public void testOkTelephoneNumbers() throws IOException {
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("rfc3966Example", TelephoneNumber.parse("+12015550123"));
		surfObject.setPropertyValue("brazil", TelephoneNumber.parse("+552187654321"));
		assertThat(surfObject, equalTo(parseTestResource(OK_TELEPHONE_NUMBERS_RESOURCE_NAME).get())); //verify the test data
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_TELEPHONE_NUMBERS_RESOURCE_NAME)));
		}
	}

	//TODO add IllegalArgumentException test for telephone number not in global form 

	//##temporal

	/** @see SurfTestResources#OK_TEMPORALS_RESOURCE_NAME */
	@Test
	public void testOkTemporals() throws IOException {
		//test serializing java.util.Date separately, because the parser never sends that back
		final Date nowDate = new Date();
		assertThat(new SurfSerializer().serialize(nowDate), equalTo("@" + nowDate.toInstant().toString()));
		final SurfObject surfObject = new SurfObject();
		surfObject.setPropertyValue("instant", Instant.parse("2017-02-12T23:29:18.829Z"));
		surfObject.setPropertyValue("zonedDateTime", ZonedDateTime.parse("2017-02-12T15:29:18.829-08:00[America/Los_Angeles]"));
		surfObject.setPropertyValue("offsetDateTime", OffsetDateTime.parse("2017-02-12T15:29:18.829-08:00"));
		surfObject.setPropertyValue("offsetTime", OffsetTime.parse("15:29:18.829-08:00"));
		surfObject.setPropertyValue("localDateTime", LocalDateTime.parse("2017-02-12T15:29:18.829"));
		surfObject.setPropertyValue("localDate", LocalDate.parse("2017-02-12"));
		surfObject.setPropertyValue("localTime", LocalTime.parse("15:29:18.829"));
		surfObject.setPropertyValue("yearMonth", YearMonth.parse("2017-02"));
		surfObject.setPropertyValue("monthDay", MonthDay.parse("--02-12"));
		surfObject.setPropertyValue("year", Year.parse("2017"));
		assertThat(surfObject, equalTo(parseTestResource(OK_TEMPORALS_RESOURCE_NAME).get())); //verify the test data
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_TEMPORALS_RESOURCE_NAME)));
		}
	}

	//##UUID

	/** @see SurfTestResources#OK_UUID_RESOURCE_NAME */
	@Test
	public void testOkUuid() throws IOException {
		final UUID uuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(uuid);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_UUID_RESOURCE_NAME)));
		}
	}

	//#collections

	//##list

	/** @see SurfTestResources#OK_LIST_EMPTY_RESOURCE_NAMES */
	@Test
	public void testOkListEmpty() throws IOException {
		final List<?> list = emptyList();
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(list);
			for(final String okListEmptyResourceName : OK_LIST_EMPTY_RESOURCE_NAMES) {
				assertThat(okListEmptyResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okListEmptyResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_LIST_ONE_ITEM_RESOURCE_NAMES */
	@Test
	public void testOkListOneItem() throws IOException {
		final List<?> list = asList("one");
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(list);
			for(final String okListOneItemResourceName : OK_LIST_ONE_ITEM_RESOURCE_NAMES) {
				assertThat(okListOneItemResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okListOneItemResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_LIST_TWO_ITEMS_RESOURCE_NAMES */
	@Test
	public void testOkListTwoItems() throws IOException {
		final List<?> list = asList("one", "two");
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(list);
			for(final String okListTwoItemsResourceName : OK_LIST_TWO_ITEMS_RESOURCE_NAMES) {
				assertThat(okListTwoItemsResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okListTwoItemsResourceName)));
			}
		}
	}

	//#map

	/** @see SurfTestResources#OK_MAPS_RESOURCE_NAME */
	@Test
	public void testOkMaps() throws IOException {
		final Map<Object, Object> map = new HashMap<>();
		map.put("foo", "bar");
		map.put(123, "number");
		map.put(false, "Boolean");
		map.put(true, "Boolean");
		map.put(Arrays.asList(1, 2, 3), new BigDecimal("1.23"));
		final Map<Object, Object> pingPong = new HashMap<>();
		pingPong.put("ping", Arrays.asList(CodePointCharacter.of('p'), CodePointCharacter.of('o'), CodePointCharacter.of('n'), CodePointCharacter.of('g')));
		map.put("map", pingPong);
		map.put(new HashSet<Object>(Arrays.asList("foo", false)), true);
		map.put(new SurfObject("Game", "pingpong"), "ping pong");
		final SurfObject bullsEye = new SurfObject("Point");
		bullsEye.setPropertyValue("x", 0);
		bullsEye.setPropertyValue("y", 0);
		map.put(bullsEye, "Bull's Eye");
		final Map<String, String> abbrMap = new HashMap<>();
		abbrMap.put("ITTF", "International Table Tennis Federation");
		map.put(abbrMap, "abbr");
		assertThat(map, equalTo(parseTestResource(OK_MAPS_RESOURCE_NAME).get())); //verify the test data
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(map);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_MAPS_RESOURCE_NAME)));
		}
	}

	/** @see SurfTestResources#OK_MAP_EMPTY_RESOURCE_NAMES */
	@Test
	public void testOkMapEmpty() throws IOException {
		final Map<?, ?> map = emptyMap();
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(map);
			for(final String okMapEmptyResourceName : OK_MAP_EMPTY_RESOURCE_NAMES) {
				assertThat(okMapEmptyResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okMapEmptyResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_MAP_ONE_ENTRY_RESOURCE_NAMES */
	@Test
	public void testOkMapOneEntry() throws IOException {
		final Map<String, String> map = new HashMap<>();
		map.put("I", "one");
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(map);
			for(final String okMapOneEntryResourceName : OK_MAP_ONE_ENTRY_RESOURCE_NAMES) {
				assertThat(okMapOneEntryResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okMapOneEntryResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_MAP_TWO_ENTRIES_RESOURCE_NAMES */
	@Test
	public void testOkMapTwoEntries() throws IOException {
		final Map<String, String> map = new HashMap<>();
		map.put("I", "one");
		map.put("II", "two");
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(map);
			for(final String okMapTwoEntriesResourceName : OK_MAP_TWO_ENTRIES_RESOURCE_NAMES) {
				assertThat(okMapTwoEntriesResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okMapTwoEntriesResourceName)));
			}
		}
	}

	//##set

	/** @see SurfTestResources#OK_SET_EMPTY_RESOURCE_NAMES */
	@Test
	public void testOkSetEmpty() throws IOException {
		final Set<?> set = emptySet();
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(set);
			for(final String okSetEmptyResourceName : OK_SET_EMPTY_RESOURCE_NAMES) {
				assertThat(okSetEmptyResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okSetEmptyResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_SET_ONE_ITEM_RESOURCE_NAMES */
	@Test
	public void testOkSetOneItem() throws IOException {
		final Set<?> set = new HashSet<>(asList("one"));
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(set);
			for(final String okSetOneItemResourceName : OK_SET_ONE_ITEM_RESOURCE_NAMES) {
				assertThat(okSetOneItemResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okSetOneItemResourceName)));
			}
		}
	}

	/** @see SurfTestResources#OK_SET_TWO_ITEMS_RESOURCE_NAMES */
	@Test
	public void testOkSetTwoItems() throws IOException {
		final Set<?> set = new HashSet<>(asList("one", "two"));
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(set);
			for(final String okSetTwoItemsResourceName : OK_SET_TWO_ITEMS_RESOURCE_NAMES) {
				assertThat(okSetTwoItemsResourceName, new SurfParser().parse(serialization), equalTo(parseTestResource(okSetTwoItemsResourceName)));
			}
		}
	}

	//#labels

	/** @see SurfTestResources#OK_LABELS_RESOURCE_NAME */
	@Test
	public void testOkLabels() throws IOException {
		final SurfObject root = new SurfObject();
		root.setPropertyValue("foo", 123);
		//TODO circular references: root.setPropertyValue("self", root);
		root.setPropertyValue("value", false);
		final SurfObject object = new SurfObject("example-Type");
		final SurfObject exampleThing = new SurfObject(URI.create("http://example.com/thing"), "example-Thing");
		exampleThing.setPropertyValue("name", "Example Thing");
		object.setPropertyValue("stuff", asList("one", 123, "three", exampleThing));
		root.setPropertyValue("thing", object);
		final SurfObject fooBar = new SurfObject("Bar", "foo");
		fooBar.setPropertyValue("prop", "val");
		//TODO circular references: fooBar.setPropertyValue("self", fooBar);
		root.setPropertyValue("foobar", fooBar);
		final Map<Integer, Object> me = new HashMap<>();
		//TODO circular references: me.put(0, me);
		me.put(1, "one");
		me.put(2, 123);
		me.put(4, fooBar);
		me.put(99, exampleThing);
		me.put(100, object);
		root.setPropertyValue("map", me);
		final Set<Object> these = new HashSet<>();
		these.add(123);
		these.add(false);
		these.add(object);
		//TODO circular references: these.add(root);
		final SurfObject newThing = new SurfObject("example-Thing");
		newThing.setPropertyValue("description", "a new thing");
		these.add(newThing);
		final SurfObject another = new SurfObject();
		another.setPropertyValue("description", "yet another thing");
		these.add(another);
		//TODO circular references: these.add(these);
		root.setPropertyValue("set", these);
		assertThat(root, equalTo(parseTestResource(OK_LABELS_RESOURCE_NAME).get())); //verify the test data
		//test with generated references
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(root);
			//TODO fix circular references; both the parser and serializer seem to support them --- the difficulty is comparing them! 
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_LABELS_RESOURCE_NAME)));
		}
	}

}
