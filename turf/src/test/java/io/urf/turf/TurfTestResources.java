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

package io.urf.turf;

import static com.globalmentor.collections.Sets.*;

import java.util.Set;

/**
 * Defines available TURF test resources.
 * @author Garret Wilson
 */
public class TurfTestResources {

	//# handles
	public static final String OK_OBJECT_HANDLE_0_RESOURCE_NAME = "ok-object-handle-0.turf";
	public static final String OK_OBJECT_HANDLE_1_RESOURCE_NAME = "ok-object-handle-1.turf";

	/** Names of TURF document resources semantically equivalent to {@value #OK_OBJECT_HANDLE_0_RESOURCE_NAME}. */
	public static Set<String> OK_OBJECT_HANDLE_RESOURCE_NAMES = immutableSetOf(OK_OBJECT_HANDLE_0_RESOURCE_NAME, OK_OBJECT_HANDLE_1_RESOURCE_NAME);

	public static final String OK_OBJECT_HANDLE_TYPE_RESOURCE_NAME = "ok-object-handle-type.turf";

	//## potentially ambiguous handles
	public static final String OK_HANDLE_AMBIGUOUS_PROPERTY_RESOURCE_NAME = "ok-handle-ambiguous-property.turf";
	public static final String OK_HANDLE_AMBIGUOUS_TAG_RESOURCE_NAME = "ok-handle-ambiguous-tag.turf";
	public static final String OK_HANDLE_AMBIGUOUS_TYPE_RESOURCE_NAME = "ok-handle-ambiguous-type.turf";
	public static final String OK_HANDLE_AMBIGUOUS_VALUE_RESOURCE_NAME = "ok-handle-ambiguous-value.turf";

	//# IDs
	public static final String OK_IDS_RESOURCE_NAME = "ok-ids.turf";

	//# namespaces
	public static final String OK_NAMESPACES_RESOURCE_NAME = "ok-namespaces.turf";
	public static final String OK_NAMESPACES_ALIASES_RESOURCE_NAME = "ok-namespaces-aliases.turf";

	/** Names of TURF document resources semantically equivalent to {@value #OK_NAMESPACES_RESOURCE_NAME}. */
	public static Set<String> OK_NAMESPACES_RESOURCE_NAMES = immutableSetOf(OK_NAMESPACES_RESOURCE_NAME, OK_NAMESPACES_ALIASES_RESOURCE_NAME);

	//# roots
	public static final String OK_ROOTS_WHITESPACE_RESOURCE_NAME = "ok-roots-whitespace.turf";

	//# properties files
	public static final String OK_PROPERTIES_RESOURCE_NAME = "ok-properties.turf";
	public static final String OK_PROPERTIES_TURF_PROPERTIES_RESOURCE_NAME = "ok-properties.turf-properties";
	public static final String OK_PROPERTIES_WITH_HEADER_RESOURCE_NAME = "ok-properties-with-header.turf";
	public static final String OK_PROPERTIES_WITH_HEADER_TURF_PROPERTIES_RESOURCE_NAME = "ok-properties-with-header.turf-properties";

	/** Names of TURF and TURF Properties document resources semantically equivalent to {@value #OK_PROPERTIES_RESOURCE_NAME}. */
	public static Set<String> OK_PROPERTIES_RESOURCE_NAMES = immutableSetOf(OK_PROPERTIES_RESOURCE_NAME, OK_PROPERTIES_TURF_PROPERTIES_RESOURCE_NAME,
			OK_PROPERTIES_WITH_HEADER_RESOURCE_NAME, OK_PROPERTIES_WITH_HEADER_TURF_PROPERTIES_RESOURCE_NAME);

	public static final String OK_PROPERTIES_NAMESPACES_ALIASES_RESOURCE_NAME = "ok-properties-namespaces-aliases.turf-properties";

	//# short-hand property object descriptions
	public static final String OK_PROPERTY_OBJECT_DESCRIPTIONS_RESOURCE_NAME = "ok-property-object-descriptions.turf";
	public static final String OK_PROPERTY_OBJECT_DESCRIPTIONS_SHORT_RESOURCE_NAME = "ok-property-object-descriptions-short.turf";
	public static final String OK_PROPERTY_OBJECT_DESCRIPTIONS_SHORT_NO_WHITESPACE_RESOURCE_NAME = "ok-property-object-descriptions-short-no-whitespace.turf";

	/** Names of TURF document resources semantically equivalent to {@value #OK_PROPERTY_OBJECT_DESCRIPTIONS_RESOURCE_NAME}. */
	public static Set<String> OK_PROPERTY_OBJECT_DESCRIPTIONS_RESOURCE_NAMES = immutableSetOf(OK_PROPERTY_OBJECT_DESCRIPTIONS_RESOURCE_NAME,
			OK_PROPERTY_OBJECT_DESCRIPTIONS_SHORT_RESOURCE_NAME, OK_PROPERTY_OBJECT_DESCRIPTIONS_SHORT_NO_WHITESPACE_RESOURCE_NAME);

	//# n-ary properties

	//## one n-ary property
	public static final String OK_NARY_ONE_PROPERTY_ONE_VALUE_RESOURCE_NAME = "ok-nary-one-property-one-value.turf";
	public static final String OK_NARY_ONE_PROPERTY_TWO_VALUES_RESOURCE_NAME = "ok-nary-one-property-two-values.turf";
	public static final String OK_NARY_ONE_PROPERTY_THREE_VALUES_RESOURCE_NAME = "ok-nary-one-property-three-values.turf";

	//## two n-ary properties
	public static final String OK_NARY_TWO_PROPERTIES_RESOURCE_NAME = "ok-nary-two-properties.turf";

	//## mixed n-ary and binary properties
	public static final String OK_NARY_MIXED_PROPERTIES_0_RESOURCE_NAME = "ok-nary-mixed-properties-0.turf";
	public static final String OK_NARY_MIXED_PROPERTIES_1_RESOURCE_NAME = "ok-nary-mixed-properties-1.turf";
	public static final String OK_NARY_MIXED_PROPERTIES_2_RESOURCE_NAME = "ok-nary-mixed-properties-2.turf";

	/** Names of TURF document resources semantically equivalent to {@value #OK_NARY_MIXED_PROPERTIES_0_RESOURCE_NAME}. */
	public static Set<String> OK_NARY_MIXED_PROPERTIES_RESOURCE_NAMES = immutableSetOf(OK_NARY_MIXED_PROPERTIES_0_RESOURCE_NAME,
			OK_NARY_MIXED_PROPERTIES_1_RESOURCE_NAME, OK_NARY_MIXED_PROPERTIES_2_RESOURCE_NAME);

}
