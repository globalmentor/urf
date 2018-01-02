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

import static com.globalmentor.net.URIs.*;

import java.net.URI;
import java.util.Optional;

import javax.annotation.*;

import com.globalmentor.net.URIs;

import io.urf.URF;

/**
 * Definitions for the Text URF (SURF) document format.
 * @author Garret Wilson
 */
public class TURF {

	/**
	 * Utilities for working with TURF handles.
	 * @author Garret Wilson
	 */
	public static final class Handle {

		/** The delimiter used to separate ad-hoc namespaces in a TURF handle. */
		public static final char SEGMENT_DELIMITER = '-';

		/**
		 * Confirms that the given string conforms to the rules for a TURF handle.
		 * @param string The string to check.
		 * @return The given string.
		 * @throws NullPointerException if the given string is <code>null</code>.
		 * @throws IllegalArgumentException if the given string does not conform to the rules for a TURF handle.
		 */
		//TODO * @see #PATTERN
		public static String checkArgumentValid(final String string) {
			//TODO implement checkArgument(isValid(string), "Invalid TURF handle \"%s\".", string);
			return string;
		}

		//TODO decide whether encoding/decoding is needed, as IRIs are used

		/**
		 * Determines the TURF handle to represent the given resource tag.
		 * <p>
		 * Not every tag has a handle. A tag with no namespace or no name has no handle.
		 * </p>
		 * <p>
		 * This implementation does not yet support formal namespaces.
		 * </p>
		 * @param tag The tag for which a handle should be determined.
		 * @return The TURF handle representing the given tag.
		 * @throws NullPointerException if the given tag is <code>null</code>.
		 * @throws IllegalArgumentException if the given URI is not a valid tag.
		 */
		public static Optional<String> fromTag(@Nonnull final URI tag) {
			URF.Tag.checkArgumentValid(tag);
			final Optional<String> optionalName = URF.Tag.getName(tag);
			//if there is a name, convert it to a handle based on the namespace
			return optionalName.flatMap(name -> {
				//if there is a namespace, use it to convert the name to a handle
				return URF.Tag.getNamespace(tag).flatMap(namespace -> {
					final URI adHocNamespaceRelativeURI = URF.AD_HOC_NAMESPACE.relativize(namespace);
					if(adHocNamespaceRelativeURI.equals(namespace)) { //if the namespace is not relative to the ad-hoc namespace
						return Optional.empty(); //there is no handle TODO add support for namespace prefixes
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
						if(!URF.Name.isValidToken(segmentToken)) { //if part of a segment isn't a name token, there can be no handle
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
		 * <p>
		 * This implementation does not yet support formal namespaces.
		 * </p>
		 * @param handle The handle for which a tag should be determined.
		 * @return The tag that the given handle represents.
		 * @throws NullPointerException if the given handle is <code>null</code>.
		 * @throws IllegalArgumentException if the given string is not a valid handle.
		 */
		public static URI toTag(@Nonnull final String handle) {
			checkArgumentValid(handle);
			//TODO add support for namespace prefixes
			final String[] handleSegments = handle.split(String.valueOf(SEGMENT_DELIMITER));
			assert handleSegments.length > 0; //a valid handle always has at least one segment
			final StringBuilder adHocRelativePathBuilder = new StringBuilder();
			for(final String handleSegment : handleSegments) {
				if(adHocRelativePathBuilder.length() > 0) {
					adHocRelativePathBuilder.append(PATH_SEPARATOR); ///
				}
				//TODO determine whether to encode for IRI
				adHocRelativePathBuilder.append(handleSegment); //token
			}
			return URF.AD_HOC_NAMESPACE.resolve(adHocRelativePathBuilder.toString());
		}

	}

}
