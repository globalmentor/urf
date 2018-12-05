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
import static io.urf.URF.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.net.URI;
import java.util.*;

import io.urf.URF;
import io.urf.URF.Tag;

/**
 * Processes URF statements by constructing a graph of simple objects. Tags and descriptions are not supported for collections and value objects.
 * <p>
 * This processor wraps value objects and collections using an instance of {@link ObjectUrfResource}.
 * </p>
 * <p>
 * This implementation does not support described properties, collections, or value objects.
 * </p>
 * <p>
 * This implementation collects roots objects, retrievable via {@link #getReportedRoots()}, and returns them as {@link #getResult()} as well.
 * </p>
 * @author Garret Wilson
 */
public class SimpleGraphUrfProcessor extends BaseUrfProcessor<List<Object>> {

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

	private List<Object> reportedRoots = new ArrayList<>();

	/**
	 * {@inheritDoc}
	 * @implSpec This version unwraps any object wrapped by the resource.
	 */
	@Override
	public void reportRootResource(final UrfResource root) {
		final Object rootObject = asValueObject(root).orElseGet(() -> {
			final UrfResource graphRoot = declaredResources.get(root.getTag().orElseThrow(() -> new IllegalArgumentException("Root resource missing tag.")));
			checkArgument(graphRoot != null, "Undeclared root with tag %s.", root.getTag().get());
			return ObjectUrfResource.unwrap(graphRoot); //our own object may have wrapped a Collection, for example 
		});

		reportedRoots.add(rootObject);
	}

	/**
	 * Returns any reported roots. If any resources wrapped objects such as value objects or collections, those objects will be unwrapped.
	 * @apiNote The returned roots may include the same object more than once, as the same resource may appear as a root multiple times, e.g. as a reference, in
	 *          some contexts.
	 * @return The roots that were reported during processing, if any.
	 */
	public List<Object> getReportedRoots() {
		return unmodifiableList(reportedRoots);
	}

	private Map<URI, UrfResource> declaredResources = new LinkedHashMap<>();

	/** @return The resources that were created. */
	public Collection<UrfResource> getCreatedResources() {
		return unmodifiableCollection(declaredResources.values()); //TODO danger; some of these could be wrapped collections; tidy all this up
	}

	/**
	 * {@inheritDoc}
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
	 */
	@Override
	public void declareResource(final URI tag, final URI typeTag) {
		declaredResources.computeIfAbsent(requireNonNull(tag), resourceTag -> createResource(!Tag.isBlank(resourceTag) ? resourceTag : null, typeTag));
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
		final UrfResource graphSubject = declaredResources.get(subject.getTag().orElseThrow(() -> new IllegalArgumentException("Subject resource missing tag.")));
		checkArgument(graphSubject != null, "Undeclared subject with tag %s.", subject.getTag().get());
		final Object graphSubjectObject = ObjectUrfResource.unwrap(graphSubject);

		final URI propertyTag = property.getTag().orElseThrow(IllegalArgumentException::new);

		//only look up our own resource/object if no value object was supplied;
		//by trusting the caller to supply value objects, it prevents us from needlessly creating their tags
		final Object propertyObject = asValueObject(propertyValue).orElseGet(() -> {
			final UrfResource graphPropertyValue = declaredResources
					.get(propertyValue.getTag().orElseThrow(() -> new IllegalArgumentException("Property value resource missing tag.")));
			checkArgument(graphPropertyValue != null, "Undeclared property value with tag %s.", propertyValue.getTag().get());
			return ObjectUrfResource.unwrap(graphPropertyValue); //our own object may have wrapped a Collection, for example 
		});

		//add to collections
		if(graphSubjectObject instanceof Collection) {
			@SuppressWarnings("unchecked")
			final Collection<Object> collection = (Collection<Object>)graphSubjectObject;
			//TODO make sure is was actually a member; otherwise, add to description
			collection.add(propertyObject); //TODO document; ensure correct order; fix for set and map
			return;
		} else if(graphSubjectObject instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<Object, Object> map = (Map<Object, Object>)graphSubjectObject;
			//TODO make sure is was actually a member; otherwise, add to description
			//TODO make sure the property value is of type urf-MapEntry
			//TODO make sure the property value is a description
			final UrfResourceDescription mapEntry = (UrfResourceDescription)propertyObject;
			final Object keyObject = mapEntry.getPropertyValue(KEY_PROPERTY_TAG).map(ObjectUrfResource::unwrap).orElseThrow(IllegalArgumentException::new);
			final Object valueObject = mapEntry.getPropertyValue(VALUE_PROPERTY_TAG).map(ObjectUrfResource::unwrap).orElseThrow(IllegalArgumentException::new);
			map.put(keyObject, valueObject);
			return;
		}

		if(graphSubject instanceof UrfResourceDescription) { //if the resource can be described
			final UrfResourceDescription description = (UrfResourceDescription)graphSubject;
			//TODO finalize how to support properties with values
			description.setPropertyValue(propertyTag, propertyObject);
		}

		//TODO test and throw an error if a property, value object, or collection is attempted to be described

		//try to infer at least one root
		if(inferredRoot == null || inferredRoot == propertyObject) { //TODO fix; this won't work if the caller isn't required to re-use resources
			inferredRoot = graphSubjectObject;
		}
	}

	//TODO document; basically this says that we trust the source to provide value objects
	protected Optional<Object> asValueObject(UrfResource resource) { //TODO refactor to use ValueSupport
		if(requireNonNull(resource) instanceof ObjectUrfResource) {
			final Object object = ((ObjectUrfResource<?>)resource).getObject();
			if(!(object instanceof Collection) && !(object instanceof Map)) {
				return Optional.of(object);
			}
		}
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the registered roots.
	 * @implNote The returned roots may include the same object more than once, as the same resource may appear as a root multiple times, e.g. as a reference, in
	 *           some contexts.
	 * @see #getReportedRoots()
	 */
	@Override
	public List<Object> getResult() {
		return getReportedRoots();
	}
}
