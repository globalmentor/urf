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
import java.util.*;

/**
 * Processes URF statements by constructing a graph of simple objects. Tags and descriptions are not supported for collections and value objects.
 * @author Garret Wilson
 */
public class SimpleGraphUrfProcessor implements UrfProcessor {

	/** {@inheritDoc} This implementation returns an instance of {@link UrfObject}. */
	@Override
	public UrfResource createResource(final URI tag, final URI typeTag) {
		return new UrfObject(tag, typeTag);
	}

	@Override
	public UrfResource createListResource(URI tag, URI typeTag) {
		return new SimpleObjectUrfResource<List<?>>(tag, typeTag, new ArrayList<>());
	}

	@Override
	public UrfResource createMapResource(URI tag, URI typeTag) {
		return new SimpleObjectUrfResource<Map<?, ?>>(tag, typeTag, new HashMap<>());
	}

	@Override
	public UrfResource createSetResource(URI tag, URI typeTag) {
		return new SimpleObjectUrfResource<Set<?>>(tag, typeTag, new HashSet<>());
	}

	@Override
	public void process(final UrfResource subject, final UrfResource property, final UrfResource propertyValue) {
		final URI propertyTag = property.getTag().orElseThrow(IllegalArgumentException::new);
		//unwrap any property value
		final Object propertyValueObject = propertyValue instanceof ObjectUrfResource ? ((ObjectUrfResource<?>)propertyValue).getObject() : propertyValue;

		//add to collections
		if(subject instanceof ObjectUrfResource) {
			final Object subjectObject = ((ObjectUrfResource<?>)subject).getObject();
			if(subjectObject instanceof Collection) {
				@SuppressWarnings("unchecked")
				final Collection<Object> collection = (Collection<Object>)subjectObject;
				//TODO make sure is was actually a member; otherwise, add to description
				collection.add(propertyValueObject); //TODO document; ensure correct order; fix for set and map
				return;
			} else if(subjectObject instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<Object, Object> map = (Map<Object, Object>)subjectObject;
				//TODO make sure is was actually a member; otherwise, add to description
				//TODO make sure the property value is of type urf-MapEntry
				//TODO make sure the property value is a description
				final UrfResourceDescription mapEntry = (UrfResourceDescription)propertyValue;
				final Object keyObject = mapEntry.getPropertyValue(KEY_PROPERTY_TAG)
						.map(key -> key instanceof ObjectUrfResource ? ((ObjectUrfResource<?>)key).getObject() : key).orElseThrow(IllegalArgumentException::new);
				final Object valueObject = mapEntry.getPropertyValue(VALUE_PROPERTY_TAG)
						.map(value -> value instanceof ObjectUrfResource ? ((ObjectUrfResource<?>)value).getObject() : value).orElseThrow(IllegalArgumentException::new);
				map.put(keyObject, valueObject);
				return;
			}
		}

		if(subject instanceof UrfResourceDescription) { //if the resource can be described
			final UrfResourceDescription description = (UrfResourceDescription)subject;
			//TODO finalize how to support properties with values
			description.setPropertyValue(propertyTag, propertyValueObject);

		}
	}

}
