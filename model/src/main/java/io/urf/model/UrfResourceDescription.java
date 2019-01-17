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

package io.urf.model;

import java.net.URI;
import java.util.*;

import javax.annotation.*;

import io.urf.URF.Handle;
import io.urf.URF.Tag;

/**
 * Provides a description of a resource via a graph of properties.
 * @author Garret Wilson
 */
public interface UrfResourceDescription {

	/** @return The number of unique properties the resource has. */
	public int getPropertyCount();

	/** @return <code>true</code> if the resource has at least one property. */
	public default boolean hasProperties() {
		return getPropertyCount() > 0;
	}

	/** @return The number of unique properties the resource has. */
	public int getPropertyValueCount();

	/**
	 * Retrieves all values of a property by the property tag, whether the property is a binary property or an n-ary property. If the property does not exist, an
	 * empty set is returned.
	 * @param propertyTag The tag of the property.
	 * @return The values of the property.
	 * @throws NullPointerException if the given property tag is <code>null</code>.
	 * @throws IllegalArgumentException if the given URI is not a valid tag.
	 */
	public Set<Object> getPropertyValues(@Nonnull URI propertyTag);

	/**
	 * Retrieves all values of a property by the property handle, whether the property is a binary property or an n-ary property. If the property does not exist,
	 * an empty set is returned.
	 * @implSpec The default implementation delegates to {@link #getPropertyValues(URI)}.
	 * @param propertyHandle The handle of the property.
	 * @return The values of the property.
	 * @throws NullPointerException if the given property handle is <code>null</code>.
	 * @throws IllegalArgumentException if the given string is not a valid handle.
	 * @see #getPropertyValues(URI)
	 */
	public default Set<Object> getPropertyValuesByHandle(@Nonnull String propertyHandle) {
		return getPropertyValues(Handle.toTag(propertyHandle));
	}

	/**
	 * Retrieves the value of a property by the property tag. If the property is an n-ary property, there it is undefined which of the property values will be
	 * returned.
	 * @param propertyTag The tag of the property.
	 * @return The value of the property, if any.
	 * @throws NullPointerException if the given property tag is <code>null</code>.
	 * @throws IllegalArgumentException if the given URI is not a valid tag.
	 */
	public Optional<Object> findPropertyValue(@Nonnull URI propertyTag);

	/**
	 * Retrieves the value of a property by the property handle.
	 * @implSpec The default implementation delegates to {@link #findPropertyValue(URI)}.
	 * @param propertyHandle The handle of the property.
	 * @return The value of the property, if any.
	 * @throws NullPointerException if the given property handle is <code>null</code>.
	 * @throws IllegalArgumentException if the given string is not a valid handle.
	 * @see #findPropertyValue(URI)
	 */
	public default Optional<Object> findPropertyValueByHandle(@Nonnull String propertyHandle) {
		return findPropertyValue(Handle.toTag(propertyHandle));
	}

	/**
	 * Sets a property value by the property tag. The new value will replace any and all values of the property, even for n-arity properties.
	 * @param propertyTag The name of the property.
	 * @param propertyValue The new value of the property.
	 * @return The previous value of the property, if any; if the property is n-ary, it is undefined which property value will be returned.
	 * @throws NullPointerException if the given property tag and/or property value is <code>null</code>.
	 * @throws IllegalArgumentException if the given URI is not a valid tag.
	 */
	public Optional<Object> setPropertyValue(@Nonnull URI propertyTag, @Nonnull Object propertyValue);

	/**
	 * Sets a property value by the property handle.
	 * @implSpec The default implementation delegates to {@link #setPropertyValue(URI, Object)}.
	 * @param propertyHandle The handle of the property.
	 * @param propertyValue The new value of the property.
	 * @return The previous value of the property, if any.
	 * @throws NullPointerException if the given property property tag and/or property value is <code>null</code>.
	 * @throws IllegalArgumentException if the given string is not a valid handle.
	 * @see #setPropertyValue(URI, Object)
	 */
	public default Optional<Object> setPropertyValueByHandle(@Nonnull String propertyHandle, @Nonnull Object propertyValue) {
		return setPropertyValue(Handle.toTag(propertyHandle), propertyValue);
	}

