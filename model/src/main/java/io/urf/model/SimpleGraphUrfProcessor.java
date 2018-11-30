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
import static java.util.Collections.*;

import java.net.URI;
import java.util.*;

import com.globalmentor.collections.*;

/**
 * Processes URF statements by constructing a graph of simple objects. Tags and descriptions are not supported for collections and value objects.
 * <p>
 * This processor wraps value objects and collections using an instance of {@link ObjectUrfResource}.
 * </p>
 * <p>
 * This implementation does not support described properties, collections, or value objects.
 * </p>
 * <p>
 * This implementation collects roots objects, retrievable via {@link #getRegisteredRoots()}, and returns them as {@link #getResult()} as well.
 * </p>
 * @author Garret Wilson
 */
public class SimpleGraphUrfProcessor extends BaseUrfProcessor<Set<Object>> {

	private Object inferredRoot = null;

	/**
	 * Returns the inferred root object of the processed graph. If there was at least one resource processed, there is guaranteed to be an inferred root. If there
	 * is more than one possible root, it is not specified which will be returned.
	 * @apiNote The returned object, if any, is not guaranteed to describe the entire graph; that is, there may be other roots describing graphs that are not
	 *          connected to the graph this root describes.
	 * @return The inferred roots, which may be empty if no resources were processed.
	 */
	public Optional<Object> getInferredRoot() {
		return Optional.ofNullable(inferredRoot);
	}

	private Set<Object> registeredRoots = new LinkedHashSet<>();

	/**
	 * {@inheritDoc}
	 * @implSpec This version unwraps any object wrapped by the resource.
	 */
	@Override
	public void registerRootResource(final UrfResource root) {
		registeredRoots.add(ObjectUrfResource.unwrap(root));
	}

	/**
	 * Returns any registered roots. If any resources wrapped objects such as value objects or collections, those objects will be unwrapped.
	 * @return The roots that were registered during processing, if any.
	 */
	public Set<Object> getRegisteredRoots() {
		return unmodifiableSet(registeredRoots);
	}

	private Set<Object> processedSubjects = new IdentityHashSet<>();

	/** @return The subject objects that were processed. */
	public Set<Object> getProcessedSubjects() {
		return unmodifiableSet(processedSubjects);
	}

	/** {@inheritDoc} This implementation returns an instance of {@link UrfObject}. */
	@Override
	public UrfResource createDefaultResource(final URI tag, final URI typeTag) {
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
		final Object subjectObject = ObjectUrfResource.unwrap(subject);
		final URI propertyTag = property.getTag().orElseThrow(IllegalArgumentException::new);
		final Object propertyValueObject = ObjectUrfResource.unwrap(propertyValue);

		//collect the subjects
		processedSubjects.add(subjectObject);

		//add to collections
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
			final Object keyObject = mapEntry.getPropertyValue(KEY_PROPERTY_TAG).map(ObjectUrfResource::unwrap).orElseThrow(IllegalArgumentException::new);
			final Object valueObject = mapEntry.getPropertyValue(VALUE_PROPERTY_TAG).map(ObjectUrfResource::unwrap).orElseThrow(IllegalArgumentException::new);
			map.put(keyObject, valueObject);
			return;
		}

		if(subject instanceof UrfResourceDescription) { //if the resource can be described
			final UrfResourceDescription description = (UrfResourceDescription)subject;
			//TODO finalize how to support properties with values
			description.setPropertyValue(propertyTag, propertyValueObject);
		}

		//TODO test and throw an error if a property, value object, or collection is attempted to be described

		//try to infer at least one root
		if(inferredRoot == null || inferredRoot == propertyValueObject) { //TODO fix; this won't work if the caller isn't required to re-use resources
			inferredRoot = subjectObject;
		}
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the registered roots.
	 * @see #getRegisteredRoots()
	 */
	@Override
	public Set<Object> getResult() {
		return getRegisteredRoots();
	}
}
