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

package io.urf;

import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.net.URIs.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.net.URI;
import java.util.*;
import java.util.regex.*;

import javax.annotation.*;

import com.globalmentor.java.Characters;
import com.globalmentor.net.URIs;

/**
 * Definitions for the Uniform Resource Framework (URF).
 * @author Garret Wilson
 */
public class URF {

	/** The URF ad-hoc namespace. */
	public static final URI AD_HOC_NAMESPACE = URI.create("https://urf.name/");

	/** The URF ontology namespace. */
	public static final URI NAMESPACE = URI.create("https://urf.name/urf/");

	//#types

	/** The tag of the <code>urf-Binary</code> type. */
	public static final URI BINARY_TYPE_TAG = NAMESPACE.resolve("Binary");
	/** The tag of the <code>urf-Boolean</code> type. */
	public static final URI BOOLEAN_TYPE_TAG = NAMESPACE.resolve("Boolean");
	/** The tag of the <code>urf-Character</code> type. */
	public static final URI CHARACTER_TYPE_TAG = NAMESPACE.resolve("Character");
	/** The tag of the <code>urf-Decimal</code> type. */
	public static final URI DECIMAL_TYPE_TAG = NAMESPACE.resolve("Decimal");
	/** The tag of the <code>urf-EmailAddress</code> type. */
	public static final URI EMAIL_ADDRESS_TYPE_TAG = NAMESPACE.resolve("EmailAddress");
	/** The tag of the <code>urf-Element</code> type. */
	public static final URI ELEMENT_TYPE_TAG = NAMESPACE.resolve("Element");
	/** The tag of the <code>urf-Instant</code> type. */
	public static final URI INSTANT_TYPE_TAG = NAMESPACE.resolve("Instant");
	/** The tag of the <code>urf-Integer</code> type. */
	public static final URI INTEGER_TYPE_TAG = NAMESPACE.resolve("Integer");
	/** The tag of the <code>urf-Iri</code> type. */
	public static final URI IRI_TYPE_TAG = NAMESPACE.resolve("Iri");
	/** The tag of the <code>urf-LocalDate</code> type. */
	public static final URI LOCAL_DATE_TYPE_TAG = NAMESPACE.resolve("LocalDate");
	/** The tag of the <code>urf-LocalDateTime</code> type. */
	public static final URI LOCAL_DATE_TIME_TYPE_TAG = NAMESPACE.resolve("LocalDateTime");
	/** The tag of the <code>urf-LocalTime</code> type. */
	public static final URI LOCAL_TIME_TYPE_TAG = NAMESPACE.resolve("LocalTime");
	/** The tag of the <code>urf-List</code> type. */
	public static final URI LIST_TYPE_TAG = NAMESPACE.resolve("List");
	/** The tag of the <code>urf-Map</code> type. */
	public static final URI MAP_TYPE_TAG = NAMESPACE.resolve("Map");
	/** The tag of the <code>urf-MapEntry</code> type. */
	public static final URI MAP_ENTRY_TYPE_TAG = NAMESPACE.resolve("MapEntry");
	/** The tag of the <code>urf-Member</code> type. */
	public static final URI MEMBER_TYPE_TAG = NAMESPACE.resolve("Member");
	/** The tag of the <code>urf-MonthDay</code> type. */
	public static final URI MONTH_DAY_TYPE_TAG = NAMESPACE.resolve("MonthDay");
	/** The tag of the <code>urf-OffsetDate</code> type. */
	public static final URI OFFSET_DATE_TYPE_TAG = NAMESPACE.resolve("OffsetDate");
	/** The tag of the <code>urf-OffsetDateTime</code> type. */
	public static final URI OFFSET_DATE_TIME_TYPE_TAG = NAMESPACE.resolve("OffsetDateTime");
	/** The tag of the <code>urf-OffsetTime</code> type. */
	public static final URI OFFSET_TIME_TYPE_TAG = NAMESPACE.resolve("OffsetTime");
	/** The tag of the <code>urf-Real</code> type. */
	public static final URI REAL_TYPE_TAG = NAMESPACE.resolve("Real");
	/** The tag of the <code>urf-RegularExpression</code> type. */
	public static final URI REGULAR_EXPRESSION_TYPE_TAG = NAMESPACE.resolve("RegularExpression");
	/** The tag of the <code>urf-Resource</code> type. */
	public static final URI RESOURCE_TYPE_TAG = NAMESPACE.resolve("Resource");
	/** The tag of the <code>urf-Set</code> type. */
	public static final URI SET_TYPE_TAG = NAMESPACE.resolve("Set");
	/** The tag of the <code>urf-String</code> type. */
	public static final URI STRING_TYPE_TAG = NAMESPACE.resolve("String");
	/** The tag of the <code>urf-TelephoneNumber</code> type. */
	public static final URI TELEPHONE_NUMBER_TYPE_TAG = NAMESPACE.resolve("TelephoneNumber");
	/** The tag of the <code>urf-Uuid</code> type. */
	public static final URI UUID_TYPE_TAG = NAMESPACE.resolve("Uuid");
	/** The tag of the <code>urf-Year</code> type. */
	public static final URI YEAR_TYPE_TAG = NAMESPACE.resolve("Year");
	/** The tag of the <code>urf-YearMonth</code> type. */
	public static final URI YEAR_MONTH_TYPE_TAG = NAMESPACE.resolve("YearMonth");
	/** The tag of the <code>urf-ZonedDateTime</code> type. */
	public static final URI ZONED_DATE_TIME_TYPE_TAG = NAMESPACE.resolve("ZonedDateTime");

