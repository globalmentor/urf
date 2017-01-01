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

package io.urf.surf.parser;

import static java.util.Objects.*;

import java.util.Optional;

import javax.annotation.*;

/**
 * A data model representing the contents of a SURF document.
 * @author Garret Wilson
 */
public class SurfDocument {

	/** The root URF object in the document. */
	private Object documentObject;

	/** @return The root URF object in the document. */
	public Optional<Object> getDocumentObject() {
		return Optional.ofNullable(documentObject);
	}

	/**
	 * Sets the root URF object.
	 * @param documentObject The root URF object in the document.
	 * @throws NullPointerException if the given document object is <code>null</code>.
	 */
	public void setDocumentObject(@Nonnull final Object documentObject) {
		this.documentObject = requireNonNull(documentObject);
	}

	/** Empty document constructor. */
	public SurfDocument() {
		this.documentObject = null;
	}

	/**
	 * Document object constructor.
	 * @param documentObject The root URF object in the document.
	 * @throws NullPointerException if the given document object is <code>null</code>.
	 */
	public SurfDocument(@Nonnull final Object documentObject) {
		this.documentObject = requireNonNull(documentObject);
	}

}
