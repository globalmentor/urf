/*
 * Copyright © 2016 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.surf.model;

import java.util.*;

import javax.annotation.*;

import com.globalmentor.model.NameValuePair;

/**
 * Simple access to an URF resource description.
 * @author Garret Wilson
 */
public interface SimpleUrfResource extends Resource { //TODO probably transfer to model package

	/** @return The name of the resource type if known. */
	public Optional<String> getTypeName();

	/** @return The number of properties this resource has. */
	public int getPropertyCount();

	/**
	 * Retrieves the value of a property by the property name.
	 * 
	 * @param propertyName The name of the property.
	 * @return The value of the property, if any.
	 */
	public Optional<Object> getPropertyValue(@Nonnull String propertyName);

	/**
	 * Sets a property value by the property name.
	 * 
	 * @param propertyName The name of the property.
	 * @param value The new value of the property.
	 * @return The previous value of the property, if any.
	 */
	public Optional<Object> setPropertyValue(@Nonnull String propertyName, @Nonnull Object value);

	/** @return An iterable to the object's named properties and their values. */
	public Iterable<NameValuePair<String, Object>> getPropertyNameValuePairs();

	/**
	 * Sets property values from a map of property names and values. Neither <code>null</code> property names nor <code>null</code> property values are allowed.
	 * 
	 * @param properties The map of properties and values.
	 * @throws NullPointerException if one or more of the given property names or values is <code>null</code>.
	 */
	public default void setPropertyValues(@Nonnull final Map<String, ?> properties) {
		properties.forEach(this::setPropertyValue);
	}
}