	//#properties

	//##intrinsic properties

	/** The tag of <code>urf-type</code>. */
	public static final URI TYPE_PROPERTY_TAG = NAMESPACE.resolve("type");

	//##description

	/** The tag of <code>urf-key</code>. */
	public static final URI KEY_PROPERTY_TAG = NAMESPACE.resolve("key");
	/** The tag of <code>urf-value</code>. */
	public static final URI VALUE_PROPERTY_TAG = NAMESPACE.resolve("value");

	//##aggregation

	/** The tag of <code>urf-member</code>. */
	public static final URI MEMBER_PROPERTY_TAG = NAMESPACE.resolve("member");

	//##content

	/** The tag of <code>urf-content</code>. */
	public static final URI CONTENT_PROPERTY_TAG = NAMESPACE.resolve("content");

	/**
	 * Utilities for working with URF names.
	 * @author Garret Wilson
	 */
	public static final class Name {

		/** Regular expression pattern to match an URF name token. */
		public static final Pattern TOKEN_PATTERN = Pattern.compile("\\p{L}[\\p{L}\\p{M}\\p{N}\\p{Pc}]*"); //TODO add test

		/** Regular expression pattern to match an URF name ID token. */
		public static final Pattern ID_TOKEN_PATTERN = Pattern.compile("[\\p{L}\\p{M}\\p{N}\\p{Pc}]+"); //TODO add test

		/** The delimiter that may appear in an URF name to indicate the start of the ID, if any. */
		public static final char ID_DELIMITER = '#';

		/**
		 * Determines whether the given string conforms to the rules for an URF name token.
		 * @param string The string to test.
		 * @return <code>true</code> if the string is a valid URF name token.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @see #TOKEN_PATTERN
		 */
		public static boolean isValidToken(final String string) {
			return TOKEN_PATTERN.matcher(requireNonNull(string)).matches();
		}

		/**
		 * Confirms that the given string conforms to the rules for an URF name token.
		 * @param string The string to check.
		 * @return The given string.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @throws IllegalArgumentException if the given string does not conform to the rules for an URF name token.
		 * @see #TOKEN_PATTERN
		 */
		public static String checkArgumentValidToken(final String string) {
			checkArgument(isValidToken(string), "Invalid URF name token \"%s\".", string);
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

		/** Regular expression pattern to match an URF name . */
		public static final Pattern PATTERN = Pattern.compile(String.format("(%s)(?:%s(.*))?", TOKEN_PATTERN, ID_DELIMITER)); //TODO add test; document matching groups

		/**
		 * Determines whether the given string conforms to the rules for an URF name.
		 * @param string The string to test.
		 * @return <code>true</code> if the string is a valid URF name.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @see #PATTERN
		 */
		public static boolean isValid(final String string) {
			return PATTERN.matcher(requireNonNull(string)).matches();
		}

		/**
		 * Confirms that the given string conforms to the rules for an URF name.
		 * @param string The string to check.
		 * @return The given string.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @throws IllegalArgumentException if the given string does not conform to the rules for an URF name.
		 * @see #PATTERN
		 */
		public static String checkArgumentValid(final String string) {
			checkArgument(isValid(string), "Invalid URF name \"%s\".", string);
			return string;
		}

		/**
		 * Creates a name for an instance of a type.
		 * @param typeName The type name.
		 * @param id The ID identifying the instance of the type.
		 * @return A name with the given type name and ID.
		 */
		public static String forTypeId(@Nonnull String typeName, @Nonnull String id) {
			return new StringBuilder(requireNonNull(typeName)).append(ID_DELIMITER).append(encode(requireNonNull(id))).toString(); //TODO check encoding
		}

	}

