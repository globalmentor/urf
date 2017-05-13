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

import static com.globalmentor.io.IOOptionals.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Conditions.*;
import static io.urf.SURF.*;
import static java.util.Objects.*;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.io.*;
import com.globalmentor.io.function.IOConsumer;
import com.globalmentor.java.CodePointCharacter;
import com.globalmentor.net.EmailAddress;

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
 * <h3>character</h3>
 * <ul>
 * <li>{@link Character}</li>
 * <li>{@link CodePointCharacter}</li>
 * </ul>
 * <h3>email address</h3>
 * <ul>
 * <li>{@link EmailAddress}</li>
 * </ul>
 * <h3>string</h3>
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

	private final static String CHARACTER_CLASS_NAME = "java.lang.Character";
	private final static String CODE_POINT_CHARACTER_CLASS_NAME = "com.globalmentor.java.CodePointCharacter";
	private final static String EMAIL_ADDRESS_CLASS_NAME = "com.globalmentor.net.EmailAddress";
	private final static String STRING_CLASS_NAME = "java.lang.String";
	private final static String STRING_BUILDER_CLASS_NAME = "java.lang.StringBuilder";

	private boolean formatted = false;

	/**
	 * Returns whether this serializer will format the document with additional whitespace and newlines.
	 * <p>
	 * This implementation defaults to no formatting.
	 * </p>
	 * @return Whether serialization output should be formatted.
	 */
	public boolean isFormatted() {
		return formatted;
	}

	/**
	 * Sets whether the serialization should be formatted.
	 * @param formatted Whether this serializer will format the document with additional whitespace and newlines.
	 */
	public void setFormatted(final boolean formatted) {
		this.formatted = formatted;
	}

	private CharSequence indentSequence = String.valueOf(CHARACTER_TABULATION_CHAR);

	/**
	 * Returns the sequence of characters used for each indention level.
	 * <p>
	 * This implementation defaults to the horizontal tab character.
	 * </p>
	 * @return The character(s) used for indention.
	 */
	public CharSequence getIndentSequence() {
		return indentSequence;
	}

	/**
	 * Set the sequence of characters used for each indention level.
	 * @param indentSequence The character(s) to use for indention.
	 */
	public void setIndentSequence(@Nonnull final CharSequence indentSequence) {
		this.indentSequence = requireNonNull(indentSequence);
	}

	/** The zero-based indention level. */
	private int indentLevel = 0;

	/**
	 * Increases the indention level. No information is appended.
	 * @return An object that will automatically unindent when {@link Closeable#close()} is called.
	 */
	protected Closeable increaseIndentLevel() {
		indentLevel++;
		return Close.by(this::decreaseIndentLevel);
	}

	/**
	 * Appends indention characters at the appropriate level if formatting is enabled.
	 * <p>
	 * If formatting is turned off, no content will be added.
	 * </p>
	 * @param appendable The appendable to which SURF data should be appended.
	 * @see #isFormatted()
	 * @see #getIndentSequence()
	 */
	protected void formatIndent(@Nonnull final Appendable appendable) throws IOException {
		if(isFormatted()) {
			final CharSequence indentSequence = getIndentSequence();
			for(int i = 0; i < indentLevel; ++i) {
				appendable.append(indentSequence);
			}
		}
	}

	/** Decreases the indention level. No information is appended. */
	protected void decreaseIndentLevel() {
		indentLevel--;
	}

	private CharSequence lineSeparator = System.lineSeparator();

	/**
	 * Returns the sequence of characters used to separate lines.
	 * <p>
	 * This implementation defaults to the platform-dependent line separator for the current system.
	 * </p>
	 * @return The character(s) used for line endings.
	 * @see System#lineSeparator()
	 */
	public CharSequence getLineSeparator() {
		return lineSeparator;
	}

	/**
	 * Sets the sequence of characters used to separate lines.
	 * @param lineSeparator The character(s) to use for line endings.
	 * @see System#lineSeparator()
	 */
	public void setLineSeparator(@Nonnull final CharSequence lineSeparator) {
		this.lineSeparator = requireNonNull(lineSeparator);
	}

	/**
	 * Separates lines by adding a line separation character sequence if formatting is enabled.
	 * <p>
	 * If formatting is turned off, no content will be added.
	 * </p>
	 * @param appendable The appendable to which SURF data should be appended.
	 * @return Whether or not a line separator sequence was actually appended.
	 * @see #isFormatted()
	 * @see #getLineSeparator()
	 */
	protected boolean formatNewLine(@Nonnull final Appendable appendable) throws IOException {
		final boolean isNewlineAppended = isFormatted();
		if(isNewlineAppended) {
			appendable.append(lineSeparator);
		}
		return isNewlineAppended;
	}

	private boolean sequenceSeparatorRequired = false;

	/**
	 * Whether separators will always be added between sequence items even if newlines are present.
	 * <p>
	 * This implementation defaults to not adding sequence separators if not needed.
	 * </p>
	 * @return Whether sequence separators will always be added even when options.
	 */
	public boolean isSequenceSeparatorRequired() {
		return sequenceSeparatorRequired;
	}

	/**
	 * Sets whether separators will always be added between sequence items even if newlines are present.
	 * @param sequenceSeparatorRequired Whether sequence separators will always be added even when options.
	 */
	public void setSequenceSeparatorRequired(final boolean sequenceSeparatorRequired) {
		this.sequenceSeparatorRequired = sequenceSeparatorRequired;
	}

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
	 * @param appendable The appendable to which SURF data should be appended.
	 * @param root The root SURF resource, or <code>null</code> if there is no resource to serialize.
	 * @throws NullPointerException if the given appendable is <code>null</code>.
	 * @throws IOException If there was an error writing the SURF data.
	 */
	public void serialize(@Nonnull final Appendable appendable, @Nullable Object root) throws IOException {
		if(root == null) {
			return;
		}
		serializeResource(appendable, root);
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
			serializeObject(appendable, (SurfObject)resource);
		} else if(resource instanceof List) { //collections
			throw new UnsupportedOperationException(); //TODO
		} else { //literals
			switch(resource.getClass().getName()) { //use shortcut for final classes for efficiency
				//character
				case CHARACTER_CLASS_NAME:
					serializeCharacter(appendable, ((Character)resource).charValue());
					break;
				case CODE_POINT_CHARACTER_CLASS_NAME:
					serializeCharacter(appendable, ((CodePointCharacter)resource).getCodePoint());
					break;
				case EMAIL_ADDRESS_CLASS_NAME:
					serializeEmailAddress(appendable, (EmailAddress)resource);
					break;
				//string
				case STRING_CLASS_NAME:
				case STRING_BUILDER_CLASS_NAME:
					serializeString(appendable, (CharSequence)resource);
					break;
				default:
					if(resource instanceof CharSequence) { //catch any other character sequence
						serializeString(appendable, (CharSequence)resource);
					} else {
						throw new UnsupportedOperationException("Unsupported SURF serialization type: " + resource.getClass().getName());
					}
					break;
			}
		}
	}

	//objects

	/**
	 * Serializes a SURF object.
	 * @param appendable The appendable to which SURF data should be appended.
	 * @param surfObject The information to be serialized as a SURF string.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see SURF#OBJECT_BEGIN
	 * @see SURF#PROPERTIES_BEGIN
	 * @see SURF#PROPERTIES_END
	 * @see #parseCharacterCodePoint(Reader, char)
	 */
	public void serializeObject(@Nonnull final Appendable appendable, @Nonnull final SurfObject surfObject) throws IOException {
		appendable.append(OBJECT_BEGIN); //*
		ifPresent(surfObject.getTypeName(), appendable::append); //typeName
		appendable.append(PROPERTIES_BEGIN); //:
		formatNewLine(appendable);
		try (final Closeable indention = increaseIndentLevel()) {
			serializeSequence(appendable, surfObject.getPropertyNameValuePairs(), property -> {
				appendable.append(property.getName());
				if(formatted) {
					appendable.append(SPACE_CHAR);
				}
				appendable.append(PROPERTY_VALUE_DELIMITER); //=
				if(formatted) {
					appendable.append(SPACE_CHAR);
				}
				serializeResource(appendable, property.getValue());
			});
		}
		formatIndent(appendable);
		appendable.append(PROPERTIES_END); //;
	}

	//literals

	/**
	 * Serializes a character surrounded by character delimiters.
	 * @param appendable The appendable to which SURF data should be appended.
	 * @param codePoint The Unicode code point to be serialized as a SURF character.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IllegalArgumentException if the given code point is not a valid Unicode code point.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see SURF#CHARACTER_DELIMITER
	 * @see #serializeCharacterCodePoint(Appendable, char, int)
	 */
	public static void serializeCharacter(@Nonnull final Appendable appendable, @Nonnull final int codePoint) throws IOException {
		checkArgument(Character.isValidCodePoint(codePoint), "The value %d does not represent is not a valid code point.", codePoint);
		appendable.append(CHARACTER_DELIMITER);
		serializeCharacterCodePoint(appendable, CHARACTER_DELIMITER, codePoint);
		appendable.append(CHARACTER_DELIMITER);
	}

	/**
	 * Serializes an email address along with its delimiter.
	 * @param appendable The appendable to which SURF data should be appended.
	 * @param emailAddress The information to be serialized as a SURF email address.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see SURF#EMAIL_ADDRESS_BEGIN
	 */
	public static void serializeEmailAddress(@Nonnull final Appendable appendable, @Nonnull final EmailAddress emailAddress) throws IOException {
		appendable.append(EMAIL_ADDRESS_BEGIN);
		appendable.append(emailAddress.toString());
	}

	/**
	 * Serializes a string surrounded by string delimiters.
	 * @param appendable The appendable to which SURF data should be appended.
	 * @param charSequence The information to be serialized as a SURF string.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see SURF#STRING_DELIMITER
	 * @see #serializeCharacterCodePoint(Appendable, char, int)
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
	 * @throws IOException if there is an error appending to the appender.
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
			return;
		}
		//code points within the BMP
		assert Character.isBmpCodePoint(codePoint); //everything else should be in the BMP and not need escaping
		appendable.append((char)codePoint);
	}

	/**
	 * Serializes a general SURF sequence (such as a list). For each sequence item, {@link IOConsumer#accept(Object)} is called, passing the {@link Appendable},
	 * for the item to be serialized. The item serialization strategy will return <code>false</code> to indicate that there are no further items.
	 * @param <I> The type of item in the sequence.
	 * @param appendable The appendable to which SURF data should be appended.
	 * @param itemSerializer The serialization strategy, which is passed the {@link Appendable} to use for serialization.
	 * @throws NullPointerException if the given appendable and/or item serializer is <code>null</code>.
	 * @throws IOException if there is an error appending to the appender.
	 */
	protected <I> void serializeSequence(@Nonnull final Appendable appendable, @Nonnull Iterable<I> sequence, @Nonnull final IOConsumer<I> itemSerializer)
			throws IOException {
		final boolean sequenceSeparatorRequired = isSequenceSeparatorRequired();
		final Iterator<I> iterator = sequence.iterator();
		while(iterator.hasNext()) {
			formatIndent(appendable);
			final I item = iterator.next();
			itemSerializer.accept(item); //serialize the item
			final boolean hasNext = iterator.hasNext(); //see if there is another item after this one
			if(sequenceSeparatorRequired && hasNext) { //add a separator if one is required
				appendable.append(SEQUENCE_DELIMITER);
			}
			//skip to the next line, if formatting;
			//if none was added (and we didn't already add a separator) and there is another item,
			//add a separator
			if(!formatNewLine(appendable) && !sequenceSeparatorRequired && hasNext) {
				appendable.append(SEQUENCE_DELIMITER);
			}
		}
		;
	}

}
