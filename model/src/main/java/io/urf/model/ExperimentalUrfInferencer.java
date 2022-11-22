/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.urf.model;

import static com.globalmentor.java.Conditions.*;
import static io.urf.URF.*;

import java.net.URI;
import java.util.*;

import javax.annotation.*;

import io.urf.URF;

/**
 * Inferencer implementation for testing ideas relating to inferencing.
 * @implSpec This implementation currently only supports {@link URF#INVERSE_PROPERTY_TAG}.
 * @author Garret Wilson
 */
public class ExperimentalUrfInferencer implements UrfInferencer {

	/** Inverse properties associated with the tag of their inverse-of property subjects. */
	private final Map<URI, UrfReference> inversePropertiesByPropertyTag = new HashMap<>();

	/**
	 * Schema objects constructor. The {@link UrfObject} instances in the schema represent schema statements.
	 * @param schema The URF object graph representing a schema.
	 * @throws IllegalArgumentException if the schema is invalid.
	 */
	public ExperimentalUrfInferencer(@Nonnull final Iterable<Object> schema) { //TODO switch to using some other general schema source, like UrfInstance
		schema.forEach(object -> {
			if(object instanceof UrfObject) {
				final UrfObject urfObject = (UrfObject)object;
				//TODO require this to be a property?
				//TODO throw an exception if the value is not an UrfObject?
				urfObject.findPropertyValue(INVERSE_PROPERTY_TAG).filter(UrfObject.class::isInstance).map(UrfObject.class::cast).ifPresent(inverseProperty -> {
					final URI propertyTag = checkArgumentPresent(urfObject.getTag());
					final URI inversePropertyTag = checkArgumentPresent(inverseProperty.getTag());
					//TODO we can probably just use the inverse property itself, as it is a reference; see if there is a benefit either way 
					final UrfReference inversePropertyReference = UrfReference.ofTag(inversePropertyTag); //TODO create UrfReference.ofResource() that uses checkArgumentPresent()
					inversePropertiesByPropertyTag.put(propertyTag, inversePropertyReference);
				});
			}
		});
	}

	private boolean isProcessing = false;

	@Override
	public void processStatement(final UrfProcessor<?> processor, final UrfReference subject, final UrfReference property, final UrfReference object) {
		if(isProcessing) { //prevent recursion
			return;
		}

		isProcessing = true;
		try {

			final URI propertyTag = checkArgumentPresent(property.getTag());

			//inverse properties
			final UrfReference inverseProperty = inversePropertiesByPropertyTag.get(propertyTag); //TODO create Optional accessor method?
			if(inverseProperty != null) {
				processor.processStatement(object, inverseProperty, subject); //process the statement inverse
			}

		} finally {
			isProcessing = false;
		}

	}

}
