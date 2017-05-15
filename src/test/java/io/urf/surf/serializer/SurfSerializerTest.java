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
import java.util.*;

import javax.annotation.*;

import static io.urf.surf.test.SurfTestResources.*;
import static java.util.Arrays.*;
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

}
