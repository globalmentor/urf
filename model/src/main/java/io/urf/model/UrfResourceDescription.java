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

/**
 * Provides a description of a resource via a graph of properties.
 * @author Garret Wilson
 */
public interface UrfResourceDescription {

	/** @return The number of properties this resource has. */
	public int getPropertyCount();

	/**
	 * Retrieves the value of a property by the property tag.
	 * 
	 * @param propertyTag The tag of the property.
	 * @return The value of the property, if any.
	 * @throws NullPointerException if the given property tag is <code>null</code>.
	 */
	public Optional<Object> getPropertyValue(@Nonnull URI propertyTag);

	/**
	 * Sets a property value by the property tag.
	 * 
	 * @param propertyTag The name of the property.
	 * @param propertyValue The new value of the property.
	 * @return The previous value of the property, if any.
	 * @throws NullPointerException if the given property tag and/or property value is <code>null</code>.
	 */
	public Optional<Object> setPropertyValue(@Nonnull URI propertyTag, @Nonnull Object propertyValue);

	/** @return An iterable to the resource's tagged properties and their values. */
	//TODO add, but use name that won't be confused with URF property "name": public Iterable<NameValuePair<String, Object>> getPropertyNameValuePairs();

	/**
	 * Sets property values from a map of property tags and values. Neither <code>null</code> property tags nor <code>null</code> property values are allowed.
	 * 
	 * @param properties The map of properties and values.
	 * @throws NullPointerException if one or more of the given property tags or property values is <code>null</code>.
	 */
	public default void setPropertyValues(@Nonnull final Map<URI, ?> properties) {
		properties.forEach(this::setPropertyValue);
	}
}
