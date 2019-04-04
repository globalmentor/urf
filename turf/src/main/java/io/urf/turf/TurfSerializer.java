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

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.util.Optionals.*;
import static io.urf.URF.*;
import static io.urf.turf.TURF.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.io.*;
import com.globalmentor.io.function.IOBiConsumer;
import com.globalmentor.itu.TelephoneNumber;
import com.globalmentor.java.CodePointCharacter;
import com.globalmentor.model.UUIDs;
import com.globalmentor.net.EmailAddress;
import com.globalmentor.text.ASCII;

import io.urf.model.*;

/**
 * Serializer for the Text URF (TURF) document format.
 * <p>
 * This serializer recognizes and can serialize the following type for TURF categories of resources:
 * </p>
 * <h2>Objects</h2>
 * <ul>
 * <li>{@link UrfObject}</li>
 * </ul>
 * <h2>Literals</h2>
 * <h3>binary</h3>
 * <ul>
 * <li><code>byte[]</code></li>
 * <li>{@link ByteBuffer}</li>
 * </ul>
 * <h3>Boolean</h3>
 * <ul>
 * <li>{@link Boolean}</li>
 * </ul>
 * <h3>character</h3>
 * <ul>
 * <li>{@link Character}</li>
 * <li>{@link CodePointCharacter}</li>
 * </ul>
 * <h3>email address</h3>
 * <ul>
 * <li>{@link EmailAddress}</li>
 * </ul>
 * <h3>IRI</h3>
 * <ul>
 * <li>{@link URI}</li>
 * <li>{@link URL}</li>
 * </ul>
 * <h3>number</h3>
 * <ul>
 * <li>{@link BigInteger} (serialized as decimal)</li>
 * <li>{@link BigDecimal} (serialized as decimal)</li>
 * <li>{@link Number} (including {@link Integer}, {@link Long}, and {@link Double})</li>
 * </ul>
 * <h3>regular expression</h3>
 * <ul>
 * <li>{@link Pattern}</li>
 * </ul>
 * <h3>string</h3>
 * <ul>
 * <li>{@link CharSequence} (including {@link String})</li>
 * </ul>
 * <h3>telephone number</h3>
 * <ul>
 * <li>{@link TelephoneNumber}</li>
 * </ul>
 * <h3>temporal</h3>
 * <ul>
 * <li>{@link Date} (serialized as instant)</li>
 * <li>{@link Instant}</li>
 * <li>{@link MonthDay}</li>
 * <li>{@link LocalDate}</li>
 * <li>{@link LocalDateTime}</li>
 * <li>{@link LocalTime}</li>
 * <li>{@link OffsetDateTime}</li>
 * <li>{@link OffsetTime}</li>
 * <li>{@link Year}</li>
 * <li>{@link YearMonth}</li>
 * <li>{@link ZonedDateTime}</li>
 * </ul>
 * <h3>UUID</h3>
 * <ul>
 * <li>{@link UUID}</li>
 * </ul>
 * <h2>Collections</h2>
 * <ul>
 * <li>{@link List}</li>
 * <li>{@link Map}</li>
 * <li>{@link Set}</li>
 * </ul>
 * <p>
 * This serializer is meant to be used once for generating a single TURF document. It should not be used to serialize multiple documents, as it maintains
 * serialization state.
 * </p>
 * <p>
 * The serializer should be released after use so as not to leak memory of parsed resources when resources are present with tags/IDs and/or generate aliases.
 * </p>
 * <p>
 * This implementation is not thread safe.
 * </p>
 * @author Garret Wilson
 */
public class TurfSerializer {

	/** The prefix used when generating aliases. */
	public static final String GENERATED_ALIAS_PREFIX = "resource";

	private final static String ARRAY_LIST_CLASS_NAME = "java.util.ArrayList";
	private final static String BIG_DECIMAL_CLASS_NAME = "java.math.BigDecimal";
	private final static String BIG_INTEGER_CLASS_NAME = "java.math.BigInteger";
	private final static String BOOLEAN_CLASS_NAME = "java.lang.Boolean";
	private final static String BYTE_CLASS_NAME = "java.lang.Byte";
	private final static String BYTE_ARRAY_CLASS_NAME = "[B";
	private final static String CHARACTER_CLASS_NAME = "java.lang.Character";
	private final static String CODE_POINT_CHARACTER_CLASS_NAME = "com.globalmentor.java.CodePointCharacter";
	private final static String DATE_CLASS_NAME = "java.util.Date";
	private final static String DOUBLE_CLASS_NAME = "java.lang.Double";
	private final static String EMAIL_ADDRESS_CLASS_NAME = "com.globalmentor.net.EmailAddress";
	private final static String FLOAT_CLASS_NAME = "java.lang.Float";
	private final static String HASH_MAP_CLASS_NAME = "java.util.HashMap";
	private final static String HASH_SET_CLASS_NAME = "java.util.HashSet";
	private final static String INSTANT_CLASS_NAME = "java.time.Instant";
	private final static String INTEGER_CLASS_NAME = "java.lang.Integer";
	private final static String LINKED_HASH_MAP_CLASS_NAME = "java.util.LinkedHashMap";
	private final static String LINKED_HASH_SET_CLASS_NAME = "java.util.LinkedHashSet";
	private final static String LINKED_LIST_CLASS_NAME = "java.util.LinkedList";
	private final static String LOCAL_DATE_CLASS_NAME = "java.time.LocalDate";
	private final static String LOCAL_DATE_TIME_CLASS_NAME = "java.time.LocalDateTime";
	private final static String LOCAL_TIME_CLASS_NAME = "java.time.LocalTime";
	private final static String LONG_CLASS_NAME = "java.lang.Long";
	private final static String MONTH_DAY_CLASS_NAME = "java.time.MonthDay";
	private final static String OFFSET_DATE_TIME_CLASS_NAME = "java.time.OffsetDateTime";
	private final static String OFFSET_TIME_CLASS_NAME = "java.time.OffsetTime";
	private final static String PATTERN_CLASS_NAME = "java.util.regex.Pattern";
	private final static String SHORT_CLASS_NAME = "java.lang.Short";
	private final static String STRING_CLASS_NAME = "java.lang.String";
	private final static String STRING_BUILDER_CLASS_NAME = "java.lang.StringBuilder";
	private final static String TELEPHONE_NUMBER_CLASS_NAME = "com.globalmentor.itu.TelephoneNumber";
	private final static String TREE_MAP_CLASS_NAME = "java.util.TreeMap";
	private final static String TREE_SET_CLASS_NAME = "java.util.TreeSet";
	private final static String URI_CLASS_NAME = "java.net.URI";
	private final static String URL_CLASS_NAME = "java.net.URL";
	private final static String UUID_CLASS_NAME = "java.util.UUID";
	private final static String YEAR__CLASS_NAME = "java.time.Year";
	private final static String YEAR_MONTH_CLASS_NAME = "java.time.YearMonth";
	private final static String ZONED_DATE_TIME_CLASS_NAME = "java.time.ZonedDateTime";

