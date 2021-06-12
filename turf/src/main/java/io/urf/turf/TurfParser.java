/*
 * Copyright © 2017-2018 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static com.globalmentor.io.ReaderParser.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.net.URIs.*;
import static io.urf.URF.*;
import static io.urf.turf.TURF.*;
import static io.urf.turf.TURF.WHITESPACE_CHARACTERS;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static org.zalando.fauxpas.FauxPas.throwingFunction;

import java.io.*;
import java.math.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.*;

import com.globalmentor.io.ParseIOException;
import com.globalmentor.io.function.IOConsumer;
import com.globalmentor.iso.ISO8601;
import com.globalmentor.itu.TelephoneNumber;
import com.globalmentor.java.Characters;
import com.globalmentor.java.CodePointCharacter;
import com.globalmentor.model.UUIDs;
import com.globalmentor.net.MediaType;
import com.globalmentor.net.EmailAddress;
import com.globalmentor.text.*;
import com.globalmentor.util.Optionals;

import io.urf.URF;
import io.urf.model.*;

/**
 * Parser for the Text URF (TURF) document format.
 * <p>
 * This parser is meant to be used once for parsing a single TURF document. It should not be used to parse multiple documents, as it maintains parsing state.
 * </p>
 * <p>
 * The parser should be released after use so as not to leak memory of parsed resources when resources are present with tags/IDs/aliases.
 * </p>
 * <p>
 * This implementation is not thread safe.
 * </p>
 * @param <R> The type of result returned by the parser, based upon the {@link UrfProcessor} used.
 * @author Garret Wilson
 * @see UrfProcessor
 */
public class TurfParser<R> {

	/**
	 * Represents an alias label parsed in a TURF document.
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

	/** The map of resource references that have been given an alias */
	private final Map<Alias, UrfReference> aliasedReferences = new HashMap<>();

	/**
	 * Returns a reference to a parsed resource by its alias.
	 * @param alias The alias used by the resource in the document.
	 * @return The resource associated with the given alias, if any.
	 */
	public Optional<UrfReference> findResourceByAlias(@Nonnull final String alias) {
		return Optional.ofNullable(aliasedReferences.get(new Alias(alias))); //TODO be more rigorous here if/when null can be aliased
	}

	private Map<String, URI> namepaces = emptyMap();

	/** @return The known namespace mappings, keyed to aliases */
	protected Map<String, URI> getNamespaces() {
		return namepaces;
	}

	private final UrfProcessor<R> processor;

	/** @return The strategy for processing the parsed URF graph. */
	protected UrfProcessor<R> getProcessor() {
		return processor;
	}

	/**
	 * Constructor.
	 * @param processor The strategy for processing the parsed URF graph.
	 */
	public TurfParser(@Nonnull final UrfProcessor<R> processor) {
		this.processor = requireNonNull(processor);
	}

