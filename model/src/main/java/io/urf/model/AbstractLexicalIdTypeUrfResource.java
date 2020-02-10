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

import static com.globalmentor.java.Conditions.*;
import static java.util.Objects.*;

import java.net.URI;
import java.util.Optional;

import javax.annotation.*;

import io.urf.URF;
import io.urf.URF.Name;
import io.urf.URF.Tag;

/**
 * Abstract base class for an URF resource that has lexical ID type. The tag and type tag are dynamically generated based upon the type and ID representation.
 * <p>
 * All implementations of this class must return an ID in {@link #getLexicalId()}.
 * </p>
 * @author Garret Wilson
 */
public abstract class AbstractLexicalIdTypeUrfResource extends AbstractUrfResource {

	private final URI typeNamespace;

	private final String typeName;

	@Override
	public Optional<URI> getTag() {
		return Optional.of(URF.Tag.forTypeId(typeNamespace, typeName, getLexicalId()));
	}

	/** {@inheritDoc} This implementation calls {@link #getLexicalId()}. */
	@Override
	public Optional<String> getName() {
		return Optional.of(Name.forTypeId(typeName, getLexicalId()));
	}

	/** {@inheritDoc} This implementation calls {@link #getLexicalId()}. */
	@Override
	public final Optional<String> getId() {
		return Optional.of(getLexicalId());
	}

	@Override
	public Optional<URI> getTypeTag() {
		return Optional.of(URF.Tag.forType(typeNamespace, typeName));
	}

	/**
	 * Type tag constructor. Type namespace and name will be extracted from the tag.
	 * @param typeTag The resource type tag.
	 * @throws IllegalArgumentException if the given type tag has no namespace and/or name, or has an ID.
	 */
	public AbstractLexicalIdTypeUrfResource(@Nonnull final URI typeTag) {
		this(Tag.findNamespace(typeTag).orElseThrow(IllegalArgumentException::new), Tag.findName(typeTag).orElseThrow(IllegalArgumentException::new));
		checkArgument(!Tag.findId(typeTag).isPresent(), "Type tag may not contain ID.");
	}

	/**
	 * Type namespace and type name constructor.
	 * 
	 * @param typeNamespace The resource type namespace.
	 * @param typeName The resource type name.
	 */
	public AbstractLexicalIdTypeUrfResource(@Nonnull final URI typeNamespace, @Nonnull final String typeName) {
		this.typeNamespace = requireNonNull(typeNamespace);
		this.typeName = requireNonNull(typeName);
	}

	/**
	 * Returns the lexical form of this resource for its type.
	 * @apiNote The lexical form is equivalent to the non-optional ID of a lexical ID type resource.
	 * @return The lexical form of the resource.
	 */
	public abstract String getLexicalId();

}
