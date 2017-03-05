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

import java.io.*;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.io.ParseIOException;
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
		if(skipFiller(reader) < 0) { //skip filler; if we reached the end of the stream
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
			skipFiller(reader);
			c = peekEnd(reader); //peek the next character after the label
		}
		final Object resource;
		switch(c) {
			case BOOLEAN_FALSE_BEGIN:
			case BOOLEAN_TRUE_BEGIN:
				resource = parseBoolean(reader);
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
		int c = skipNonListSeparatorFiller(reader);
		//type (optional)
		final String typeName;
		if(c >= 0 && c != PROPERTIES_BEGIN && !EOL_CHARACTERS.contains((char)c)) { //if there is something before properties, it must be a type
			typeName = parseName(reader);
			c = skipNonListSeparatorFiller(reader);
		} else {
			typeName = null;
		}
		final SurfResource resource = new SurfResource(typeName);
		//properties (optional)
		if(c == PROPERTIES_BEGIN) {
			check(reader, PROPERTIES_BEGIN); //:
			c = skipFiller(reader);
			if(c != PROPERTIES_END) { //if this is not an empty properties section
				do {
					final String propertyName = parseName(reader);
					skipFiller(reader);
					check(reader, PROPERTY_VALUE_DELIMITER); //=
					skipFiller(reader);
					final Object value = parseResource(reader);
					resource.setPropertyValue(propertyName, value);
				} while(skipListSeparators(reader, PROPERTIES_END) == LIST_DELIMITER); //skip list separators and see if there should be another list item
			}
			check(reader, PROPERTIES_END); //;
		}
		return resource;
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
	 * Skips over SURF fillers in a reader. This method skips all filler characters {@link SURF#FILLER_CHARACTERS as well as any comments. The new position will
	 * either be the that of the first non-filler character or the end of the input stream.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The next character that will be returned the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipFiller(final Reader reader) throws IOException {
		int c; //we'll store the next non-filler character here so that it can be returned
		while((c = skip(reader, FILLER_CHARACTERS)) == COMMENT_BEGIN) { //skip all fillers; if the start of a comment was encountered
			check(reader, COMMENT_BEGIN); //read the beginning comment delimiter
			reachEnd(reader, EOL_CHARACTERS); //skip to the end of the line; we'll then skip all filler characters and see if another comment starts
		}
		return c; //return the last character read
	}

	/**
	 * Skips over SURF non-list-separator fillers in a reader. This method skips all non-list-separator filler characters
	 * {@link SURF#NON_LIST_SEPARATOR_FILLER_CHARACTERS}, as well as any comments. The new position will either be the that of the first character that is not a
	 * list separator or a filler character, or the end of the input stream.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The next character that will be returned the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipNonListSeparatorFiller(final Reader reader) throws IOException {
		int c; //we'll store the next non-filler character here so that it can be returned
		if((c = skip(reader, NON_LIST_SEPARATOR_FILLER_CHARACTERS)) == COMMENT_BEGIN) { //skip all non-list-separator fillers; if the start of a comment was encountered
			check(reader, COMMENT_BEGIN); //read the beginning comment delimiter
			reachEnd(reader, EOL_CHARACTERS); //skip to the end of the line; no need to read more, because EOL characters are not non-list-separator filler
			c = peekEnd(reader); //see what the next character will be so we can return it
		}
		return c; //return the last character read
	}

	/**
	 * Skips over SURF filler and list separators in a reader. This method skips all filler characters {@link SURF#FILLER_CHARACTERS}, the list delimiter
	 * character {@link SURF#LIST_DELIMITER}, as well as any comments. The new position will either be the that of the first non-list separator character or the
	 * end of the input stream.
	 * <p>
	 * If the returned character is {@link SURF#LIST_DELIMITER}, it indicates that the list delimiter or one or more EOL characters was encountered and, if an EOL
	 * character was encountered, the next character is not the end of the list; there will be no indication of the next character in the reader. Otherwise, the
	 * next character that will be returned from the reader will be given.
	 * </p>
	 * @param reader The reader the contents of which to be parsed.
	 * @param listEnd The character expected to end the list.
	 * @return The next character that will be returned by the reader's {@link Reader#read()} operation; or {@link SURF#LIST_DELIMITER} if a list separator (the
	 *         list delimiter or an EOL character) was encountered; or <code>-1</code> if the end of the reader has been reached without encountering a list
	 *         separator or non-separator character.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipListSeparators(final Reader reader, final char listEnd) throws IOException {
		boolean foundListDelimiter = false;
		boolean foundEOL = false;
		int c;
		while((c = skip(reader, NON_LIST_SEPARATOR_FILLER_CHARACTERS)) >= 0) { //while the end of the data has not been reached, skip fillers that do not delimit lists, and peek the next character
			if(c == LIST_DELIMITER) { //there can be one list delimiter character
				checkParseIO(reader, !foundListDelimiter, "Duplicate list delimiter: %s", LIST_DELIMITER);
				//TODO delete if not appropriate				checkParseIO(reader, !foundEOL, "The list delimiter %s and EOL characters cannot both be used to delimit two list items.", LIST_DELIMITER);
				foundListDelimiter = true;
				check(reader, (char)c); //skip the character
			} else if(EOL_CHARACTERS.contains((char)c)) { //there may be multiple newline characters
				//TODO delete if not appropriate				checkParseIO(reader, !foundListDelimiter, "The list delimiter %s and EOL characters cannot both be used to delimit two list items.", LIST_DELIMITER);
				foundEOL = true;
				check(reader, (char)c); //skip the character
			} else if(c == COMMENT_BEGIN) { //if the start of a comment was encountered
				check(reader, COMMENT_BEGIN); //read the beginning comment delimiter
				reachEnd(reader, EOL_CHARACTERS); //skip past the end of the comment; we'll then skip all separator characters and see if another comment starts
			} else { //if any other character was found, we've reached the end of the list
				break; //stop parsing the list separators
			}
		}
		return foundListDelimiter //if we found the list delimiter, we'll return it
				|| (foundEOL && c != listEnd) //a newline will also be interpreted as a list delimiter---but only if we didn't encounter the end-of-list character
						? LIST_DELIMITER : c; //return the list delimiter of a list separator was found, or the next character (which may indicate the end of the data)
	}

}
