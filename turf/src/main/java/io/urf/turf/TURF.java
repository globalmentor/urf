/*
 * Copyright © 2007-2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.turf;

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.net.ContentType.*;
import static java.nio.charset.StandardCharsets.*;

import java.nio.charset.Charset;

import com.globalmentor.java.Characters;
import com.globalmentor.net.ContentType;

/**
 * Definitions for the Text URF (TURF) document format.
 * @author Garret Wilson
 */
public class TURF {

	/** The content type for TURF: <code>text/urf</code>. */
	public static final ContentType CONTENT_TYPE = ContentType.create(TEXT_PRIMARY_TYPE, "urf");

	/** An extension for TURF filenames. */
	public static final String FILENAME_EXTENSION = "turf";

	/** The default TURF charset. */
	public static final Charset DEFAULT_CHARSET = UTF_8;

	/** Characters recognized as whitespace. */
	public static final Characters WHITESPACE_CHARACTERS = SPACE_SEPARATOR_CHARACTERS.add(CHARACTER_TABULATION_CHAR, LINE_TABULATION_CHAR, FORM_FEED_CHAR,
			SPACE_CHAR, NO_BREAK_SPACE_CHAR, ZERO_WIDTH_NO_BREAK_SPACE_CHAR);

	/** The character that separates items in a sequence. */
	public static final char SEQUENCE_DELIMITER = ',';

	/** The sequence separator characters, including the {@link #SEQUENCE_DELIMITER} and the EOL characters. */
	public static final Characters SEQUENCE_SEPARATOR_CHARACTERS = EOL_CHARACTERS.add(SEQUENCE_DELIMITER);

	/** The character indicating the start of a single-line comment. */
	public static final char LINE_COMMENT_BEGIN = '!';

	/** The delimiter that begins and ends labels (tags, IDs, and aliases). */
	public static final char LABEL_DELIMITER = '|';

	//objects

	/** The indicator of an object (an anonymous resource instance). */
	public static final char OBJECT_BEGIN = '*';
	/** The delimiter that begins an object description. */
	public static final char DESCRIPTION_BEGIN = ':';
	/** The delimiter that ends a property description. */
	public static final char DESCRIPTION_END = ';';
	/** The character that separates properties and assigned values. */
	public static final char PROPERTY_VALUE_DELIMITER = '=';

	//literals

	/** The delimiter that begins binary literal values. */
	public static final char BINARY_BEGIN = '%';
	/** The <cite>RFC 4648</cite> "base64url" alphabet, without padding characters, used by binary literals. */
	public static final Characters BINARY_BASE64URL_CHARACTERS = Characters.of(Characters.ofRange('A', 'Z'), Characters.ofRange('a', 'z'),
			Characters.ofRange('0', '9'), Characters.of('-', '_'));

	/** The lexical representation of the Boolean value <code>false</code>. */
	public static final String BOOLEAN_FALSE_LEXICAL_FORM = "false";
	/** The lexical representation of the Boolean <code>true</code>. */
	public static final String BOOLEAN_TRUE_LEXICAL_FORM = "true";
	/** The beginning delimiter of the lexical form of the Boolean value <code>false</code>. */
	public static final char BOOLEAN_FALSE_BEGIN = 'f';
	/** The beginning delimiter of the lexical form of the Boolean value <code>true</code>. */
	public static final char BOOLEAN_TRUE_BEGIN = 't';

