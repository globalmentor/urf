/*
 * Copyright © 2007-2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import java.net.URI;
import java.nio.charset.Charset;

import javax.annotation.*;

import com.globalmentor.net.ContentType;

/**
 * The URF content ontology.
 * @author Garret Wilson
 */
public class Content {

	/** The URI to the URF content namespace. */
	public static final URI NAMESPACE = URF.AD_HOC_NAMESPACE.resolve("content/");

	//#classes

	/** The URI of the <code>content-Charset</code> class. */
	public static final URI CHARSET_CLASS_TAG = NAMESPACE.resolve("Charset");
	/** The URI of the <code>content-ContentResource</code> class. */
	public static final URI CONTENT_RESOURCE_CLASS_TAG = NAMESPACE.resolve("ContentResource");
	/** The URI of the <code>content-MediaType</code> class. */
	public static final URI MEDIA_TYPE_CLASS_TAG = NAMESPACE.resolve("MediaType");
	/** The URI of the <code>content-Text</code> class. */
	public static final URI TEXT_CLASS_TAG = NAMESPACE.resolve("Text");

	//#properties

	/** The instant when a resource was last accessed. */
	public static final URI ACCESSED_PROPERTY_TAG = NAMESPACE.resolve("accessedAt");
	/** The charset of a resource. */
	public static final URI CHARSET_PROPERTY_TAG = NAMESPACE.resolve("charset");
	/** The instant when a resource was created. */
	public static final URI CREATED_PROPERTY_TAG = NAMESPACE.resolve("createdAt");
	/** The resource that created this resource atthe indicated created instant. */
	public static final URI CREATOR_PROPERTY_TAG = NAMESPACE.resolve("creator");
	/** The actual content, such as bytes or a string, of a resource. */
	public static final URI CONTENT_PROPERTY_TAG = NAMESPACE.resolve("content");
	/** The instant a resource was last modified. */
	public static final URI MODIFIED_PROPERTY_TAG = NAMESPACE.resolve("modifiedAt");
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
		 */
		public static URI forCharset(@Nonnull final Charset charset) {
			return URF.Tag.forTypeId(CHARSET_CLASS_TAG, charset.name());
		}

		/**
		 * Creates an ID tag for an instance of {@link ContentType}.
		 * @param mediaType The Internet media type to represent.
		 * @return A tag for the given media type.
		 * @see Content#MEDIA_TYPE_CLASS_TAG
		 * @see ContentType#getBaseContentType()
		 */
		public static URI forMediaType(@Nonnull final ContentType mediaType) {
			return URF.Tag.forTypeId(MEDIA_TYPE_CLASS_TAG, mediaType.getBaseType());
		}
	}

}