	/**
	 * Utilities for working with TURF tags.
	 * @author Garret Wilson
	 */
	public static final class Tag {

		/**
		 * Ensures that the given URI is a valid resource tag.
		 * <p>
		 * Primarily this ensures that the given URI is absolute.
		 * </p>
		 * @param tag The tag to validate.
		 * @return The given tag.
		 * @throws IllegalArgumentException if the given URI is not a valid tag.
		 */
		public static URI checkArgumentValid(@Nonnull final URI tag) {
			return checkAbsolute(tag); //TODO have a test to make sure that the tag is not blank in normal circumstances
		}

		/**
		 * Retrieves the namespace of the given tag. The namespace is the parent collection URI of the tag.
		 * <p>
		 * Not every tag is in a namespace. For example, a tag URI without a path is not in any namespace. A tag URI with the root path is not in any namespace
		 * </p>
		 * @param tag The tag URI from which a namespace should be retrieved.
		 * @return The namespace of the tag.
		 * @throws IllegalArgumentException if the given URI is not a valid tag.
		 * @throws NullPointerException if the given tag is <code>null</code>.
		 */
		public static Optional<URI> getNamespace(@Nonnull final URI tag) {
			checkArgumentValid(tag);
			if(hasPath(tag)) {
				return Optional.ofNullable(getParentURI(tag));
			}
			return Optional.empty();
		}

		/**
		 * Retrieves the resource name from the given tag. The name is the decoded last non-collection path segment of the URI, if any, including the URI fragment,
		 * if any.
		 * <p>
		 * Not every tag has a name. For example, a tag URI without a path has no name. A tag URI that is a collection has no name. A tag URI with a fragment but no
		 * last path segment has no name. A name must be valid or no name will be returned.
		 * </p>
		 * @param tag The tag URI from which a name should be retrieved.
		 * @return The resource name, if any, from its tag.
		 * @throws IllegalArgumentException if the given URI is not a valid tag.
		 * @throws NullPointerException if the given tag is <code>null</code>.
		 * @see #getId(URI)
		 */
		public static Optional<String> getName(@Nonnull final URI tag) {
			checkArgumentValid(tag);
			final String rawPath = tag.getRawPath();
			if(rawPath != null && !rawPath.isEmpty() && !endsWith(rawPath, PATH_SEPARATOR)) { //if there is a raw path that isn't a collection
				String name = decode(URIs.getName(rawPath));
				final String rawFragment = tag.getRawFragment();
				if(rawFragment != null) { //see if there is an ID to append
					name = name + Name.ID_DELIMITER + decode(rawFragment);
				}
				if(Name.isValid(name)) { //make sure the supposed name is valid
					return Optional.of(name);
				}
			}
			return Optional.empty();
		}

		/**
		 * Retrieves the resource type from the given ID tag. The type URI with the fragment removed.
		 * <p>
		 * To be considered an ID tag, a URI must have a non-collection path and have a fragment that is not the empty string.
		 * </p>
		 * @param tag The ID tag URI from which a type should be retrieved.
		 * @return The type of the given ID tag URI, which will not be present if the URI does not represent an ID tag.
		 * @throws NullPointerException if the given tag is <code>null</code>.
		 */
		public static Optional<URI> getIdTypeTag(@Nonnull final URI tag) {
			checkArgumentValid(tag);
			final String rawPath = tag.getRawPath();
			final String rawFragment = tag.getRawFragment();
			if(rawPath != null && !rawPath.isEmpty() && !isCollectionPath(rawPath) && rawFragment != null && !rawFragment.isEmpty()) {
				return Optional.of(removeFragment(tag));
			}
			return Optional.empty();
		}

