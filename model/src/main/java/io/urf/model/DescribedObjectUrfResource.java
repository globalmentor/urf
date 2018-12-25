/*
 * Copyright © 2018 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static java.util.Objects.*;

import java.net.URI;

import javax.annotation.*;

import io.urf.URF;

/**
 * An URF resource wrapping an object, along with a description.
 * @param <T> The type of object decorated by the URF resource.
 * @author Garret Wilson
 */
public class DescribedObjectUrfResource<T> extends AbstractDescribedUrfResource implements ObjectUrfResource<T> {

	private final T object;

	@Override
	public T getObject() {
		return object;
	}

	/**
	 * Constructor of a resource with no tag and an unknown type.
	 * @param object The object to be wrapped by this URF resource.
	 */
	public DescribedObjectUrfResource(@Nonnull final T object) {
		this(null, (URI)null, object);
	}

	/**
	 * Optional tag constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param object The object to be wrapped by this URF resource.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public DescribedObjectUrfResource(@Nullable final URI tag, @Nonnull final T object) {
		this(tag, (URI)null, object);
	}

	/**
	 * Optional tag and optional type handle constructor.
	 * @param tag The identifying object tag, or <code>null</code> if not known.
	 * @param typeHandle The handle of the object type, or <code>null</code> if not known.
	 * @param object The object to be wrapped by this URF resource.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute URI.
	 * @throws IllegalArgumentException if the given type handle is not a valid URF handle.
	 */
	public DescribedObjectUrfResource(@Nullable final URI tag, @Nullable final String typeHandle, @Nonnull final T object) {
		this(tag, typeHandle != null ? URF.Handle.toTag(typeHandle) : null, object);
	}

	/**
	 * Optional type handle constructor.
	 * @param typeHandle The handle of the object type, or <code>null</code> if not known.
	 * @param object The object to be wrapped by this URF resource.
	 * @throws IllegalArgumentException if the given type handle is not a valid URF handle.
	 * @deprecated This constructor is too confusing; it makes it seem like a handle is being used for the tag.
	 */
	@Deprecated
	public DescribedObjectUrfResource(@Nullable final String typeHandle, @Nonnull final T object) {
		this((URI)null, typeHandle, object);
	}

	/**
	 * Optional tag and optional type name constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @param object The object to be wrapped by this URF resource.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public DescribedObjectUrfResource(@Nullable final URI tag, @Nullable final URI typeTag, @Nonnull final T object) {
		super(tag, typeTag);
		this.object = requireNonNull(object);
	}

}
