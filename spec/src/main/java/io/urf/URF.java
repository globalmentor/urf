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
import static java.util.Objects.*;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.*;

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

	/** The delimiter that may appear in an URF name to indicate the start of the ID, if any. */
	public static final char NAME_ID_DELIMITER = '#';

	/**
	 * Utilities for working with URF names.
	 * @author Garret Wilson
	 */
	public static final class Name {

		/** Regular expression pattern to match an URF name token . */
		public static final Pattern TOKEN_PATTERN = Pattern.compile("\\p{L}[\\p{L}\\p{M}\\p{N}\\p{Pc}]*"); //TODO add test

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

		/** Regular expression pattern to match an URF name . */
		public static final Pattern PATTERN = Pattern.compile(String.format("(%s)(?:%s(.*))?", TOKEN_PATTERN, NAME_ID_DELIMITER)); //TODO add test; document matching groups

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
			return checkAbsolute(tag);
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
		public static Optional<String> getName(final URI tag) {
			checkArgumentValid(tag);
			final String rawPath = tag.getRawPath();
			if(rawPath != null && !rawPath.isEmpty() && !endsWith(rawPath, PATH_SEPARATOR)) { //if there is a raw path that isn't a collection
				String name = decode(URIs.getName(rawPath));
				final String rawFragment = tag.getRawFragment();
				if(rawFragment != null) { //see if there is an ID to append
					name = name + NAME_ID_DELIMITER + decode(rawFragment);
				}
				if(Name.isValid(name)) { //make sure the supposed name is valid
					return Optional.of(name);
				}
			}
			return Optional.empty();
		}

		/**
		 * Retrieves the resource ID from the given tag. The name is the decoded fragment of the URI, if any.
		 * <p>
		 * Not every tag has a name. For example, a tag URI without a path has no ID.
		 * </p>
		 * @param tag The tag URI from which an ID should be retrieved.
		 * @return The local name of the given URI, or <code>null</code> if the URI has no path or the path ends with a path separator.
		 * @throws NullPointerException if the given URI is <code>null</code>.
		 */
		public static Optional<String> getId(final URI tag) {
			checkArgumentValid(tag);
			final String rawFragment = tag.getRawFragment();
			if(rawFragment != null) {
				return Optional.of(decode(rawFragment)); //decode the fragment manually for consistency and for better error-handling
			}
			return Optional.empty();
		}

	}

}
