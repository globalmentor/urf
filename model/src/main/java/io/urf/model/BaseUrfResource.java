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
import java.util.*;

import javax.annotation.*;

import io.urf.URF;

/**
 * A base abstract implementation of an URF resource providing storage for a tag a type.
 * @author Garret Wilson
 */
public abstract class BaseUrfResource extends AbstractUrfResource {

	private final URI tag;

	@Override
	public Optional<URI> getTag() {
		return Optional.ofNullable(tag);
	}

	private final URI typeTag;

	@Override
	public Optional<URI> getTypeTag() {
		return Optional.ofNullable(typeTag);
	}

	/**
	 * Optional tag and optional type tag constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public BaseUrfResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		this.tag = tag != null ? URF.Tag.checkArgumentValid(tag) : null;
		this.typeTag = typeTag != null ? URF.Tag.checkArgumentValid(typeTag) : null;
	}

}
