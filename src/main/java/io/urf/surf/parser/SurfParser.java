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

package io.urf.surf.parser;

import static com.globalmentor.io.ReaderParser.*;
import static com.globalmentor.java.Characters.*;
import static io.urf.SURF.*;
import static io.urf.SURF.WHITESPACE_CHARACTERS;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.io.ParseIOException;
import com.globalmentor.io.function.IOConsumer;
import com.globalmentor.java.Characters;

import io.urf.SURF;

/**
 * Simple parser for the Simple URF (SURF) document format.
 * @author Garret Wilson
 */
public class SurfParser {

	/** The map of resources that have been labeled. */
	private final Map<String, SurfResource> labeledResources = new HashMap<>();

	/**
	 * Parses an URF resource from an input stream.
	 * @param inputStream The input stream containing SURF data.
	 * @return The root URF resource, which may be empty if the SURF document was empty.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Optional<Object> parse(@Nonnull final InputStream inputStream) throws IOException {
		return parse(new LineNumberReader(new InputStreamReader(inputStream, CHARSET)));
	}

	/**
	 * Parses an URF resource from a reader.
	 * @param reader The reader containing SURF data.
	 * @return The root URF resource, which may be empty if the SURF document was empty.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Optional<Object> parse(@Nonnull final Reader reader) throws IOException {
		if(skipWhitespaceLineBreaks(reader) < 0) { //skip whitespace, comments, and line breaks; if we reached the end of the stream
			return Optional.empty(); //the SURF document is empty
		}
		return Optional.of(parseResource(reader));
	}

	/**
	 * Parses a name composed of a name beginning character followed by zero or more name characters. The current position must be that of the first name
	 * character. The new position will be that immediately after the last name character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The name parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if there are are no name characters.
	 * @see TURF#isNameBeginCharacter(int)
	 * @see TURF#isNameCharacter(int)
	 */
	public static String parseName(@Nonnull final Reader reader) throws IOException, ParseIOException {
		final StringBuilder stringBuilder = new StringBuilder(); //create a string builder for reading the name segment
		int c = reader.read(); //read the first name character
		if(!isSurfNameBeginCharacter(c)) { //if the name doesn't start with a name character
			checkReaderNotEnd(reader, c); //make sure we're not at the end of the reader
			throw new ParseIOException(reader, String.format("Expected name begin character; found %s.", Characters.getLabel(c)));
		}
		do {
			stringBuilder.append((char)c); //append the character
			reader.mark(1); //mark our current position
			c = reader.read(); //read another character
		} while(isSurfNameCharacter(c)); //keep reading and appending until we reach a non-name character
		if(c >= 0) { //if we didn't reach the end of the stream
			reader.reset(); //reset to the last mark, which was set right before the non-name character we found
		}
		return stringBuilder.toString(); //return the name segment we read
	}