		/**
		 * Retrieves the resource ID from the given tag. The ID is the decoded fragment of the URI, if any.
		 * <p>
		 * To be considered an ID tag, a URI must have a non-collection path and have a fragment that is not the empty string.
		 * </p>
		 * @param tag The tag URI from which an ID should be retrieved.
		 * @return The ID of the given ID tag URI, which will not be present if the URI does not represent an ID tag.
		 * @throws NullPointerException if the given tag is <code>null</code>.
		 */
		public static Optional<String> getId(@Nonnull final URI tag) {
			checkArgumentValid(tag);
			final String rawPath = tag.getRawPath();
			final String rawFragment = tag.getRawFragment();
			if(rawPath != null && !rawPath.isEmpty() && !isCollectionPath(rawPath) && rawFragment != null && !rawFragment.isEmpty()) {
				return Optional.of(decode(rawFragment)); //decode the fragment manually for consistency and for better error-handling
			}
			return Optional.empty();
		}

		/**
		 * Creates a tag for a type.
		 * @param typeNamespace The type namespace.
		 * @param typeName The type name.
		 * @return A tag for the type.
		 */
		public static URI forType(@Nonnull final URI typeNamespace, @Nonnull String typeName) {
			return typeNamespace.resolve(requireNonNull(typeName)); //TODO fix encoding
		}

		/**
		 * Creates a tag for an instance of a type by its namespace and name.
		 * @param typeNamespace The type namespace.
		 * @param typeName The type name.
		 * @param id The ID identifying the instance of the type.
		 * @return A tag with the given type and ID.
		 */
		public static URI forTypeId(@Nonnull final URI typeNamespace, @Nonnull String typeName, @Nonnull String id) {
			return forTypeId(forType(typeNamespace, typeName), id);
		}

		/**
		 * Creates a tag for an instance of a type by its tag.
		 * @param typeTag The type tag.
		 * @param id The ID identifying the instance of the type.
		 * @return A tag with the given type and ID.
		 */
		public static URI forTypeId(@Nonnull final URI typeTag, @Nonnull String id) {
			//TODO ensure that the type tag has no fragment
			return URI.create(new StringBuilder(typeTag.toString()).append(FRAGMENT_SEPARATOR).append(encode(requireNonNull(id))).toString());
		}

		/**
		 * Generates a unique blank tag.
		 * @return A new blank tag with a unique ID.
		 */
		public static URI generateBlank() {
			return generateBlank(UUID.randomUUID().toString());
		}

		/**
		 * Generates a blank tag using the given ID.
		 * @param id The blank tag ID to use.
		 * @return A blank tag with the given ID.
		 * @throws NullPointerException if the given ID is <code>null</code>.
		 */
		public static URI generateBlank(@Nonnull final String id) {
			return AD_HOC_NAMESPACE.resolve(FRAGMENT_SEPARATOR + id); //TODO make more rigorous; test
		}

		/**
		 * Determines whether the given tag is a blank tag.
		 * @param tag The tag to test.
		 * @return <code>true</code> if the tag is a blank tag.
		 */
		public static boolean isBlank(@Nonnull final URI tag) {
			return !AD_HOC_NAMESPACE.relativize(tag).isAbsolute() && ROOT_PATH.equals(tag.getRawPath()) && tag.getRawQuery() == null && tag.getRawFragment() != null;
		}

		/**
		 * Determines ID of a potentially blank tag.
		 * @param tag The tag, which may be blank.
		 * @return The ID of the blank tag, which may be empty if the tag is not a blank tag.
		 */
		public static Optional<String> getBlankId(@Nonnull final URI tag) {
			if(!isBlank(tag)) {
				return Optional.empty();
			}
			assert tag.getRawFragment() != null : "Blank tags are expected to have a fragment.";
			return Optional.of(tag.getFragment()); //use the decoded form
		}

	}

	/**
	 * Utilities for working with TURF handles.
	 * @author Garret Wilson
	 */
	public static final class Handle {

		/** The delimiter used to separate a namespace alias prefix from the rest of a TURF handle. */
		public static final char NAMESPACE_ALIAS_DELIMITER = '/';

		/** The delimiter used to separate ad-hoc namespaces in a TURF handle. */
		public static final char SEGMENT_DELIMITER = '-';

		/** All the possible delimiters used by a TURF handle. */
		public static final Characters DELIMITERS = Characters.of(NAMESPACE_ALIAS_DELIMITER, SEGMENT_DELIMITER, Name.ID_DELIMITER);

