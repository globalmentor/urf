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
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.net.URIs;

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
public class SimpleGraphUrfProcessor extends AbstractUrfProcessor<List<Object>> {

	/** The inference strategy installed for this processor. */
	private final UrfInferencer inferencer;

	/** Constructor with a no inferencing. */
	public SimpleGraphUrfProcessor() {
		this(UrfInferencer.NOP);
	}

	/**
	 * Inferencer constructor.
	 * @param inferencer The strategy for performing inferences.
	 */
	public SimpleGraphUrfProcessor(@Nonnull final UrfInferencer inferencer) {
		this.inferencer = requireNonNull(inferencer);
	}

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

	@Override
	public void reportRootResource(final UrfReference root) {
		final Object rootObject = ObjectUrfResource.findObject(root) //if we were given a wrapped value, its a value object
				.orElseGet(() -> {
					final URI rootTag = checkArgumentPresent(root.getTag());
					final UrfResource rootResource = determineDeclaredResource(rootTag);
					return ObjectUrfResource.unwrap(rootResource); //our actual resource may be wrapping a collection
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

	/**
	 * Returns the resources that were declared. Some of these may be instances of {@link ObjectUrfResource} wrapping collections or maps.
	 * @return The resources that created from being declared.
	 */
	public Stream<UrfResource> getDeclaredResources() { //TODO allow described maps/collections; then this will become useful
		return declaredResources.values().stream();
	}

	/** @return The objects that were created from being declared. */
	public Stream<Object> getDeclaredObjects() {
		return getDeclaredResources().map(ObjectUrfResource::unwrap); //unwrap any native collections/maps
	}

	/**
	 * Retrieves an object that was created from being declared.
	 * @param tag The tag of the object to retrieve.
	 * @return The object, which may not be present if no resource was declared with the given tag.
	 * @throws NullPointerException if the given tag is <code>null</code>.
	 */
	public Optional<Object> findDeclaredObject(@Nonnull final URI tag) {
		return Optional.ofNullable(declaredResources.get(requireNonNull(tag))).map(ObjectUrfResource::unwrap); //unwrap any native collections/maps
	}

	/**
	 * Convenience method to looking up an object by its ID tag, based upon its type tag and ID.
	 * @implSpec This implementation delegates to {@link #findDeclaredObject(URI)}.
	 * @param typeTag The tag of the object's type.
	 * @param id The object ID for the indicated type.
	 * @return The object with the given ID, if any.
	 * @see Tag#forTypeId(URI, String)
	 */
	public Optional<Object> findDeclaredObjectByTypeId(@Nonnull final URI typeTag, @Nonnull final String id) {
		return findDeclaredObject(Tag.forTypeId(typeTag, id));
	}

	/**
	 * Convenience method to looking up an object by its ID tag, based upon its type handle and ID.
	 * @implSpec This implementation delegates to {@link #findDeclaredObjectByTypeId(URI, String)}.
	 * @param typeHandle The handle of the object's type.
	 * @param id The object ID for the indicated type.
	 * @return The object with the given ID, if any.
	 * @see Tag#forTypeId(URI, String)
	 */
	public Optional<Object> findDeclaredObjectByTypeId(@Nonnull final String typeHandle, @Nonnull final String id) {
		return findDeclaredObjectByTypeId(Handle.toTag(typeHandle), id);
	}

	@Override
	public void declareResource(final URI declaredTag, final URI typeTag) {
		determineDeclaredResource(declaredTag, typeTag);
	}

	/**
	 * Returns a declared resource with no type or for which no type is known, declaring it it has not yet been declared.
	 * @implSpec This implementation delegates to {@link #determineDeclaredResource(URI, URI)}.
	 * @param declaredTag The identifying resource tag for declaration, which may be a blank tag if the resource has no tag.
	 * @return An existing or newly declared resource.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	protected UrfResource determineDeclaredResource(@Nonnull final URI declaredTag) {
		return determineDeclaredResource(declaredTag, null);
	}

	/**
	 * Returns a declared resource, declaring it it has not yet been declared.
	 * @param declaredTag The identifying resource tag for declaration, which may be a blank tag if the resource has no tag.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @return An existing or newly declared resource.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 * @throws IllegalArgumentException if a type is given that doesn't match a previously declared type.
	 */
	protected UrfResource determineDeclaredResource(@Nonnull final URI declaredTag, @Nullable final URI typeTag) {
		final UrfResource resource = declaredResources.computeIfAbsent(requireNonNull(declaredTag), tag -> createResource(!Tag.isBlank(tag) ? tag : null, typeTag));
		checkArgument(typeTag == null || typeTag.equals(resource.getTypeTag().orElse(null)), "Resource %s with type %s cannot be redeclared as type %s.",
				declaredTag, resource.getTypeTag().orElse(null), typeTag);
		return resource;
	}

	/**
	 * Creates an appropriate resource to be used by the processor and potentially become part of the graph. Collections and maps will be wrapped in some instance
	 * of {@link ObjectUrfResource}.
	 * @param tag The tag of the resource to create, or <code>null</code> if the resource has no tag.
	 * @param typeTag The type of the resource to crate, or <code>null</code> if the resource should have no type.
	 * @return A new instance for the processor to use to represent the resource.
	 */
	protected UrfResource createResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		if(typeTag != null) {
			if(typeTag.equals(LIST_TYPE_TAG)) {
				return new SimpleObjectUrfResource<List<?>>(tag, typeTag, new ArrayList<>());
			} else if(typeTag.equals(MAP_TYPE_TAG)) {
				return new SimpleObjectUrfResource<Map<?, ?>>(tag, typeTag, new HashMap<>());
			} else if(typeTag.equals(SET_TYPE_TAG)) {
				return new SimpleObjectUrfResource<Set<?>>(tag, typeTag, new HashSet<>());
			}
		}
		return new UrfObject(tag, typeTag);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation delegates to {@link #declareResource(URI, URI)} if the property tag is {@link URF#TYPE_PROPERTY_TAG}.
	 */
	@Override
	public void processStatement(final UrfReference subject, final UrfReference property, final UrfReference object) {
		final URI subjectTag = checkArgumentPresent(subject.getTag());
		final URI propertyTag = checkArgumentPresent(property.getTag());

		//don't attempt to retrieve the object tag yet; if they sent a value object, we'll never have to access and generate a tag for it

		//urf-type declarations
		if(propertyTag.equals(TYPE_PROPERTY_TAG)) {
			declareResource(subjectTag, checkArgumentPresent(object.getTag()));
			return;
		}

		final UrfResource subjectResource = determineDeclaredResource(subjectTag);

		final Object propertyValue = ObjectUrfResource.findObject(object) //if we were given a wrapped value, its a value object
				.orElseGet(() -> {
					final URI objectTag = checkArgumentPresent(object.getTag());
					final UrfResource objectResource = determineDeclaredResource(objectTag);
					return ObjectUrfResource.unwrap(objectResource); //our actual resource may be wrapping a collection
				});

		if(propertyTag.equals(MEMBER_PROPERTY_TAG)) { //set membership
			final Object subjectObject = checkArgumentPresent(ObjectUrfResource.findObject(subjectResource),
					"Processor does not support members of non-collection %s with type %s.", subjectTag, subjectResource.getTypeTag().orElse(null)); //we will have wrapped the collection
			if(subjectObject instanceof Set) {
				@SuppressWarnings("unchecked")
				final Set<Object> set = (Set<Object>)subjectObject;
				set.add(propertyValue);
			} else if(subjectObject instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<Object, Object> map = (Map<Object, Object>)subjectObject;
				//TODO make sure the property value is of type urf-MapEntry
				//TODO make sure the property value is a description
				final UrfResourceDescription mapEntry = (UrfResourceDescription)propertyValue;
				//TODO fix; this currently only works for the TURF parser, which creates an actual map entry object; will need to be upgraded to support lookup of these items 
				final Object keyObject = mapEntry.findPropertyValue(KEY_PROPERTY_TAG).map(ObjectUrfResource::unwrap).orElseThrow(IllegalArgumentException::new);
				final Object valueObject = mapEntry.findPropertyValue(VALUE_PROPERTY_TAG).map(ObjectUrfResource::unwrap).orElseThrow(IllegalArgumentException::new);
				map.put(keyObject, valueObject); //TODO note somewhere that this will leave orphaned map entry objects
			} else {
				throw new IllegalArgumentException(
						String.format("Processor does not %s of type %s to have members.", subjectTag, subjectResource.getTypeTag().orElse(null)));
			}
			return; //this property is processed specially; no further processing to do
		} else if(propertyTag.toString().startsWith(ELEMENT_TYPE_TAG.toString() + URIs.FRAGMENT_SEPARATOR)) { //list element TODO create and use a Tag.getTypeTag() utility method
			final Object subjectObject = checkArgumentPresent(ObjectUrfResource.findObject(subjectResource),
					"Processor does not support elements of non-collection %s with type %s.", subjectTag, subjectResource.getTypeTag().orElse(null)); //we will have wrapped the collection
			if(subjectObject instanceof List) {
				@SuppressWarnings("unchecked")
				final List<Object> list = (List<Object>)subjectObject;
				list.add(propertyValue); //TODO ensure correct order
			} else {
				throw new IllegalArgumentException(
						String.format("Processor does not %s of type %s to have elements.", subjectTag, subjectResource.getTypeTag().orElse(null)));
			}
			return; //this property is processed specially; no further processing to do
		}

		//TODO decide if we want to allow descriptions for collections; if so, change the type of wrapper we create above

		checkArgument(subjectResource instanceof UrfResourceDescription, "Processor does not allow %s of type %s to be described with property %s.", subjectTag,
				subjectResource.getTypeTag().orElse(null), propertyTag);

		//merge in the property value, accounting for n-ary (one-to-many) properties
		((UrfResourceDescription)subjectResource).mergePropertyValue(propertyTag, propertyValue);

		//try to infer at least one root
		if(inferredRoot == null || inferredRoot == propertyValue) {
			inferredRoot = ObjectUrfResource.unwrap(subjectResource);
		}

		inferencer.processStatement(this, subjectResource, property, object); //let the inferencer make inferences as appropriate
	}

	//TODO document; basically this says that we trust the source to provide value objects
	@Deprecated
	protected Optional<Object> asValueObject(UrfReference resource) { //TODO refactor to use ValueSupport
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
	 * @implSpec This implementation returns the recorded roots.
	 * @implNote The returned roots may include the same object more than once, as the same resource may appear as a root multiple times, e.g. as a reference, in
	 *           some contexts.
	 * @see #getReportedRoots()
	 */
	@Override
	public List<Object> getResult() {
		return getReportedRoots();
	}
}