	/**
	 * Parses a TURF document from a string.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @implSpec This implementation delegates to {@link #parseDocument(String, MediaType)} using {@link TURF#MEDIA_TYPE}.
	 * @param string The string containing TURF data.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parseDocument(@Nonnull final String string) throws IOException, ParseIOException {
		return parseDocument(string, TURF.MEDIA_TYPE);
	}

	/**
	 * Parses a TURF properties document from a string.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @implSpec This implementation delegates to {@link #parseDocument(String, MediaType)} using {@link TURF#PROPERTIES_MEDIA_TYPE}.
	 * @param string The string containing TURF data.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parsePropertiesDocument(@Nonnull final String string) throws IOException, ParseIOException {
		return parseDocument(string, TURF.PROPERTIES_MEDIA_TYPE);
	}

	/**
	 * Parses a TURF document from a string.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @implSpec This is a convenience method that delegates to {@link #parseDocument(Reader, MediaType)}.
	 * @param string The string containing TURF data.
	 * @param contentType The Internet media type of the TURF document being parsed, or <code>null</code> if the TURF variant should be automatically detected.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IllegalArgumentException if the given content type is not supported.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parseDocument(@Nonnull final String string, @Nullable final MediaType contentType) throws IOException, ParseIOException {
		try (final Reader stringReader = new StringReader(string)) {
			return parseDocument(stringReader, contentType);
		}
	}

	/**
	 * Parses a TURF resource from a TURF document input stream.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @implSpec This implementation delegates to {@link #parseDocument(InputStream, MediaType)} using {@link TURF#MEDIA_TYPE}.
	 * @param inputStream The input stream containing TURF data.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parseDocument(@Nonnull final InputStream inputStream) throws IOException, ParseIOException {
		return parseDocument(inputStream, TURF.MEDIA_TYPE);
	}

	/**
	 * Parses a TURF resource from a TURF properties document input stream.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @implSpec This implementation delegates to {@link #parseDocument(InputStream, MediaType)} using {@link TURF#PROPERTIES_MEDIA_TYPE}.
	 * @param inputStream The input stream containing TURF data.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parsePropertiesDocument(@Nonnull final InputStream inputStream) throws IOException, ParseIOException {
		return parseDocument(inputStream, TURF.PROPERTIES_MEDIA_TYPE);
	}

	/**
	 * Parses a TURF resource from an input stream.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @implSpec This implementation delegates to {@link #parseDocument(Reader, MediaType)}.
	 * @param inputStream The input stream containing TURF data.
	 * @param contentType The Internet media type of the TURF document being parsed, or <code>null</code> if the TURF variant should be automatically detected.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IllegalArgumentException if the given content type is not supported.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parseDocument(@Nonnull final InputStream inputStream, @Nullable final MediaType contentType) throws IOException, ParseIOException {
		return parseDocument(new LineNumberReader(new InputStreamReader(inputStream, DEFAULT_CHARSET)), contentType);
	}

	/**
	 * Parses TURF resources from a TURF document reader.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @implSpec This implementation delegates to {@link #parseDocument(Reader, MediaType)} using {@link TURF#MEDIA_TYPE}.
	 * @param reader The reader containing TURF data.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parseDocument(@Nonnull final Reader reader) throws IOException, ParseIOException {
		return parseDocument(reader, TURF.MEDIA_TYPE);
	}

	/**
	 * Parses TURF resources from a TURF properties document reader.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @implSpec This implementation delegates to {@link #parseDocument(Reader, MediaType)} using {@link TURF#PROPERTIES_MEDIA_TYPE}.
	 * @param reader The reader containing TURF data.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parsePropertiesDocument(@Nonnull final Reader reader) throws IOException, ParseIOException {
		return parseDocument(reader, TURF.PROPERTIES_MEDIA_TYPE);
	}

	/**
	 * Parses TURF resources from a reader.
	 * @apiNote One of the root resources is returned as a convenience. There is no guarantee which root resource will be returned.
	 * @param reader The reader containing TURF data.
	 * @param contentType The Internet media type of the TURF document being parsed, or <code>null</code> if the TURF variant should be automatically detected.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IllegalArgumentException if the given content type is not supported.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public R parseDocument(@Nonnull final Reader reader, @Nullable final MediaType contentType) throws IOException, ParseIOException {
		//TODO add support for SURF?
		checkArgument(contentType == null || contentType.hasBaseType(TURF.MEDIA_TYPE) || contentType.hasBaseType(TURF.PROPERTIES_MEDIA_TYPE),
				"TURF parser does not support documents of type >%s<.", contentType);

		//header
		final Optional<MediaType> foundDoctype;
		int c = peek(reader); //the header has to come a the first of the document, if at all
		final boolean hasHeader = c == DIVISION_BEGIN;
		if(hasHeader) {
			check(reader, DIVISION); //===
			final Map.Entry<MediaType, Optional<Map<URI, ValueUrfResource<?>>>> namespaceMapForDoctype = parseMediaType(reader, true);
			final MediaType doctype = namespaceMapForDoctype.getKey();
			if(contentType != null) {
				checkParseIO(reader, doctype.hasBaseType(contentType), "Doctype >%s< does not has same base type as requested content type >%s<.", doctype,
						contentType);
			} else { //autodetect
				checkArgument(doctype.hasBaseType(TURF.MEDIA_TYPE) || doctype.hasBaseType(TURF.PROPERTIES_MEDIA_TYPE),
						"Document type >%s< not supported; must be >%s< or >%s<.", doctype, TURF.MEDIA_TYPE, TURF.PROPERTIES_MEDIA_TYPE);
			}
			foundDoctype = Optional.of(doctype);
			//TODO store the document type somewhere for later retrieval

			//gather the namespace declarations from the embedded doctype description
			namepaces = Optionals.stream(namespaceMapForDoctype.getValue().map(Map::entrySet)).flatMap(Set::stream)
					//only look at namespace property
					.filter(entry -> Tag.findNamespace(entry.getKey()).filter(SPACE_NAMESPACE::equals).isPresent())
					//convert each namespace property to a namespace entry
					.map(throwingFunction(entry -> {
						final String namespaceAlias = Tag.findName(entry.getKey())
								.orElseThrow(() -> new ParseIOException("Document property `<" + entry.getKey() + ">` missing namespace alias."));
						final URI namespace = ObjectUrfResource.findObject(entry.getValue()).filter(URI.class::isInstance).map(URI.class::cast)
								.orElseThrow(() -> new ParseIOException("Namespace alias " + namespaceAlias + " must be mapped to an IRI."));
						//TODO add method to check that a URI is a potential namespace
						return new AbstractMap.SimpleImmutableEntry<>(namespaceAlias, namespace);
					})).collect(Collectors.collectingAndThen(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue), Collections::unmodifiableMap));
			//TODO log warning for ignored directives?
		} else {
			foundDoctype = Optional.empty();
		}

		//Because we checked for the header at the beginning of the document, we now need to skip vertical filler,
		//whether or not there was a header.
		c = skipFiller(reader);

		//document description
		if(c == DOCUMENT_DESCRIPTION_DELIMITER) {
			check(reader, DOCUMENT_DESCRIPTION_DELIMITER); //#
			c = skipFiller(reader);
			final Map<String, URI> namespaces = new HashMap<>();
			parseSequence(reader, DOCUMENT_DESCRIPTION_DELIMITER, r -> {
				final String propertyHandle = parseHandle(reader);
				@SuppressWarnings("unused")
				final URI propertyTag;
				try {
					propertyTag = Handle.toTag(propertyHandle, namespaces);
				} catch(final IllegalArgumentException illegalArgumentException) {
					throw new ParseIOException(reader, illegalArgumentException.getMessage(), illegalArgumentException);
				}
				skipFiller(reader);
				check(reader, PROPERTY_VALUE_DELIMITER); //=
				skipFiller(reader);
				@SuppressWarnings("unused")
				final UrfReference value = parseResource(reader, false);
				//TODO make sure the resource is a literal
				//TODO process document description
			});
			check(reader, DOCUMENT_DESCRIPTION_DELIMITER); //#
			c = skipFiller(reader);
		}

		try { //if neither a content type or a doctype was specified, assumed >text/urf< 
			parsePartBody(reader, foundDoctype.orElseGet(() -> contentType != null ? contentType : TURF.MEDIA_TYPE));
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new ParseIOException(reader, illegalArgumentException.getMessage(), illegalArgumentException);
		}

		//TODO footer
		return getProcessor().getResult();
	}

	/**
	 * Parses body of a TURF part. Resources are reported as root resources.
	 * @apiNote Currently there is only one TURF part, the TURF document itself, so this method parses the document body, and the part type is the doctype.
	 * @param reader The reader containing TURF data.
	 * @param partType The type of this part; usually defined in the part header.
	 * @throws IllegalArgumentException if the part type is not supported or has unsupported parameters.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	protected void parsePartBody(@Nonnull final Reader reader, @Nonnull final MediaType partType) throws IOException, ParseIOException {
		if(partType.hasBaseType(TURF.MEDIA_TYPE)) { //>text/urf<
			checkArgument(partType.getParameters().isEmpty(), "Body with type >%s< does not support media type parameters.", partType);
			boolean nextItemRequired = false; //at the beginning out there is no requirement for items (i.e. an empty document is possible)
			boolean nextItemProhibited = false;
			int c;
			while((c = peek(reader)) >= 0 || nextItemRequired) {
				if(c >= 0 && nextItemProhibited) {
					throw new ParseIOException(reader, "Unexpected data; perhaps a missing sequence delimiter.");
				}
				final UrfReference rootResource = parseResource(reader);
				getProcessor().reportRootResource(rootResource); //register the resource as a root
				final Optional<Boolean> requireItem = skipSequenceDelimiters(reader);
				//TODO add check for SURF documents: checkParseIO(reader, skipLineBreaks(reader) < 0, "No content allowed after root resource.");
				if(requireItem.isPresent()) {
					nextItemRequired = requireItem.get().booleanValue(); //see if a new item is required
					nextItemProhibited = false;
				} else {
					nextItemRequired = false;
					nextItemProhibited = true;
				}
			}
		} else if(partType.hasBaseType(TURF.PROPERTIES_MEDIA_TYPE)) { //>text/urf-properties<
			final URI blankTag = Tag.generateBlank();
			final UrfResource object = new SimpleUrfResource(blankTag, (URI)null);
			getProcessor().declareResource(blankTag);
			final int c = parseDescriptionBody(reader, DIVISION_BEGIN, object);
			checkParseIO(reader, c != DIVISION_BEGIN, "Ending and inter-divisions not yet supported; character `%s` not allowed after body properties.", c);
			assert c == -1 : "End of reader should have been found after body properties.";
			getProcessor().reportRootResource(object); //register the object as a root
		} else {
			throw new IllegalArgumentException(String.format("Unsupported body type >%s<.", partType));
		}
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
	 * @see TURF#LABEL_DELIMITER
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
	 * @see URF.Name#isTokenBeginCharacter(int)
	 * @see URF.Name#isTokenCharacter(int)
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
			reader.mark(MINIMUM_MARK); //mark our current position
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
	 * Parses a name ID token composed of name token characters. The current position must be that of the first name token character. The new position will be
	 * that immediately after the last name token character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The name token parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if there are are no name characters.
	 * @see URF.Name#isTokenCharacter(int)
	 */
	protected static String parseNameIdToken(@Nonnull final Reader reader) throws IOException, ParseIOException {
		final StringBuilder stringBuilder = new StringBuilder(); //create a string builder for reading the name segment
		int c = reader.read(); //read the first name ID character
		if(!Name.isTokenCharacter(c)) { //if the name doesn't start with a name token character
			checkReaderNotEnd(reader, c); //make sure we're not at the end of the reader
			throw new ParseIOException(reader, String.format("Expected name token character; found %s.", Characters.getLabel(c)));
		}
		do {
			stringBuilder.append((char)c); //append the character
			reader.mark(MINIMUM_MARK); //mark our current position
			c = reader.read(); //read another character
		} while(Name.isTokenCharacter(c)); //keep reading and appending until we reach a non-name token character
		if(c >= 0) { //if we didn't reach the end of the stream
			reader.reset(); //reset to the last mark, which was set right before the non-name character we found
		}
		final String nameToken = stringBuilder.toString();
		checkParseIO(reader, Name.isValidIdToken(nameToken), "Invalid name ID token %s.", nameToken);
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
		if(confirm(reader, Handle.NAMESPACE_ALIAS_DELIMITER)) { //if this was a namespace delimiter
			stringBuilder.append(Handle.NAMESPACE_ALIAS_DELIMITER); ///
			stringBuilder.append(parseNameToken(reader)); //there will always be one name token after the namespace delimiter
		}
		while(confirm(reader, Handle.SEGMENT_DELIMITER)) { //read another handle segments if present
			stringBuilder.append(Handle.SEGMENT_DELIMITER); //-
			stringBuilder.append(parseNameToken(reader)); //name token
		}
		if(confirm(reader, Name.NARY_DELIMITER)) { //see if there is an n-ary delimiter
			stringBuilder.append(Name.NARY_DELIMITER); //+
		}
		if(confirm(reader, Name.ID_DELIMITER)) { //see if there is an ID
			stringBuilder.append(Name.ID_DELIMITER); //#
			stringBuilder.append(parseNameIdToken(reader)); //name ID Token
		}
		final String handle = stringBuilder.toString();
		checkParseIO(reader, Handle.isValid(handle), "Invalid handle %s.", handle);
		return handle;
	}

