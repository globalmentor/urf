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

import java.net.URI;

import javax.annotation.Nonnull;

/**
 * The default implementation of a value URF resource.
 * <p>
 * <strong>Important:</strong> This implementation uses the value object's {@link Object#toString()} value as the resource ID. If this is not appropriate,
 * extend {@link AbstractValueUrfResource} and override {@link AbstractValueUrfResource#getIdImpl()}.
 * </p>
 * @param <T> The type of value the resource contains.
 * @author Garret Wilson
 */
public class DefaultValueUrfResource<T> extends AbstractValueUrfResource<T> {

	/**
	 * Type tag constructor. Type namespace and name will be extracted from the tag.
	 * @param typeTag The resource type tag.
	 * @param value The value object encapsulated by this resource.
	 * @throws IllegalArgumentException if the given type tag has no namespace and/or name, or has an ID.
	 */
	public DefaultValueUrfResource(@Nonnull final URI typeTag, @Nonnull final T value) {
		super(typeTag, value);
	}

	/**
	 * Type namespace and type name constructor.
	 * 
	 * @param typeNamespace The resource type namespace.
	 * @param typeName The resource type name.
	 * @param value The value object encapsulated by this resource.
	 */
	public DefaultValueUrfResource(@Nonnull final URI typeNamespace, @Nonnull final String typeName, @Nonnull final T value) {
		super(typeNamespace, typeName, value);
	}

	/**
	 * {@inheritDoc} This version delegates to the value's {@link Object#toString()} result.
	 * @see #getValue()
	 */
	@Override
	protected final String getIdImpl() {
		return getValue().toString();
	}

}