	/**
	 * Parses a resource; either a label or a resource representation. The next character read must be the start of the resource.
	 * @param document The document being parsed.
	 * @param reader The reader containing SURF data.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Object parseResource(@Nonnull final Reader reader) throws IOException {
		String label = null;
		int c = peekEnd(reader); //peek the next character
		if(c == LABEL_BEGIN) {
			//TODO parse label
			c = skipWhitespace(reader);
		}
		final Object resource;
		switch(c) {
			case BOOLEAN_FALSE_BEGIN:
			case BOOLEAN_TRUE_BEGIN:
				resource = parseBoolean(reader);
				break;
			case LIST_BEGIN:
				resource = parseList(reader);
				break;
			case OBJECT_BEGIN:
				resource = parseObject(reader);
				break;
			case STRING_BEGIN:
				resource = parseString(reader, STRING_BEGIN, STRING_END); //parse the string
				break;
			default:
				throw new ParseIOException(reader, "Expected resource; found character: " + Characters.getLabel(c));
		}
		return resource;
	}

	/**
	 * Parses a Boolean value.
	 * @param document The document being parsed. The next character read is expected to be the start of {@link SURF#BOOLEAN_FALSE} or {@link SURF#BOOLEAN_TRUE}.
	 * @param reader The reader containing SURF data.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Object parseBoolean(@Nonnull final Reader reader) throws IOException {
		int c = peek(reader); //peek the next character
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
	 * Parses an object; that is, an anonymous resource instance indicated by <code>*</code>.
	 * @param document The document being parsed. The next character read is expected to be {@value SURF#OBJECT_BEGIN}.
	 * @param reader The reader containing SURF data.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public SurfResource parseObject(@Nonnull final Reader reader) throws IOException {
		check(reader, OBJECT_BEGIN); //*
		int c = skipWhitespace(reader);
		//type (optional)
		final String typeName;
		if(c >= 0 && c != PROPERTIES_BEGIN && !EOL_CHARACTERS.contains((char)c)) { //if there is something before properties, it must be a type
			typeName = parseName(reader);
			c = skipWhitespace(reader);
		} else {
			typeName = null;
		}
		final SurfResource resource = new SurfResource(typeName);
		//properties (optional)
		if(c == PROPERTIES_BEGIN) {
			check(reader, PROPERTIES_BEGIN); //:
			parseSequence(reader, PROPERTIES_END, r -> {
				final String propertyName = parseName(reader);
				skipWhitespaceLineBreaks(reader);
				check(reader, PROPERTY_VALUE_DELIMITER); //=
				skipWhitespaceLineBreaks(reader);
				final Object value = parseResource(reader);
				resource.setPropertyValue(propertyName, value);
			});
			check(reader, PROPERTIES_END); //;
		}
		return resource;
	}

	/**
	 * Parses an a list. The current position must be for {@value SURF#LIST_BEGIN}. The new position will be that immediately following {@value SURF#LIST_END}.
	 * @param document The document being parsed.
	 * @param reader The reader containing SURF data.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public List<?> parseList(@Nonnull final Reader reader) throws IOException {
		final List<Object> list = new ArrayList<>();
		check(reader, LIST_BEGIN); //[
		parseSequence(reader, LIST_END, r -> list.add(parseResource(r)));
		check(reader, LIST_END); //]
		return list;
	}

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
		int c = skipWhitespaceLineBreaks(reader);
		while(c >= 0 && (nextItemRequired || c != sequenceEnd)) {
			itemParser.accept(reader); //parse the item
			final Optional<Boolean> requireItem = skipSequenceDelimiters(reader);
			c = peek(reader); //we'll need to know the next character whatever the case
			if(!requireItem.isPresent()) { //if there was no item delimiter at all
				break; //no possibility for a new item
			}
			nextItemRequired = requireItem.get().booleanValue(); //see if a new item is required
		}
		return c;
	}

	/**
	 * Parses a string surrounded by string delimiters. The current position must be that of the first string delimiter character. The new position will be that
	 * immediately after the string number delimiter character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The string parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the string is not escaped correctly or reader has no more characters before the current string is completely parsed.
	 * @see SURF#STRING_BEGIN
	 * @see SURF#STRING_END
	 */
	public static String parseString(final Reader reader) throws IOException, ParseIOException {
		return parseString(reader, STRING_BEGIN, STRING_END);
	}