	/** The delimiter that begins and ends character literal representations. */
	public static final char CHARACTER_DELIMITER = '\'';
	/** The character used for escaping a character. */
	public static final char CHARACTER_ESCAPE = '\\';
	/** Characters that must be escaped as characters or in strings. */
	public static Characters CHARACTER_REQUIRED_ESCAPED_CHARACTERS = Characters.of(CHARACTER_ESCAPE, BACKSPACE_CHAR, FORM_FEED_CHAR, LINE_FEED_CHAR,
			CARRIAGE_RETURN_CHAR, CHARACTER_TABULATION_CHAR, LINE_TABULATION_CHAR);
	/** Additional characters that may be escaped as characters or in strings. */
	public static Characters CHARACTER_OPTIONAL_ESCAPED_CHARACTERS = Characters.of(SOLIDUS_CHAR);
	//escaped forms of characters
	public static final char ESCAPED_BACKSPACE = 'b'; //b backspace
	public static final char ESCAPED_FORM_FEED = 'f'; //f form feed
	public static final char ESCAPED_LINE_FEED = 'n'; //n line feed
	public static final char ESCAPED_CARRIAGE_RETURN = 'r'; //r carriage return
	public static final char ESCAPED_TAB = 't'; //t tab
	public static final char ESCAPED_VERTICAL_TAB = 'v'; //v vertical tab
	public static final char ESCAPED_UNICODE = 'u'; //u Unicode

	/** The delimiter that begins email address literal values. */
	public static final char EMAIL_ADDRESS_BEGIN = '^';

	/** The delimiter that begins IRI literal representations. */
	public static final char IRI_BEGIN = '<';
	/** The delimiter that ends IRI literal representations. */
	public static final char IRI_END = '>';

	/** The delimiter that begins a decimal number. */
	public static final char NUMBER_DECIMAL_BEGIN = '$';
	/** The symbol that delimits the fractional part of a number. */
	public static final char NUMBER_FRACTION_DELIMITER = '.';
	/** The symbols that delimits the exponent part of a number. */
	public static final Characters NUMBER_EXPONENT_DELIMITER_CHARACTERS = Characters.of('e', 'E');
	/** The symbols that indicate the sign of the exponent part of a number. */
	public static final Characters NUMBER_EXPONENT_SIGN_CHARACTERS = Characters.of('-', '+');
	/** The symbol that indicates a negative number. */
	public static final char NUMBER_NEGATIVE_SYMBOL = '-';

	/** The delimiter that begins and ends a regular expressions. */
	public static final char REGULAR_EXPRESSION_DELIMITER = '/';
	/** The character used for escaping characters in a regular expression. */
	public static final char REGULAR_EXPRESSION_ESCAPE = '\\';

	/** The delimiter that begins and ends string literal representations. */
	public static final char STRING_DELIMITER = '"';

	/** The delimiter that begins telephone number literal values. */
	public static final char TELEPHONE_NUMBER_BEGIN = '+';

	/** The delimiter that begins temporal literal representations. */
	public static final char TEMPORAL_BEGIN = '@';
	/** The ISO 8601 extension delimiter indicating the start of a time zone designation. */
	public static final char TEMPORAL_ZONE_BEGIN = '[';
	/** The ISO 8601 extension delimiter indicating the end of a time zone designation. */
	public static final char TEMPORAL_ZONE_END = ']';

	/** The delimiter that begins UUID literal representations. */
	public static final char UUID_BEGIN = '&';
	/** The delimiter that separates internal groups of UUID hexadecimal digits. */
	public static final char UUID_GROUP_DELIMITER = '-';

	//collections

	/** The delimiter that begins lists. */
	public static final char LIST_BEGIN = '[';
	/** The delimiter that ends lists. */
	public static final char LIST_END = ']';

	/** The delimiter that begins maps. */
	public static final char MAP_BEGIN = '{';
	/** The delimiter that ends maps. */
	public static final char MAP_END = '}';
	/** The delimiter that optionally begins and ends map keys. */
	public static final char MAP_KEY_DELIMITER = '\\';
	/** The character that separates keys and values in a map entry. */
	public static final char ENTRY_KEY_VALUE_DELIMITER = ':';

	/** The delimiter that begins sets. */
	public static final char SET_BEGIN = '(';
	/** The delimiter that ends sets. */
	public static final char SET_END = ')';

}
