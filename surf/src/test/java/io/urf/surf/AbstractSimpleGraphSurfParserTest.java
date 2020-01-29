/*
 * Copyright © 2016-2018 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.java.Bytes.*;
import static io.urf.surf.SurfTestResources.*;

import org.junit.*;

import com.globalmentor.itu.TelephoneNumber;
import com.globalmentor.java.CodePointCharacter;
import com.globalmentor.net.ContentType;
import com.globalmentor.net.EmailAddress;

/**
 * Abstract tests of a SURF parser that produces a graph of simple objects.
 * @param <SO> The type of SURF object the parser uses.
 * @author Garret Wilson
 */
public abstract class AbstractSimpleGraphSurfParserTest<SO> {

	/**
	 * Loads and parses an object graph by parsing the indicated SURF document resource.
	 * <p>
	 * The default implementation defaults to {@link #parseTestResource(InputStream)}.
	 * @param testResourceName The name of the SURF document resource for testing, relative to {@link AbstractSimpleGraphSurfParserTest}.
	 * @return The optional resource instance parsed from the named SURF document resource.
	 */
	protected Optional<Object> parseTestResource(@Nonnull final String testResourceName) throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(testResourceName)) {
			return parseTestResource(inputStream);
		}
	}

	/**
	 * Loads and parses an object graph by parsing the indicated SURF document resource.
	 * @param inputStream The input stream containing the SURF document for testing.
	 * @return The optional resource instance parsed from the given SURF document input stream.
	 */
	protected abstract Optional<Object> parseTestResource(@Nonnull final InputStream inputStream) throws IOException;

	/** @return The type of class used by the parser to represent a SURF object. */
	protected abstract Class<SO> getSurfObjectClass(); //TODO rename; remove "SURF" from name

	/**
	 * Indicates the SURF object type.
	 * @param surfObject The parsed SURF object.
	 * @return The handle of the SURF object type.
	 */
	public abstract Optional<String> getTypeHandle(@Nonnull final SO surfObject);

	/**
	 * Returns the number of SURF object properties.
	 * @param surfObject The parsed SURF object.
	 * @return The number of properties a SURF object has.
	 */
	protected abstract int getPropertyCount(@Nonnull final SO surfObject);

	/**
	 * Retrieves the value of a SURF object property by the property handle. As a convenience, the property value is automatically cast to that expected.
	 * @param <T> The type of object expected to be returned.
	 * @param surfObject The parsed SURF object.
	 * @param propertyHandle The handle of the property.
	 * @return The value of the property, if any.
	 */
	public abstract <T> Optional<T> getPropertyValue(@Nonnull final SO surfObject, @Nonnull final String propertyHandle);

	//identifiers

	//TODO create bad test with handles containing subsequent dashes
	//TODO create bad test with non-absolute-URI tag
	//TODO create bad test with tag with fragment
	//TODO create bad test with ID but no type

	//simple files

	/** @see SurfTestResources#OK_SIMPLE_RESOURCE_NAMES */
	@Test
	public void testOkSimpleResources() throws IOException {
		for(final String okSimpleResourceName : OK_SIMPLE_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okSimpleResourceName);
			assertThat(okSimpleResourceName, object, isPresent());
			assertThat(okSimpleResourceName, object, isPresentAnd(instanceOf(getSurfObjectClass())));
			@SuppressWarnings("unchecked")
			final SO resource = (SO)object.get();
			assertThat(okSimpleResourceName, getTypeHandle(resource), not(isPresent()));
			assertThat(okSimpleResourceName, getPropertyCount(resource), is(0));
		}
	}

	//TODO add test for bad document with content after root resource; make sure this prohibition gets into the spec 

	//#objects

	/** @see SurfTestResources#OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkObjectNoProperties() throws IOException {
		for(final String okObjectNoPropertiesResourceName : OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okObjectNoPropertiesResourceName);
			assertThat(okObjectNoPropertiesResourceName, object, isPresent());
			assertThat(okObjectNoPropertiesResourceName, object, isPresentAnd(instanceOf(getSurfObjectClass())));
			@SuppressWarnings("unchecked")
			final SO resource = (SO)object.get();
			assertThat(okObjectNoPropertiesResourceName, getTypeHandle(resource), not(isPresent()));
			assertThat(okObjectNoPropertiesResourceName, getPropertyCount(resource), is(0));
		}
	}

	/** @see SurfTestResources#OK_OBJECT_ONE_PROPERTY_RESOURCE_NAMES */
	@Test
	public void testOkObjectOneProperty() throws IOException {
		for(final String okObjectOnePropertyResourceName : OK_OBJECT_ONE_PROPERTY_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okObjectOnePropertyResourceName);
			assertThat(okObjectOnePropertyResourceName, object, isPresent());
			assertThat(okObjectOnePropertyResourceName, object, isPresentAnd(instanceOf(getSurfObjectClass())));
			@SuppressWarnings("unchecked")
			final SO resource = (SO)object.get();
			assertThat(okObjectOnePropertyResourceName, getTypeHandle(resource), not(isPresent()));
			assertThat(okObjectOnePropertyResourceName, getPropertyCount(resource), is(1));
			assertThat(okObjectOnePropertyResourceName, getPropertyValue(resource, "one"), isPresent());
			assertThat(okObjectOnePropertyResourceName, getPropertyValue(resource, "one"), isPresentAndIs("one"));
			assertThat(okObjectOnePropertyResourceName, getPropertyValue(resource, "two"), not(isPresent()));
		}
	}

	/** @see SurfTestResources#OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES */
	@Test
	public void testOkObjectTwoProperties() throws IOException {
		for(final String okObjectTwoPropertiesResourceName : OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okObjectTwoPropertiesResourceName);
			assertThat(okObjectTwoPropertiesResourceName, object, isPresent());
			assertThat(okObjectTwoPropertiesResourceName, object, isPresentAnd(instanceOf(getSurfObjectClass())));
			@SuppressWarnings("unchecked")
			final SO resource = (SO)object.get();
			assertThat(okObjectTwoPropertiesResourceName, getTypeHandle(resource), not(isPresent()));
			assertThat(okObjectTwoPropertiesResourceName, getPropertyCount(resource), is(2));
			assertThat(okObjectTwoPropertiesResourceName, getPropertyValue(resource, "one"), isPresent());
			assertThat(okObjectTwoPropertiesResourceName, getPropertyValue(resource, "one"), isPresentAndIs("one"));
			assertThat(okObjectTwoPropertiesResourceName, getPropertyValue(resource, "two"), isPresent());
			assertThat(okObjectTwoPropertiesResourceName, getPropertyValue(resource, "two"), isPresentAndIs("two"));
			assertThat(okObjectTwoPropertiesResourceName, getPropertyValue(resource, "three"), not(isPresent()));
		}
	}

	/** @see SurfTestResources#OK_OBJECT_TYPE_RESOURCE_NAMES */
	@Test
	public void testOkObjectType() throws IOException {
		for(final String okObjectTypeResourceName : OK_OBJECT_TYPE_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okObjectTypeResourceName);
			assertThat(okObjectTypeResourceName, object, isPresent());
			assertThat(okObjectTypeResourceName, object, isPresentAnd(instanceOf(getSurfObjectClass())));
			@SuppressWarnings("unchecked")
			final SO resource = (SO)object.get();
			assertThat(okObjectTypeResourceName, getTypeHandle(resource), isPresentAndIs("example-FooBar"));
			assertThat(okObjectTypeResourceName, getPropertyCount(resource), is(0));
		}
	}

	//TODO add error tests of duplicated property names

	//#literals

	//##binary

	/** @see SurfTestResources#OK_BINARY_RESOURCE_NAME */
	@Test
	public void testOkBinary() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_BINARY_RESOURCE_NAME).get();

		assertThat(getPropertyValue(resource, "count"), isPresentAndIs(new byte[] {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99,
				(byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd, (byte)0xee, (byte)0xff}));

		assertThat(getPropertyValue(resource, "rfc4648Example1"), isPresentAndIs(new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03, (byte)0xd9, 0x7e}));
		assertThat(getPropertyValue(resource, "rfc4648Example2"), isPresentAndIs(new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03, (byte)0xd9}));
		assertThat(getPropertyValue(resource, "rfc4648Example3"), isPresentAndIs(new byte[] {0x14, (byte)0xfb, (byte)0x9c, 0x03}));

		assertThat(getPropertyValue(resource, "rfc4648TestVector1"), isPresentAndIs(NO_BYTES));
		assertThat(getPropertyValue(resource, "rfc4648TestVector2"), isPresentAndIs("f".getBytes(US_ASCII)));
		assertThat(getPropertyValue(resource, "rfc4648TestVector3"), isPresentAndIs("fo".getBytes(US_ASCII)));
		assertThat(getPropertyValue(resource, "rfc4648TestVector4"), isPresentAndIs("foo".getBytes(US_ASCII)));
		assertThat(getPropertyValue(resource, "rfc4648TestVector5"), isPresentAndIs("foob".getBytes(US_ASCII)));
		assertThat(getPropertyValue(resource, "rfc4648TestVector6"), isPresentAndIs("fooba".getBytes(US_ASCII)));
		assertThat(getPropertyValue(resource, "rfc4648TestVector7"), isPresentAndIs("foobar".getBytes(US_ASCII)));
	}

	//TODO add bad tests with padding; see commit history of `ok-binary.surf` for examples

	//##Boolean

	/** @see SurfTestResources#OK_BOOLEAN_FALSE_RESOURCE_NAME */
	@Test
	public void testOkBooleanFalse() throws IOException {
		final Optional<Object> object = parseTestResource(OK_BOOLEAN_FALSE_RESOURCE_NAME);
		assertThat(object, isPresent());
		assertThat(object, isPresentAndIs(Boolean.FALSE));
	}

	/** @see SurfTestResources#OK_BOOLEAN_TRUE_RESOURCE_NAME */
	@Test
	public void testOkBooleanTrue() throws IOException {
		final Optional<Object> object = parseTestResource(OK_BOOLEAN_TRUE_RESOURCE_NAME);
		assertThat(object, isPresent());
		assertThat(object, isPresentAndIs(Boolean.TRUE));
	}

	//##character

	/** @see SurfTestResources#OK_CHARACTERS_RESOURCE_NAME */
	@Test
	public void testOkCharacters() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_CHARACTERS_RESOURCE_NAME).get();
		assertThat(getPropertyValue(resource, "foo"), isPresentAndIs(CodePointCharacter.of('|')));
		assertThat(getPropertyValue(resource, "quote"), isPresentAndIs(CodePointCharacter.of('"')));
		assertThat(getPropertyValue(resource, "apostrophe"), isPresentAndIs(CodePointCharacter.of('\'')));
		assertThat(getPropertyValue(resource, "backslash"), isPresentAndIs(CodePointCharacter.of('\\')));
		assertThat(getPropertyValue(resource, "solidus"), isPresentAndIs(CodePointCharacter.of('/')));
		assertThat(getPropertyValue(resource, "backspace"), isPresentAndIs(CodePointCharacter.of('\b')));
		assertThat(getPropertyValue(resource, "ff"), isPresentAndIs(CodePointCharacter.of('\f')));
		assertThat(getPropertyValue(resource, "lf"), isPresentAndIs(CodePointCharacter.of('\n')));
		assertThat(getPropertyValue(resource, "cr"), isPresentAndIs(CodePointCharacter.of('\r')));
		assertThat(getPropertyValue(resource, "tab"), isPresentAndIs(CodePointCharacter.of('\t')));
		assertThat(getPropertyValue(resource, "vtab"), isPresentAndIs(CodePointCharacter.of('\u000B')));
		assertThat(getPropertyValue(resource, "devanagari-ma"), isPresentAndIs(CodePointCharacter.of('\u092E')));
		assertThat(getPropertyValue(resource, "devanagari-maEscaped"), isPresentAndIs(CodePointCharacter.of('\u092E')));
		assertThat(getPropertyValue(resource, "tearsOfJoy"), isPresentAndIs(CodePointCharacter.of(0x1F602)));
		assertThat(getPropertyValue(resource, "tearsOfJoyEscaped"), isPresentAndIs(CodePointCharacter.of(0x1F602)));
	}

	//TODO add bad tests with empty character
	//TODO add bad tests with control characters
	//TODO add bad tests to prevent escaping normal characters 
	//TODO add bad tests with invalid surrogate character sequences

	//##email address

	/** @see SurfTestResources#OK_EMAIL_ADDRESSES_RESOURCE_NAME */
	@Test
	public void testOkEmailAddresses() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_EMAIL_ADDRESSES_RESOURCE_NAME).get();
		assertThat(getPropertyValue(resource, "example"), isPresentAndIs(EmailAddress.fromString("jdoe@example.com")));
		assertThat(getPropertyValue(resource, "dot"), isPresentAndIs(EmailAddress.fromString("jane.doe@example.com")));
		assertThat(getPropertyValue(resource, "tag"), isPresentAndIs(EmailAddress.fromString("jane.doe+tag@example.com")));
		assertThat(getPropertyValue(resource, "dash"), isPresentAndIs(EmailAddress.fromString("jane.doe-foo@example.com")));
		assertThat(getPropertyValue(resource, "x"), isPresentAndIs(EmailAddress.fromString("x@example.com")));
		assertThat(getPropertyValue(resource, "dashedDomain"), isPresentAndIs(EmailAddress.fromString("foo-bar@strange-example.com")));
		assertThat(getPropertyValue(resource, "longTLD"), isPresentAndIs(EmailAddress.fromString("example@s.solutions")));
	}

	//TODO add bad test with no content
	//TODO add bad test with ending dot (after clarifying whether this is allowed; it doesn't seem to be in RFC 5322)
	//TODO add bad test with comments
	//TODO add bad test with folding whitespace
	//TODO add bad tests from https://en.wikipedia.org/wiki/Email_address

	//##IRI

	/** @see SurfTestResources#OK_IRIS_RESOURCE_NAME */
	@Test
	public void testOkIris() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_IRIS_RESOURCE_NAME).get();
		assertThat(getPropertyValue(resource, "example"), isPresentAndIs(URI.create("http://www.example.com/")));
		assertThat(getPropertyValue(resource, "iso_8859_1"), isPresentAndIs(URI.create("http://www.example.org/Dürst")));
		assertThat(getPropertyValue(resource, "encodedForbidden"), isPresentAndIs(URI.create("http://xn--99zt52a.example.org/%E2%80%AE")));
		assertThat(getPropertyValue(resource, "encodedForbidden"), isPresentAndIs(URI.create("http://xn--99zt52a.example.org/%E2%80%AE")));
		assertThat(getPropertyValue(resource, "mailto"), isPresentAndIs(URI.create("mailto:jdoe@example.com")));
		assertThat(getPropertyValue(resource, "tel"), isPresentAndIs(URI.create("tel:+12015550123")));
		assertThat(getPropertyValue(resource, "urn_uuid"), isPresentAndIs(URI.create("urn:uuid:5623962b-22b1-4680-ae1c-7174a46144fc")));
		assertThat(getPropertyValue(resource, "isbn"), isPresentAndIs(URI.create("urn:isbn:0-395-36341-1")));
	}

	//TODO add tests for extended characters; bad IRIs (such as a non-absolute IRI); a test containing U+202E, as described in RFC 3987 3.2.1

	/** @see SurfTestResources#OK_MEDIA_TYPES_RESOURCE_NAME */
	@Test
	public void testOkMediaType() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_MEDIA_TYPES_RESOURCE_NAME).get();
		assertThat(getPropertyCount(resource), is(14)); //make sure we're up-to-date with the latest test
		assertThat(getPropertyValue(resource, "xml"), isPresentAndIs(ContentType.of("text", "xml")));
		assertThat(getPropertyValue(resource, "textXml"), isPresentAndIs(ContentType.of("text", "xml")));
		assertThat(getPropertyValue(resource, "markdowns"), isPresentAnd(hasSize(7)));
		final ContentType markdownUtf8 = ContentType.of("text", "markdown", ContentType.Parameter.CHARSET_UTF_8);
		for(final ContentType contentType : this.<List<ContentType>>getPropertyValue(resource, "markdowns").get()) {
			assertThat(contentType, is(markdownUtf8));
		}
		assertThat(getPropertyValue(resource, "json"), isPresentAndIs(ContentType.of("application", "json")));
		assertThat(getPropertyValue(resource, "png"), isPresentAndIs(ContentType.of("image", "png")));
		assertThat(getPropertyValue(resource, "threeParams"), isPresentAndIs(ContentType.of("text", "plain", ContentType.Parameter.CHARSET_US_ASCII,
				ContentType.Parameter.of("foo", "bar"), ContentType.Parameter.of("test", "example"))));
		assertThat(getPropertyValue(resource, "duplicateNames"), isPresentAndIs(ContentType.of("text", "plain", ContentType.Parameter.CHARSET_US_ASCII,
				ContentType.Parameter.of("test", "foo"), ContentType.Parameter.of("test", "bar"))));
		assertThat(getPropertyValue(resource, "special"), isPresentAndIs(
				ContentType.of("text", "plain", ContentType.Parameter.CHARSET_US_ASCII, ContentType.Parameter.of("test", "(foo)<bar>@\",;:\\/[foobar]?="))));
		assertThat(getPropertyValue(resource, "escaped"),
				isPresentAndIs(ContentType.of("text", "plain", ContentType.Parameter.CHARSET_US_ASCII, ContentType.Parameter.of("test", "foo\"bar\tandxmore\\stuff"))));
		assertThat(getPropertyValue(resource, "webForm"), isPresentAndIs(ContentType.of("application", "x-www-form-urlencoded")));
		assertThat(getPropertyValue(resource, "docx"),
				isPresentAndIs(ContentType.of("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")));
		assertThat(getPropertyValue(resource, "odt"), isPresentAndIs(ContentType.of("application", "vnd.oasis.opendocument.text")));
		assertThat(getPropertyValue(resource, "multipartForm"), isPresentAndIs(
				ContentType.of("multipart", "form-data", ContentType.Parameter.CHARSET_UTF_8, ContentType.Parameter.of("boundary", "q1w2e3r4ty:9-5xyz"))));
		assertThat(getPropertyValue(resource, "jsonApi"), isPresentAndIs(ContentType.of("application", "vnd.api+json")));
	}

	//TODO add bad tests with illegal type, subtype, and parameter names

	//##number

	//TODO create several single-number documents to test various number components ending the document, e.g. 123 and 123.456

	/** @see SurfTestResources#OK_NUMBERS_RESOURCE_NAME */
	@Test
	public void testOkNumbers() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_NUMBERS_RESOURCE_NAME).get();
		assertThat(getPropertyValue(resource, "zero"), isPresentAndIs(Long.valueOf(0)));
		assertThat(getPropertyValue(resource, "zeroFraction"), isPresentAndIs(Double.valueOf(0)));
		assertThat(getPropertyValue(resource, "one"), isPresentAndIs(Long.valueOf(1)));
		assertThat(getPropertyValue(resource, "oneFraction"), isPresentAndIs(Double.valueOf(1)));
		assertThat(getPropertyValue(resource, "integer"), isPresentAndIs(Long.valueOf(123)));
		assertThat(getPropertyValue(resource, "negative"), isPresentAndIs(Long.valueOf(-123)));
		assertThat(getPropertyValue(resource, "long"), isPresentAndIs(Long.valueOf(3456789123L)));
		assertThat(getPropertyValue(resource, "maxLong"), isPresentAndIs(Long.valueOf(Long.MAX_VALUE)));
		assertThat(getPropertyValue(resource, "big"), isPresentAndIs(new BigInteger(Long.toString(Long.MAX_VALUE)).add(BigInteger.ONE)));
		assertThat(getPropertyValue(resource, "bigger"), isPresentAndIs(new BigInteger("100000000000000000000"))); //10^20
		assertThat(getPropertyValue(resource, "fraction"), isPresentAndIs(Double.valueOf(12345.6789)));
		assertThat(getPropertyValue(resource, "scientific1"), isPresentAndIs(Double.valueOf(1.23e+4)));
		assertThat(getPropertyValue(resource, "scientific2"), isPresentAndIs(Double.valueOf(12.3e-4)));
		assertThat(getPropertyValue(resource, "scientific3"), isPresentAndIs(Double.valueOf(-123.4e+5)));
		assertThat(getPropertyValue(resource, "scientific4"), isPresentAndIs(Double.valueOf(-321.45e-12)));
		assertThat(getPropertyValue(resource, "scientific5"), isPresentAndIs(Double.valueOf(45.67e+89)));
		//These BigDecimal tests require identical scale, which is why "$0.0" isn't compared to BigDecimal.ZERO.
		//If value equivalence regardless of scale is desired, use BigDecimal.compare().
		assertThat(getPropertyValue(resource, "decimal"), isPresentAndIs(new BigDecimal("0.3")));
		assertThat(getPropertyValue(resource, "money"), isPresentAndIs(new BigDecimal("1.23")));
		assertThat(getPropertyValue(resource, "decimalZero"), isPresentAndIs(BigDecimal.ZERO));
		assertThat(getPropertyValue(resource, "decimalZeroFraction"), isPresentAndIs(new BigDecimal("0.0")));
		assertThat(getPropertyValue(resource, "decimalOne"), isPresentAndIs(BigDecimal.ONE));
		assertThat(getPropertyValue(resource, "decimalOneFraction"), isPresentAndIs(new BigDecimal("1.0")));
		assertThat(getPropertyValue(resource, "decimalInteger"), isPresentAndIs(new BigDecimal("123")));
		assertThat(getPropertyValue(resource, "decimalNegative"), isPresentAndIs(new BigDecimal("-123")));
		assertThat(getPropertyValue(resource, "decimalLong"), isPresentAndIs(new BigDecimal("3456789123")));
		assertThat(getPropertyValue(resource, "decimalFraction"), isPresentAndIs(new BigDecimal("12345.6789")));
		assertThat(getPropertyValue(resource, "decimalScientific1"), isPresentAndIs(new BigDecimal("1.23e+4")));
		assertThat(getPropertyValue(resource, "decimalScientific2"), isPresentAndIs(new BigDecimal("12.3e-4")));
		assertThat(getPropertyValue(resource, "decimalScientific3"), isPresentAndIs(new BigDecimal("-123.4e+5")));
		assertThat(getPropertyValue(resource, "decimalScientific4"), isPresentAndIs(new BigDecimal("-321.45e-12")));
		assertThat(getPropertyValue(resource, "decimalScientific5"), isPresentAndIs(new BigDecimal("45.67e+89")));
	}

	//##regular expression

	/** @see SurfTestResources#OK_REGULAR_EXPRESSIONS_RESOURCE_NAME */
	@Test
	public void testOkRegularExpressions() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_REGULAR_EXPRESSIONS_RESOURCE_NAME).get();
		assertThat(getPropertyValue(resource, "empty").map(Pattern.class::cast).map(Pattern::pattern), isPresentAndIs(""));
		assertThat(getPropertyValue(resource, "abc").map(Pattern.class::cast).map(Pattern::pattern), isPresentAndIs("abc"));
		assertThat(getPropertyValue(resource, "regexEscape").map(Pattern.class::cast).map(Pattern::pattern), isPresentAndIs("ab\\.c"));
		assertThat(getPropertyValue(resource, "doubleBackslash").map(Pattern.class::cast).map(Pattern::pattern), isPresentAndIs("\\\\"));
		assertThat(getPropertyValue(resource, "slash").map(Pattern.class::cast).map(Pattern::pattern), isPresentAndIs("/"));
	}

	//TODO add bad tests with control characters

	//##string

	/** @see SurfTestResources#OK_STRING_FOOBAR_RESOURCE_NAME */
	@Test
	public void testOkStringFoobar() throws IOException {
		final Optional<Object> object = parseTestResource(OK_STRING_FOOBAR_RESOURCE_NAME);
		assertThat(object, isPresent());
		assertThat(object, isPresentAndIs("foobar"));
	}

	/** @see SurfTestResources#OK_STRINGS_RESOURCE_NAME */
	@Test
	public void testOkStrings() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_STRINGS_RESOURCE_NAME).get();
		assertThat(getPropertyValue(resource, "empty"), isPresentAndIs(""));
		assertThat(getPropertyValue(resource, "foo"), isPresentAndIs("bar"));
		assertThat(getPropertyValue(resource, "quote"), isPresentAndIs("\""));
		assertThat(getPropertyValue(resource, "backslash"), isPresentAndIs("\\"));
		assertThat(getPropertyValue(resource, "solidus"), isPresentAndIs("/"));
		assertThat(getPropertyValue(resource, "ff"), isPresentAndIs("\f"));
		assertThat(getPropertyValue(resource, "lf"), isPresentAndIs("\n"));
		assertThat(getPropertyValue(resource, "cr"), isPresentAndIs("\r"));
		assertThat(getPropertyValue(resource, "tab"), isPresentAndIs("\t"));
		assertThat(getPropertyValue(resource, "vtab"), isPresentAndIs("\u000B"));
		assertThat(getPropertyValue(resource, "devanagari-ma"), isPresentAndIs("\u092E"));
		assertThat(getPropertyValue(resource, "devanagari-maEscaped"), isPresentAndIs("\u092E"));
		assertThat(getPropertyValue(resource, "tearsOfJoy"), isPresentAndIs(String.valueOf(Character.toChars(0x1F602))));
		assertThat(getPropertyValue(resource, "tearsOfJoyEscaped"), isPresentAndIs(String.valueOf(Character.toChars(0x1F602))));
	}

	//TODO add bad tests with control characters
	//TODO add bad tests to prevent escaping normal characters 
	//TODO add bad tests with invalid surrogate character sequences

	//##telephone number

	/** @see SurfTestResources#OK_TELEPHONE_NUMBERS_RESOURCE_NAME */
	@Test
	public void testOkTelephoneNumbers() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_TELEPHONE_NUMBERS_RESOURCE_NAME).get();
		assertThat(getPropertyValue(resource, "rfc3966Example"), isPresentAndIs(TelephoneNumber.parse("+12015550123")));
		assertThat(getPropertyValue(resource, "brazil"), isPresentAndIs(TelephoneNumber.parse("+552187654321")));
	}

	//TODO add bad tests with no digits
	//TODO add bad tests with visual separators 

	//##temporal

	/** @see SurfTestResources#OK_TEMPORALS_RESOURCE_NAME */
	@Test
	public void testOkTemporals() throws IOException {
		@SuppressWarnings("unchecked")
		final SO resource = (SO)parseTestResource(OK_TEMPORALS_RESOURCE_NAME).get();
		assertThat(getPropertyValue(resource, "instant"), isPresentAndIs(Instant.parse("2017-02-12T23:29:18.829Z")));
		assertThat(getPropertyValue(resource, "zonedDateTime"), isPresentAndIs(ZonedDateTime.parse("2017-02-12T15:29:18.829-08:00[America/Los_Angeles]")));
		assertThat(getPropertyValue(resource, "offsetDateTime"), isPresentAndIs(OffsetDateTime.parse("2017-02-12T15:29:18.829-08:00")));
		assertThat(getPropertyValue(resource, "offsetTime"), isPresentAndIs(OffsetTime.parse("15:29:18.829-08:00")));
		assertThat(getPropertyValue(resource, "localDateTime"), isPresentAndIs(LocalDateTime.parse("2017-02-12T15:29:18.829")));
		assertThat(getPropertyValue(resource, "localDate"), isPresentAndIs(LocalDate.parse("2017-02-12")));
		assertThat(getPropertyValue(resource, "localTime"), isPresentAndIs(LocalTime.parse("15:29:18.829")));
		assertThat(getPropertyValue(resource, "yearMonth"), isPresentAndIs(YearMonth.parse("2017-02")));
		assertThat(getPropertyValue(resource, "monthDay"), isPresentAndIs(MonthDay.parse("--02-12")));
		assertThat(getPropertyValue(resource, "year"), isPresentAndIs(Year.parse("2017")));
	}

	//##UUID

	/** @see SurfTestResources#OK_UUID_RESOURCE_NAME */
	@Test
	public void testOkUuid() throws IOException {
		final Optional<Object> object = parseTestResource(OK_UUID_RESOURCE_NAME);
		assertThat(object, isPresent());
		assertThat(object, isPresentAndIs(UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")));
	}

	//#collections

	//##list

	/** @see SurfTestResources#OK_LIST_EMPTY_RESOURCE_NAMES */
	@Test
	public void testOkListEmpty() throws IOException {
		for(final String okListEmptyResourceName : OK_LIST_EMPTY_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okListEmptyResourceName);
			assertThat(okListEmptyResourceName, object, isPresent());
			assertThat(okListEmptyResourceName, object, isPresentAnd(instanceOf(List.class)));
			final List<?> list = (List<?>)object.get();
			assertThat(okListEmptyResourceName, list, hasSize(0));
		}
	}

	/** @see SurfTestResources#OK_LIST_ONE_ITEM_RESOURCE_NAMES */
	@Test
	public void testOkListOneItem() throws IOException {
		for(final String okListOneItemResourceName : OK_LIST_ONE_ITEM_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okListOneItemResourceName);
			assertThat(okListOneItemResourceName, object, isPresent());
			assertThat(okListOneItemResourceName, object, isPresentAnd(instanceOf(List.class)));
			final List<?> list = (List<?>)object.get();
			assertThat(okListOneItemResourceName, list, hasSize(1));
			assertThat(okListOneItemResourceName, list.get(0), is("one"));
		}
	}

	/** @see SurfTestResources#OK_LIST_TWO_ITEMS_RESOURCE_NAMES */
	@Test
	public void testOkListTwoItems() throws IOException {
		for(final String okListTwoItemsResourceName : OK_LIST_TWO_ITEMS_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okListTwoItemsResourceName);
			assertThat(okListTwoItemsResourceName, object, isPresent());
			assertThat(okListTwoItemsResourceName, object, isPresentAnd(instanceOf(List.class)));
			final List<?> list = (List<?>)object.get();
			assertThat(okListTwoItemsResourceName, list, hasSize(2));
			assertThat(okListTwoItemsResourceName, list.get(0), is("one"));
			assertThat(okListTwoItemsResourceName, list.get(1), is("two"));
		}
	}

	//TODO create tests for bad properties, such as double list item separators

	//#map

	/** @see SurfTestResources#OK_MAPS_RESOURCE_NAME */
	@Test
	public void testOkMaps() throws IOException {
		final Optional<Object> object = parseTestResource(OK_MAPS_RESOURCE_NAME);
		assertThat(object, isPresent());
		assertThat(object, isPresentAnd(instanceOf(Map.class)));
		final Map<?, ?> map = (Map<?, ?>)object.get();
		assertThat(map.size(), is(10));
		assertThat(map.get("foo"), is("bar"));
		assertThat(map.get(123L), is("number"));
		assertThat(map.get(false), is("Boolean"));
		assertThat(map.get(true), is("Boolean"));
		assertThat(map.get(Arrays.asList(1L, 2L, 3L)), is(new BigDecimal("1.23")));
		final Map<Object, Object> pingPong = new HashMap<>();
		pingPong.put("ping", Arrays.asList(CodePointCharacter.of('p'), CodePointCharacter.of('o'), CodePointCharacter.of('n'), CodePointCharacter.of('g')));
		assertThat(map.get("map"), is(pingPong));
		assertThat(map.get(new HashSet<Object>(Arrays.asList("foo", false))), is(true));
		//TODO bring back map key test in a general way to work for SURF and TURF parsers
		//		assertThat(map.get(new SurfObject("Game", "pingpong")), is("ping pong"));
		//		//find the bull's eye map entry by iteration to avoid relying on object key equality
		//		SurfObject bullsEye = null;
		//		for(final Map.Entry<?, ?> entry : map.entrySet()) {
		//			if(entry.getValue().equals("Bull's Eye")) {
		//				assertThat(entry.getKey(), instanceOf(getSurfObjectClass()));
		//				bullsEye = (SurfObject)entry.getKey();
		//				break;
		//			}
		//		}
		//		assertThat(bullsEye.getTypeHandle(), isPresentAndIs("Point"));
		//		assertThat(bullsEye.getPropertyValue("x"), isPresentAndIs(0));
		//		assertThat(bullsEye.getPropertyValue("y"), isPresentAndIs(0));
		final Map<String, String> abbrMap = new HashMap<>();
		abbrMap.put("ITTF", "International Table Tennis Federation");
		assertThat(map.get(abbrMap), is("abbr"));
	}

	/** @see SurfTestResources#OK_MAP_EMPTY_RESOURCE_NAMES */
	@Test
	public void testOkMapEmpty() throws IOException {
		for(final String okMapEmptyResourceName : OK_MAP_EMPTY_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okMapEmptyResourceName);
			assertThat(okMapEmptyResourceName, object, isPresent());
			assertThat(okMapEmptyResourceName, object, isPresentAnd(instanceOf(Map.class)));
			final Map<?, ?> map = (Map<?, ?>)object.get();
			assertThat(map.size(), is(0));
		}
	}

	/** @see SurfTestResources#OK_MAP_ONE_ENTRY_RESOURCE_NAMES */
	@Test
	public void testOkMapOneEntry() throws IOException {
		for(final String okMapOneEntryResourceName : OK_MAP_ONE_ENTRY_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okMapOneEntryResourceName);
			assertThat(okMapOneEntryResourceName, object, isPresent());
			assertThat(okMapOneEntryResourceName, object, isPresentAnd(instanceOf(Map.class)));
			final Map<?, ?> map = (Map<?, ?>)object.get();
			assertThat(map.size(), is(1));
			assertThat(okMapOneEntryResourceName, map.containsKey("I"), is(true));
			assertThat(okMapOneEntryResourceName, map.get("I"), is("one"));
			assertThat(okMapOneEntryResourceName, map.containsKey("II"), is(false));
		}
	}

	/** @see SurfTestResources#OK_MAP_TWO_ENTRIES_RESOURCE_NAMES */
	@Test
	public void testOkMapTwoEntries() throws IOException {
		for(final String okMapTwoEntriesResourceName : OK_MAP_TWO_ENTRIES_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okMapTwoEntriesResourceName);
			assertThat(okMapTwoEntriesResourceName, object, isPresent());
			assertThat(okMapTwoEntriesResourceName, object, isPresentAnd(instanceOf(Map.class)));
			final Map<?, ?> map = (Map<?, ?>)object.get();
			assertThat(map.size(), is(2));
			assertThat(okMapTwoEntriesResourceName, map.containsKey("I"), is(true));
			assertThat(okMapTwoEntriesResourceName, map.get("I"), is("one"));
			assertThat(okMapTwoEntriesResourceName, map.containsKey("II"), is(true));
			assertThat(okMapTwoEntriesResourceName, map.get("II"), is("two"));
			assertThat(okMapTwoEntriesResourceName, map.containsKey("III"), is(false));
		}
	}

	//##set

	/** @see SurfTestResources#OK_SET_EMPTY_RESOURCE_NAMES */
	@Test
	public void testOkSetEmpty() throws IOException {
		for(final String okSetEmptyResourceName : OK_SET_EMPTY_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okSetEmptyResourceName);
			assertThat(okSetEmptyResourceName, object, isPresent());
			assertThat(okSetEmptyResourceName, object, isPresentAnd(instanceOf(Set.class)));
			final Set<?> set = (Set<?>)object.get();
			assertThat(okSetEmptyResourceName, set, hasSize(0));
		}
	}

	/** @see SurfTestResources#OK_SET_ONE_ITEM_RESOURCE_NAMES */
	@Test
	public void testOkSetOneItem() throws IOException {
		for(final String okSetOneItemResourceName : OK_SET_ONE_ITEM_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okSetOneItemResourceName);
			assertThat(okSetOneItemResourceName, object, isPresent());
			assertThat(okSetOneItemResourceName, object, isPresentAnd(instanceOf(Set.class)));
			final Set<?> set = (Set<?>)object.get();
			assertThat(okSetOneItemResourceName, set, equalTo(new HashSet<Object>(asList("one"))));
		}
	}

	/** @see SurfTestResources#OK_SET_TWO_ITEMS_RESOURCE_NAMES */
	@Test
	public void testOkSetTwoItems() throws IOException {
		for(final String okSetTwoItemsResourceName : OK_SET_TWO_ITEMS_RESOURCE_NAMES) {
			final Optional<Object> object = parseTestResource(okSetTwoItemsResourceName);
			assertThat(okSetTwoItemsResourceName, object, isPresent());
			assertThat(okSetTwoItemsResourceName, object, isPresentAnd(instanceOf(Set.class)));
			final Set<?> set = (Set<?>)object.get();
			assertThat(okSetTwoItemsResourceName, set, hasSize(2));
			assertThat(okSetTwoItemsResourceName, set, equalTo(new HashSet<Object>(asList("one", "two"))));
		}
	}

	//TODO create tests for duplicate items and double list item separators

	//#labels

	//Labels are tested in SURF test subclass, because the approach for finding labels
	//and especially aliases will likely vary among parsers.

	//TODO create test with bad labels, such as aliases with whitespace, tags for null, and redefined labels
}
