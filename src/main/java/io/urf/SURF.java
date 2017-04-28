/*
 * Copyright © 2016 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf;

import static com.globalmentor.java.Characters.*;

import static com.globalmentor.java.Conditions.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Objects.*;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

import com.globalmentor.java.Characters;

/**
 * Definitions for the Simple URF (SURF) document format.
 * @author Garret Wilson
 */
public class SURF {

	public static final Charset CHARSET = UTF_8;

	/** The delimiter separating segments in a name. */
	public static final char NAME_SEGMENT_DELIMITER = '-';

	/** Regular expression pattern to match a SURF name . */
	public static final Pattern NAME_PATTERN = Pattern.compile("\\p{L}[\\p{L}\\p{M}\\p{N}\\p{Pc}\\-]*"); //TODO fix to prevent two hyphens together

	/** Characters recognized by SURF as whitespace. */
	public static final Characters WHITESPACE_CHARACTERS = SPACE_SEPARATOR_CHARACTERS.add(CHARACTER_TABULATION_CHAR, LINE_TABULATION_CHAR, FORM_FEED_CHAR,
			SPACE_CHAR, NO_BREAK_SPACE_CHAR, ZERO_WIDTH_NO_BREAK_SPACE_CHAR);

	/** The character that separates items in a sequence. */
	public static final char SEQUENCE_DELIMITER = ',';

	/** The SURF sequence separator characters, including the {@link #SEQUENCE_DELIMITER} and the EOL characters. */
	public static final Characters SEQUENCE_SEPARATOR_CHARACTERS = EOL_CHARACTERS.add(SEQUENCE_DELIMITER);

	/** The character indicating the start of a Surf single-line comment. */
	public static final char LINE_COMMENT_BEGIN = '!';

	/** The delimiter that begins and ends labels. */
	public static final char LABEL_DELIMITER = '|';

	//objects

	/** The indicator of an object (an anonymous resource instance). */
	public static final char OBJECT_BEGIN = '*';
	/** The delimiter that begins property declarations. */
	public static final char PROPERTIES_BEGIN = ':';
	/** The delimiter that ends property declarations. */
	public static final char PROPERTIES_END = ';';
	/** The character that separates properties and assigned values. */
	public static final char PROPERTY_VALUE_DELIMITER = '=';

	//literals

	/** The delimiter that begins and ends binary literal values. */
	public static final char BINARY_DELIMITER = '%';

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
	//escaped forms of characters
	public static final char ESCAPED_BACKSPACE = 'b'; //b backspace
	public static final char ESCAPED_FORM_FEED = 'f'; //f form feed
	public static final char ESCAPED_LINE_FEED = 'n'; //n line feed
	public static final char ESCAPED_CARRIAGE_RETURN = 'r'; //r carriage return
	public static final char ESCAPED_TAB = 't'; //t tab
	public static final char ESCAPED_VERTICAL_TAB = 'v'; //v vertical tab
	public static final char ESCAPED_UNICODE = 'u'; //u Unicode

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

	/** The delimiter that begins temporal literal representations. */
	public static final char TEMPORAL_BEGIN = '@';
	/** The ISO 8601 extension delimiter indicating the start of a time zone designation. */
	public static final char TEMPORAL_ZONE_BEGIN = '[';
	/** The ISO 8601 extension delimiter indicating the end of a time zone designation. */
	public static final char TEMPORAL_ZONE_END = ']';

	//collections

	/** The delimiter that begins lists. */
	public static final char LIST_BEGIN = '[';
	/** The delimiter that ends lists. */
	public static final char LIST_END = ']';

	/** The delimiter that begins sets. */
	public static final char SET_BEGIN = '(';
	/** The delimiter that ends sets. */
	public static final char SET_END = ')';

	/**
	 * Determines whether the given string conforms to the rules for a SURF name.
	 * @param string The string to test.
	 * @return <code>true</code> if the string is a valid SURF name.
	 * @throws NullPointerException if the given string is <code>null</code>.
	 * @see #NAME_PATTERN
	 */
	public static boolean isValidSurfName(final String string) {
		return NAME_PATTERN.matcher(requireNonNull(string)).matches();
	}

	/**
	 * Confirms that the given string conforms to the rules for a SURF name.
	 * @param string The string to check.
	 * @return The given string.
	 * @throws NullPointerException if the given string is <code>null</code>.
	 * @throws IllegalArgumentException if the given string does not conform to the rules for a SURF name.
	 * @see #NAME_PATTERN
	 */
	public static String checkArgumentValidSurfName(final String string) {
		checkArgument(isValidSurfName(string), "Invalid SURF name \"%s\".", string);
		return string;
	}

	/**
	 * Determines if the given character is a SURF name begin character. A name begin character is a Unicode letter.
	 * @param c The character to check.
	 * @return <code>true</code> if the character is a SURF name begin character.
	 */
	public static final boolean isSurfNameBeginCharacter(final int c) {
		return Character.isLetter(c); //see if this is a letter
	}

	/**
	 * Determines if the given character is a SURF name character. A name character is a Unicode letter, mark, number, or connector punctuation.
	 * @param c The character to check.
	 * @return <code>true</code> if the character is a SURF name character.
	 */
	public static final boolean isSurfNameCharacter(final int c) {
		return (((
		//letter
		(1 << Character.UPPERCASE_LETTER) | (1 << Character.LOWERCASE_LETTER) | (1 << Character.TITLECASE_LETTER) | (1 << Character.MODIFIER_LETTER)
				| (1 << Character.OTHER_LETTER) |
				//mark
				(1 << Character.NON_SPACING_MARK) | (1 << Character.COMBINING_SPACING_MARK) | (1 << Character.ENCLOSING_MARK) |
				//digit
				(1 << Character.DECIMAL_DIGIT_NUMBER) |
				//connector punctuation
				(1 << Character.CONNECTOR_PUNCTUATION)) >> Character.getType(c)) & 1) != 0
				//hyphen-minus
				|| c == HYPHEN_MINUS_CHAR;
	}

}
