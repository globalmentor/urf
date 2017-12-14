/*
 * Copyright © 2016–2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static com.globalmentor.io.ReaderParser.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.net.URIs.*;
import static io.urf.surf.SURF.*;
import static io.urf.surf.SURF.WHITESPACE_CHARACTERS;
import static java.util.Objects.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.*;

import com.globalmentor.io.ParseIOException;
import com.globalmentor.io.function.IOConsumer;
import com.globalmentor.iso.datetime.ISO8601;
import com.globalmentor.itu.TelephoneNumber;
import com.globalmentor.java.Characters;
import com.globalmentor.java.CodePointCharacter;
import com.globalmentor.model.UUIDs;
import com.globalmentor.net.EmailAddress;
import com.globalmentor.text.*;

import io.urf.surf.SURF.Handle;

/**
 * Simple parser for the Simple URF (SURF) document format.
 * <p>
 * This parser is meant to be used once for parsing a single SURF document. It should not be used to parse multiple documents, as it maintains parsing state.
 * </p>
 * <p>
 * The parser should be released after use so as not to leak memory of parsed resources when resources are present with tags/IDs/aliases.
 * </p>
 * <p>
 * This implementation is not thread safe.
 * </p>
 * @author Garret Wilson
 */
public class SurfParser {

	/**
	 * Represents an alias label parsed in a SURF document.
	 * @author Garret Wilson
	 */
	private final static class Alias {
		private final String string;

		private Alias(@Nonnull final String string) {
			this.string = requireNonNull(string);
		}

		@Override
		public int hashCode() {
			return string.hashCode();
		}

		@Override
		public boolean equals(final Object object) {
			if(object == this) {
				return true;
			}
			return object instanceof Alias && string.equals(((Alias)object).string);
		}

		@Override
		public String toString() {
			return string;
		}
	}

	/**
	 * The map of resources that have been given a label. Each key is either:
	 * <dl>
	 * <dt>tag</dt>
	 * <dd>A {@link URI} containing the tag IRI.</dd>
	 * <dt>ID</dt>
	 * <dd>A {@link Map.Entry} containing the type handle as {@link Map.Entry#getKey()} and the ID as {@link Map.Entry#getValue()}.</dd>
	 * <dt>alias</dt>
	 * <dd>An {@link Alias} containing the alias string.</dd>
	 * </dl>
	 */
	private final Map<Object, Object> labeledResources = new HashMap<>();

	/**
	 * Returns a parsed resource by its alias.
	 * @param alias The alias used by the resource in the document.
	 * @return The resource associated with the given alias, if any.
	 */
	public Optional<Object> findResourceByAlias(@Nonnull final String alias) {
		return Optional.ofNullable(labeledResources.get(new Alias(alias))); //TODO be more rigorous here if/when null can be aliased
	}

	/**
	 * Returns a parsed object by its tag.
	 * @param tag The global IRI identifier tag of the resource.
	 * @return The object associated with the given tag, if any.
	 */
	public Optional<SurfObject> findObjectByTag(@Nonnull final URI tag) {
		return Optional.ofNullable((SurfObject)labeledResources.get(requireNonNull(tag)));
	}

	/**
	 * Returns a parsed object by its type handle and ID.
	 * @param typeHandle The handle of the object's type.
	 * @param id The object ID for the indicated type.
	 * @return The typed object with the given ID, if any.
	 */
	public Optional<SurfObject> findObjectById(@Nonnull final String typeHandle, @Nonnull final String id) {
		return Optional.ofNullable(getObjectById(typeHandle, id));
	}

	/**
	 * Returns a parsed object by its type handle and ID.
	 * @param typeHandle The handle of the object's type.
	 * @param id The object ID for the indicated type.
	 * @return The typed object with the given ID, or <code>null</code> if no such object was found.
	 */
	protected SurfObject getObjectById(@Nonnull final String typeHandle, @Nonnull final String id) {
		//TODO switch to Java 9 Map.entry()
		return (SurfObject)labeledResources.get(new AbstractMap.SimpleImmutableEntry<String, String>(requireNonNull(typeHandle), requireNonNull(id)));
	}

	/**
	 * Parses a SURF resource from a string.
	 * <p>
	 * This is a convenience method that delegates to {@link #parse(Reader)}.
	 * </p>
	 * @param string The string containing SURF data.
	 * @return The root SURF resource, which may be empty if the SURF document was empty.
	 * @throws IOException If there was an error reading the SURF data.
	 * @throws ParseIOException if the SURF data was invalid.
	 */
	public Optional<Object> parse(@Nonnull final String string) throws IOException, ParseIOException {
		try (final Reader stringReader = new StringReader(string)) {
			return parse(stringReader);
		}
	}

	/**
	 * Parses a SURF resource from an input stream.
	 * @param inputStream The input stream containing SURF data.
	 * @return The root SURF resource, which may be empty if the SURF document was empty.
	 * @throws IOException If there was an error reading the SURF data.
	 * @throws ParseIOException if the SURF data was invalid.
	 */
	public Optional<Object> parse(@Nonnull final InputStream inputStream) throws IOException, ParseIOException {
		return parse(new LineNumberReader(new InputStreamReader(inputStream, CHARSET)));
	}

	/**
	 * Parses a SURF resource from a reader.
	 * @param reader The reader containing SURF data.
	 * @return The root SURF resource, which may be empty if the SURF document was empty.
	 * @throws IOException If there was an error reading the SURF data.
	 * @throws ParseIOException if the SURF data was invalid.
	 */
	public Optional<Object> parse(@Nonnull final Reader reader) throws IOException, ParseIOException {
		if(skipLineBreaks(reader) < 0) { //skip whitespace, comments, and line breaks; if we reached the end of the stream
			return Optional.empty(); //the SURF document is empty
		}
		final Object resource = parseResource(reader);
		checkParseIO(reader, skipLineBreaks(reader) < 0, "No content allowed after root resource.");
		return Optional.of(resource);
	}