	/**
	 * Parses a string surrounded by the indicated delimiters. The current position must be that of the first string delimiter character. The new position will be
	 * that immediately after the string number delimiter character.
	 * @param reader The reader the contents of which to be parsed.
	 * @param stringBegin The beginning string delimiter.
	 * @param stringEnd The ending string delimiter.
	 * @return The string parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the string is not escaped correctly or reader has no more characters before the current string is completely parsed.
	 */
	public static String parseString(final Reader reader, final char stringBegin, final char stringEnd) throws IOException, ParseIOException {
		check(reader, stringBegin); //read the beginning string delimiter
		final StringBuilder stringBuilder = new StringBuilder(); //create a new string builder to use when reading the string
		char c = readCharacter(reader); //read a character
		while(c != stringEnd) { //keep reading character until we reach the end of the string
			if(c == STRING_ESCAPE) { //if this is an escape character
				c = readCharacter(reader); //read another a character
				switch(c) { //see what the next character
					case STRING_ESCAPE: //\\
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
						final String unicodeString = readString(reader, 4); //read the four Unicode code point hex characters
						try {
							c = (char)Integer.parseInt(unicodeString, 16); //parse the hex characters and use the resulting code point
						} catch(final NumberFormatException numberFormatException) { //if the hex integer was not in the correct format
							throw new ParseIOException(reader, "Invalid Unicode escape sequence " + unicodeString + ".", numberFormatException);
						}
					}
						break;
					default: //if another character was escaped
						if(c != stringBegin && c != stringEnd) { //if this is not the delimiter that was escaped
							throw new ParseIOException(reader, "Unknown escaped character: " + Characters.getLabel(c));
						}
						break;
				}
			}
			stringBuilder.append(c); //append the character to the string we are constructing
			c = readCharacter(reader); //read another a character
		}
		return stringBuilder.toString(); //return the string we constructed
	}

	/**
	 * Skips over SURF whitespace in a reader. Line comments will be ignored and skipped as well. The new position will either be the that of the first
	 * non-whitespace character or the end of the input stream.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The next character that will be returned the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipWhitespace(final Reader reader) throws IOException {
		int c = skip(reader, WHITESPACE_CHARACTERS); //skip all whitespace
		if(c == LINE_COMMENT_BEGIN) { //if the start of a line comment was encountered
			check(reader, LINE_COMMENT_BEGIN); //read the beginning comment delimiter
			reachEnd(reader, EOL_CHARACTERS); //skip to the end of the line or the end of the stream; no need to read more, because EOL characters are not whitespace
			c = peekEnd(reader); //see what the next character will be so we can return it			
		}
		return c; //return the next character to be character read
	}

	/**
	 * Characters that indicate the start of filler or the end of a line. This includes {@link SURF#WHITESPACE_CHARACTERS}, the start of a line comment
	 * {@link SURF#LINE_COMMENT_BEGIN}, and {@link Characters#EOL_CHARACTERS}.
	 */
	public static final Characters WHITESPACE_EOL_CHARACTERS = WHITESPACE_CHARACTERS.add(EOL_CHARACTERS);

	/**
	 * Skips over SURF line breaks in a reader. Whitespace will be ignored and skipped as well. The new position will either be the that of the first
	 * non-whitespace and non-EOL character; or the end of the input stream.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The next character that will be returned the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipWhitespaceLineBreaks(final Reader reader) throws IOException {
		int c; //we'll store the next non-line-break-filler character here so that it can be returned
		while((c = skip(reader, WHITESPACE_EOL_CHARACTERS)) == LINE_COMMENT_BEGIN) { //skip all fillers; if the start of a comment was encountered
			check(reader, LINE_COMMENT_BEGIN); //read the beginning comment delimiter
			reachEnd(reader, EOL_CHARACTERS); //skip to the end of the line; we'll then skip all line-break filler characters again and see if another comment starts
		}
		return c; //return the last character read
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
	protected static Optional<Boolean> skipSequenceDelimiters(final Reader reader) throws IOException {
		int c = skipWhitespace(reader); //skip whitespace (and comments)
		if(c < 0 && !SEQUENCE_SEPARATOR_CHARACTERS.contains((char)c)) { //see if we encounter some sequence delimiter
			return Optional.empty();
		}
		boolean requireItem = false;
		do {
			if(c == COMMA_CHAR) { //if we found a comma
				requireItem = true; //note we found a comma
				check(reader, COMMA_CHAR); //skip the comma
			}
			c = skipWhitespaceLineBreaks(reader); //skip any newlines
		} while(!requireItem && c >= 0 && SEQUENCE_SEPARATOR_CHARACTERS.contains((char)c));
		return Optional.of(requireItem);
	}

}
