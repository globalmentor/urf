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

package io.urf.surf.serializer;

import static org.junit.Assert.*;

import java.io.*;
import java.math.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;

import javax.annotation.*;

import static com.globalmentor.java.Bytes.*;
import static io.urf.surf.test.SurfTestResources.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;

import com.globalmentor.java.CodePointCharacter;
import com.globalmentor.net.EmailAddress;

import io.clogr.Clogged;
import io.urf.surf.parser.*;
import io.urf.surf.test.SurfTestResources;

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

		surfObject.setPropertyValue("count", new byte[] {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99, (byte)0xaa,
				(byte)0xbb, (byte)0xcc, (byte)0xdd, (byte)0xee, (byte)0xff});

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
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_EMAIL_ADDRESSES_RESOURCE_NAME)));
		}
	}

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
		for(final boolean formatted : asList(false, true)) {
			final SurfSerializer serializer = new SurfSerializer();
			serializer.setFormatted(formatted);
			final String serialization = serializer.serialize(surfObject);
			assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_NUMBERS_RESOURCE_NAME)));
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

}