		/**
		 * Regular expression pattern to match an URF handle.
		 * <p>
		 * Matching groups:
		 * </p>
		 * <dl>
		 * <dt>1</dt>
		 * <dd>namespace alias (optional)</dd>
		 * <dt>2</dt>
		 * <dd>segments</dd>
		 * <dt>3</dt>
		 * <dd>ID (optional)</dd>
		 * </dl>
		 */
		public static final Pattern PATTERN = Pattern.compile(String.format("(?:(%s)%s)?(%s(?:%s%s)*)(?:%s(%s))?", Name.TOKEN_PATTERN, NAMESPACE_ALIAS_DELIMITER,
				Name.TOKEN_PATTERN, SEGMENT_DELIMITER, Name.TOKEN_PATTERN, Name.ID_DELIMITER, Name.ID_TOKEN_PATTERN));

		/**
		 * The pattern matching group for the optional namespace alias.
		 * @see #PATTERN
		 */
		public final static int PATTERN_NAMESPACE_ALIAS_GROUP = 1;

		/**
		 * The pattern matching group for the handle segments.
		 * @see #PATTERN
		 */
		public final static int PATTERN_SEGEMENTS_GROUP = 2;

		/**
		 * The pattern matching group for the optional ID.
		 * @see #PATTERN
		 */
		public final static int PATTERN_ID_GROUP = 3;

		/**
		 * Determines if the given character is valid to begin an URF handle. An URF handle begins with a name token.
		 * @param c The character to check.
		 * @return <code>true</code> if the character is an URF handle begin character.
		 * @see Name#isTokenBeginCharacter(int)
		 */
		public static final boolean isBeginCharacter(final int c) {
			return Name.isTokenBeginCharacter(c); //see if this is a letter
		}

		/**
		 * Determines whether the given string conforms to the rules for an URF handle.
		 * @param string The string to test.
		 * @return <code>true</code> if the string is a valid SURF handle.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @see #PATTERN
		 */
		public static boolean isValid(final String string) {
			return PATTERN.matcher(requireNonNull(string)).matches();
		}

		/**
		 * Confirms that the given string conforms to the rules for an URF handle.
		 * @param string The string to check.
		 * @return The given string.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @throws IllegalArgumentException if the given string does not conform to the rules for an URF handle.
		 * @see #PATTERN
		 */
		public static String checkArgumentValid(final String string) {
			checkArgument(isValid(string), "Invalid URF handle \"%s\".", string);
			return string;
		}

		/**
		 * Confirms that the given string conforms to the rules for an URF handle, returning a matcher
		 * @param string The string to check.
		 * @return A matcher that has successfully matched the given string.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @throws IllegalArgumentException if the given string does not conform to the rules for an URF handle.
		 * @see #PATTERN
		 */
		public static Matcher checkArgumentMatchesValid(final String string) {
			final Matcher matcher = PATTERN.matcher(requireNonNull(string));
			checkArgument(matcher.matches(), "Invalid URF handle \"%s\".", string);
			return matcher;
		}

		//TODO decide whether encoding/decoding is needed, as IRIs are used

		/**
		 * Determines the TURF handle to represent the given resource tag.
		 * <p>
		 * Not every tag has a handle. A tag with no namespace or no name has no handle.
		 * </p>
		 * @param tag The tag for which a handle should be determined.
		 * @return The TURF handle representing the given tag.
		 * @throws NullPointerException if the given tag is <code>null</code>.
		 * @throws IllegalArgumentException if the given URI is not a valid tag.
		 */
		public static Optional<String> fromTag(@Nonnull final URI tag) {
			return fromTag(tag, emptyMap());
		}