	/**
	 * Adds an property value. Any existing value for the property will not change.
	 * @param propertyTag The name of the property.
	 * @param propertyValue The additional value of the property.
	 * @return <code>true</code> if the added value was unique.
	 * @throws NullPointerException if the given property tag and/or property value is <code>null</code>.
	 * @throws IllegalArgumentException if the given URI is not a valid tag.
	 * @throws IllegalStateException if object the already has a value for the given property and the property is not an n-ary property.
	 * @see Tag#isNary(URI)
	 */
	public boolean addPropertyValue(@Nonnull URI propertyTag, @Nonnull Object propertyValue);

	/**
	 * Adds an property value by the property handle. Any existing value for the property will not change.
	 * @implSpec The default implementation delegates to {@link #addPropertyValue(URI, Object)}.
	 * @param propertyHandle The handle of the property.
	 * @param propertyValue The additional value of the property.
	 * @return <code>true</code> if the added value was unique.
	 * @throws NullPointerException if the given property handle and/or property value is <code>null</code>.
	 * @throws IllegalArgumentException if the given string is not a valid handle.
	 * @throws IllegalStateException if object the already has a value for the given property and the property is not an n-ary property.
	 * @see #addPropertyValue(URI, Object)
	 */
	public default boolean addPropertyValueByHandle(@Nonnull String propertyHandle, @Nonnull Object propertyValue) {
		return addPropertyValue(Handle.toTag(propertyHandle), propertyValue);
	}

	/**
	 * Merges a property and value into the object. If the property is an n-ary property, the property value will be added to whatever value (if any) the object
	 * has for the property. If the property is a non n-ary property (a binary property), the value (if any) will be replaced with the given value.
	 * @implSpec This method function equivalently to calling {@link #addPropertyValue(URI, Object)} if the property is an n-ary property; otherwise calling
	 *           {@link #addPropertyValue(URI, Object)}.
	 * @param propertyTag The name of the property.
	 * @param propertyValue The value of the property to add or set.
	 * @return <code>true</code> if the merged value resulted in a change.
	 * @throws NullPointerException if the given property tag and/or property value is <code>null</code>.
	 * @throws IllegalArgumentException if the given URI is not a valid tag.
	 * @see Tag#isNary(URI)
	 * @see #setPropertyValue(URI, Object)
	 * @see #addPropertyValue(URI, Object)
	 */
	public default boolean mergePropertyValue(@Nonnull URI propertyTag, @Nonnull Object propertyValue) {
		if(Tag.isNary(propertyTag)) {
			return addPropertyValue(propertyTag, propertyValue);
		} else {
			final Object previousValue = setPropertyValue(propertyTag, propertyValue);
			return !propertyValue.equals(previousValue);
		}
	}

	/**
	 * Merges a property and value into the object by the property handle. If the property is an n-ary property, the property value will be added to whatever
	 * value (if any) the object has for the property. If the property is a non n-ary property (a binary property), the value (if any) will be replaced with the
	 * given value.
	 * @implSpec The default implementation delegates to {@link #addPropertyValue(URI, Object)}.
	 * @param propertyHandle The handle of the property.
	 * @param propertyValue The value of the property to add or set.
	 * @return <code>true</code> if the merged value resulted in a change.
	 * @throws NullPointerException if the given property handle and/or property value is <code>null</code>.
	 * @throws IllegalArgumentException if the given string is not a valid handle.
	 * @see #mergePropertyValue(URI, Object)
	 */
	public default boolean mergePropertyValueByHandle(@Nonnull String propertyHandle, @Nonnull Object propertyValue) {
		return mergePropertyValue(Handle.toTag(propertyHandle), propertyValue);
	}

	/** @return An iterable to the resource's tagged properties and their values. */
	//TODO add, but use name that won't be confused with URF property "name": public Iterable<NameValuePair<String, Object>> getPropertyNameValuePairs();

	/**
	 * Sets property values from a map of property tags and values. Neither <code>null</code> property tags nor <code>null</code> property values are allowed.
	 * @param properties The map of properties and values.
	 * @throws NullPointerException if one or more of the given property tags or property values is <code>null</code>.
	 */
	public default void setPropertyValues(@Nonnull final Map<URI, ?> properties) {
		properties.forEach(this::setPropertyValue);
	}

}
