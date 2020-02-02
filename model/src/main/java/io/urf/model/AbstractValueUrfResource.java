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

import javax.annotation.Nonnull;

/**
 * An abstract implementation of a value URF resource.
 * @param <T> The type of value the resource contains.
 * @author Garret Wilson
 */
public abstract class AbstractValueUrfResource<T> extends AbstractLexicalIdTypeUrfResource implements ValueUrfResource<T> {

	private final T value;

	@Override
	public T getObject() {
		return value;
	}

	/**
	 * Type tag constructor. Type namespace and name will be extracted from the tag.
	 * @param typeTag The resource type tag.
	 * @param value The value object encapsulated by this resource.
	 * @throws IllegalArgumentException if the given type tag has no namespace and/or name, or has an ID.
	 */
	public AbstractValueUrfResource(@Nonnull final URI typeTag, @Nonnull final T value) {
		super(typeTag);
		this.value = requireNonNull(value);
	}

	/**
	 * Type namespace and type name constructor.
	 * 
	 * @param typeNamespace The resource type namespace.
	 * @param typeName The resource type name.
	 * @param value The value object encapsulated by this resource.
	 */
	public AbstractValueUrfResource(@Nonnull final URI typeNamespace, @Nonnull final String typeName, @Nonnull final T value) {
		super(typeNamespace, typeName);
		this.value = requireNonNull(value);
	}

}