	/**
	 * Parses a reference to a resource. A reference is either a handle (e.g. {@code example-fooBar}) or a tag label ({@code |<https://example.com/fooBar>|}. The
	 * current position must be that of the first reference character. The new position will be that immediately after the last reference character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The tag representing the resource reference parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if there are are no reference characters, if a non-tag label was encountered, or if a handle refers to an unregistered namespace
	 *           alias.
	 * @see #parseHandle(Reader)
	 * @see #parseLabel(Reader)
	 */
	protected URI parseTagReference(@Nonnull final Reader reader) throws IOException, ParseIOException {
		return parseTagReference(reader, getNamespaces());
	}

	/**
	 * Parses a reference to a resource tag. A tag reference is either a handle (e.g. {@code example-fooBar}) or a tag label
	 * ({@code |<https://example.com/fooBar>|}. The current position must be that of the first reference character. The new position will be that immediately
	 * after the last reference character.
	 * @param reader The reader the contents of which to be parsed.
	 * @param namespaces The registered namespaces, associated with their aliases.
	 * @return The tag representing the resource reference parsed from the reader.
	 * @throws NullPointerException if the given reader and/or namespaces map is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if there are are no reference characters, if a non-tag label was encountered, or if a handle refers to an unregistered namespace
	 *           alias.
	 * @see #parseHandle(Reader)
	 * @see #parseLabel(Reader)
	 */
	public static URI parseTagReference(@Nonnull final Reader reader, @Nonnull final Map<String, URI> namespaces) throws IOException, ParseIOException {
		requireNonNull(namespaces);
		if(peek(reader) == LABEL_DELIMITER) {
			final Object label = parseLabel(reader);
			checkParseIO(reader, label instanceof URI, "Non-tag label %s encountered as reference.", label);
			return (URI)label;
		} else {
			final String handle = parseHandle(reader);
			try {
				return Handle.toTag(handle, namespaces);
			} catch(final IllegalArgumentException illegalArgumentException) {
				throw new ParseIOException(reader, illegalArgumentException.getMessage(), illegalArgumentException);
			}
		}
	}

