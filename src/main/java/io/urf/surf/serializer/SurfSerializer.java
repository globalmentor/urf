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

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Conditions.*;
import static io.urf.SURF.*;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.io.ParseIOException;

import io.urf.SURF;
import io.urf.surf.parser.SurfObject;

/**
 * Simple serializer for the Simple URF (SURF) document format.
 * <p>
 * This serializer recognizes and can serialize the following type for SURF categories of resources:
 * </p>
 * <h2>Objects</h2>
 * <ul>
 * <li>{@link SurfObject}</li>
 * </ul>
 * <h2>Literals</h2>
 * <h3>String</h3>
 * <ul>
 * <li>{@link CharSequence} (including {@link String})</li>
 * </ul>
 * <h2>Collections</h2>
 * <ul>
 * <li>{@link List}</li>
 * <li>{@link Map}</li>
 * <li>{@link Set}</li>
 * </ul>
 * <p>
 * This serializer is meant to be used once for generating a single SURF document. It should not be used to serialize multiple documents, as it maintains
 * serialization state.
 * </p>
 * <p>
 * The serializer should be released after use so as not to leak memory of parsed resources when labeled resources are present.
 * </p>
 * <p>
 * This implementation is not thread safe.
 * </p>
 * @author Garret Wilson
 */
public class SurfSerializer {

	private final static String STRING_CLASS_NAME = "java.lang.String";
	private final static String STRING_BUILDER_CLASS_NAME = "java.lang.StringBuilder";

	/**
	 * Serializes a SURF resource graph to a string.
	 * <p>
	 * This is a convenience method that delegates to {@link #serialize(Writer, Object)}.
	 * </p>
	 * @param root The root SURF resource, or <code>null</code> if there is no resource to serialize.
	 * @throws IOException If there was an error writing the SURF data.
	 * @return A serialized string representation of the given SURF resource graph.
	 */
	public String serialize(@Nonnull @Nullable Object root) throws IOException {
		final Writer stringWriter = new StringWriter();
		try {
			serialize(stringWriter, root);
		} finally {
			stringWriter.close(); //close for completeness, not for necessity
		}
		return stringWriter.toString();
	}

	/**
	 * Serializes a SURF resource graph to an output stream..
	 * @param outputStream The output stream to receive SURF data.
	 * @param root The root SURF resource, or <code>null</code> if there is no resource to serialize.
	 * @throws IOException If there was an error writing the SURF data.
	 */
	public void serialize(@Nonnull final OutputStream outputStream, @Nullable Object root) throws IOException {
		serialize(new BufferedWriter(new OutputStreamWriter(outputStream, CHARSET)), root);
	}

	/**
	 * Serializes a SURF resource graph to a writer.
	 * @param writer The writer to receive SURF data.
	 * @param root The root SURF resource, or <code>null</code> if there is no resource to serialize.
	 * @throws NullPointerException if the given appendable is <code>null</code>.
	 * @throws IOException If there was an error writing the SURF data.
	 */
	public void serialize(@Nonnull final Writer writer, @Nullable Object root) throws IOException {
		if(root == null) {
			return;
		}
		serializeResource(writer, root);
	}

	/**
	 * Serializes a SURF resource to a writer.
	 * @param writer The writer to receive SURF data.
	 * @param resource The SURF resource to serialize to serialize.
	 * @throws NullPointerException if the given appendable and/or resource is <code>null</code>.
	 * @throws IOException If there was an error appending the SURF data.
	 */
	public void serializeResource(@Nonnull final Appendable appendable, @Nullable Object resource) throws IOException {
		if(resource instanceof SurfObject) { //objects
			throw new UnsupportedOperationException(); //TODO
		} else if(resource instanceof List) { //collections
			throw new UnsupportedOperationException(); //TODO
		} else { //literals

			switch(resource.getClass().toString()) { //use shortcut for final classes for efficiency
				//string
				case STRING_CLASS_NAME:
				case STRING_BUILDER_CLASS_NAME:
					serializeString(appendable, (CharSequence)resource);
					break;
				default:
					if(resource instanceof CharSequence) { //catch any other character sequence
						serializeString(appendable, (CharSequence)resource);
					}
					break;
			}
		}
	}

