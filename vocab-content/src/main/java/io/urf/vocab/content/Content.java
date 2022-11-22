/*
 * Copyright © 2007-2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.urf.vocab.content;

import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.util.Optionals.*;

import java.net.URI;
import java.nio.charset.Charset;

import javax.annotation.*;

import io.urf.URF;

/**
 * The URF content vocabulary.
 * @author Garret Wilson
 */
public class Content {

	/** The URI to the URF content namespace. */
	public static final URI NAMESPACE = URF.AD_HOC_NAMESPACE.resolve("content/");

	//# classes

	/** The URI of the <code>content-Charset</code> class. */
	public static final URI CHARSET_CLASS_TAG = NAMESPACE.resolve("Charset");
	/** The URI of the <code>content-ContentResource</code> class. */
	public static final URI CONTENT_RESOURCE_CLASS_TAG = NAMESPACE.resolve("ContentResource");
	/** The URI of the <code>content-Text</code> class. */
	public static final URI TEXT_CLASS_TAG = NAMESPACE.resolve("Text");

	//# properties

	/** The instant when a resource was last accessed. */
	public static final URI ACCESSED_AT_PROPERTY_TAG = NAMESPACE.resolve("accessedAt");
	/**
	 * The charset of a resource.
	 * @deprecated This property is experimental; as the charset is now normally part of the {@link #TYPE_PROPERTY_TAG} property, it is unclear whether a separate
	 *             property adds any value.
	 */
	@Deprecated
	public static final URI CHARSET_PROPERTY_TAG = NAMESPACE.resolve("charset");
	/** The instant when a resource was created. */
	public static final URI CREATED_AT_PROPERTY_TAG = NAMESPACE.resolve("createdAt");
	/**
	 * The resource that created this resource at the indicated created instant.
	 * @deprecated To be removed; it is unclear how or why it was added.
	 */
	@Deprecated
	public static final URI CREATOR_PROPERTY_TAG = NAMESPACE.resolve("creator");
	/**
	 * The bytes that identify the larger contents for modification detection.
	 * @see <a href="https://en.wikipedia.org/wiki/Fingerprint_(computing)">Fingerprint (computing)</a>
	 */
	public static final URI FINGERPRINT_PROPERTY_TAG = NAMESPACE.resolve("fingerprint");
	/** The instant a resource was last modified. */
	public static final URI MODIFIED_AT_PROPERTY_TAG = NAMESPACE.resolve("modifiedAt");
	/**
	 * The size of the contents of the resource. For <code>urf-Binary</code> content, this indicates the number of bytes. For <code>urf-String</code> content,
	 * this indicates the number of characters.
	 */
	public static final URI LENGTH_PROPERTY_TAG = NAMESPACE.resolve("length");
	/** The Internet media type of a resource. */
	public static final URI TYPE_PROPERTY_TAG = NAMESPACE.resolve("type");

	/**
	 * Utilities for working with TURF tags related to content.
	 * @author Garret Wilson
	 */
	public static final class Tag {

		/**
		 * Creates an ID tag for an instance of {@link Charset}.
		 * @param charset The charset to represent.
		 * @return A tag for the given charset.
		 * @see Content#CHARSET_CLASS_TAG
		 * @see Charset#name()
		 * @deprecated to be removed; as the charset is now normally part of the {@link Content#TYPE_PROPERTY_TAG} property, it is unclear whether a separate
		 *             property adds any value.
		 */
		@Deprecated
		public static URI fromCharset(@Nonnull final Charset charset) {
			return URF.Tag.forTypeId(CHARSET_CLASS_TAG, charset.name());
		}

		/**
		 * Determines the charset represented by the tag.
		 * @param tag The tag to convert to a charset.
		 * @return The tag indicated charset.
		 * @throws IllegalArgumentException if the given tag does not represent a valid charset.
		 * @see Content#CHARSET_CLASS_TAG
		 * @deprecated to be removed; as the charset is now normally part of the {@link Content#TYPE_PROPERTY_TAG} property, it is unclear whether a separate
		 *             property adds any value.
		 */
		@Deprecated
		public static Charset toCharset(@Nonnull final URI tag) throws IllegalArgumentException {
			checkArgument(isPresentAndEquals(URF.Tag.findIdTypeTag(tag), CHARSET_CLASS_TAG), "Tag %s not a charset tag.", tag);
			final String charsetName = URF.Tag.findId(tag).orElseThrow(() -> new IllegalArgumentException("Tag " + tag + " has no charset indicated."));
			return Charset.forName(charsetName);
		}

	}

	/**
	 * The URF content check vocabulary.
	 * @author Garret Wilson
	 */
	public static class Check {

		/** The URI to the URF content check namespace. */
		public static final URI NAMESPACE = Content.NAMESPACE.resolve("check/");

		//## properties

		/** MD2 message digest. */
		public static final URI MD2_PROPERTY_TAG = NAMESPACE.resolve("md2");
		/** MD5 message digest. */
		public static final URI MD5_PROPERTY_TAG = NAMESPACE.resolve("md5");
		/** SHA1 message digest. */
		public static final URI SHA1_PROPERTY_TAG = NAMESPACE.resolve("sha1");
		/**
		 * SHA2 message digest; differing size functions such as SHA-512/224 are not supported.
		 * @see <a href="https://doi.org/10.6028/NIST.FIPS.180-4">NIST FIPS 180-4: Secure Hash Standard (SHS)</a>
		 */
		public static final URI SHA2_PROPERTY_TAG = NAMESPACE.resolve("sha2");
		/**
		 * SHA3 message digest.
		 * @see <a href="https://doi.org/10.6028/NIST.FIPS.202">NIST FIPS 202: SHA-3 Standard: Permutation-Based Hash and Extendable-Output Functions</a>
		 */
		public static final URI SHA3_PROPERTY_TAG = NAMESPACE.resolve("sha3");
		/**
		 * A general checksum the result of using an unspecified algorithm.
		 * @apiNote The algorithm is not specified. It is assumed that a consistent algorithm will be used by the consumer, and an algorithm change will result in
		 *          the contents being considered modified.
		 */
		public static final URI SUM_PROPERTY_TAG = NAMESPACE.resolve("sum");

	}

}