	/**
	 * Parses a resource; either a tag or a resource representation with an optional description. The next character read must be the start of the resource.
	 * @param reader The reader containing TURF data.
	 * @return A reference to the resource read and parsed from the reader.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public UrfReference parseResource(@Nonnull final Reader reader) throws IOException {
		return parseResource(reader, true);
	}

	/**
	 * Parses a resource; either a tag or a resource representation. The next character read must be the start of the resource.
	 * @param reader The reader containing TURF data.
	 * @param allowDescription Whether a description is allowed; if <code>false</code>, any following description delimiter will not be considered part of the
	 *          resource.
	 * @return A reference to the resource read and parsed from the reader.
	 * @throws IOException If there was an error reading the TURF data.
	 * @throws ParseIOException if the TURF data was invalid.
	 */
	public UrfReference parseResource(@Nonnull final Reader reader, final boolean allowDescription) throws IOException {
		Object label = null;
		int c = peek(reader);
		if(c == LABEL_DELIMITER) {
			label = parseLabel(reader);
			if(label instanceof Alias) { //the TURF parser keeps track of certain aliased things, so return it if we have it already
				final UrfReference reference = aliasedReferences.get((Alias)label);
				if(reference != null) {
					return reference; //TODO do any of the things the TURF parser keeps track of allow descriptions with the alias?
				}
			}
			c = skip(reader, WHITESPACE_CHARACTERS);
		}

		UrfResource instanceReference = null; //we'll record whether we parse some instance reference TODO think of better name; one we can use in the specification 

		final UrfResource resource;
		switch(c) {
			//objects
			case OBJECT_BEGIN: //* type?
				resource = parseObject(label, reader);
				instanceReference = resource;
				break;
			//literals
			case BINARY_BEGIN:
				resource = new UrfBinaryResource(parseBinary(reader));
				break;
			case CHARACTER_DELIMITER:
				resource = new DefaultValueUrfResource<>(CHARACTER_TYPE_TAG, parseCharacter(reader));
				break;
			/* TURF support handles, which could begin with `false` or `true`, so they must both be handled along with object handles, below
			case BOOLEAN_FALSE_BEGIN:
			case BOOLEAN_TRUE_BEGIN:
				resource = new DefaultValueUrfResource<>(BOOLEAN_TYPE_TAG, parseBoolean(reader));
				break;
			*/
			case EMAIL_ADDRESS_BEGIN:
				resource = new DefaultValueUrfResource<>(EMAIL_ADDRESS_TYPE_TAG, parseEmailAddress(reader));
				break;
			case IRI_BEGIN:
				resource = new DefaultValueUrfResource<>(IRI_TYPE_TAG, parseIRI(reader));
				break;
			case MEDIA_TYPE_BEGIN:
				resource = new UrfMediaTypeResource(parseMediaType(reader));
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
				resource = parseNumberResource(reader);
				break;
			case REGULAR_EXPRESSION_DELIMITER:
				resource = new UrfRegularExpressionResource(parseRegularExpression(reader));
				break;
			case STRING_DELIMITER:
				resource = new DefaultValueUrfResource<>(STRING_TYPE_TAG, parseString(reader));
				break;
			case TELEPHONE_NUMBER_BEGIN:
				resource = new DefaultValueUrfResource<>(TELEPHONE_NUMBER_TYPE_TAG, parseTelephoneNumber(reader));
				break;
			case TEMPORAL_BEGIN:
				resource = parseTemporalResource(reader);
				break;
			case UUID_BEGIN:
				resource = new DefaultValueUrfResource<>(UUID_TYPE_TAG, parseUuid(reader));
				break;
			//collections
			case LIST_BEGIN:
				checkParseIO(reader, label == null || label instanceof Alias, "Non-alias label |%s| cannot be used with collection.", label);
				resource = parseListResource((Alias)label, reader);
				break;
			case MAP_BEGIN:
				checkParseIO(reader, label == null || label instanceof Alias, "Non-alias label |%s| cannot be used with collection.", label);
				resource = parseMapResource((Alias)label, reader);
				break;
			case SET_BEGIN:
				checkParseIO(reader, label == null || label instanceof Alias, "Non-alias label |%s| cannot be used with collection.", label);
				resource = parseSetResource((Alias)label, reader);
				break;
			default:
				//object
				if(Handle.isBeginCharacter(c)) { //false | true | handle [* type]
					final String handle = parseHandle(reader);
					//check the special cases of `false` and `true`, which are Boolean representations that are also handles
					switch(handle) {
						case BOOLEAN_FALSE_LEXICAL_FORM: //false
							resource = new DefaultValueUrfResource<>(BOOLEAN_TYPE_TAG, Boolean.FALSE);
							break;
						case BOOLEAN_TRUE_LEXICAL_FORM: //true
							resource = new DefaultValueUrfResource<>(BOOLEAN_TYPE_TAG, Boolean.TRUE);
							break;
						default: //handle [* type]
							checkParseIO(reader, !(label instanceof URI), "Object with tag label %s may not also be represented by handle %s.", label, handle);
							checkParseIO(reader, !(label instanceof String), "Object with ID label %s may not also be represented by handle %s.", label, handle);
							final URI handleTag = Handle.toTag(handle, getNamespaces());
							if(skip(reader, WHITESPACE_CHARACTERS) == OBJECT_BEGIN) { //handle * type
								//TODO this implementation prevents (ignores) an alias for a handle reference; do we want to allow that?
								resource = parseObject(handleTag, reader);
							} else { //handle
								resource = new SimpleUrfResource(handleTag);
							}
							instanceReference = resource;
							break;
					}
				} else if(label instanceof URI) { //"bare" tag label
					final URI tag = (URI)label;
					getProcessor().declareResource(tag);
					resource = new SimpleUrfResource(tag);
					instanceReference = resource;
				} else if(label instanceof Alias) { //"bare" alias label
					final URI blankTag = Tag.generateBlank(label.toString());
					getProcessor().declareResource(blankTag);
					resource = new SimpleUrfResource(blankTag);
				} else if(label instanceof String) { //"bare" ID label
					throw new ParseIOException(reader, String.format("Object with ID %s does not indicate an object and type.", label));
				} else { //no label at all
					throw new ParseIOException(reader, "Expected resource; found character: " + Characters.getLabel(c));
				}
				break;
		}

		//check and associate with the label as needed
		if(label instanceof URI) { //tag
			checkParseIO(reader, resource instanceof UrfResource, "Tag |%s| cannot be used with this resource.", label);
			assert label.equals(((UrfResource)resource).getTag().orElse(null));
		} else if(label instanceof String) { //ID
			checkParseIO(reader, resource instanceof UrfResource, "ID |%s| cannot be used with this resource.", label);
			final UrfResource urfResource = (UrfResource)resource;
			assert urfResource.getTag().isPresent() : String.format("Object with ID %s should have been given a tag when parsing.", label);
		} else if(label instanceof Alias) { //alias
			checkParseIO(reader, resource != null, "Cannot use alias |%s| with null.", label);
			if(!(resource instanceof Collection)) { //collections already saved the association
				aliasedReferences.put((Alias)label, resource);
			}
			//aliases can refer to UrfObjects, collections, or value objects; normally we would want to ensure == for TurfObjects,
			//but value objects are substitutable, so it's not good practice to compare them using ==
			assert findResourceByAlias(label.toString()).isPresent();
		}

		//if there was some object reference, process the object
		if(instanceReference != null) { //TODO there may be a better way to check; maybe just see if the resource is an instance of SimpleUrfResource, or not an object resource
			final URI tag = instanceReference.getTag().orElseThrow(() -> new AssertionError("Parsed object missing tag.")); //TODO don't we have a utility method for this supplier?
			final URI declaredTypeTag = instanceReference.getTypeTag().orElse(null); //TODO switch to Java 9 Optional.or()
			getProcessor().declareResource(tag, declaredTypeTag);
		}

		//description (optional)
		//TODO decide how much to allow: if(allowDescription && resource instanceof UrfObject) { //TODO make everything an UrfResource, e.g. PojoUrfResource for strings, and allow anything to have a description
		if(allowDescription) { //TODO make everything an UrfResource, e.g. PojoUrfResource for strings, and allow anything to have a description
			c = peek(reader);
			if(c == DESCRIPTION_BEGIN) {
				parseDescription(reader, resource);
			}
		}

		return resource;
	}

	//objects

	/**
	 * Parses an object, a described resource instance indicated by {@value TURF#OBJECT_BEGIN}. The next character read is expected to be
	 * {@value TURF#OBJECT_BEGIN}.
	 * @param tag The object resource's globally identifying tag, or <code>null</code> if the object has no tag.
	 * @param reader The reader containing TURF data.
	 * @return The resource read from the reader.
	 * @throws IOException If there was an error reading the TURF data.
	 * @see TURF#OBJECT_BEGIN
	 */
	public UrfResource parseObject(@Nullable URI tag, @Nonnull final Reader reader) throws IOException {
		return parseObject((Object)tag, reader);
	}

	/**
	 * Parses an object indicated by {@value TURF#OBJECT_BEGIN}. The next character read is expected to be {@value TURF#OBJECT_BEGIN}.
	 * <p>
	 * If the given label indicates an ID, after parsing the object type if an object already exists with the given ID for that type, it will be immediately
	 * returned.
	 * </p>
	 * <p>
	 * This method only collects the object information; it does not pass them to the URF processor. That is the responsibility of the caller.
	 * </p>
	 * @param label The object label (or tag from a handle); either an {@link Alias}, a {@link URI} representing a tag IRI, or a {@link String} representing an
	 *          ID.
	 * @param reader The reader containing TURF data.
	 * @return The resource read from the reader.
	 * @throws IOException If there was an error reading the TURF data.
	 * @see TURF#OBJECT_BEGIN
	 */
	protected UrfResource parseObject(@Nullable Object label, @Nonnull final Reader reader) throws IOException {
		//TODO add support for described types; make sure no tag is present in that case
		check(reader, OBJECT_BEGIN); //*
		int c = skip(reader, WHITESPACE_CHARACTERS);
		//type (optional)
		final URI typeTag;
		if(c >= 0 && (c == LABEL_DELIMITER || Handle.isBeginCharacter((char)c))) {
			typeTag = parseTagReference(reader);
			//if a type#id was already defined return it
			//TODO check for actual IDs in handle; somewhere, perhaps in caller
			if(label instanceof String) {
				//TODO add to specification
				//TODO add bad TURF document to tests
				checkParseIO(reader, typeTag.getRawFragment() == null, "Type tag %s with fragment may not be used with additional ID %s.", typeTag, label);
				//TODO document in the spec that TURF objects can have additional descriptions elsewhere and do not need *
			}
			c = skip(reader, WHITESPACE_CHARACTERS);
		} else {
			typeTag = null;
		}
		final UrfResource resource; //create a resource based upon the type of label
		if(label instanceof URI) { //tag
			//TODO make sure that any tag label doesn't specify a different type than the object specifies
			//TODO if an ID tag was _not_ passed, declare the type of the resource with the processor
			resource = new SimpleUrfResource((URI)label, typeTag);
		} else if(label instanceof String) { //ID
			checkParseIO(reader, typeTag != null, "Object with ID %s does not indicate a type.", label);
			final URI tag = URF.Tag.forTypeId(typeTag, (String)label);
			resource = new SimpleUrfResource(tag, typeTag);
		} else if(label instanceof Alias) { //alias
			final URI blankTag = Tag.generateBlank(label.toString());
			resource = new SimpleUrfResource(blankTag, typeTag); //the type may be null 
		} else { //no tag, ID, or alias
			assert label == null;
			final URI blankTag = Tag.generateBlank();
			resource = new SimpleUrfResource(blankTag, typeTag); //the type may be null 
		}
		return resource;
	}

