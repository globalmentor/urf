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

package io.urf.model;

import java.net.URI;

import javax.annotation.*;

import io.urf.URF;

/**
 * An implementation of an URF resource that provides access to its own description via a graph of properties.
 * @author Garret Wilson
 */
public class UrfObject extends AbstractDescribedUrfResource {

	/** Constructor of a resource with no tag and an unknown type. */
	public UrfObject() {
		this(null, (URI)null);
	}

	/**
	 * Optional tag constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public UrfObject(@Nullable final URI tag) {
		this(tag, (URI)null);
	}

	/**
	 * Optional tag and optional type handle constructor.
	 * @param tag The identifying object tag, or <code>null</code> if not known.
	 * @param typeHandle The handle of the object type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute URI.
	 * @throws IllegalArgumentException if the given type handle is not a valid URF handle.
	 */
	public UrfObject(@Nullable final URI tag, @Nullable final String typeHandle) {
		this(tag, typeHandle != null ? URF.Handle.toTag(typeHandle) : null);
	}

	/**
	 * Optional type handle constructor.
	 * @param typeHandle The handle of the object type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if the given type handle is not a valid URF handle.
	 * @deprecated This constructor is too confusing; it makes it seem like a handle is being used for the tag.
	 */
	@Deprecated
	public UrfObject(@Nullable final String typeHandle) {
		this((URI)null, typeHandle);
	}

	/**
	 * Optional tag and optional type name constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public UrfObject(@Nullable final URI tag, @Nullable final URI typeTag) {
		super(tag, typeTag);
	}

}
