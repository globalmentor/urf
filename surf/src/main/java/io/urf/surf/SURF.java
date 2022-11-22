/*
 * Copyright © 2016-2017 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.urf.surf;

import static com.globalmentor.java.Characters.*;

import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.net.MediaType.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Objects.*;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import javax.annotation.*;

import com.globalmentor.java.Characters;
import com.globalmentor.net.MediaType;

/**
 * Definitions for the Simple URF (SURF) document format.
 * @author Garret Wilson
 */
public class SURF {

	/** The media type for SURF: <code>text/simple-urf</code>. */
	public static final MediaType MEDIA_TYPE = MediaType.of(TEXT_PRIMARY_TYPE, "simple-urf");

	/** An extension for SURF filenames. */
	public static final String FILENAME_EXTENSION = "surf";

	/** The SURF charset. */
	public static final Charset CHARSET = UTF_8;

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

	/** Escaped backspace specifier. */
	public static final char ESCAPED_BACKSPACE = 'b'; //b backspace
	/** Escaped form feed specifier. */
	public static final char ESCAPED_FORM_FEED = 'f'; //f form feed
	/** Escaped line feed specifier. */
	public static final char ESCAPED_LINE_FEED = 'n'; //n line feed
	/** Escaped carriage return specifier. */
	public static final char ESCAPED_CARRIAGE_RETURN = 'r'; //r carriage return
	/** Escaped tab specifier. */
	public static final char ESCAPED_TAB = 't'; //t tab
	/** Escaped vertical tab specifier. */
	public static final char ESCAPED_VERTICAL_TAB = 'v'; //v vertical tab
	/** Escaped Unicode specifier. */
	public static final char ESCAPED_UNICODE = 'u'; //u Unicode

	/** The delimiter that begins email address literal values. */
	public static final char EMAIL_ADDRESS_BEGIN = '^';

	/** The delimiter that begins IRI literal representations. */
	public static final char IRI_BEGIN = '<';
	/** The delimiter that ends IRI literal representations. */
	public static final char IRI_END = '>';

	/** The delimiter that begins media type literal representations. */
	public static final char MEDIA_TYPE_BEGIN = '>';
	/** The delimiter that ends media type literal representations. */
	public static final char MEDIA_TYPE_END = '<';

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

	/**
	 * Utilities for working with SURF tags.
	 * @author Garret Wilson
	 */
	public static final class Tag {

		/**
		 * Ensures that the given URI is a valid resource tag.
		 * <p>
		 * Primarily this ensures that the given URI is absolute and does not contain a fragment.
		 * </p>
		 * @param tag The tag to validate.
		 * @return The given tag.
		 * @throws NullPointerException if the given tag is <code>null</code>.
		 * @throws IllegalArgumentException if the given URI is not a valid SURF tag.
		 */
		public static URI checkArgumentValid(@Nonnull final URI tag) {
			requireNonNull(tag);
			checkArgument(tag.isAbsolute(), "SURF tag %s must be absolute.", tag);
			checkArgument(tag.getRawFragment() == null, "SURF tag %s not allowed to have a fragment.", tag);
			return tag;
		}

	}

	/**
	 * Utilities for working with SURF names.
	 * @author Garret Wilson
	 */
	public static final class Name {

		/** Regular expression pattern to match a SURF name token . */
		public static final Pattern TOKEN_PATTERN = Pattern.compile("\\p{L}[\\p{L}\\p{M}\\p{N}\\p{Pc}]*"); //TODO add test

		/**
		 * Determines whether the given string conforms to the rules for a SURF name token.
		 * @param string The string to test.
		 * @return <code>true</code> if the string is a valid URF name token.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @see #TOKEN_PATTERN
		 */
		public static boolean isValidToken(final String string) {
			return TOKEN_PATTERN.matcher(requireNonNull(string)).matches();
		}

		/**
		 * Confirms that the given string conforms to the rules for a SURF name token.
		 * @param string The string to check.
		 * @return The given string.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @throws IllegalArgumentException if the given string does not conform to the rules for a SURF name token.
		 * @see #TOKEN_PATTERN
		 */
		public static String checkArgumentValidToken(final String string) {
			checkArgument(isValidToken(string), "Invalid SURF name token \"%s\".", string);
			return string;
		}