	/**
	 * Parses the description properties of a resource. The next character read is expected to be the start of the description {@value TURF#DESCRIPTION_BEGIN}.
	 * @param reader The reader containing TURF data.
	 * @param subject The resource to which properties should be added.
	 * @throws IOException If there was an error reading the TURF data.
	 * @see TURF#DESCRIPTION_BEGIN
	 */
	protected void parseDescription(@Nonnull final Reader reader, @Nonnull final UrfResource subject) throws IOException {
		requireNonNull(subject);
		check(reader, DESCRIPTION_BEGIN); //:
		parseDescriptionBody(reader, DESCRIPTION_END, subject);
		check(reader, DESCRIPTION_END); //;
	}

	/**
	 * Parses the body of the description properties of a resource, that is, inside the description delimiters.
	 * @apiNote This method will return if it encounters the end of the reader, so if the sequence end is required the caller must check the return value.
	 * @param reader The reader containing TURF data.
	 * @param sequenceEnd The character expected to end the sequence.
	 * @param subject The resource to which properties should be added.
	 * @return The next character that will be returned by the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached without encountering the end of the sequence.
	 * @throws NullPointerException if the given reader and/or subject is <code>null</code>.
	 * @throws IOException If there was an error reading the TURF data.
	 */
	protected int parseDescriptionBody(@Nonnull final Reader reader, final char sequenceEnd, @Nonnull final UrfResource subject) throws IOException {
		requireNonNull(subject);
		return parseSequence(reader, sequenceEnd, r -> {
			final URI propertyTag = parseTagReference(reader);
			//TODO fix; do we want to declare properties? if not, what if they are described? final UrfResource property = getProcessor().declareResource(propertyTag, null); //TODO create default urf processor methods for just tags, and for handles; maybe add a createPropertyResource()
			final UrfResource property = new SimpleUrfResource(propertyTag); //TODO decide whether to declare properties
			skipFiller(reader);
			final UrfReference value;
			if(peek(reader) == DESCRIPTION_BEGIN) { //: (short-hand property object description) 
				final URI blankTag = Tag.generateBlank();
				final UrfResource object = new SimpleUrfResource(blankTag, (URI)null);
				getProcessor().declareResource(blankTag);
				parseDescription(reader, object);
				value = object;
			} else { //normal property-value association
				check(reader, PROPERTY_VALUE_DELIMITER); //=
				skipFiller(reader);
				value = parseResource(reader);
			}
			getProcessor().processStatement(subject, property, value);
			/*TODO transfer to TurfGraphProcessor
						final Optional<Object> oldValue = subject.setPropertyValue(propertyHandle, value);
						checkParseIO(reader, !oldValue.isPresent(), "Resource has duplicate definition for property %s.", propertyHandle);
			*/
		});
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
	 * @see TURF#BINARY_BEGIN
	 */
	public static byte[] parseBinary(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, BINARY_BEGIN);
		final String base64String = readWhile(reader, BINARY_BASE64URL_CHARACTERS);
		try {
			return Base64.getUrlDecoder().decode(base64String);
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new ParseIOException(reader, "Invalid TURF binary Base64 (base64url) encoding: " + base64String, illegalArgumentException);
		}
	}

	/**
	 * Parses a Boolean value. The next character read is expected to be the start of {@link TURF#BOOLEAN_FALSE_LEXICAL_FORM} or
	 * {@link TURF#BOOLEAN_TRUE_LEXICAL_FORM}.
	 * @param reader The reader containing TURF data.
	 * @return A {@link Boolean} representing the TURF boolean literal read from the reader.
	 * @throws IOException If there was an error reading the TURF data.
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
	 * @return An instance of {@link EmailAddress} representing the TURF email addresses parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the email address is not in the correct format.
	 * @see TURF#EMAIL_ADDRESS_BEGIN
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
			throw new ParseIOException(reader, "Invalid TURF email address format: " + localPart + EmailAddress.LOCAL_PART_DOMAIN_DELIMITER + domain,
					illegalArgumentException);
		}
	}

	/**
	 * Parses an IRI. The current position must be that of the beginning IRI delimiter character. The new position will be that immediately after the ending IRI
	 * delimiter character.
	 * <p>
	 * TURF IRI short forms are accepted.
	 * </p>
	 * @param reader The reader the contents of which to be parsed.
	 * @return A Java {@link URI} containing the IRI parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the IRI is not in the correct format.
	 * @see TURF#IRI_BEGIN
	 * @see TURF#IRI_END
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
			default:
				{
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
	 * Parses a media type. The current position must be that of the beginning media type delimiter character. The new position will be that immediately after the
	 * ending media type delimiter character.
	 * @param reader The reader the contents of which to be parsed.
	 * @return An instance of {@link MediaType} representing the TURF media type parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the media type is not in the correct format.
	 * @see TURF#MEDIA_TYPE_BEGIN
	 * @see TURF#MEDIA_TYPE_END
	 */
	public MediaType parseMediaType(@Nonnull final Reader reader) throws IOException, ParseIOException {
		return parseMediaType(reader, false).getKey();
	}

