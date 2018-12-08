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

import java.util.Optional;

import javax.annotation.*;

/**
 * An URF resource that wraps an object, which may be a mutable collection or an immutable value object.
 * @param <T> The type of object decorated by the URF resource.
 * @author Garret Wilson
 */
public interface ObjectUrfResource<T> extends UrfResource {

	/**
	 * Unwraps an object that may be an object resource. If the given resource is an {@link ObjectUrfResource}, the resource's object is returned; otherwise, the
	 * object can't be unwrapped, and the object itself is returned.
	 * @param object The object to unwrap.
	 * @return The object or, if the object is an {@link ObjectUrfResource}, the object contained in the resource.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 * @see #getObject()
	 */
	public static Object unwrap(@Nonnull final Object object) {
		return requireNonNull(object) instanceof ObjectUrfResource ? ((ObjectUrfResource<?>)object).getObject() : object;
	}

	/**
	 * Returns the object of an {@link ObjectUrfResource}, if the given reference is an {@link ObjectUrfResource}.
	 * @param reference The reference to unwrap.
	 * @return If the object is an {@link ObjectUrfResource}, the object contained in the resource.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 * @see #getObject()
	 */
	public static Optional<Object> findObject(@Nonnull final UrfReference reference) {
		return requireNonNull(reference) instanceof ObjectUrfResource ? Optional.of(((ObjectUrfResource<?>)reference).getObject()) : Optional.empty();
	}

	/** @return The wrapped object. */
	public T getObject();

}
