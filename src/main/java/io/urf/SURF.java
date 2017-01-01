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

	/** The signature of a SURF document. */
	public static final String SIGNATURE = "~URF";

	/** The delimiter separating segments in a name. */
	public static final char NAME_SEGMENT_DELIMITER = '-';

	/** Regular expression pattern to match a SURF name . */
	public static final Pattern NAME_PATTERN = Pattern.compile("\\p{L}[\\p{L}\\p{M}\\p{N}\\p{Pc}\\-]*"); //TODO fix to prevent two hyphens together

	/** Characters recognized by SURF as whitespace. */
	public static final Characters WHITESPACE_CHARACTERS = Characters.WHITESPACE_CHARACTERS; //TODO fix; maybe use Posix

	/** The SURF general filler characters. */
	public static final Characters FILLER_CHARACTERS = WHITESPACE_CHARACTERS.add(PARAGRAPH_SEPARATOR_CHARS).add(SEGMENT_SEPARATOR_CHARS);

	/** The character indicating the start of a Surf single-line comment. */
	public static final char COMMENT_BEGIN = '!';

	/** The beginning delimiter of a SURF document body. */
	public static final char BODY_BEGIN = '$';

	/** The delimiter that begins labels. */
	public static final char LABEL_BEGIN = '|';
	/** The delimiter that ends labels. */
	public static final char LABEL_END = LABEL_BEGIN;

	/** The indicator of an object (an anonymous resource instance). */
	public static final char OBJECT_BEGIN = '*';

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
		checkArgument(isValidSurfName(string), "Invalid SURF name \"%s\".", string); //TODO verify JAVA-5
		return string;
	}

}