	/**
	 * Parses a media type, optionally allowing an embedded description. The current position must be that of the beginning media type delimiter character. The
	 * new position will be that immediately after the ending media type delimiter character.
	 * @implSpec This implementation only allows property handles and literal values in the description.
	 * @param reader The reader the contents of which to be parsed.
	 * @param allowDescription Whether an embedded description is recognized and allowed in the media type literal.
	 * @return An instance of {@link MediaType} representing the TURF media type parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the media type is not in the correct format.
	 * @see TURF#MEDIA_TYPE_BEGIN
	 * @see TURF#MEDIA_TYPE_END
	 */
	public Map.Entry<MediaType, Optional<Map<URI, ValueUrfResource<?>>>> parseMediaType(@Nonnull final Reader reader, final boolean allowDescription)
			throws IOException, ParseIOException {
		check(reader, MEDIA_TYPE_BEGIN); //`>`
		final String primaryType;
		final String subType;
		final String firstToken = parseMediaTypeRestrictedName(reader);
		char c = peekRequired(reader);
		if(c == MediaType.TYPE_DIVIDER) { //`/`
			check(reader, MediaType.TYPE_DIVIDER);
			primaryType = firstToken;
			subType = parseMediaTypeRestrictedName(reader);
			c = peekRequired(reader);
		} else { //default to the `text` type if no type divider was found
			primaryType = MediaType.TEXT_PRIMARY_TYPE;
			subType = firstToken;
		}
		final Set<MediaType.Parameter> parameters;
		if(c == MediaType.PARAMETER_DELIMITER_CHAR) { //`;` parameters
			parameters = new HashSet<>();
			do {
				check(reader, MediaType.PARAMETER_DELIMITER_CHAR);
				skip(reader, ABNF.WSP_CHARACTERS);
				final String parameterName = parseMediaTypeRestrictedName(reader);
				check(reader, MediaType.PARAMETER_ASSIGNMENT_CHAR);
				final String parameterValue;
				c = peekRequired(reader);
				if(c == MediaType.STRING_QUOTE_CHAR) { //`"` (quoted value)
					final StringBuilder parameterValueBuilder = new StringBuilder();
					check(reader, MediaType.STRING_QUOTE_CHAR); //beginning quote
					while((c = readRequired(reader)) != MediaType.STRING_QUOTE_CHAR) {
						if(c == MediaType.STRING_ESCAPE_CHAR) { //skip the escape `\` character
							c = readRequired(reader);
						}
						parameterValueBuilder.append(c);
					}
					parameterValue = parameterValueBuilder.toString();
				} else {
					//Note that with the current ContentType implementation this will include any control characters;
					//nevertheless this will be checked when the object is constructed. 
					parameterValue = readUntil(reader, MediaType.ILLEGAL_TOKEN_CHARACTERS);
				}
				parameters.add(MediaType.Parameter.of(parameterName, parameterValue));
				c = peekRequired(reader);
			} while(c == MediaType.PARAMETER_DELIMITER_CHAR); //`;`
		} else {
			parameters = emptySet();
		}
		final Map<URI, ValueUrfResource<?>> description;
		if(c == DESCRIPTION_BEGIN) { //`:` description
			check(reader, DESCRIPTION_BEGIN);
			description = new HashMap<>();
			parseSequence(reader, DESCRIPTION_END, r -> {
				final String propertyHandle = parseHandle(reader);
				final URI propertyTag;
				try {
					propertyTag = Handle.toTag(propertyHandle);
				} catch(final IllegalArgumentException illegalArgumentException) {
					throw new ParseIOException(reader, illegalArgumentException.getMessage(), illegalArgumentException);
				}
				skipFiller(reader);
				check(reader, PROPERTY_VALUE_DELIMITER); //=
				skipFiller(reader);
				final UrfReference value = parseResource(reader, false);
				checkParseIO(reader, value instanceof ValueUrfResource, "Media type description value `%s` must be a supported literal.", value);
				final boolean duplicate = description.putIfAbsent(propertyTag, (ValueUrfResource<?>)value) != null; //TODO do we allow multiple definitions?
				checkParseIO(reader, !duplicate, "Duplicate media type description property handle `%s`.", propertyHandle);
			});
			check(reader, DESCRIPTION_END); //;
		} else {
			description = null;
		}
		check(reader, MEDIA_TYPE_END); //`<`
		try {
			return new AbstractMap.SimpleImmutableEntry<>(MediaType.of(primaryType, subType, parameters), Optional.ofNullable(description));
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new ParseIOException(reader,
					"Invalid TURF media type format and parameters: " + primaryType + MediaType.TYPE_DIVIDER + subType + " " + parameters, illegalArgumentException);
		}
	}

	/**
	 * Parses a <dfn>restricted name</dfn> of an Internet media type. The current position must be that of the first character of the restricted name. The new
	 * position will be that immediately after the restricted name, or at the end of the reader.
	 * @apiNote The <code>restricted-name</code> production in <a href="https://tools.ietf.org/html/rfc6838">RFC 6838</a> is used for the primary type, the
	 *          subtype, and each parameter name.
	 * @param reader The reader the contents of which to be parsed.
	 * @return A media type restricted name parsed from the reader. The value is guaranteed to match the {@link MediaType#RESTRICTED_NAME_PATTERN} pattern.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the restricted name is not in the correct format.
	 * @see MediaType#RESTRICTED_NAME_PATTERN
	 */
	protected static String parseMediaTypeRestrictedName(@Nonnull final Reader reader) throws IOException, ParseIOException {
		final StringBuilder builder = new StringBuilder();
		builder.append(check(reader, MediaType.RESTRICTED_NAME_FIRST_CHARACTERS));
		readWhile(reader, MediaType.RESTRICTED_NAME_CHARACTERS, builder);
		final int length = builder.length();
		checkParseIO(reader, builder.length() <= MediaType.RESTRICTED_NAME_MAX_LENGTH,
				"Media type restricted name `%s` of length %d is longer than the maximum length %d.", builder, length, MediaType.RESTRICTED_NAME_MAX_LENGTH);
		assert MediaType.RESTRICTED_NAME_PATTERN.matcher(builder).matches();
		return builder.toString();
	}

	/**
	 * Parses a number resource. The current position must be that of the first character of the number. The new position will be that immediately after the
	 * number, or at the end of the reader.
	 * <p>
	 * This method will return an instance of {@link DefaultValueUrfResource} containing the following types:
	 * </p>
	 * <dl>
	 * <dt>{@link Long}</dt>
	 * <dd>{@link URF#INTEGER_TYPE_TAG} - All non-decimal, non-fractional, non-exponent numbers that fall within the range of <code>long</code>.</dd>
	 * <dt>{@link BigInteger}</dt>
	 * <dd>{@link URF#INTEGER_TYPE_TAG} - All non-decimal, non-fractional, non-exponent numbers that fall outside the range of <code>long</code>.</dd>
	 * <dt>{@link Double}</dt>
	 * <dd>{@link URF#NUMBER_TYPE_TAG} - All non-decimal, fractional and/or exponent numbers.</dd>
	 * <dt>{@link BigDecimal}</dt>
	 * <dd>{@link URF#DECIMAL_TYPE_TAG} - All decimal numbers.</dd>
	 * </dl>
	 * @implSpec This implementation delegates to {@link #parseNumber(Reader)}.
	 * @param reader The reader the contents of which to be parsed.
	 * @return A value resource containing the number parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the number is not in the correct format, or if the number is outside the range that can be represented by this parser.
	 */
	public static ValueUrfResource<? extends Number> parseNumberResource(@Nonnull final Reader reader) throws IOException, ParseIOException {
		final Number number = parseNumber(reader);
		final URI typeTag;
		if(number instanceof Double) {
			typeTag = NUMBER_TYPE_TAG;
		} else if(number instanceof Long || number instanceof BigInteger) {
			typeTag = INTEGER_TYPE_TAG;
		} else if(number instanceof BigDecimal) {
			typeTag = DECIMAL_TYPE_TAG;
		} else {
			throw new IllegalStateException("Number parsing logic returned unexpected number type.");
		}
		return new DefaultValueUrfResource<Number>(typeTag, number);
	}