		/**
		 * Determines if the given character is a SURF token name begin character. A name token begin character is a Unicode letter.
		 * @param c The character to check.
		 * @return <code>true</code> if the character is a SURF name begin character.
		 */
		public static final boolean isTokenBeginCharacter(final int c) {
			return Character.isLetter(c); //see if this is a letter
		}

		/**
		 * Determines if the given character is a SURF name token character. A name token character is a Unicode letter, mark, number, or connector punctuation.
		 * @param c The character to check.
		 * @return <code>true</code> if the character is a SURF name character.
		 */
		public static final boolean isTokenCharacter(final int c) {
			return (((
			//letter
			(1 << Character.UPPERCASE_LETTER) | (1 << Character.LOWERCASE_LETTER) | (1 << Character.TITLECASE_LETTER) | (1 << Character.MODIFIER_LETTER)
					| (1 << Character.OTHER_LETTER) |
					//mark
					(1 << Character.NON_SPACING_MARK) | (1 << Character.COMBINING_SPACING_MARK) | (1 << Character.ENCLOSING_MARK) |
					//digit
					(1 << Character.DECIMAL_DIGIT_NUMBER) |
					//connector punctuation
					(1 << Character.CONNECTOR_PUNCTUATION)) >> Character.getType(c)) & 1) != 0;
		}

		/** A SURF name is a single name token. */
		public static final Pattern PATTERN = TOKEN_PATTERN; //TODO add test

		/**
		 * Determines whether the given string conforms to the rules for a SURF name.
		 * @param string The string to test.
		 * @return <code>true</code> if the string is a valid SURF name.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @see #PATTERN
		 */
		public static boolean isValid(final String string) {
			return PATTERN.matcher(requireNonNull(string)).matches();
		}

		/**
		 * Confirms that the given string conforms to the rules for a SURF name.
		 * @param string The string to check.
		 * @return The given string.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @throws IllegalArgumentException if the given string does not conform to the rules for a SURF name.
		 * @see #PATTERN
		 */
		public static String checkArgumentValid(final String string) {
			checkArgument(isValid(string), "Invalid SURF name \"%s\".", string);
			return string;
		}

	}

	/**
	 * Utilities for working with SURF handles.
	 * @author Garret Wilson
	 */
	public static final class Handle {

		/** The delimiter used to separate segments of a SURF handle. */
		public static final char SEGMENT_DELIMITER = '-';

		/** Regular expression pattern to match a SURF handle. */
		public static final Pattern PATTERN = Pattern.compile(String.format("(%s)(?:%s(%s))*", Name.TOKEN_PATTERN, SEGMENT_DELIMITER, Name.TOKEN_PATTERN)); //TODO add test; document matching groups

		/**
		 * Determines if the given character is valid to begin a SURF handle. A SURF handle begins with a name token.
		 * @param c The character to check.
		 * @return <code>true</code> if the character is a SURF handle begin character.
		 * @see Name#isTokenBeginCharacter(int)
		 */
		public static final boolean isBeginCharacter(final int c) {
			return Name.isTokenBeginCharacter(c); //see if this is a letter
		}

		/**
		 * Determines whether the given string conforms to the rules for a SURF handle.
		 * @param string The string to test.
		 * @return <code>true</code> if the string is a valid SURF handle.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @see #PATTERN
		 */
		public static boolean isValid(final String string) {
			return PATTERN.matcher(requireNonNull(string)).matches();
		}

		/**
		 * Confirms that the given string conforms to the rules for a SURF handle.
		 * @param string The string to check.
		 * @return The given string.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @throws IllegalArgumentException if the given string does not conform to the rules for a SURF handle.
		 * @see #PATTERN
		 */
		public static String checkArgumentValid(final String string) {
			checkArgument(isValid(string), "Invalid SURF handle \"%s\".", string);
			return string;
		}

	}

}
