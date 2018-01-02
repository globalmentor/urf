/*
 * Copyright © 2016-2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.turf.model;

import java.net.URI;
import java.util.*;

import javax.annotation.*;

import io.urf.model.UrfObject;
import io.urf.turf.TURF;

/**
 * An URF object that knows how to work with TURF handles.
 * @author Garret Wilson
 */
public class TurfObject extends UrfObject {

	/**
	 * Retrieves the value of a property by the property handle.
	 * 
	 * @param propertyHandle The handle of the property.
	 * @return The value of the property, if any.
	 * @throws NullPointerException if the given property handle is <code>null</code>.
	 * @throws IllegalArgumentException if the given string is not a valid handle.
	 */
	public Optional<Object> getPropertyValue(@Nonnull String propertyHandle) {
		return getPropertyValue(TURF.Handle.toTag(propertyHandle));
	}

	/**
	 * Sets a property value by the property handle.
	 * 
	 * @param propertyHandle The handle of the property.
	 * @param propertyValue The new value of the property.
	 * @return The previous value of the property, if any.
	 * @throws NullPointerException if the given property property tag and/or property value is <code>null</code>.
	 * @throws IllegalArgumentException if the given string is not a valid handle.
	 */
	public Optional<Object> setPropertyValue(@Nonnull String propertyHandle, @Nonnull Object propertyValue) {
		return setPropertyValue(TURF.Handle.toTag(propertyHandle), propertyValue);
	}

	/** Constructor of a resource with no tag and an unknown type. */
	public TurfObject() {
		this(null, null);
	}

	/**
	 * Optional tag constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public TurfObject(@Nullable final URI tag) {
		this(tag, null);
	}

	/**
	 * Optional tag and optional type name constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public TurfObject(@Nullable final URI tag, @Nullable final URI typeTag) {
		super(tag, typeTag);
	}

}