	/**
	 * Serializes a string surrounded by string delimiters.
	 * @param appendable The appendable to which SURF data should be appended.
	 * @param charSequence The information to be serialized as a SURF string.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see SURF#STRING_DELIMITER
	 * @see #parseCharacterCodePoint(Reader, char)
	 */
	public static void serializeString(@Nonnull final Appendable appendable, @Nonnull final CharSequence charSequence) throws IOException {
		appendable.append(STRING_DELIMITER);
		final int length = charSequence.length();
		for(int i = 0; i < length; i++) {
			final char c = charSequence.charAt(i);
			final int codePoint;
			if(Character.isHighSurrogate(c)) {
				checkArgument(c < length - 1, "Cannot serialize character sequence %s ending in high surrogate character.", charSequence);
				i++; //get complementary the high surrogate
				codePoint = Character.toCodePoint(c, charSequence.charAt(i));
			} else {
				checkArgument(!Character.isLowSurrogate(c), "Cannot serialize character sequence %s with illegal surrogate character sequence.", charSequence);
				codePoint = c;
			}
			serializeCharacterCodePoint(appendable, STRING_DELIMITER, codePoint);
		}
		appendable.append(STRING_DELIMITER);
	}

	/**
	 * Serializes a character as content, without any delimiters.
	 * <p>
	 * This implementation does not escape the solidus (slash) character <code>'/'</code>, which is not required to be escaped.
	 * </p>
	 * @param appendable The appendable to which SURF data should be appended.
	 * @param delimiter The delimiter that surrounds the character and which should be escaped.
	 * @param codePoint The code point to serialize.
	 * @throws NullPointerException if the given appendable is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if a control character was represented, if the character is not escaped correctly, or the reader has no more characters before the
	 *           current character is completely parsed.
	 * @see SURF#CHARACTER_REQUIRED_ESCAPED_CHARACTERS
	 */
	public static void serializeCharacterCodePoint(@Nonnull final Appendable appendable, final char delimiter, final int codePoint)
			throws IOException, ParseIOException {
		//TODO check for control characters
		if(codePoint == delimiter || (codePoint <= Character.MAX_VALUE && CHARACTER_REQUIRED_ESCAPED_CHARACTERS.contains((char)codePoint))) {
			appendable.append(CHARACTER_ESCAPE);
			final char escapeChar;
			switch(codePoint) {
				case CHARACTER_ESCAPE: //\\
					escapeChar = CHARACTER_ESCAPE;
					break;
				case BACKSPACE_CHAR: //\b backspace
					escapeChar = ESCAPED_BACKSPACE;
					break;
				case FORM_FEED_CHAR: //\f
					escapeChar = ESCAPED_FORM_FEED;
					break;
				case LINE_FEED_CHAR: //\n
					escapeChar = ESCAPED_LINE_FEED;
					break;
				case CARRIAGE_RETURN_CHAR: //\r
					escapeChar = ESCAPED_CARRIAGE_RETURN;
					break;
				case CHARACTER_TABULATION_CHAR: //\t
					escapeChar = ESCAPED_TAB;
					break;
				case LINE_TABULATION_CHAR: //\v
					escapeChar = ESCAPED_VERTICAL_TAB;
					break;
				default:
					assert codePoint == delimiter; //we should have covered everything else up to this point
					escapeChar = delimiter;
					break;
			}

			appendable.append(escapeChar);
			return;
		}
		//code points outside the BMP
		if(Character.isSupplementaryCodePoint(codePoint)) {
			appendable.append(Character.highSurrogate(codePoint)).append(Character.lowSurrogate(codePoint));
		}
		assert Character.isBmpCodePoint(codePoint); //everything else should be in the BMP and not need escaping
		appendable.append((char)codePoint);
	}
}