	/**
	 * Parses a label, surrounded by label delimiters, and which is returned as one of the following:
	 * <dl>
	 * <dt>tag</dt>
	 * <dd>The tag IRI as an absolute {@link URI}.</dd>
	 * <dt>ID</dt>
	 * <dd>Some object that is neither a {@link URI} nor a {@link String} and which will provide the ID using {@link Object#toString()}.</dd>
	 * <dt>alias</dt>
	 * <dd>An {@link Alias} containing the alias string.</dd>
	 * </dl>
	 * <p>
	 * The current position must be that of the beginning label delimiter character. The new position will be that immediately after the last label delimiter.
	 * </p>
	 * @param reader The reader the contents of which to be parsed.
	 * @return The tag parsed from the reader; either an {@link Alias}, an absolute {@link URI} for a tag, or {@link String} for an ID.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the label is not valid.
	 * @see SURF#LABEL_DELIMITER
	 * @see #parseHandle(Reader)
	 * @see #parseIRI(Reader)
	 */
	public static Object parseLabel(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, LABEL_DELIMITER);
		final Object label;
		switch(peekRequired(reader)) {
			case IRI_BEGIN: //tag
				try {
					label = Tag.checkArgumentValid(parseIRI(reader));
				} catch(final IllegalArgumentException illegalArgumentException) {
					throw new ParseIOException(reader, "Invalid tag.", illegalArgumentException);
				}
				break;
			case STRING_DELIMITER: //ID
				label = parseString(reader);
				break;
			default: //alias
				label = new Alias(parseNameToken(reader));
				break;
		}
		check(reader, LABEL_DELIMITER);
		return label;
	}

	/**
	 * Parses a name token composed of a name token beginning character followed by zero or more name token characters. The current position must be that of the
	 * first name token character. The new position will be that immediately after the last name token character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The name token parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if there are are no name characters.
	 * @see SURF.Name#isTokenBeginCharacter(int)
	 * @see SURF.Name#isTokenCharacter(int)
	 */
	protected static String parseNameToken(@Nonnull final Reader reader) throws IOException, ParseIOException {
		final StringBuilder stringBuilder = new StringBuilder(); //create a string builder for reading the name segment
		int c = reader.read(); //read the first name character
		if(!Name.isTokenBeginCharacter(c)) { //if the name doesn't start with a name token character
			checkReaderNotEnd(reader, c); //make sure we're not at the end of the reader
			throw new ParseIOException(reader, String.format("Expected name token begin character; found %s.", Characters.getLabel(c)));
		}
		do {
			stringBuilder.append((char)c); //append the character
			reader.mark(1); //mark our current position
			c = reader.read(); //read another character
		} while(Name.isTokenCharacter(c)); //keep reading and appending until we reach a non-name token character
		if(c >= 0) { //if we didn't reach the end of the stream
			reader.reset(); //reset to the last mark, which was set right before the non-name character we found
		}
		final String nameToken = stringBuilder.toString();
		checkParseIO(reader, Name.isValidToken(nameToken), "Invalid name token %s.", nameToken);
		return nameToken;
	}

	/**
	 * Parses a handle composed of a name token followed by zero or more name tokens, separated by handle segment delimiters. The current position must be that of
	 * the first handle character. The new position will be that immediately after the last handle character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The handle parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if there are are no handle characters.
	 */
	public static String parseHandle(@Nonnull final Reader reader) throws IOException, ParseIOException {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(parseNameToken(reader)); //there should always be one name token
		while(confirm(reader, Handle.SEGMENT_DELIMITER)) { //read another handle segments if present
			stringBuilder.append(Handle.SEGMENT_DELIMITER); //-
			stringBuilder.append(parseNameToken(reader)); //nameToken
		}
		final String handle = stringBuilder.toString();
		checkParseIO(reader, Handle.isValid(handle), "Invalid handle %s.", handle);
		return handle;
	}

	/**
	 * Parses a resource; either a tag or a resource representation with an optional description. The next character read must be the start of the resource.
	 * @param reader The reader containing SURF data.
	 * @return An object representing the SURF resource read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 * @throws ParseIOException if the SURF data was invalid.
	 */
	public Object parseResource(@Nonnull final Reader reader) throws IOException {
		return parseResource(reader, true);
	}

	/**
	 * Parses a resource; either a tag or a resource representation. The next character read must be the start of the resource.
	 * @param reader The reader containing SURF data.
	 * @param allowDescription Whether a description is allowed; if <code>false</code>, an following description delimiter will not be considered part of the
	 *          resource.
	 * @return An object representing the SURF resource read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 * @throws ParseIOException if the SURF data was invalid.
	 */
	public Object parseResource(@Nonnull final Reader reader, final boolean allowDescription) throws IOException {
		Object label = null;
		int c = peek(reader);
		if(c == LABEL_DELIMITER) {
			label = parseLabel(reader);
			if(label instanceof URI || label instanceof Alias) { //tag or alias
				final Object resource = labeledResources.get(label);
				if(resource != null) {
					return resource;
				}
			}
			c = skipFiller(reader);
		}

		final Object resource;
		switch(c) {
			//objects
			case OBJECT_BEGIN:
				resource = parseObject(label, reader);
				break;
			//literals
			case BINARY_BEGIN:
				resource = parseBinary(reader);
				break;
			case CHARACTER_DELIMITER:
				resource = parseCharacter(reader);
				break;
			case BOOLEAN_FALSE_BEGIN:
			case BOOLEAN_TRUE_BEGIN:
				resource = parseBoolean(reader);
				break;
			case EMAIL_ADDRESS_BEGIN:
				resource = parseEmailAddress(reader);
				break;
			case IRI_BEGIN:
				resource = parseIRI(reader);
				break;
			case NUMBER_DECIMAL_BEGIN:
			case NUMBER_NEGATIVE_SYMBOL:
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				resource = parseNumber(reader);
				break;
			case REGULAR_EXPRESSION_DELIMITER:
				resource = parseRegularExpression(reader);
				break;
			case STRING_DELIMITER:
				resource = parseString(reader);
				break;
			case TELEPHONE_NUMBER_BEGIN:
				resource = parseTelephoneNumber(reader);
				break;
			case TEMPORAL_BEGIN:
				resource = parseTemporal(reader);
				break;
			case UUID_BEGIN:
				resource = parseUuid(reader);
				break;
			//collections
			case LIST_BEGIN:
				checkParseIO(reader, label == null || label instanceof Alias, "Non-alias label |%s| cannot be used with collection.", label);
				resource = parseList((Alias)label, reader);
				break;
			case MAP_BEGIN:
				checkParseIO(reader, label == null || label instanceof Alias, "Non-alias label |%s| cannot be used with collection.", label);
				resource = parseMap((Alias)label, reader);
				break;
			case SET_BEGIN:
				checkParseIO(reader, label == null || label instanceof Alias, "Non-alias label |%s| cannot be used with collection.", label);
				resource = parseSet((Alias)label, reader);
				break;
			default:
				//TODO the spec currently says a tag MAY have a resource representation; should we create an object if there is none?
				throw new ParseIOException(reader, "Expected resource; found character: " + Characters.getLabel(c));
		}

		//check and associate with the label as needed
		if(label instanceof URI) { //tag
			checkParseIO(reader, resource instanceof SurfObject, "Tag |%s| can only be used an object.", label);
			assert label.equals(((SurfObject)resource).getTag().orElse(null));
			labeledResources.put(label, resource);
		} else if(label instanceof String) { //ID
			final String id = (String)label;
			checkParseIO(reader, resource instanceof SurfObject, "ID |%s| can only be used an object.", id);
			final SurfObject surfObject = (SurfObject)resource;
			assert id.equals(((SurfObject)resource).getId().orElse(null));
			final String typeHandle = surfObject.getTypeHandle()
					.orElseThrow(() -> new ParseIOException(reader, String.format("Object with ID %s does not indicate a type.", id)));
			labeledResources.put(new AbstractMap.SimpleImmutableEntry<String, String>(typeHandle, id), resource);
		} else if(label instanceof Alias) { //alias
			checkParseIO(reader, resource != null, "Cannot use alias |%s| with null.", label);
			if(!(resource instanceof Collection)) { //collections already saved the association
				labeledResources.put(label, resource);
			}
			//aliases can refer to SurfObjects, collections, or value objects; normally we would want to ensure == for SurfObjects,
			//but value objects are substitutable, so it's not good practice to compare them using ==
			assert findResourceByAlias(label.toString()).isPresent();
		}

		//description (optional)
		if(allowDescription && resource instanceof SurfObject) {
			c = peek(reader);
			if(c == DESCRIPTION_BEGIN) {
				checkParseIO(reader, resource instanceof SurfObject, "SURF only allows objects to have a description.");
				parseDescription(reader, (SurfObject)resource);
			}
		}

		return resource;
	}

	//objects

	/**
	 * Parses an object, a described resource instance indicated by {@value SURF#OBJECT_BEGIN}. The next character read is expected to be
	 * {@value SURF#OBJECT_BEGIN}.
	 * @param tag The object resource's globally identifying tag, or <code>null</code> if the object has no tag.
	 * @param reader The reader containing SURF data.
	 * @return The SURF object read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 * @see SURF#OBJECT_BEGIN
	 */
	public SurfObject parseObject(@Nullable URI tag, @Nonnull final Reader reader) throws IOException {
		return parseObject((Object)tag, reader);
	}

	/**
	 * Parses an object indicated by {@value SURF#OBJECT_BEGIN}. The next character read is expected to be {@value SURF#OBJECT_BEGIN}.
	 * <p>
	 * If the given label indicates an ID, after parsing the object type if an object already exists with the given ID for that type, it will be immediately
	 * returned.
	 * </p>
	 * @param label The object label; either an {@link Alias}, a {@link URI} representing a tag IRI, or a {@link String} representing an ID.
	 * @param reader The reader containing SURF data.
	 * @return The SURF object read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 * @see SURF#OBJECT_BEGIN
	 */
	protected SurfObject parseObject(@Nullable Object label, @Nonnull final Reader reader) throws IOException {
		check(reader, OBJECT_BEGIN); //*
		int c = skipFiller(reader);
		//type (optional)
		final String typeHandle;
		if(c >= 0 && Handle.isBeginCharacter((char)c)) {
			typeHandle = parseHandle(reader);
			//if a type#id was already defined return it
			if(label instanceof String) {
				final SurfObject objectById = getObjectById(typeHandle, (String)label);
				if(objectById != null) {
					return objectById;
				}
			}
			c = skipFiller(reader);
		} else {
			typeHandle = null;
		}
		final SurfObject resource; //create a resource based upon the type of label
		if(label instanceof URI) { //tag
			resource = new SurfObject((URI)label, typeHandle);
		} else if(label instanceof String) { //ID
			checkParseIO(reader, typeHandle != null, "Object with ID %s does not indicate a type.", label);
			resource = new SurfObject(typeHandle, (String)label);
		} else { //no tag or ID
			resource = new SurfObject(typeHandle); //the type may be null
		}
		return resource;
	}

	/**
	 * Parses the description properties of a resource. The next character read is expected to be the start of the description {@value SURF#DESCRIPTION_BEGIN}.
	 * @param reader The reader containing SURF data.
	 * @param resource The resource to which properties should be added; SURF only supports object descriptions.
	 * @return The described SURF resource read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 * @see SURF#DESCRIPTION_BEGIN
	 */
	protected SurfObject parseDescription(@Nonnull final Reader reader, @Nonnull final SurfObject resource) throws IOException {
		requireNonNull(resource);
		check(reader, DESCRIPTION_BEGIN); //:
		parseSequence(reader, DESCRIPTION_END, r -> {
			final String propertyHandle = parseHandle(reader);
			skipLineBreaks(reader);
			check(reader, PROPERTY_VALUE_DELIMITER); //=
			skipLineBreaks(reader);
			final Object value = parseResource(reader);
			final Optional<Object> oldValue = resource.setPropertyValue(propertyHandle, value);
			checkParseIO(reader, !oldValue.isPresent(), "Resource has duplicate definition for property %s.", propertyHandle);
		});
		check(reader, DESCRIPTION_END); //;
		return resource;
	}

	//literals

	/**
	 * Parses a binary literal. The current position must be that of the beginning binary delimiter character. The new position will be that immediately after the
	 * last character in the base64 alphabet.
	 * @param reader The reader the contents of which to be parsed.
	 * @return An array of bytes Java {@link URI} containing the IRI parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the IRI is not in the correct format.
	 * @see SURF#BINARY_BEGIN
	 */
	public static byte[] parseBinary(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, BINARY_BEGIN);
		final String base64String = readWhile(reader, BINARY_BASE64URL_CHARACTERS);
		try {
			return Base64.getUrlDecoder().decode(base64String);
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new ParseIOException(reader, "Invalid SURF binary Base64 (base64url) encoding: " + base64String, illegalArgumentException);
		}
	}

	/**
	 * Parses a Boolean value. The next character read is expected to be the start of {@link SURF#BOOLEAN_FALSE_LEXICAL_FORM} or
	 * {@link SURF#BOOLEAN_TRUE_LEXICAL_FORM}.
	 * @param reader The reader containing SURF data.
	 * @return A {@link Boolean} representing the SURF boolean literal read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public static Boolean parseBoolean(@Nonnull final Reader reader) throws IOException {
		int c = peekRequired(reader); //peek the next character
		switch(c) { //see what the next character is
			case BOOLEAN_FALSE_BEGIN: //false
				check(reader, BOOLEAN_FALSE_LEXICAL_FORM); //make sure this is really false
				return Boolean.FALSE;
			case BOOLEAN_TRUE_BEGIN: //true
				check(reader, BOOLEAN_TRUE_LEXICAL_FORM); //make sure this is really true
				return Boolean.TRUE;
			default: //if we don't recognize the start of the boolean lexical form
				checkReaderNotEnd(reader, c); //make sure we're not at the end of the reader
				throw new ParseIOException(reader, "Unrecognized start of boolean: " + (char)c);
		}
	}

	/**
	 * Parses a character literal. The current position must be that of the beginning character delimiter character. The new position will be that immediately
	 * after the ending character delimiter character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The code point character parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the character literal is empty, if a control character was present, if the character is not escaped correctly, or the reader
	 *           has no more characters before the current character is completely parsed.
	 * @see #parseCharacterCodePoint(Reader, char)
	 */
	public static CodePointCharacter parseCharacter(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, CHARACTER_DELIMITER);
		final int codePoint = parseCharacterCodePoint(reader, CHARACTER_DELIMITER);
		checkParseIO(reader, codePoint >= 0, "Character literal cannot be empty.");
		check(reader, CHARACTER_DELIMITER);
		return CodePointCharacter.of(codePoint);
	}

	/**
	 * Parses a character as content, without any delimiters. The current position must be that of the character, which may be an escape sequence. The new
	 * position will be that immediately after the character.
	 * <p>
	 * This method always allows the delimiter to be escaped.
	 * </p>
	 * @param reader The reader the contents of which to be parsed.
	 * @param delimiter The delimiter that surrounds the character and which should be escaped.
	 * @return The code point parsed from the reader, or <code>-1</code> if the unescaped delimiter was encountered.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if a control character was represented, if the character is not escaped correctly, or the reader has no more characters before the
	 *           current character is completely parsed.
	 */
	public static int parseCharacterCodePoint(@Nonnull final Reader reader, final char delimiter) throws IOException, ParseIOException {
		char c = readRequired(reader); //read a character
		//TODO check for and prevent control characters
		if(c == delimiter) {
			return -1;
		} else if(c == CHARACTER_ESCAPE) { //if this is an escape character
			c = readRequired(reader); //read another a character
			switch(c) { //see what the next character
				case CHARACTER_ESCAPE: //\\
				case SOLIDUS_CHAR: //\/
					break; //use the escaped escape character unmodified
				case ESCAPED_BACKSPACE: //\b backspace
					c = BACKSPACE_CHAR;
					break;
				case ESCAPED_FORM_FEED: //\f
					c = FORM_FEED_CHAR;
					break;
				case ESCAPED_LINE_FEED: //\n
					c = LINE_FEED_CHAR;
					break;
				case ESCAPED_CARRIAGE_RETURN: //\r
					c = CARRIAGE_RETURN_CHAR;
					break;
				case ESCAPED_TAB: //\t
					c = CHARACTER_TABULATION_CHAR;
					break;
				case ESCAPED_VERTICAL_TAB: //\v
					c = LINE_TABULATION_CHAR;
					break;
				case ESCAPED_UNICODE: //u Unicode
				{
					final String unicodeString = readRequiredCount(reader, 4); //read the four Unicode code point hex characters
					try {
						c = (char)Integer.parseInt(unicodeString, 16); //parse the hex characters and use the resulting code point
					} catch(final NumberFormatException numberFormatException) { //if the hex integer was not in the correct format
						throw new ParseIOException(reader, "Invalid Unicode escape sequence " + unicodeString + ".", numberFormatException);
					}
					if(Character.isHighSurrogate(c)) { //if this is a high surrogate, expect another Unicode escape sequence
						check(reader, CHARACTER_ESCAPE); //\
						check(reader, ESCAPED_UNICODE); //u
						final String unicodeString2 = readRequiredCount(reader, 4);
						final char c2;
						try {
							c2 = (char)Integer.parseInt(unicodeString2, 16);
						} catch(final NumberFormatException numberFormatException) { //if the hex integer was not in the correct format
							throw new ParseIOException(reader, "Invalid Unicode escape sequence " + unicodeString2 + ".", numberFormatException);
						}
						if(!Character.isLowSurrogate(c2)) {
							throw new ParseIOException(reader, "Unicode high surrogate character " + Characters.getLabel(c)
									+ " must be followed by low surrogate character; found " + Characters.getLabel(c2));
						}
						return Character.toCodePoint(c, c2); //short-circuit and return the surrogate pair code point
					}
					if(Character.isLowSurrogate(c)) {
						throw new ParseIOException(reader, "Unicode character escape sequence cannot begin with low surrogate character " + Characters.getLabel(c));
					}
				}
					break;
				default: //if another character was escaped
					if(c != delimiter) { //if this is not the delimiter that was escaped
						throw new ParseIOException(reader, "Unknown escaped character: " + Characters.getLabel(c));
					}
					break;
			}
		} else if(Character.isHighSurrogate(c)) { //if this is a high surrogate, expect another character
			final char c2 = readRequired(reader); //read another character
			if(!Character.isLowSurrogate(c2)) {
				throw new ParseIOException(reader,
						"Unicode high surrogate character " + Characters.getLabel(c) + " must be followed by low surrogate character; found " + Characters.getLabel(c2));
			}
			return Character.toCodePoint(c, c2); //short-circuit and return the surrogate pair code point
		} else if(Character.isLowSurrogate(c)) {
			throw new ParseIOException(reader, "Unicode character cannot begin with low surrogate character " + Characters.getLabel(c));
		}
		return c;
	}

	/**
	 * Parses an email address. The current position must be that of the beginning email delimiter character. The new position will be that immediately after the
	 * last character in the email address.
	 * @param reader The reader the contents of which to be parsed.
	 * @return An instance of {@link EmailAddress} representing the SURF email addresses parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the email address is not in the correct format.
	 * @see SURF#EMAIL_ADDRESS_BEGIN
	 */
	public static EmailAddress parseEmailAddress(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, EMAIL_ADDRESS_BEGIN); //^
		//TODO improve for dot-atom | quoted-string, verify characters, etc.
		final String localPart = readUntilRequired(reader, EmailAddress.LOCAL_PART_DOMAIN_DELIMITER); //@
		check(reader, EmailAddress.LOCAL_PART_DOMAIN_DELIMITER);
		final String domain;
		final StringBuilder domainStringBuilder = new StringBuilder();
		//TODO maybe add readIf() method
		if(peek(reader) == EmailAddress.DOMAIN_LITERAL_BEGIN) { //domain-literal
			domainStringBuilder.append(check(reader, EmailAddress.DOMAIN_LITERAL_BEGIN)); //[
			readWhile(reader, EmailAddress.DTEXT_CHARACTERS, domainStringBuilder); //dtext*
			domainStringBuilder.append(check(reader, EmailAddress.DOMAIN_LITERAL_END)); //]
		} else { //dot-atom
			boolean hasAText;
			do {
				readRequiredMinimumCount(reader, EmailAddress.ATEXT_CHARACTERS, 1, domainStringBuilder); //at least one "atext" character is required
				hasAText = peek(reader) == '.';
				if(hasAText) { //if we have another dot section
					domainStringBuilder.append(check(reader, '.')); //read the dot
				}
			} while(hasAText); //read all the "dot-atom" sections
		}
		domain = domainStringBuilder.toString();
		try {
			return EmailAddress.of(localPart, domain);
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new ParseIOException(reader, "Invalid SURF email address format: " + localPart + EmailAddress.LOCAL_PART_DOMAIN_DELIMITER + domain,
					illegalArgumentException);
		}
	}

	/**
	 * Parses an IRI. The current position must be that of the beginning IRI delimiter character. The new position will be that immediately after the ending IRI
	 * delimiter character.
	 * <p>
	 * SURF IRI short forms are accepted.
	 * </p>
	 * @param reader The reader the contents of which to be parsed.
	 * @return A Java {@link URI} containing the IRI parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the IRI is not in the correct format.
	 * @see SURF#IRI_BEGIN
	 * @see SURF#IRI_END
	 */
	public static URI parseIRI(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, IRI_BEGIN);
		final URI iri;
		switch(peekRequired(reader)) { //check for short forms
			case EMAIL_ADDRESS_BEGIN: //^
				iri = URI.create(MAILTO_SCHEME + SCHEME_SEPARATOR + parseEmailAddress(reader).toString());
				break;
			case TELEPHONE_NUMBER_BEGIN: //+
				iri = URI.create(TEL_SCHEME + SCHEME_SEPARATOR + parseTelephoneNumber(reader).getCanonicalString());
				break;
			case UUID_BEGIN: //&
				iri = UUIDs.toURI(parseUuid(reader));
				break;
			default: {
				final String iriString = readUntilRequired(reader, IRI_END);
				try {
					iri = new URI(iriString);
				} catch(final URISyntaxException uriSyntaxException) {
					throw new ParseIOException(reader, "Invalid IRI: " + iriString, uriSyntaxException);
				}
			}
				break;
		}
		check(reader, IRI_END);
		return iri;
	}

	/**
	 * Parses a number. The current position must be that of the first character of the number. The new position will be that immediately after the number, or at
	 * the end of the reader.
	 * <p>
	 * This implementation will return one of the following types:
	 * </p>
	 * <dl>
	 * <dt>{@link Integer}</dt>
	 * <dd>All non-decimal, non-fractional, non-exponent non-decimal numbers that fall within the range of <code>int</code>.</dd>
	 * <dt>{@link Long}</dt>
	 * <dd>All non-decimal, non-fractional, non-exponent numbers that fall outside the range of <code>int</code> but within the range of <code>long</code>.</dd>
	 * <dt>{@link Double}</dt>
	 * <dd>All non-decimal fractional numbers that fall within the range of <code>double</code>.</dd>
	 * <dt>{@link BigInteger}</dt>
	 * <dd>All non-fractional decimal numbers.</dd>
	 * <dt>{@link BigDecimal}</dt>
	 * <dd>All fractional decimal numbers.</dd>
	 * </dl>
	 * @param reader The reader the contents of which to be parsed.
	 * @return A Java {@link Number} containing the number parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the number is not in the correct format, or if the number is outside the range that can be represented by this parser.
	 */
	public static Number parseNumber(@Nonnull final Reader reader) throws IOException, ParseIOException {
		int c = peekRequired(reader); //there must be at least one number character
		//check for decimal: $
		final boolean isDecimal;
		if(c == NUMBER_DECIMAL_BEGIN) {
			isDecimal = true;
			check(reader, NUMBER_DECIMAL_BEGIN);
			c = peekRequired(reader);
		} else {
			isDecimal = false;
		}
		boolean hasFraction = false;
		boolean hasExponent = false;
		final StringBuilder stringBuilder = new StringBuilder();
		if(c == NUMBER_NEGATIVE_SYMBOL) { //-
			stringBuilder.append(check(reader, NUMBER_NEGATIVE_SYMBOL));
		}
		//TODO check for beginning zero and follow final SURF octal rules
		readRequiredMinimumCount(reader, ASCII.DIGIT_CHARACTERS, 1, stringBuilder); //read all integer digits; there must be at least one
		c = peek(reader); //peek the next character
		if(c >= 0) { //if we're not at the end of the reader
			//check for fraction
			if(c == NUMBER_FRACTION_DELIMITER) { //if there is a fractional part
				hasFraction = true; //we found a fraction
				stringBuilder.append(check(reader, NUMBER_FRACTION_DELIMITER)); //read and append the beginning decimal point
				readRequiredMinimumCount(reader, ASCII.DIGIT_CHARACTERS, 1, stringBuilder); //read all digits; there must be at least one
				c = peek(reader); //peek the next character
			}
			//check for exponent
			if(c >= 0 && NUMBER_EXPONENT_DELIMITER_CHARACTERS.contains((char)c)) { //this is an exponent
				hasExponent = true; //we found an exponent
				stringBuilder.append(check(reader, (char)c)); //read and append the exponent character
				c = peek(reader); //peek the next character
				if(c >= 0 && NUMBER_EXPONENT_SIGN_CHARACTERS.contains((char)c)) { //if the exponent starts with a sign
					stringBuilder.append(check(reader, (char)c)); //append the sign
				}
				readRequiredMinimumCount(reader, ASCII.DIGIT_CHARACTERS, 1, stringBuilder); //read all exponent digits; there must be at least one
			}
		}
		final String numberString = stringBuilder.toString(); //convert the number to a string
		try {
			if(isDecimal) {
				if(hasFraction || hasExponent) { //if there was a fraction or exponent
					return new BigDecimal(numberString);
				} else { //if there is no fraction or exponent
					return new BigInteger(numberString);
				}
			} else {
				if(hasFraction || hasExponent) { //if there was a fraction or exponent
					return Double.valueOf(Double.parseDouble(numberString)); //parse a double and return it
				} else { //if there is no fraction or exponent
					final long longValue = Long.parseLong(numberString);
					if(longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) { //return an Integer if we can
						return Integer.valueOf((int)longValue);
					} else { //otherwise return a Long
						return Long.valueOf(longValue);
					}
				}
			}
		} catch(final NumberFormatException numberFormatException) { //if the number was not syntactically correct
			throw new ParseIOException(reader, "Invalid number format: " + numberString, numberFormatException);
		}
	}

	/**
	 * Parses a regular expression surrounded by regular expression delimiters and optionally ending with flags. The current position must be that of the first
	 * regular expression delimiter character. The new position will be that immediately after the regular expression delimiter character or, if there are flags,
	 * after the last flag.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The pattern representing the regular expression parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the regular expressions is not escaped correctly or the reader has no more characters before the current regular expression is
	 *           completely parsed.
	 * @see SURF#REGULAR_EXPRESSION_DELIMITER
	 * @see SURF#REGULAR_EXPRESSION_ESCAPE
	 */
	public static Pattern parseRegularExpression(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, REGULAR_EXPRESSION_DELIMITER);
		final StringBuilder regexBuilder = new StringBuilder();
		char c = readRequired(reader);
		while(c != REGULAR_EXPRESSION_DELIMITER) {
			//TODO check for and prevent control characters
			if(c == REGULAR_EXPRESSION_ESCAPE) { //if this is an escape character
				final char next = readRequired(reader); //read the next character (to be valid there must be another character)
				if(next != REGULAR_EXPRESSION_DELIMITER) { //if the next character is not a regular expression delimiter
					regexBuilder.append(c); //we can use the escape character that wasn't
				}
				c = next; //prepare the next character to be consumed as well (also preventing the second escape in "\\" from being interpreted as an escape)
			}
			regexBuilder.append(c); //append the character to the string we are constructing
			c = readRequired(reader); //read another a character
		}
		//TODO parse flags
		return Pattern.compile(regexBuilder.toString());
	}

	/**
	 * Parses a string surrounded by string delimiters. The current position must be that of the first string delimiter character. The new position will be that
	 * immediately after the string delimiter character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The string parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if a control character was present, if the string is not escaped correctly, or the reader has no more characters before the
	 *           current string is completely parsed.
	 * @see SURF#STRING_DELIMITER
	 * @see #parseCharacterCodePoint(Reader, char)
	 */
	public static String parseString(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, STRING_DELIMITER); //read the beginning string delimiter
		final StringBuilder stringBuilder = new StringBuilder(); //create a new string builder to use when reading the string
		int codePoint; //keep reading and appending code points until we encounter the end of the string
		while((codePoint = parseCharacterCodePoint(reader, STRING_DELIMITER)) >= 0) {
			stringBuilder.appendCodePoint(codePoint);
		}
		return stringBuilder.toString(); //return the string we constructed; the ending string delimiter will already have been consumed
	}

	/**
	 * Parses a telephone number. The current position must be that of the beginning telephone number delimiter character. The new position will be that
	 * immediately after the last character in the telephone number.
	 * @param reader The reader the contents of which to be parsed.
	 * @return An instance of {@link TelephoneNumber} representing the SURF telephone number parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the telephone number is not in the correct format.
	 * @see SURF#TELEPHONE_NUMBER_BEGIN
	 */
	public static TelephoneNumber parseTelephoneNumber(@Nonnull final Reader reader) throws IOException, ParseIOException {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(check(reader, TELEPHONE_NUMBER_BEGIN)); //+
		final String telephoneNumberDigits = readRequiredMinimumCount(reader, ABNF.DIGIT_CHARACTERS, 1, stringBuilder).toString(); //at least one digit is required
		try {
			return TelephoneNumber.parse(telephoneNumberDigits);
		} catch(final IllegalArgumentException illegalArgumentException) { //this should never happen with the manual parsing logic above
			throw new ParseIOException(reader, "Invalid SURF telephone number digits: " + telephoneNumberDigits, illegalArgumentException);
		}
	}

	/**
	 * Parses a temporal. The current position must be that of the beginning temporal delimiter. The new position will be that immediately after the temporal, or
	 * at the end of the reader.
	 * @param reader The reader the contents of which to be parsed.
	 * @return A Java representation of the temporal parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the temporal is not in the correct format, or if the temporal is outside the range that can be represented by this parser.
	 * @see SURF#TEMPORAL_BEGIN
	 */
	public static TemporalAccessor parseTemporal(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, TEMPORAL_BEGIN);

		int c; //we'll use this to keep track of the next character
		final StringBuilder stringBuilder = new StringBuilder(); //create a new string builder to use when reading the temporal
		try {
			//YearMonth, MonthDay, and Year are returned from within the parsing logic; all other types are returned at the end
			c = peek(reader);
			//TODO implement duration
			if(c == ISO8601.DATE_DELIMITER) { //--MM-DD
				return MonthDay.parse(readRequiredCount(reader, 7));
			}
			String temporalStart = readRequiredMinimumCount(reader, ASCII.DIGIT_CHARACTERS, 1); //read all digits; there should be at least one
			final int dateTimeStartLength = temporalStart.length(); //get the length of the date time start
			final boolean hasDate; //determine if there is a date
			final boolean hasTime; //determine if there is a time
			if(dateTimeStartLength == 4) { //we've started a date
				//YYYY
				hasDate = true;
				stringBuilder.append(temporalStart); //append the beginning date time characters
				c = peek(reader); //peek the next character
				if(c != ISO8601.DATE_DELIMITER) { //if the year is not part of a longer date
					return Year.parse(stringBuilder.toString());
				}
				stringBuilder.append(check(reader, ISO8601.DATE_DELIMITER)); //read and add the date delimiter
				stringBuilder.append(readRequiredCount(reader, ASCII.DIGIT_CHARACTERS, 2)); //read the month
				c = peek(reader); //peek the next character
				if(c != ISO8601.DATE_DELIMITER) { //if the year and month are not part of a longer date
					return YearMonth.parse(stringBuilder.toString());
				}
				stringBuilder.append(check(reader, ISO8601.DATE_DELIMITER)); //read and add the date delimiter
				stringBuilder.append(readRequiredCount(reader, ASCII.DIGIT_CHARACTERS, 2)); //read the day
				//YYYY-MM-DD
				c = peek(reader); //peek the next character
				//TODO consider supporting OffsetDate; see e.g. http://stackoverflow.com/q/7788267/421049
				hasTime = c == ISO8601.TIME_BEGIN;
				if(hasTime) { //if this is the beginning of a time
					//YYYY-MM-DDThh:mm:ss
					stringBuilder.append(check(reader, ISO8601.TIME_BEGIN)); //read and append the time delimiter
					temporalStart = readRequiredCount(reader, ASCII.DIGIT_CHARACTERS, 2); //read the hour, and then let the time code take over
				}
			} else { //if we didn't start a date
				hasDate = false;
				if(dateTimeStartLength == 2) { //if we're starting a time
					hasTime = true; //we need to parse the time
				} else { //if this is neither the start of a date nor the start of a time
					throw new ParseIOException(reader, "Incorrect start of a date, time, or date time: " + temporalStart);
				}
			}
			final boolean isUTC; //determine if there is a UTC designation
			final boolean hasOffset; //determine if there is a zone offset
			final boolean hasZone; //determine if there is a time zone
			if(hasTime) { //if we need to parse time
				//…:mm:ss
				stringBuilder.append(temporalStart); //append the beginning date time characters
				stringBuilder.append(check(reader, ISO8601.TIME_DELIMITER)); //read and add the time delimiter
				stringBuilder.append(readRequiredCount(reader, ASCII.DIGIT_CHARACTERS, 2)); //read the minutes
				stringBuilder.append(check(reader, ISO8601.TIME_DELIMITER)); //read and add the time delimiter
				stringBuilder.append(readRequiredCount(reader, ASCII.DIGIT_CHARACTERS, 2)); //read the seconds
				c = peek(reader); //peek the next character
				if(c == ISO8601.TIME_SUBSECONDS_DELIMITER) { //if this is a time subseconds delimiter
					//…:mm:ss.s
					stringBuilder.append(check(reader, ISO8601.TIME_SUBSECONDS_DELIMITER)); //read and append the time subseconds delimiter
					readRequiredMinimumCount(reader, ASCII.DIGIT_CHARACTERS, 1, stringBuilder); //read all subseconds
				}
				c = peek(reader); //peek the next character
				isUTC = c == ISO8601.UTC_DESIGNATOR;
				hasOffset = !isUTC && c >= 0 && ISO8601.SIGNS.contains((char)c);
				if(isUTC) {
					//…:mm:ss…Z
					hasZone = false;
					stringBuilder.append(check(reader, ISO8601.UTC_DESIGNATOR)); //read and add the UTC designator
				} else if(hasOffset) { //if this is the start of a UTC offset
					//…:mm:ss…±hh:mm
					stringBuilder.append(check(reader, (char)c)); //read and add the delimiter
					stringBuilder.append(readRequiredCount(reader, ASCII.DIGIT_CHARACTERS, 2)); //read two digits
					stringBuilder.append(check(reader, ISO8601.TIME_DELIMITER)); //read and add the time delimiter
					stringBuilder.append(readRequiredCount(reader, ASCII.DIGIT_CHARACTERS, 2)); //read two digits
					c = peek(reader); //peek the next character
					hasZone = c == TEMPORAL_ZONE_BEGIN;
					if(hasZone) {
						//…:mm:ss…±hh:mm[tz]
						stringBuilder.append(check(reader, TEMPORAL_ZONE_BEGIN));
						//TODO add some check to verify that there is tz content
						stringBuilder.append(readUntilRequired(reader, TEMPORAL_ZONE_END));
						stringBuilder.append(check(reader, TEMPORAL_ZONE_END));
					}
				} else {
					hasZone = false;
				}
			} else {
				isUTC = false;
				hasOffset = false;
				hasZone = false;
			}
			//YearMonth, MonthDay, and Year have been handled separately at this point
			final String temporalString = stringBuilder.toString();
			if(hasDate) {
				if(hasTime) {
					if(isUTC) {
						assert !hasOffset;
						return Instant.parse(temporalString);
					} else if(hasOffset) {
						if(hasZone) {
							return ZonedDateTime.parse(temporalString);
						} else { //offset with no zone
							return OffsetDateTime.parse(temporalString);
						}
					} else { //not UTC, no offset, and no zone
						return LocalDateTime.parse(temporalString);
					}
				} else { //date with no time
					return LocalDate.parse(temporalString);
				}
			} else { //no date
				assert hasTime;
				assert !isUTC;
				if(hasOffset) {
					return OffsetTime.parse(temporalString);
				} else { //not UTC, no offset, and no zone
					return LocalTime.parse(temporalString);
				}
			}
		} catch(final DateTimeParseException dateTimeParseException) {
			throw new ParseIOException(reader, dateTimeParseException);
		}
	}

	/**
	 * Parses a UUID. The current position must be that of the beginning UUID delimiter character. The new position will be that immediately after the last
	 * character in the UUID.
	 * @param reader The reader the contents of which to be parsed.
	 * @return An instance of {@link UUID} representing the SURF UUID parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the UUID is not in the correct format.
	 * @see SURF#UUID_BEGIN
	 */
	public static UUID parseUuid(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, UUID_BEGIN); //&
		final StringBuilder stringBuilder = new StringBuilder();
		checkCount(reader, ASCII.HEX_CHARACTERS, 8, stringBuilder);
		stringBuilder.append(check(reader, UUID_GROUP_DELIMITER)); //-
		checkCount(reader, ASCII.HEX_CHARACTERS, 4, stringBuilder);
		stringBuilder.append(check(reader, UUID_GROUP_DELIMITER)); //-
		checkCount(reader, ASCII.HEX_CHARACTERS, 4, stringBuilder);
		stringBuilder.append(check(reader, UUID_GROUP_DELIMITER)); //-
		checkCount(reader, ASCII.HEX_CHARACTERS, 4, stringBuilder);
		stringBuilder.append(check(reader, UUID_GROUP_DELIMITER)); //-
		checkCount(reader, ASCII.HEX_CHARACTERS, 12, stringBuilder);
		try {
			return UUID.fromString(stringBuilder.toString());
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new ParseIOException(reader, "Invalid SURF UUID contents: " + stringBuilder, illegalArgumentException);
		}
	}

	//collections

	/**
	 * Parses an a list. The current position must be for {@value SURF#LIST_BEGIN}. The new position will be that immediately following {@value SURF#LIST_END}.
	 * <p>
	 * Once the list is created, it will be associated with the given alias if any.
	 * </p>
	 * @param alias The resource alias, or <code>null</code> if the resource has no alias.
	 * @param reader The reader containing SURF data.
	 * @return The list read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public List<Object> parseList(@Nullable final Alias alias, @Nonnull final Reader reader) throws IOException {
		final List<Object> list = new ArrayList<>();
		if(alias != null) {
			labeledResources.put(alias, list);
		}
		check(reader, LIST_BEGIN); //[
		parseSequence(reader, LIST_END, r -> list.add(parseResource(r)));
		check(reader, LIST_END); //]
		return list;
	}

	/**
	 * Parses a map. The current position must be for {@value SURF#MAP_BEGIN}. The new position will be that immediately following {@value SURF#MAP_END}.
	 * <p>
	 * Once the map is created, it will be associated with the given alias if any.
	 * </p>
	 * @param alias The resource alias, or <code>null</code> if the resource has no alias.
	 * @param reader The reader containing SURF data.
	 * @return The map read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Map<Object, Object> parseMap(@Nullable final Alias alias, @Nonnull final Reader reader) throws IOException {
		final Map<Object, Object> map = new HashMap<>();
		if(alias != null) {
			labeledResources.put(alias, map);
		}
		check(reader, MAP_BEGIN); //{
		parseSequence(reader, MAP_END, r -> {
			final Object key;
			if(peek(reader) == MAP_KEY_DELIMITER) {
				check(reader, MAP_KEY_DELIMITER); //\
				key = parseResource(reader, true);
				check(reader, MAP_KEY_DELIMITER); //\
			} else {
				key = parseResource(reader, false);
			}
			skipLineBreaks(reader);
			check(reader, ENTRY_KEY_VALUE_DELIMITER); //:
			skipLineBreaks(reader);
			final Object value = parseResource(reader);
			map.put(key, value); //put the value in the map, replacing any old value
		});
		check(reader, MAP_END); //}
		return map;
	}

	/**
	 * Parses a set. The current position must be for {@value SURF#SET_BEGIN}. The new position will be that immediately following {@value SURF#SET_END}.
	 * <p>
	 * Once the set is created, it will be associated with the given alias if any.
	 * </p>
	 * @param alias The resource alias, or <code>null</code> if the resource has no alias.
	 * @param reader The reader containing SURF data.
	 * @return The set read from the reader.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Set<Object> parseSet(@Nullable final Alias alias, @Nonnull final Reader reader) throws IOException {
		final Set<Object> set = new HashSet<>();
		if(alias != null) {
			labeledResources.put(alias, set);
		}
		check(reader, SET_BEGIN); //[
		parseSequence(reader, SET_END, r -> set.add(parseResource(r)));
		check(reader, SET_END); //]
		return set;
	}

	//parsing

	/**
	 * Parses a general SURF sequence (such as a list). This method skips whitespace, comments, and sequence delimiters. For each sequence item,
	 * {@link IOConsumer#accept(Object)} is called, passing the {@link Reader}, for the item to be parsed.
	 * @param reader The reader containing the sequence to parse.
	 * @param sequenceEnd The character expected to end the sequence.
	 * @param itemParser The parser strategy, which is passed the {@link Reader} to use for parsing.
	 * @return The next character that will be returned by the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached without encountering the end of the sequence.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int parseSequence(@Nonnull final Reader reader, final char sequenceEnd, @Nonnull final IOConsumer<Reader> itemParser) throws IOException {
		boolean nextItemRequired = false; //at the beginning out there is no requirement for items (i.e. an empty sequence is possible)
		int c = skipLineBreaks(reader);
		while(c >= 0 && (nextItemRequired || c != sequenceEnd)) {
			itemParser.accept(reader); //parse the item
			final Optional<Boolean> requireItem = skipSequenceDelimiters(reader);
			c = peekRequired(reader); //we'll need to know the next character whatever the case
			if(!requireItem.isPresent()) { //if there was no item delimiter at all
				break; //no possibility for a new item
			}
			nextItemRequired = requireItem.get().booleanValue(); //see if a new item is required
		}
		return c;
	}

	/**
	 * Skips over SURF sequence delimiters in a reader. Whitespace and comments. The new position will either be the that of the first non-whitespace and non-EOL
	 * character; or the end of the input stream.
	 * @param reader The reader the contents of which to be parsed.
	 * @return {@link Boolean#TRUE} if a line delimiter was encountered that requires a following item, {@link Boolean#FALSE} if a line delimiter was encountered
	 *         for which a following item is optional, or {@link Optional#empty()} if no line ending was encountered.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static Optional<Boolean> skipSequenceDelimiters(@Nonnull final Reader reader) throws IOException {
		int c = skipFiller(reader); //skip whitespace (and comments)
		if(c < 0 && !SEQUENCE_SEPARATOR_CHARACTERS.contains((char)c)) { //see if we encounter some sequence delimiter
			return Optional.empty();
		}
		boolean requireItem = false;
		do {
			if(c == COMMA_CHAR) { //if we found a comma
				requireItem = true; //note we found a comma
				check(reader, COMMA_CHAR); //skip the comma
			}
			c = skipLineBreaks(reader);
		} while(!requireItem && c >= 0 && SEQUENCE_SEPARATOR_CHARACTERS.contains((char)c));
		return Optional.of(requireItem);
	}

	/**
	 * Skips over SURF filler in a reader, including whitespace and line comments.The new position will either be the that of the first non-whitespace character
	 * or the end of the input stream.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The next character that will be returned the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipFiller(@Nonnull final Reader reader) throws IOException {
		int c = skip(reader, WHITESPACE_CHARACTERS); //skip all whitespace
		if(c == LINE_COMMENT_BEGIN) { //if the start of a line comment was encountered
			check(reader, LINE_COMMENT_BEGIN); //read the beginning comment delimiter
			reach(reader, EOL_CHARACTERS); //skip to the end of the line or the end of the stream; no need to read more, because EOL characters are not whitespace
			c = peek(reader); //see what the next character will be so we can return it			
		}
		return c; //return the next character to be character read
	}

	/** Whitespace and end-of-line characters. */
	protected static final Characters WHITESPACE_EOL_CHARACTERS = WHITESPACE_CHARACTERS.add(EOL_CHARACTERS);

	/**
	 * Skips over SURF line breaks in a reader, including whitespace and line comments. The new position will either be the that of the first non-whitespace and
	 * non-EOL character; or the end of the input stream.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The next character that will be returned the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipLineBreaks(@Nonnull final Reader reader) throws IOException {
		int c; //we'll store the next non-line-break character here so that it can be returned
		while((c = skip(reader, WHITESPACE_EOL_CHARACTERS)) == LINE_COMMENT_BEGIN) { //skip all fillers; if the start of a comment was encountered
			check(reader, LINE_COMMENT_BEGIN); //read the beginning comment delimiter
			reach(reader, EOL_CHARACTERS); //skip to the end of the line; we'll then skip all line-break filler characters again and see if another comment starts
		}
		return c; //return the last character read
	}

}