	/**
	 * Determines whether the given resource is a compound resource, that is, one that can contain other resources. This method returns <code>true</code> for
	 * {@link UrfObject}, any {@link Collection}, and any {@link Map}.
	 * @param resource The resource which may or may not be a compound resource.
	 * @return <code>true</code> if the given resource may hold other resources.
	 */
	public static boolean isCompoundResource(@Nonnull final Object resource) {
		requireNonNull(resource);
		return resource instanceof UrfObject || resource instanceof Collection || resource instanceof Map;
	}

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

	private final Map<URI, String> namespaceAliases = new HashMap<>();

	/**
	 * Registers a namespace with the given namespace alias. If the namespace was already registered
	 * @param namespace The namespace to register.
	 * @param alias The alias to use in place of the namespace
	 * @return This serializer.
	 */
	public TurfSerializer registerNamespace(@Nonnull final URI namespace, @Nonnull final String alias) {
		namespaceAliases.put(requireNonNull(namespace), requireNonNull(alias));
		return this;
	}

	/** @return The map of namespaces aliases associated with their namespaces. */
	protected Map<URI, String> getNamespaceAliases() {
		return namespaceAliases;
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
	 * @param appendable The appendable to which serialized data should be appended.
	 * @return The given appendable.
	 * @throws IOException If there was an error writing the indent.
	 * @see #isFormatted()
	 * @see #getIndentSequence()
	 */
	protected Appendable formatIndent(@Nonnull final Appendable appendable) throws IOException {
		if(isFormatted()) {
			final CharSequence indentSequence = getIndentSequence();
			for(int i = 0; i < indentLevel; ++i) {
				appendable.append(indentSequence);
			}
		}
		return appendable;
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
	 * Separates lines by adding a single line separation character sequence if formatting is enabled.
	 * <p>
	 * If formatting is turned off, no content will be added.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @return Whether or not a line separator sequence was actually appended.
	 * @throws IOException If there was an error writing the line separator.
	 * @see #isFormatted()
	 * @see #getLineSeparator()
	 */
	protected boolean formatNewLine(@Nonnull final Appendable appendable) throws IOException {
		return formatNewLine(appendable, 1);
	}

	/**
	 * Separates lines by adding one or more line separation character sequences if formatting is enabled.
	 * <p>
	 * If formatting is turned off, no content will be added.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param newlineCount The number of newlines to add.
	 * @return Whether or not a line separator sequence was actually appended.
	 * @throws IOException If there was an error writing the line separator.
	 * @see #isFormatted()
	 * @see #getLineSeparator()
	 */
	protected boolean formatNewLine(@Nonnull final Appendable appendable, final int newlineCount) throws IOException {
		final boolean isNewlineAppended = isFormatted();
		if(isNewlineAppended) {
			for(int count = 1; count <= newlineCount; count++) {
				appendable.append(lineSeparator);
			}
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
	 * A map indicating whether a resource has a reference.
	 * <p>
	 * If a resource does not appear as a key in the map, it has not been encountered in the graph. If the resource is associated with {@link Boolean#FALSE}, it
	 * has no references. If it is associated with {@link Boolean#TRUE}, it has at least one reference and will need an alias if it has no tag or ID.
	 * </p>
	 */
	private final Map<Object, Boolean> resourceHasReferenceMap = new IdentityHashMap<>();

	/**
	 * Stores whether a resource has references based upon the resources currently seen.
	 * <p>
	 * If we've never seen the resource, it will be associated with <code>false</code>; if we have seen it, it will be associated with <code>true</code> in the
	 * map.
	 * </p>
	 * @param resource The resource to check.
	 * @return Whether the resource has at least one reference (i.e. it has been seen before).
	 */
	private boolean calculateResourceHasReference(@Nonnull final Object resource) {
		final boolean hasReference = resourceHasReferenceMap.containsKey(resource);
		resourceHasReferenceMap.put(resource, hasReference);
		return hasReference;
	}

	//TODO make sure references to type resources are accounted for, in case they have properties

	/**
	 * Discovers whether there are references to the given resource and recursively all nested resources.
	 * <p>
	 * This method should not be called more than once for any resource graph, or all resources in the graph will be marked as having references.
	 * </p>
	 * @param resource The resource graph for which references should be discovered.
	 */
	protected void discoverResourceReferences(@Nonnull final Object resource) {
		requireNonNull(resource);
		if(resource instanceof UrfObject) { //discover references to object property values
			if(!calculateResourceHasReference(resource)) { //don't recheck referenced resources
				((UrfObject)resource).getProperties().forEach(propertyEntry -> {
					discoverResourceReferences(propertyEntry.getValue());
				});
			}
		} else if(resource instanceof Collection) { //discover references to collection members
			if(!calculateResourceHasReference(resource)) { //don't recheck referenced resources
				((Collection<?>)resource).forEach(this::discoverResourceReferences);
			}
		} else if(resource instanceof Map) { //discover references to map keys and values
			if(!calculateResourceHasReference(resource)) { //don't recheck referenced resources
				((Map<?, ?>)resource).forEach((key, value) -> {
					discoverResourceReferences(key);
					discoverResourceReferences(value);
				});
			}
		}
	}

	/** The map of aliases for objects and collections, with identity keys. */
	private final Map<Object, String> aliasesByCompoundResource = new IdentityHashMap<>();

	/** The map of aliases for value objects (which become literals), with equality keys. */
	private final Map<Object, String> aliasesByValue = new HashMap<>();

	/**
	 * Returns any alias associated with a resource.
	 * @param resource The resource with which an alias may be associated.
	 * @return The alias, if any associated with the given resource.
	 */
	public Optional<String> getAliasForResource(@Nonnull final Object resource) {
		requireNonNull(resource);
		if(isCompoundResource(resource)) {
			return Optional.ofNullable(aliasesByCompoundResource.get(resource));
		} else {
			return Optional.ofNullable(aliasesByValue.get(resource));
		}
	}

	/**
	 * Associates an alias with a resource. This method allows associating aliases with non-object and non-collection resources as well, such as the number 5.
	 * <p>
	 * Aliases are not allowed to be set for objects with tags, which themselves serve as labels.
	 * </p>
	 * @param resource The resource to associate with an alias.
	 * @param alias The alias to associate with the resource.
	 * @throws NullPointerException if the given resource and/or alias is <code>null</code>.
	 * @throws IllegalArgumentException if the given alias is not a valid name token.
	 * @throws IllegalArgumentException if an alias is given for a {@link UrfObject} with a tag or an ID.
	 */
	public void setAliasForResource(@Nonnull final Object resource, @Nonnull final String alias) {
		requireNonNull(resource);
		Name.checkArgumentValidToken(alias);
		if(isCompoundResource(resource)) {
			if(resource instanceof UrfObject) {
				final UrfObject urfObject = (UrfObject)resource;
				checkArgument(!urfObject.getTag().isPresent(), "An alias cannot be specified for object with tag %s.", urfObject.getTag().orElse(null));
			}
			aliasesByCompoundResource.put(resource, alias);
		} else {
			aliasesByValue.put(resource, alias);
		}
	}

	/** The number of generated aliases. */
	private long generatedAliasCount = 0;

	/**
	 * Determines an alias to use with a resource, generating one if the graph requires it.
	 * <ul>
	 * <li>If an alias has already been associated with a resource, it is returned.</li>
	 * <li>Otherwise if the resource is a compound resource that has references, a new alias is generated, associated with the resource for future use and
	 * returned.</li>
	 * <li>An alias is never generated for a {@link UrfObject} with an a tag or an ID, as that serves as a label.</li>
	 * </ul>
	 * @param resource The resource with which an alias may be associated, or <code>null</code> if there is no alias.
	 * @return The alias, if any associated with the given resource.
	 */
	protected String determineAliasForResource(@Nonnull final Object resource) {
		requireNonNull(resource);
		return getAliasForResource(resource).orElseGet(() -> { //TODO improve with Java 9 Optional.or()
			//if this is a compound resource
			if(isCompoundResource(resource)) {
				if(resource instanceof UrfObject) {
					final UrfObject urfObject = (UrfObject)resource;
					if(urfObject.getTag().isPresent()) {
						return null; //an URF object with a tag does not need an alias
					}
				}
				//if the compound resource has references
				if(Boolean.TRUE.equals(resourceHasReferenceMap.get(resource))) {
					final long aliasNumber = ++generatedAliasCount;
					final String newAlias = GENERATED_ALIAS_PREFIX + aliasNumber;
					setAliasForResource(resource, newAlias);
					return newAlias;
				}
			}
			return null;
		});
	}

	/** The set of objects and collections that have been serialized. */
	private final Set<Object> serializedCompoundResources = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());

	/** The map of value objects (which become literals), that have been serialized. */
	private final Set<Object> serializedValues = new HashSet<>();

	/**
	 * Determines whether a given resource has already been serialized.
	 * @param resource The resource to check.
	 * @return <code>true</code> if the resource has already been serialized.
	 */
	protected boolean isSerialized(@Nonnull final Object resource) {
		return (isCompoundResource(resource) ? serializedCompoundResources : serializedValues).contains(resource);
	}

	/**
	 * Sets a resource has having been serialized.
	 * @param resource The resource to record as serialized.
	 * @return Whether the resource was previously marked as serialized before this call.
	 */
	protected boolean setSerialized(@Nonnull final Object resource) {
		return !(isCompoundResource(resource) ? serializedCompoundResources : serializedValues).add(resource);
	}

	/**
	 * Serializes a TURF document to a string.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @implSpec This is a convenience method that delegates to {@link #serializeRoot(Appendable, Object)}.
	 * @param root The root resource.
	 * @throws NullPointerException if the root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the data.
	 * @return A serialized string representation of the given resource graph.
	 */
	public String serializeDocument(@Nonnull Object root) throws IOException {
		return serializeDocument(singleton(root));
	}

	/**
	 * Serializes a TURF document to a string.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @implSpec This is a convenience method that delegates to {@link #serializeRoot(Appendable, Object)}.
	 * @param roots The root resources, if any.
	 * @throws NullPointerException if the roots and/or any root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the data.
	 * @return A serialized string representation of the given resource graphs.
	 */
	public String serializeDocument(@Nonnull Object... roots) throws IOException {
		return serializeDocument(asList(roots));
	}

	/**
	 * Serializes a TURF document to a string.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @implSpec This is a convenience method that delegates to {@link #serializeRoot(Appendable, Object)}.
	 * @param roots The root resources, if any.
	 * @throws NullPointerException if the roots iterable and/or any root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the data.
	 * @return A serialized string representation of the given resource graphs.
	 */
	public String serializeDocument(@Nonnull Iterable<?> roots) throws IOException {
		try (final Writer stringWriter = new StringWriter()) {
			serializeDocument(stringWriter, roots);
			return stringWriter.toString();
		}
	}

	/**
	 * Serializes the result of URF processing as a TURF document to an output stream.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @param outputStream The output stream to receive the serialized data.
	 * @param simpleGraphUrfProcessor The simple graph URF processor that has already processed some URF content..
	 * @throws NullPointerException if the given output stream and/or processor is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public void serializeDocument(@Nonnull final OutputStream outputStream, @Nonnull SimpleGraphUrfProcessor simpleGraphUrfProcessor) throws IOException {
		final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, DEFAULT_CHARSET));
		serializeDocument(writer, simpleGraphUrfProcessor);
		writer.flush(); //flush what we wrote, because the caller doesn't have access to the writer we created
	}

	/**
	 * Serializes a TURF document to an output stream.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @param outputStream The output stream to receive the serialized data.
	 * @param root The root resource.
	 * @throws NullPointerException if the given output stream and/or root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public void serializeDocument(@Nonnull final OutputStream outputStream, @Nonnull Object root) throws IOException {
		serializeDocument(outputStream, singleton(root));
	}

	/**
	 * Serializes a TURF document to an output stream.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @param outputStream The output stream to receive the serialized data.
	 * @param roots The root resources, if any.
	 * @throws NullPointerException if the given output stream, roots, and/or any root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public void serializeDocument(@Nonnull final OutputStream outputStream, @Nonnull Object... roots) throws IOException {
		serializeDocument(outputStream, asList(roots));
	}

	/**
	 * Serializes a TURF document to an output stream.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @param outputStream The output stream to receive the serialized data.
	 * @param roots The root resources, if any.
	 * @throws NullPointerException if the given output stream, roots iterable, and/or any root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public void serializeDocument(@Nonnull final OutputStream outputStream, @Nonnull Iterable<?> roots) throws IOException {
		final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, DEFAULT_CHARSET));
		serializeDocument(writer, roots);
		writer.flush(); //flush what we wrote, because the caller doesn't have access to the writer we created
	}

	/**
	 * Serializes the result of URF processing as a TURF document to an appendable such as a writer.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param simpleGraphUrfProcessor The simple graph URF processor that has already processed some URF content..
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable and/or processor is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeDocument(@Nonnull final Appendable appendable, @Nonnull SimpleGraphUrfProcessor simpleGraphUrfProcessor) throws IOException {
		serializeDocument(appendable, simpleGraphUrfProcessor.getReportedRoots()); //if roots were reported (not all sources report roots), always make them roots in the output
		simpleGraphUrfProcessor.getInferredRoot().ifPresent(throwingConsumer(inferredRoot -> {
			serializeRoot(appendable, inferredRoot, false); //if there was an inferred root, serialize it if it hasn't been serialized already
		}));
		return serializeRoots(appendable, () -> simpleGraphUrfProcessor.getDeclaredObjects().iterator(), false); //finally make sure that all resources have been serialized
	}

	/**
	 * Serializes a TURF document to an appendable such as a writer.
	 * @apiNote All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param root The root resource.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable and/or root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeDocument(@Nonnull final Appendable appendable, @Nonnull Object root) throws IOException {
		return serializeDocument(appendable, singleton(root));
	}

	/**
	 * Serializes a TURF document to an appendable such as a writer.
	 * @apiNote All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param roots The root resources, if any.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable, roots, and/or any root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeDocument(@Nonnull final Appendable appendable, @Nonnull Object... roots) throws IOException {
		return serializeDocument(appendable, asList(roots));
	}

	/**
	 * Serializes a TURF document to an appendable such as a writer.
	 * @apiNote This method discovers resource references to that aliases may be generated as needed. This record of resource references is reset after
	 *          serialization, but any generated aliases remain. This allows the same serializer to be used multiple times for the same graph, with the same
	 *          aliases being used.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param roots The root resources, if any.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable, roots iterable, and/or any root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeDocument(@Nonnull final Appendable appendable, @Nonnull Iterable<?> roots) throws IOException {
		//header
		final boolean includeHeader = !namespaceAliases.isEmpty(); //TODO add option(s) to force a header
		if(includeHeader) {
			serializeHeader(appendable);
		}

		//roots
		try {
			int rootCount = 0;
			for(final Object root : roots) {
				rootCount++;
				discoverResourceReferences(root);
			}
			if(rootCount > 0) {
				if(includeHeader) { //separate the header from the roots with a blank line
					formatNewLine(appendable, 2);
				}
				serializeRoots(appendable, roots);
			}
		} finally {
			resourceHasReferenceMap.clear();
		}
		return appendable;
	}

	/**
	 * Serializes a TURF document header to a writer, including the namespace declarations, if present.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeHeader(@Nonnull final Appendable appendable) throws IOException {
		appendable.append(DIVISION_DELIMITER); //\
		//TODO serialize signature, when finalized, based on configuration
		formatNewLine(appendable);
		//map the namespaces to space-alias/namespaceIri properties
		final Stream<Map.Entry<URI, Object>> namespaceProperties = namespaceAliases.entrySet().stream()
				.map(namespaceAliasEntry -> new AbstractMap.SimpleEntry<>(Tag.forType(SPACE_NAMESPACE, namespaceAliasEntry.getValue()), namespaceAliasEntry.getKey()));
		try (final Closeable indention = increaseIndentLevel()) {
			serializeSequence(appendable, namespaceProperties::iterator, this::serializeProperty);
		}
		formatIndent(appendable);
		return appendable.append(DIVISION_DELIMITER); //\
	}

	long serializedRootCount = 0;

	/**
	 * Serializes a resource graph to an appendable such as a writer. The root will be reserialized as a reference if it has already been serialized.
	 * <p>
	 * This method may be called multiple times to serialize additional roots.
	 * </p>
	 * @apiNote All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param root The root resource.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable and/or root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeRoot(@Nonnull final Appendable appendable, @Nonnull Object root) throws IOException {
		return serializeRoot(appendable, root, true);
	}

	/**
	 * Serializes a resource graph to an appendable such as a writer.
	 * <p>
	 * This method may be called multiple times to serialize additional roots.
	 * </p>
	 * @apiNote All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param root The root resource.
	 * @param includeDuplicates <code>true</code> if object should be included again if they have already been serialized, or <code>false</code> if it should be
	 *          skipped.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable and/or root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeRoot(@Nonnull final Appendable appendable, @Nonnull Object root, final boolean includeDuplicates) throws IOException {
		return serializeRoots(appendable, singleton(root), includeDuplicates);
	}

	/**
	 * Serializes resource graphs to an appendable such as a writer. Roots are reserialized as references if they have already been serialized.
	 * <p>
	 * This method may be called multiple times to serialize additional roots.
	 * </p>
	 * @apiNote All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param roots The root resources, if any.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable, roots iterable, and/or any root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeRoots(@Nonnull final Appendable appendable, @Nonnull Iterable<?> roots) throws IOException {
		return serializeRoots(appendable, roots, true);
	}

	/**
	 * Serializes resource graphs to an appendable such as a writer.
	 * <p>
	 * This method may be called multiple times to serialize additional roots.
	 * </p>
	 * @apiNote All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param roots The root resources, if any.
	 * @param includeDuplicates <code>true</code> if object should be included again if they have already been serialized, or <code>false</code> if it should be
	 *          skipped.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable, roots iterable, and/or any root resource is <code>null</code>.
	 * @throws IOException If there was an error writing the serialized data.
	 */
	public Appendable serializeRoots(@Nonnull final Appendable appendable, @Nonnull Iterable<?> roots, final boolean includeDuplicates) throws IOException {
		final boolean sequenceSeparatorRequired = isSequenceSeparatorRequired();
		for(final Object root : roots) {
			if(!includeDuplicates && isSerialized(root)) {
				continue;
			}
			if(serializedRootCount > 0) { //separate roots
				if(sequenceSeparatorRequired) { //add a separator if one is required
					appendable.append(SEQUENCE_DELIMITER);
				}
				//separate root objects with a blank line
				if(!formatNewLine(appendable, 2) && !sequenceSeparatorRequired) {
					appendable.append(SEQUENCE_DELIMITER);
				}
			}
			serializeResource(appendable, root);
			serializedRootCount++;
		}
		return appendable;
	}

	/**
	 * Serializes a resource to an appendable such as a writer.
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param resource The URF resource to serialize.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable and/or resource is <code>null</code>.
	 * @throws IOException If there was an error appending the serialized data.
	 */
	public Appendable serializeResource(@Nonnull final Appendable appendable, @Nullable Object resource) throws IOException {
		final boolean wasSerialized = setSerialized(resource); //mark this resource as having been serialized
		final String alias = determineAliasForResource(resource);
		if(alias != null) {
			appendable.append(LABEL_DELIMITER).append(alias).append(LABEL_DELIMITER);
			if(wasSerialized) { //an aliased resource never has to be serialized twice
				return appendable;
			}
		}
		switch(resource.getClass().getName()) { //use shortcut for final classes for efficiency
			//#literals
			//##binary
			case BYTE_ARRAY_CLASS_NAME:
				serializeBinary(appendable, ((byte[])resource));
				break;
			//##Boolean
			case BOOLEAN_CLASS_NAME:
				serializeBoolean(appendable, ((Boolean)resource).booleanValue());
				break;
			//##character
			case CHARACTER_CLASS_NAME:
				serializeCharacter(appendable, ((Character)resource).charValue());
				break;
			case CODE_POINT_CHARACTER_CLASS_NAME:
				serializeCharacter(appendable, ((CodePointCharacter)resource).getCodePoint());
				break;
			//##email address
			case EMAIL_ADDRESS_CLASS_NAME:
				serializeEmailAddress(appendable, (EmailAddress)resource);
				break;
			//##IRI
			case URI_CLASS_NAME:
				serializeIri(appendable, ((URI)resource));
				break;
			case URL_CLASS_NAME:
				try {
					serializeIri(appendable, ((URL)resource).toURI());
				} catch(final URISyntaxException uriURISyntaxException) {
					throw new IllegalArgumentException(String.format("URL %s is not a valid URI.", resource), uriURISyntaxException);
				}
				break;
			//##number
			case BIG_DECIMAL_CLASS_NAME:
			case BIG_INTEGER_CLASS_NAME:
			case BYTE_CLASS_NAME:
			case DOUBLE_CLASS_NAME:
			case FLOAT_CLASS_NAME:
			case INTEGER_CLASS_NAME:
			case LONG_CLASS_NAME:
			case SHORT_CLASS_NAME:
				serializeNumber(appendable, (Number)resource);
				break;
			//##regular expression
			case PATTERN_CLASS_NAME:
				serializeRegularExpression(appendable, (Pattern)resource);
				break;
			//##string
			case STRING_CLASS_NAME:
			case STRING_BUILDER_CLASS_NAME:
				serializeString(appendable, (CharSequence)resource);
				break;
			//##telephone number
			case TELEPHONE_NUMBER_CLASS_NAME:
				serializeTelephoneNumber(appendable, (TelephoneNumber)resource);
				break;
			//##temporal
			case DATE_CLASS_NAME:
				serializeTemporal(appendable, ((Date)resource).toInstant());
				break;
			case INSTANT_CLASS_NAME:
			case MONTH_DAY_CLASS_NAME:
			case LOCAL_DATE_CLASS_NAME:
			case LOCAL_DATE_TIME_CLASS_NAME:
			case LOCAL_TIME_CLASS_NAME:
				//TODO consider supporting OffsetDate; see e.g. http://stackoverflow.com/q/7788267/421049
			case OFFSET_DATE_TIME_CLASS_NAME:
			case OFFSET_TIME_CLASS_NAME:
			case YEAR__CLASS_NAME:
			case YEAR_MONTH_CLASS_NAME:
			case ZONED_DATE_TIME_CLASS_NAME:
				serializeTemporal(appendable, (TemporalAccessor)resource);
				break;
			//##UUID
			case UUID_CLASS_NAME:
				serializeUuid(appendable, (UUID)resource);
				break;
			//#collections
			//#list
			case ARRAY_LIST_CLASS_NAME:
			case LINKED_LIST_CLASS_NAME:
				serializeList(appendable, (List<?>)resource);
				break;
			//#map
			case HASH_MAP_CLASS_NAME:
			case LINKED_HASH_MAP_CLASS_NAME:
			case TREE_MAP_CLASS_NAME:
				serializeMap(appendable, (Map<?, ?>)resource);
				break;
			//#set
			case HASH_SET_CLASS_NAME:
			case LINKED_HASH_SET_CLASS_NAME:
			case TREE_SET_CLASS_NAME:
				serializeSet(appendable, (Set<?>)resource);
				break;
			default:
				//handle general base types and interfaces
				if(resource instanceof UrfObject) { //objects TODO additionally create class name constant when package stabilizes
					final UrfResource urfObject = (UrfObject)resource;
					final URI tag = urfObject.getTag().orElse(null);
					final URI typeTag = urfObject.getTypeTag().orElse(null);
					if(tag != null) {
						serializeObjectReference(appendable, tag, typeTag, !wasSerialized); //force a declaration the first time the object is serialized
						if(wasSerialized) { //an object with a tag of any sort never has to be serialized twice
							return appendable;
						}
					} else {
						serializeObject(appendable, typeTag);
					}
				} else if(resource instanceof List) { //list
					serializeList(appendable, (List<?>)resource);
				} else if(resource instanceof Map) { //map
					serializeMap(appendable, (Map<?, ?>)resource);
				} else if(resource instanceof Set) { //set
					serializeSet(appendable, (Set<?>)resource);
				} else if(resource instanceof ByteBuffer) { //binary
					serializeBinary(appendable, (ByteBuffer)resource);
				} else if(resource instanceof Number) { //number
					serializeNumber(appendable, (Number)resource);
				} else if(resource instanceof CharSequence) { //string
					serializeString(appendable, (CharSequence)resource);
					//TODO remove instanceof check and only use name lookup after making TelephoneNumber final
				} else if(resource instanceof TelephoneNumber) { //telephone number
					serializeTelephoneNumber(appendable, (TelephoneNumber)resource);
				} else if(resource instanceof Date) { //temporal
					serializeTemporal(appendable, (TemporalAccessor)resource);
				} else {
					throw new UnsupportedOperationException("Unsupported TURF serialization type: " + resource.getClass().getName());
				}
				break;
		}

		//serialize description if appropriate
		if(resource instanceof UrfObject) {
			final UrfObject urfObject = (UrfObject)resource;
			if(urfObject.getPropertyCount() > 0) { //if there are properties (otherwise skip the {} altogether)
				serializeDescription(appendable, urfObject);
			}
		}

		return appendable;
	}

	/**
	 * Serializes a label containing a tag.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param tag The tag to be serialized.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#LABEL_DELIMITER
	 */
	public static Appendable serializeTagLabel(@Nonnull final Appendable appendable, @Nonnull final URI tag) throws IOException {
		appendable.append(LABEL_DELIMITER);
		serializeIri(appendable, tag);
		return appendable.append(LABEL_DELIMITER);
	}

	/**
	 * Serializes a reference to resource. The reference will be a handle if possible; otherwise a label will be serialized for the tag.
	 * <p>
	 * As per the TURF specification, the handles {@link TURF#BOOLEAN_FALSE_LEXICAL_FORM} and {@link TURF#BOOLEAN_TRUE_LEXICAL_FORM} will be represented as tag
	 * labels and not as handles.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param tag The tag to be serialized.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see #serializeTagLabel(Appendable, URI)
	 */
	public Appendable serializeTagReference(@Nonnull final Appendable appendable, @Nonnull final URI tag) throws IOException { //TODO rename to "reference" or "resource reference" instead of "tag reference"?
		return serializeTagReference(appendable, tag, getNamespaceAliases());
	}

	/**
	 * Serializes a reference to resource. The reference will be a handle if possible; otherwise a label will be serialized for the tag.
	 * <p>
	 * As per the TURF specification, the handles {@link TURF#BOOLEAN_FALSE_LEXICAL_FORM} and {@link TURF#BOOLEAN_TRUE_LEXICAL_FORM} will be represented as tag
	 * labels and not as handles.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param tag The tag to be serialized.
	 * @param namespaceAliases The map of namespaces aliases associated with their namespaces.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader and/or namespace aliases map is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see #serializeTagLabel(Appendable, URI)
	 */
	public static Appendable serializeTagReference(@Nonnull final Appendable appendable, @Nonnull final URI tag, @Nonnull final Map<URI, String> namespaceAliases)
			throws IOException { //TODO rename to "reference" or "resource reference" instead of "tag reference"?
		final Optional<String> handle = Handle.fromTag(tag, namespaceAliases)
				.filter(tagHandle -> !tagHandle.equals(BOOLEAN_FALSE_LEXICAL_FORM) && !tagHandle.equals(BOOLEAN_TRUE_LEXICAL_FORM));
		ifPresentOrElse​(handle, throwingConsumer(appendable::append), throwingRunnable(() -> serializeTagLabel(appendable, tag)));
		return appendable;
	}

	//objects

	/**
	 * Serializes a reference to an object <em>without</em> the following description. An object reference will take one of the following forms, with an optional
	 * <code>Type</code>:
	 * <dl>
	 * <dt>{@code Type#id}</dt>
	 * <dd>If the object has an ID tag that can be expressed as a handle.</dd>
	 * <dt>{@code |"id"|*Type}</dt>
	 * <dd>If the object has an ID tag that cannot be expressed as a handle.</dd>
	 * <dt>{@code |"id"|*|<typeTag>|}</dt>
	 * <dd>If the object has an ID tag that cannot be expressed as a handle, and the type tag cannot be expressed as a handle.</dd>
	 * <dt>{@code handle*Type}</dt>
	 * <dd>If the object has tag that has no ID, but the tag can be expressed as a handle.</dd>
	 * <dt>{@code handle*|<typeTag>|}</dt>
	 * <dd>If the object has tag that has no ID, but the tag can be expressed as a handle; however the type tag cannot be expressed as a handle.</dd>
	 * <dt>{@code |<tag>|*Type}</dt>
	 * <dd>If the object has tag that has no ID and the tag cannot be expressed as a handle.</dd>
	 * <dt>{@code |<tag>|*|<typeTag>|}</dt>
	 * <dd>If the object has tag that has no ID, and neither the tag nor the type tag can be expressed as a handle.</dd>
	 * </dl>
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param tag The identifying tag of the object.
	 * @param typeTag The tag identifying object type, or <code>null</code> if the object has no declared type; if the tag is an ID tag and a type is given, the
	 *          type must match the type implied by the tag.
	 * @param declaration Whether the object declaration form <code>…*Type</code> should be included if there is a type.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IllegalArgumentException if a type tag was provided that does not match the implied type of an ID tag.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see #serializeObject(Appendable, URI)
	 */
	public Appendable serializeObjectReference(@Nonnull final Appendable appendable, @Nonnull final URI tag, @Nullable final URI typeTag,
			final boolean declaration) throws IOException {
		final String id = Tag.getId(tag).orElse(null);
		if(id != null) {
			if(typeTag != null) { //if a type was given, make sure it matches the implied type
				final URI idTypeTag = Tag.getIdTypeTag(tag).orElseThrow(() -> new AssertionError("If an tag has an ID, it is assumed to have an implied type."));
				checkArgument(idTypeTag.equals(typeTag), "Object reference with ID tag %s cannot have its implicit type given as %s to %s.", tag, idTypeTag, typeTag);
			}
			final String idHandle = Handle.fromTag(tag, getNamespaceAliases()).orElse(null);
			if(idHandle != null) { //Type#id
				appendable.append(idHandle);
			} else { //|"id"|*Type
				//TODO prevent both an alias and ID label from being serialized
				appendable.append(LABEL_DELIMITER);
				serializeString(appendable, id);
				appendable.append(LABEL_DELIMITER);
				serializeObject(appendable, typeTag);
			}
		} else { //|<tag>|*Type
			//TODO prevent both an alias and tag label from being serialized
			serializeTagReference(appendable, tag);
			if(declaration) { //if we should force the full declaration
				serializeObject(appendable, typeTag);
			}
		}
		return appendable;
	}

	/**
	 * Serializes an object representation in the form <code>*Type</code>, <em>without</em> the following description.
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param typeTag The tag identifying object type, or <code>null</code> if the object has no declared type.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#OBJECT_BEGIN
	 */
	public Appendable serializeObject(@Nonnull final Appendable appendable, @Nullable URI typeTag) throws IOException {
		appendable.append(OBJECT_BEGIN); //*
		if(typeTag != null) {
			serializeTagReference(appendable, typeTag); //type
		}
		return appendable;
	}

	/**
	 * Serializes a resource description. The description section, including delimiters, will be serialized even if there are no properties.
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param description The description to be serialized.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#DESCRIPTION_BEGIN
	 * @see TURF#DESCRIPTION_END
	 */
	public Appendable serializeDescription(@Nonnull final Appendable appendable, @Nonnull final UrfResourceDescription description) throws IOException {
		appendable.append(DESCRIPTION_BEGIN); //:
		formatNewLine(appendable);
		try (final Closeable indention = increaseIndentLevel()) {
			serializeSequence(appendable, description.getProperties(), this::serializeProperty);
		}
		formatIndent(appendable);
		appendable.append(DESCRIPTION_END); //;
		return appendable;
	}

	/**
	 * Serializes an object property and its value.
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param property The property and value pair to be serialized.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 */
	public Appendable serializeProperty(@Nonnull final Appendable appendable, @Nonnull final Map.Entry<URI, Object> property) throws IOException {
		serializeTagReference(appendable, property.getKey());
		if(formatted) {
			appendable.append(SPACE_CHAR);
		}
		appendable.append(PROPERTY_VALUE_DELIMITER); //=
		if(formatted) {
			appendable.append(SPACE_CHAR);
		}
		return serializeResource(appendable, property.getValue());
	}

	//literals

	/**
	 * Serializes a binary literal along with its delimiter from an array of bytes.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param bytes The information to be serialized as a binary literal.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#BINARY_BEGIN
	 */
	public static Appendable serializeBinary(@Nonnull final Appendable appendable, @Nonnull final byte[] bytes) throws IOException {
		appendable.append(BINARY_BEGIN);
		return appendable.append(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
	}

	/**
	 * Serializes a binary literal along with its delimiter from a byte buffer.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param byteBuffer The information to be serialized as a binary literal.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#BINARY_BEGIN
	 */
	public static Appendable serializeBinary(@Nonnull final Appendable appendable, @Nonnull final ByteBuffer byteBuffer) throws IOException {
		appendable.append(BINARY_BEGIN);
		final ByteBuffer base64ByteBuffer = Base64.getEncoder().withoutPadding().encode(byteBuffer);
		final byte[] base64Bytes;
		if(base64ByteBuffer.hasArray()) { //use the underlying array directly if there is one
			base64Bytes = base64ByteBuffer.array();
		} else {
			base64Bytes = new byte[base64ByteBuffer.remaining()];
			base64ByteBuffer.get(base64Bytes);
		}
		return appendable.append(new String(base64Bytes, US_ASCII));
	}

	/**
	 * Serializes a Boolean.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param bool The Boolean value to be serialized as a Boolean.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IllegalArgumentException if the given code point is not a valid Unicode code point.
	 * @throws IOException if there is an error appending to the appendable.
	 */
	public static Appendable serializeBoolean(@Nonnull final Appendable appendable, @Nonnull final boolean bool) throws IOException {
		return appendable.append(bool ? BOOLEAN_TRUE_LEXICAL_FORM : BOOLEAN_FALSE_LEXICAL_FORM);
	}

	/**
	 * Serializes a character surrounded by character delimiters.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param codePoint The Unicode code point to be serialized as a character.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IllegalArgumentException if the given code point is not a valid Unicode code point.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#CHARACTER_DELIMITER
	 * @see #serializeCharacterCodePoint(Appendable, char, int)
	 */
	public static Appendable serializeCharacter(@Nonnull final Appendable appendable, @Nonnull final int codePoint) throws IOException {
		checkArgument(Character.isValidCodePoint(codePoint), "The value %d does not represent is not a valid code point.", codePoint);
		appendable.append(CHARACTER_DELIMITER);
		serializeCharacterCodePoint(appendable, CHARACTER_DELIMITER, codePoint);
		return appendable.append(CHARACTER_DELIMITER);
	}

	/**
	 * Serializes a character as content, without any delimiters.
	 * <p>
	 * This implementation does not escape the solidus (slash) character <code>'/'</code>, which is not required to be escaped.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param delimiter The delimiter that surrounds the character and which should be escaped.
	 * @param codePoint The code point to serialize.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable is <code>null</code>.
	 * @throws IOException if there is an error appending to the appender.
	 * @throws ParseIOException if a control character was represented, if the character is not escaped correctly, or the reader has no more characters before the
	 *           current character is completely parsed.
	 * @see TURF#CHARACTER_REQUIRED_ESCAPED_CHARACTERS
	 */
	public static Appendable serializeCharacterCodePoint(@Nonnull final Appendable appendable, final char delimiter, final int codePoint)
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
			return appendable.append(escapeChar);
		}
		//code points outside the BMP
		if(Character.isSupplementaryCodePoint(codePoint)) {
			return appendable.append(Character.highSurrogate(codePoint)).append(Character.lowSurrogate(codePoint));
		}
		//code points within the BMP
		assert Character.isBmpCodePoint(codePoint); //everything else should be in the BMP and not need escaping
		return appendable.append((char)codePoint);
	}

	/**
	 * Serializes an email address along with its delimiter.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param emailAddress The information to be serialized as an email address.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#EMAIL_ADDRESS_BEGIN
	 */
	public static Appendable serializeEmailAddress(@Nonnull final Appendable appendable, @Nonnull final EmailAddress emailAddress) throws IOException {
		appendable.append(EMAIL_ADDRESS_BEGIN);
		return appendable.append(emailAddress.toString());
	}

	/**
	 * Serializes an IRI along with its delimiters.
	 * <p>
	 * This implementation serializes an IRI using an IRI short form if possible.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param iri The information to be serialized as an IRI.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IllegalArgumentException if the given IRI is not a true, absolute IRI with a scheme.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#IRI_BEGIN
	 */
	public static Appendable serializeIri(@Nonnull final Appendable appendable, @Nonnull final URI iri) throws IOException {
		checkAbsolute(iri);
		appendable.append(IRI_BEGIN);
		switch(ASCII.toLowerCase(iri.getScheme()).toString()) {
			case MAILTO_SCHEME:
				appendable.append(EMAIL_ADDRESS_BEGIN).append(iri.getSchemeSpecificPart()); //^jdoe@example.com
				break;
			case TEL_SCHEME:
				appendable.append(iri.getSchemeSpecificPart()); //+12015550123
				break;
			case URN_SCHEME: {
				final String ssp = iri.getSchemeSpecificPart();
				final Matcher urnMatcher = URN_SSP_PATTERN.matcher(ssp);
				//urn:uuid
				if(urnMatcher.matches() && ASCII.toLowerCase(urnMatcher.group(URN_SSP_PATTERN_NID_MATCHING_GROUP)).equals(UUIDs.UUID_URN_NAMESPACE)) {
					appendable.append(UUID_BEGIN).append(urnMatcher.group(URN_SSP_PATTERN_NSS_MATCHING_GROUP));
					break;
				}
				//for all other URN NIDs (e.g. "isbn"), fall through and serialize the IRI normally
			}
			default:
				appendable.append(iri.toString());
				break;
		}
		return appendable.append(IRI_END);
	}

	/**
	 * Serializes a number along with its delimiter if should be represented as a decimal.
	 * <p>
	 * This implementation represents the following types as decimal:
	 * </p>
	 * <ul>
	 * <li>{@link BigDecimal}</li>
	 * </ul>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param number The information to be serialized as a number.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#NUMBER_DECIMAL_BEGIN
	 */
	public static Appendable serializeNumber(@Nonnull final Appendable appendable, @Nonnull final Number number) throws IOException {
		final boolean isDecimal = number instanceof BigDecimal;
		if(isDecimal) {
			appendable.append(NUMBER_DECIMAL_BEGIN);
		}
		return appendable.append(number.toString());
	}

	/**
	 * Serializes a regular expression along with its delimiters.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param regularExpression The information to be serialized as a regular expression.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#REGULAR_EXPRESSION_DELIMITER
	 * @see TURF#REGULAR_EXPRESSION_ESCAPE
	 */
	public static Appendable serializeRegularExpression(@Nonnull final Appendable appendable, @Nonnull final Pattern regularExpression) throws IOException {
		appendable.append(REGULAR_EXPRESSION_DELIMITER);
		final String regexString = regularExpression.toString();
		//See if there is anything we need to escape; checking ahead of time will usually be faster than appending each character,
		//as the String.indexOf() is optimized by using internal data, and most regular expressions will not need to be escaped.
		if(regexString.indexOf(REGULAR_EXPRESSION_DELIMITER) >= 0) {
			final int regexLength = regexString.length();
			for(int i = 0; i < regexLength; i++) {
				final char c = regexString.charAt(i); //TODO provide better checks of valid surrogate pair sequences
				if(c == REGULAR_EXPRESSION_DELIMITER) { //escape the '/' character
					appendable.append(REGULAR_EXPRESSION_ESCAPE);
				}
				appendable.append(c);
			}
		} else { //no escaping needed
			appendable.append(regexString);
		}
		return appendable.append(REGULAR_EXPRESSION_DELIMITER);
		//TODO add support for flags
	}

	/**
	 * Serializes a string surrounded by string delimiters.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param charSequence The information to be serialized as a string.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#STRING_DELIMITER
	 * @see #serializeCharacterCodePoint(Appendable, char, int)
	 */
	public static Appendable serializeString(@Nonnull final Appendable appendable, @Nonnull final CharSequence charSequence) throws IOException {
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
		return appendable.append(STRING_DELIMITER);
	}

	/**
	 * Serializes a telephone number along with its delimiter.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param telephoneNumber The information to be serialized as a telephone number.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @throws IllegalArgumentException if the given telephone number is not in global form.
	 * @see TURF#TELEPHONE_NUMBER_BEGIN
	 * @see TelephoneNumber#isGlobal()
	 */
	public static Appendable serializeTelephoneNumber(@Nonnull final Appendable appendable, @Nonnull final TelephoneNumber telephoneNumber) throws IOException {
		checkArgument(telephoneNumber.isGlobal(), "Telephone number %s not in global form.", telephoneNumber);
		return appendable.append(telephoneNumber.toString());
	}

	/**
	 * Serializes a temporal literal along with its delimiter.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param temporal The information to be serialized as a temporal.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#TEMPORAL_BEGIN
	 */
	public static Appendable serializeTemporal(@Nonnull final Appendable appendable, @Nonnull final TemporalAccessor temporal) throws IOException {
		appendable.append(TEMPORAL_BEGIN);
		return appendable.append(temporal.toString());
	}

	/**
	 * Serializes a UUID along with its delimiter.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param uuid The information to be serialized as a UUID.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#UUID_BEGIN
	 */
	public static Appendable serializeUuid(@Nonnull final Appendable appendable, @Nonnull final UUID uuid) throws IOException {
		appendable.append(UUID_BEGIN);
		return appendable.append(uuid.toString());
	}

	//collections

	/**
	 * Serializes a list.
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param list The information to be serialized as a list.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#LIST_BEGIN
	 * @see TURF#LIST_END
	 */
	public Appendable serializeList(@Nonnull final Appendable appendable, @Nonnull final List<?> list) throws IOException {
		appendable.append(LIST_BEGIN); //[
		if(!list.isEmpty()) {
			formatNewLine(appendable);
			try (final Closeable indention = increaseIndentLevel()) { //TODO allow configurable indent for small collections
				serializeSequence(appendable, list, this::serializeResource);
			}
			formatIndent(appendable);
		}
		return appendable.append(LIST_END); //]
	}

	/**
	 * Serializes a map.
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param map The information to be serialized as a map.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#MAP_BEGIN
	 * @see TURF#MAP_END
	 * @see TURF#MAP_KEY_DELIMITER
	 * @see TURF#ENTRY_KEY_VALUE_DELIMITER
	 */
	public Appendable serializeMap(@Nonnull final Appendable appendable, @Nonnull final Map<?, ?> map) throws IOException {
		appendable.append(MAP_BEGIN); //{
		if(!map.isEmpty()) {
			formatNewLine(appendable);
			try (final Closeable indention = increaseIndentLevel()) { //TODO allow configurable indent for small maps
				serializeSequence(appendable, map.entrySet(), this::serializeMapEntry);
			}
			formatIndent(appendable);
		}
		return appendable.append(MAP_END); //}
	}

	/**
	 * Serializes a map entry.
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param entry The map key and value pair to be serialized.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 */
	public Appendable serializeMapEntry(@Nonnull final Appendable appendable, @Nonnull Map.Entry<?, ?> entry) throws IOException {
		final Object key = entry.getKey();
		final boolean hasDescription = key instanceof UrfObject && ((UrfObject)key).hasProperties();
		if(hasDescription) {
			appendable.append(MAP_KEY_DELIMITER); //\
		}
		serializeResource(appendable, key);
		if(hasDescription) {
			appendable.append(MAP_KEY_DELIMITER); //\
		}
		appendable.append(ENTRY_KEY_VALUE_DELIMITER); //:
		if(formatted) {
			appendable.append(SPACE_CHAR);
		}
		return serializeResource(appendable, entry.getValue());
	}

	/**
	 * Serializes a set.
	 * <p>
	 * All references to the resources in the graph must have already been discovered if aliases need to be generated.
	 * </p>
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param set The information to be serialized as a set.
	 * @return The given appendable.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error appending to the appendable.
	 * @see TURF#SET_BEGIN
	 * @see TURF#SET_END
	 */
	public Appendable serializeSet(@Nonnull final Appendable appendable, @Nonnull final Set<?> set) throws IOException {
		appendable.append(SET_BEGIN); //(
		if(!set.isEmpty()) {
			formatNewLine(appendable);
			try (final Closeable indention = increaseIndentLevel()) { //TODO allow configurable indent for small collections
				serializeSequence(appendable, set, this::serializeResource);
			}
			formatIndent(appendable);
		}
		return appendable.append(SET_END); //)
	}

	/**
	 * Serializes a general sequence (such as a list). For each sequence item, {@link IOBiConsumer#accept(Object, Object)} is called, passing the output
	 * {@link Appendable} along with the item to be serialized. The item serialization strategy will return <code>false</code> to indicate that there are no
	 * further items.
	 * @param <I> The type of item in the sequence.
	 * @param appendable The appendable to which serialized data should be appended.
	 * @param sequence An iterable representing the sequence to serialize.
	 * @param itemSerializer The serialization strategy, which is passed the {@link Appendable} to use for serialization, along with each item to serialize.
	 * @return The given appendable.
	 * @throws NullPointerException if the given appendable and/or item serializer is <code>null</code>.
	 * @throws IOException if there is an error appending to the appender.
	 */
	protected <I> Appendable serializeSequence(@Nonnull final Appendable appendable, @Nonnull Iterable<I> sequence,
			@Nonnull final IOBiConsumer<Appendable, I> itemSerializer) throws IOException {
		final boolean sequenceSeparatorRequired = isSequenceSeparatorRequired();
		final Iterator<I> iterator = sequence.iterator();
		while(iterator.hasNext()) {
			formatIndent(appendable);
			final I item = iterator.next();
			itemSerializer.accept(appendable, item); //serialize the item
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
		return appendable;
	}

}
