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

package io.urf.surf;

import static io.urf.surf.SURF.*;
import static java.util.Objects.*;

import java.net.URI;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.collections.iterables.ConverterIterable;

/**
 * A SURF object for a SURF document.
 * <p>
 * SURF objects are considered equal if their tags, type handles, IDs, and property handles and values are equal.
 * </p>
 * @author Garret Wilson
 */
public class SurfObject {

	/** The identifying object tag, or <code>null</code> if not known. */
	private final URI tag;

	/** @return The object identifier tag. */
	public Optional<URI> getTag() {
		return Optional.ofNullable(tag);
	}

	/** The handle of the object type, or <code>null</code> if not known. */
	private final String typeHandle;

	/** @return The handle of the object type if known. */
	public Optional<String> getTypeHandle() {
		return Optional.ofNullable(typeHandle);
	}

	/** The ID of the object, or <code>null</code> if the object has no ID. */
	private final String id;

	/**
	 * Returns the ID, if any, of the object for its type. Objects with no type never have an ID.
	 * @return The ID, if any, of the object.
	 */
	public Optional<String> getId() {
		return Optional.ofNullable(id);
	}

	private final Map<String, Object> properties = new HashMap<>();

	/** @return The number of properties this object has. */
	public int getPropertyCount() {
		return properties.size();
	}

	/**
	 * Retrieves the value of a property by the property handle.
	 * 
	 * @param propertyHandle The handle of the property.
	 * @return The value of the property, if any.
	 */
	public Optional<Object> getPropertyValue(final String propertyHandle) {
		return Optional.ofNullable(properties.get(requireNonNull(propertyHandle)));
	}

	/**
	 * Sets a property value by the property handle.
	 * 
	 * @param propertyHandle The handle of the property.
	 * @param value The new value of the property.
	 * @return The previous value of the property, if any.
	 * @throws IllegalArgumentException if the given property handle does not conform to the rules for a SURF handle.
	 */
	public Optional<Object> setPropertyValue(final String propertyHandle, final Object value) {
		return Optional.ofNullable(properties.put(Handle.checkArgumentValid(propertyHandle), requireNonNull(value)));
	}

	/** @return An iterable to the object's properties by handle and their associated values. */
	public Iterable<Map.Entry<String, Object>> getProperties() {
		return new ConverterIterable<>(properties.entrySet(), AbstractMap.SimpleImmutableEntry::new);
	}

	/** Removes all properties. */
	public void clearProperties() {
		properties.clear();
	}

	/**
	 * Sets property values from a map of property handles and values. Neither <code>null</code> property handles nor <code>null</code> property values are
	 * allowed.
	 * 
	 * @param properties The map of property handles and values.
	 * @throws NullPointerException if one or more of the given property handles or values is <code>null</code>.
	 * @throws IllegalArgumentException if one of the given property handles does not conform to the rules for a SURF handle.
	 */
	public void setProperties(@Nonnull final Map<String, ?> properties) {
		properties.forEach(this::setPropertyValue);
	}

	/** Constructor of a object with no tag and an unknown type. */
	public SurfObject() {
		this((URI)null, null);
	}

	/**
	 * Optional tag constructor.
	 * @param tag The identifying object tag, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public SurfObject(@Nullable final URI tag) {
		this(tag, null);
	}

	/**
	 * Optional tag and optional type handle constructor.
	 * @param tag The identifying object tag, or <code>null</code> if not known.
	 * @param typeHandle The handle of the object type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute URI.
	 * @throws IllegalArgumentException if the given type handle is not a valid SURF handle.
	 */
	public SurfObject(@Nullable final URI tag, @Nullable final String typeHandle) {
		this.tag = tag != null ? Tag.checkArgumentValid(tag) : null;
		this.typeHandle = typeHandle != null ? Handle.checkArgumentValid(typeHandle) : null;
		this.id = null;
	}

	/**
	 * Optional type handle constructor.
	 * @param typeHandle The handle of the object type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if the given type handle is not a valid SURF handle.
	 */
	public SurfObject(@Nullable final String typeHandle) {
		this((URI)null, typeHandle);
	}

	/**
	 * Type handle and optional ID constructor.
	 * @param typeHandle The handle of the object type.
	 * @param id The object identifier unique for the object type.
	 * @throws NullPointerException if the given type handle is <code>null</code>.
	 * @throws IllegalArgumentException if the given type handle is not a valid SURF handle.
	 */
	public SurfObject(@Nonnull final String typeHandle, @Nullable final String id) {
		this.tag = null;
		this.typeHandle = Handle.checkArgumentValid(typeHandle);
		this.id = id;
	}

	@Override
	public int hashCode() {
		//calculate the hash code of the properties similar to how a normal hash map would, but don't recursively
		//get the hash code of compound objects to prevent infinite recursive hashing for circular references
		int propertiesHashCode = 0;
		for(final Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
			final Object propertyValue = propertyEntry.getValue();
			final int propertyValueHashCode;
			if(propertyValue instanceof SurfObject) {
				final SurfObject surfObjectPropertyValue = (SurfObject)propertyValue;
				//don't recursively get hash codes of SurfObjet property values
				propertyValueHashCode = Objects.hash(surfObjectPropertyValue.tag, surfObjectPropertyValue.typeHandle, surfObjectPropertyValue.id,
						surfObjectPropertyValue.properties.size());
			} else if(propertyValue instanceof Collection) {
				propertyValueHashCode = ((Collection<?>)propertyValue).size();
			} else if(propertyValue instanceof Map) {
				propertyValueHashCode = ((Map<?, ?>)propertyValue).size();
			} else {
				propertyValueHashCode = propertyValue.hashCode();
			}
			propertiesHashCode += propertyEntry.getKey().hashCode() ^ propertyValueHashCode;
		}
		return Objects.hash(tag, typeHandle, id, propertiesHashCode);
	}

	@Override
	public boolean equals(final Object object) {
		if(this == object) {
			return true;
		}
		if(!(object instanceof SurfObject)) {
			return false;
		}
		final SurfObject surfObject = (SurfObject)object;
		return getTag().equals(surfObject.getTag()) && getTypeHandle().equals(surfObject.getTypeHandle()) && getId().equals(surfObject.getId())
				&& properties.equals(surfObject.properties);
	}
}