		/**
		 * Determines the TURF handle to represent the given resource tag.
		 * <p>
		 * Not every tag has a handle. A tag with no namespace or no name, or for which no namespace alias is registered, has no handle.
		 * </p>
		 * @param tag The tag for which a handle should be determined.
		 * @param namespaceAliases The registered namespace aliases, associated with their namespaces.
		 * @return The TURF handle representing the given tag.
		 * @throws NullPointerException if the given tag and/or namespace alias is <code>null</code>.
		 * @throws IllegalArgumentException if the given URI is not a valid tag.
		 */
		public static Optional<String> fromTag(@Nonnull final URI tag, @Nonnull final Map<URI, String> namespaceAliases) {
			Tag.checkArgumentValid(tag);
			final Optional<String> optionalName = Tag.getName(tag);
			//if there is a name, convert it to a handle based on the namespace
			return optionalName.flatMap(name -> {
				//if there is a namespace, use it to convert the name to a handle
				return Tag.getNamespace(tag).flatMap(namespace -> {
					final URI adHocNamespaceRelativeURI = AD_HOC_NAMESPACE.relativize(namespace);
					if(adHocNamespaceRelativeURI.equals(namespace)) { //if the namespace is not relative to the ad-hoc namespace
						final String alias = namespaceAliases.get(namespace);
						if(alias == null) {
							return Optional.empty(); //there is no handle
						}
						return Optional.of(alias + NAMESPACE_ALIAS_DELIMITER + name);
					}
					assert !adHocNamespaceRelativeURI.isAbsolute();
					assert !URIs.hasAbsolutePath(adHocNamespaceRelativeURI);
					final String adHocRelativePath = adHocNamespaceRelativeURI.getRawPath();
					if(adHocRelativePath.isEmpty()) { //if the namespace _is_ the ad-hoc namespace
						return Optional.of(name); //the name is already the handle
					}
					assert URIs.isCollectionURI(adHocNamespaceRelativeURI); //sub-namespaces are collections as well
					final String[] adHocRawPathSegments = adHocNamespaceRelativeURI.getRawPath().split(String.valueOf(PATH_SEPARATOR));
					assert adHocRawPathSegments.length > 0; //otherwise, the path would have been empty above 
					final StringBuilder handleBuilder = new StringBuilder(); //add segments as needed
					for(final String adHocRawPathSegment : adHocRawPathSegments) {
						final String segmentToken = URIs.decode(adHocRawPathSegment); //decode each segment
						if(!Name.isValidToken(segmentToken)) { //if part of a segment isn't a name token, there can be no handle
							return Optional.empty();
						}
						handleBuilder.append(segmentToken).append(SEGMENT_DELIMITER); //"token-"
					}
					handleBuilder.append(name); //"name"
					return Optional.of(handleBuilder.toString());
				});
			});
		}

		/**
		 * Produces the tag that a handle represents.
		 * @apiNote This method does not support formal namespaces.
		 * @param handle The handle for which a tag should be determined.
		 * @return The tag that the given handle represents.
		 * @throws NullPointerException if the given handle is <code>null</code>.
		 * @throws IllegalArgumentException if the given string is not a valid handle.
		 * @see #PATTERN
		 */
		public static URI toTag(@Nonnull final String handle) {
			return toTag(handle, emptyMap());
		}

		/**
		 * Produces the tag that a handle represents.
		 * @param handle The handle for which a tag should be determined.
		 * @param namespaces The registered namespaces, associated with their aliases.
		 * @return The tag that the given handle represents.
		 * @throws NullPointerException if the given handle and/or namespaces map is <code>null</code>.
		 * @throws IllegalArgumentException if the given string is not a valid handle.
		 * @throws IllegalArgumentException if the given handle refers to an unregistered namespace alias.
		 * @see #PATTERN
		 */
		public static URI toTag(@Nonnull final String handle, @Nonnull final Map<String, URI> namespaces) {
			final Matcher matcher = checkArgumentMatchesValid(handle);
			final String namespaceAlias = matcher.group(PATTERN_NAMESPACE_ALIAS_GROUP);
			final URI namespace = namespaceAlias == null ? AD_HOC_NAMESPACE : namespaces.get(namespaceAlias);
			checkArgument(namespace != null, "Unregistered namespace alias %s in handle %s.", namespaceAlias, handle);
			final String[] handleSegments = matcher.group(PATTERN_SEGEMENTS_GROUP).split(String.valueOf(SEGMENT_DELIMITER));
			assert handleSegments.length > 0; //a valid handle always has at least one segment
			checkArgument(handleSegments.length == 1 || namespaceAlias == null, "Handles with multiple segments %s only allowed in the ad-hoc namespace.", handle);
			final StringBuilder adHocRelativePathBuilder = new StringBuilder();
			for(final String handleSegment : handleSegments) {
				if(adHocRelativePathBuilder.length() > 0) {
					adHocRelativePathBuilder.append(PATH_SEPARATOR); ///
				}
				//TODO determine whether to encode for IRI
				adHocRelativePathBuilder.append(handleSegment); //token
			}
			final String id = matcher.group(PATTERN_ID_GROUP); //TODO determine whether/how the ID should be encoded
			if(id != null) {
				adHocRelativePathBuilder.append(FRAGMENT_SEPARATOR).append(id);
			}
			return namespace.resolve(adHocRelativePathBuilder.toString());
		}

	}

}
