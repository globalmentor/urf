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

import static io.urf.URF.*;

import java.net.URI;

import javax.annotation.*;

import io.urf.URF;

/**
 * A base URF processor that knows how to create different types of resources based upon the type.
 * @param <R> The type of result returned by the processor.
 * @author Garret Wilson
 */
public abstract class BaseUrfProcessor<R> extends AbstractUrfProcessor<R> {

	//TODO improve names; document

	/**
	 * TODO
	 * @implSpec This implementation calls special methods for the following special recognized types:
	 *           <dl>
	 *           <dt>{@link #createListResource(URI, URI)}</dt>
	 *           <dd>{@link URF#LIST_TYPE_TAG}</dd>
	 *           <dt>{@link #createMapResource(URI, URI)}</dt>
	 *           <dd>{@link URF#MAP_TYPE_TAG}</dd>
	 *           <dt>{@link #createSetResource(URI, URI)}</dt>
	 *           <dd>{@link URF#SET_TYPE_TAG}</dd>
	 *           <dt>{@link #createListResource(URI, URI)}</dt>
	 *           <dd>{@link URF#LIST_TYPE_TAG}</dd>
	 *           </dl>
	 * @param tag The tag of the resource to create, or <code>null</code> if the resource has no tag.
	 * @param typeTag The type of the resource to crate, or <code>null</code> if the resource should have no type.
	 * @return A new instance for the processor to use to represent the resource.
	 */
	protected UrfResource createResource(@Nullable final URI tag, @Nullable final URI typeTag) { //TODO refactor all these methods; perhaps discard class
		if(typeTag != null) {
			if(typeTag.equals(LIST_TYPE_TAG)) { //TODO create isList(), etc. methods for flexibility; then make method final?
				return createListResource(tag, typeTag);
			} else if(typeTag.equals(MAP_TYPE_TAG)) {
				return createMapResource(tag, typeTag);
			} else if(typeTag.equals(SET_TYPE_TAG)) {
				return createSetResource(tag, typeTag);
			}
		}
		return createDefaultResource(tag, typeTag);
	}

	protected UrfResource createDefaultResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		return new SimpleUrfResource(tag, typeTag);
	}

	protected UrfResource createListResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		return createDefaultResource(tag, typeTag);
	}

	protected UrfResource createMapResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		return createDefaultResource(tag, typeTag);
	}

	protected UrfResource createSetResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		return createDefaultResource(tag, typeTag);
	}

}