	/**
	 * Parses a number. The current position must be that of the first character of the number. The new position will be that immediately after the number, or at
	 * the end of the reader.
	 * <p>
	 * This method will return one of the following types:
	 * </p>
	 * <dl>
	 * <dt>{@link Long}</dt>
	 * <dd>All non-decimal, non-fractional, non-exponent numbers that fall within the range of <code>long</code>.</dd>
	 * <dt>{@link BigInteger}</dt>
	 * <dd>All non-decimal, non-fractional, non-exponent numbers that fall outside the range of <code>long</code>.</dd>
	 * <dt>{@link Double}</dt>
	 * <dd>All non-decimal, fractional and/or exponent numbers.</dd>
	 * <dt>{@link BigDecimal}</dt>
	 * <dd>All decimal numbers.</dd>
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
		//TODO check for beginning zero and follow final TURF octal rules
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
				return new BigDecimal(numberString);
			} else {
				if(hasFraction || hasExponent) { //if there was a fraction or exponent
					return Double.valueOf(numberString); //parse a double and return it
				} else { //if there is no fraction or exponent
					try {
						return Long.valueOf(numberString);
					} catch(final NumberFormatException numberFormatException) {
						//in case the number was too big, see if we can parse it as a big integer TODO make the check more efficient than catching an exception 
						return new BigInteger(numberString);
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
	 * @see TURF#REGULAR_EXPRESSION_DELIMITER
	 * @see TURF#REGULAR_EXPRESSION_ESCAPE
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
	 * @see TURF#STRING_DELIMITER
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
	 * @return An instance of {@link TelephoneNumber} representing the TURF telephone number parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the telephone number is not in the correct format.
	 * @see TURF#TELEPHONE_NUMBER_BEGIN
	 */
	public static TelephoneNumber parseTelephoneNumber(@Nonnull final Reader reader) throws IOException, ParseIOException {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(check(reader, TELEPHONE_NUMBER_BEGIN)); //+
		final String telephoneNumberDigits = readRequiredMinimumCount(reader, ABNF.DIGIT_CHARACTERS, 1, stringBuilder).toString(); //at least one digit is required
		try {
			return TelephoneNumber.parse(telephoneNumberDigits);
		} catch(final IllegalArgumentException illegalArgumentException) { //this should never happen with the manual parsing logic above
			throw new ParseIOException(reader, "Invalid TURF telephone number digits: " + telephoneNumberDigits, illegalArgumentException);
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
	 * @see TURF#TEMPORAL_BEGIN
	 */
	public static TemporalAccessor parseTemporal(@Nonnull final Reader reader) throws IOException, ParseIOException {
		return parseTemporalResource(reader).getValue();
	}

	/**
	 * Parses a temporal resource. The current position must be that of the beginning temporal delimiter. The new position will be that immediately after the
	 * temporal, or at the end of the reader.
	 * @param reader The reader the contents of which to be parsed.
	 * @return A Java representation of the temporal parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the temporal is not in the correct format, or if the temporal is outside the range that can be represented by this parser.
	 * @see TURF#TEMPORAL_BEGIN
	 */
	public static ValueUrfResource<? extends TemporalAccessor> parseTemporalResource(@Nonnull final Reader reader) throws IOException, ParseIOException {
		check(reader, TEMPORAL_BEGIN);

		int c; //we'll use this to keep track of the next character
		final StringBuilder stringBuilder = new StringBuilder(); //create a new string builder to use when reading the temporal
		try {
			//YearMonth, MonthDay, and Year are returned from within the parsing logic; all other types are returned at the end
			c = peek(reader);
			//TODO implement duration
			if(c == ISO8601.DATE_DELIMITER) { //--MM-DD
				return new DefaultValueUrfResource<>(MONTH_DAY_TYPE_TAG, MonthDay.parse(readRequiredCount(reader, 7)));
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
					return new DefaultValueUrfResource<>(YEAR_TYPE_TAG, Year.parse(stringBuilder.toString()));
				}
				stringBuilder.append(check(reader, ISO8601.DATE_DELIMITER)); //read and add the date delimiter
				stringBuilder.append(readRequiredCount(reader, ASCII.DIGIT_CHARACTERS, 2)); //read the month
				c = peek(reader); //peek the next character
				if(c != ISO8601.DATE_DELIMITER) { //if the year and month are not part of a longer date
					return new DefaultValueUrfResource<>(YEAR_MONTH_TYPE_TAG, YearMonth.parse(stringBuilder.toString()));
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
						return new DefaultValueUrfResource<>(INSTANT_TYPE_TAG, Instant.parse(temporalString));
					} else if(hasOffset) {
						if(hasZone) {
							return new DefaultValueUrfResource<>(ZONED_DATE_TIME_TYPE_TAG, ZonedDateTime.parse(temporalString));
						} else { //offset with no zone
							return new DefaultValueUrfResource<>(OFFSET_DATE_TIME_TYPE_TAG, OffsetDateTime.parse(temporalString));
						}
					} else { //not UTC, no offset, and no zone
						return new DefaultValueUrfResource<>(LOCAL_DATE_TIME_TYPE_TAG, LocalDateTime.parse(temporalString));
					}
				} else { //date with no time
					return new DefaultValueUrfResource<>(LOCAL_DATE_TYPE_TAG, LocalDate.parse(temporalString));
				}
			} else { //no date
				assert hasTime;
				assert !isUTC;
				if(hasOffset) {
					return new DefaultValueUrfResource<>(OFFSET_TIME_TYPE_TAG, OffsetTime.parse(temporalString));
				} else { //not UTC, no offset, and no zone
					return new DefaultValueUrfResource<>(LOCAL_TIME_TYPE_TAG, LocalTime.parse(temporalString));
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
	 * @return An instance of {@link UUID} representing the TURF UUID parsed from the reader.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 * @throws ParseIOException if the UUID is not in the correct format.
	 * @see TURF#UUID_BEGIN
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
			throw new ParseIOException(reader, "Invalid TURF UUID contents: " + stringBuilder, illegalArgumentException);
		}
	}

	//collections

	/**
	 * Parses a list resource. The current position must be for {@value TURF#LIST_BEGIN}. The new position will be that immediately following
	 * {@value TURF#LIST_END}.
	 * <p>
	 * Once the list is created, it will be associated with the given alias if any.
	 * </p>
	 * @param alias The resource alias, or <code>null</code> if the resource has no alias.
	 * @param reader The reader containing TURF data.
	 * @return The list read from the reader.
	 * @throws IOException If there was an error reading the TURF data.
	 */
	public UrfResource parseListResource(@Nullable final Alias alias, @Nonnull final Reader reader) throws IOException {
		final URI blankTag = Tag.generateBlank();
		getProcessor().declareResource(blankTag, LIST_TYPE_TAG);
		final UrfResource listResource = new SimpleUrfResource(blankTag, LIST_TYPE_TAG); //TODO maybe allow tagged lists 
		if(alias != null) {
			aliasedReferences.put(alias, listResource);
		}
		check(reader, LIST_BEGIN); //[
		final AtomicLong indexCounter = new AtomicLong(0);
		parseSequence(reader, LIST_END, r -> {
			final long index = indexCounter.getAndIncrement();
			//TODO final UrfResource property = getProcessor().declareResource(URF.Tag.forTypeId(ELEMENT_TYPE_TAG, Long.toString(index)), null); //TODO create default urf processor methods for just tags, and for handles; maybe add a createPropertyResource()
			final UrfResource property = new SimpleUrfResource(URF.Tag.forTypeId(ELEMENT_TYPE_TAG, Long.toString(index))); //TODO decide whether to declare properties
			final UrfReference element = parseResource(r);
			getProcessor().processStatement(listResource, property, element);
		});
		check(reader, LIST_END); //]
		return listResource;
	}

	/**
	 * Parses a map resource. The current position must be for {@value TURF#MAP_BEGIN}. The new position will be that immediately following {@value TURF#MAP_END}.
	 * <p>
	 * Once the map is created, it will be associated with the given alias if any.
	 * </p>
	 * @param alias The resource alias, or <code>null</code> if the resource has no alias.
	 * @param reader The reader containing TURF data.
	 * @return The map read from the reader.
	 * @throws IOException If there was an error reading the TURF data.
	 */
	public UrfResource parseMapResource(@Nullable final Alias alias, @Nonnull final Reader reader) throws IOException {
		final URI blankMapTag = Tag.generateBlank();
		getProcessor().declareResource(blankMapTag, MAP_TYPE_TAG);
		final UrfResource mapResource = new SimpleUrfResource(blankMapTag, MAP_TYPE_TAG); //TODO maybe allow tagged maps 
		if(alias != null) {
			aliasedReferences.put(alias, mapResource);
		}
		check(reader, MAP_BEGIN); //{
		final UrfResource memberProperty = new SimpleUrfResource(MEMBER_PROPERTY_TAG); //TODO decide whether to declare properties;
		final UrfResource keyProperty = new SimpleUrfResource(KEY_PROPERTY_TAG); //TODO decide whether to declare properties
		final UrfResource valueProperty = new SimpleUrfResource(VALUE_PROPERTY_TAG); //TODO decide whether to declare properties
		parseSequence(reader, MAP_END, r -> {
			final UrfReference key;
			if(peek(reader) == MAP_KEY_DELIMITER) {
				check(reader, MAP_KEY_DELIMITER); //\
				key = parseResource(reader, true);
				check(reader, MAP_KEY_DELIMITER); //\
			} else {
				key = parseResource(reader, false);
			}
			skipFiller(reader);
			check(reader, ENTRY_KEY_VALUE_DELIMITER); //:
			skipFiller(reader);
			final UrfReference value = parseResource(reader);
			//simulate the map entry
			final URI blankMapEntryTag = Tag.generateBlank();
			getProcessor().declareResource(blankMapEntryTag, MAP_ENTRY_TYPE_TAG);
			final UrfResource mapEntryResource = new SimpleUrfResource(blankMapEntryTag, MAP_ENTRY_TYPE_TAG);
			getProcessor().processStatement(mapEntryResource, keyProperty, key);
			getProcessor().processStatement(mapEntryResource, valueProperty, value);
			getProcessor().processStatement(mapResource, memberProperty, mapEntryResource);
		});
		check(reader, MAP_END); //}
		return mapResource;
	}

	/**
	 * Parses a set resource. The current position must be for {@value TURF#SET_BEGIN}. The new position will be that immediately following {@value TURF#SET_END}.
	 * <p>
	 * Once the set is created, it will be associated with the given alias if any.
	 * </p>
	 * @param alias The resource alias, or <code>null</code> if the resource has no alias.
	 * @param reader The reader containing TURF data.
	 * @return The set read from the reader.
	 * @throws IOException If there was an error reading the TURF data.
	 */
	public UrfResource parseSetResource(@Nullable final Alias alias, @Nonnull final Reader reader) throws IOException {
		final URI blankTag = Tag.generateBlank();
		getProcessor().declareResource(blankTag, SET_TYPE_TAG);
		final UrfResource setResource = new SimpleUrfResource(blankTag, SET_TYPE_TAG); //TODO maybe allow tagged sets 
		if(alias != null) {
			aliasedReferences.put(alias, setResource);
		}
		check(reader, SET_BEGIN); //[
		getProcessor().declareResource(MEMBER_PROPERTY_TAG, null);
		final UrfResource memberProperty = new SimpleUrfResource(MEMBER_PROPERTY_TAG);
		parseSequence(reader, SET_END, r -> {
			final UrfReference member = parseResource(r);
			getProcessor().processStatement(setResource, memberProperty, member);
		});
		check(reader, SET_END); //]
		return setResource;
	}

	//parsing

	/**
	 * Parses a general TURF sequence (such as a list). This method skips whitespace, comments, and sequence delimiters. For each sequence item,
	 * {@link IOConsumer#accept(Object)} is called, passing the {@link Reader}, for the item to be parsed. The sequence ends when the sequence end delimiter or
	 * the end of the reader is reached.
	 * @apiNote This method will return if it encounters the end of the reader, so if the sequence end is required the caller must check the return value.
	 * @apiNote The sequence end is provided to detect the end of the sequence after a newline. This method does not guarantee that the sequence end will be
	 *          reached. This means the method does not consume subsequent characters that are not part of the sequence, such as the <code>bar</code> in
	 *          <code>"foo bar"</code>, it is up to the caller to confirm that subsequent characters (which are considered "after the sequence") are as expected.
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
		int c = skipFiller(reader);
		while(c >= 0 && (nextItemRequired || c != sequenceEnd)) {
			itemParser.accept(reader); //parse the item
			final Optional<Boolean> requireItem = skipSequenceDelimiters(reader);
			c = peek(reader); //we'll need to know the next character whatever the case; don't fail for the end of the reader
			if(!requireItem.isPresent()) { //if there was no item delimiter at all
				break; //no possibility for a new item
			}
			nextItemRequired = requireItem.get().booleanValue(); //see if a new item is required
		}
		return c;
	}

	/**
	 * Skips over TURF sequence delimiters in a reader. Whitespace and comments. The new position will either be the that of the first non-whitespace and non-EOL
	 * character; or the end of the reader.
	 * @param reader The reader the contents of which to be parsed.
	 * @return {@link Boolean#TRUE} if a sequence delimiter was encountered that requires a following item, {@link Boolean#FALSE} if a sequence delimiter was
	 *         encountered for which a following item is optional, or {@link Optional#empty()} if no sequence delimiter was encountered.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static Optional<Boolean> skipSequenceDelimiters(@Nonnull final Reader reader) throws IOException {
		int c = skip(reader, WHITESPACE_CHARACTERS); //skip any whitespace
		if(c == LINE_COMMENT_BEGIN) { //skip any line comment
			skipLineComment(reader);
			c = peek(reader);
		}
		if(c < 0 || !SEQUENCE_SEPARATOR_CHARACTERS.contains((char)c)) { //if no sequence delimiter was found
			return Optional.empty();
		}
		boolean requireItem = false;
		do {
			if(c == COMMA_CHAR) { //if we found a comma
				requireItem = true; //note we found a comma
				check(reader, COMMA_CHAR); //skip the comma
			}
			c = skipFiller(reader);
		} while(!requireItem && c >= 0 && SEQUENCE_SEPARATOR_CHARACTERS.contains((char)c));
		return Optional.of(requireItem);
	}

	/** Whitespace and end-of-line characters. */
	protected static final Characters WHITESPACE_EOL_CHARACTERS = WHITESPACE_CHARACTERS.add(EOL_CHARACTERS);

	/**
	 * Skips over TURF <dfn>filler</dfn> in a reader, which includes whitespace, line comments, and line breaks. The new position will either be the that of the
	 * first non-filler character or the end of the reader.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The next character that will be returned the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipFiller(@Nonnull final Reader reader) throws IOException {
		int c; //we'll store the next non-whitespace-newline character here so that it can be returned
		while((c = skip(reader, WHITESPACE_EOL_CHARACTERS)) == LINE_COMMENT_BEGIN) { //skip all non-comment fillers; if the start of a comment was encountered
			skipLineComment(reader); //skip the line comment; we'll then skip all line-break filler characters again and see if another comment starts
		}
		return c; //return the last character read
	}

	/**
	 * Skips a single line comments. The current position must be that of the line comment delimiter. The new position will either be the that of the following
	 * newline character or the end of the reader.
	 * @param reader The reader the contents of which to be parsed.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static void skipLineComment(@Nonnull final Reader reader) throws IOException {
		check(reader, LINE_COMMENT_BEGIN); //read the beginning comment delimiter
		reach(reader, EOL_CHARACTERS); //skip to the end of the line
	}

}
