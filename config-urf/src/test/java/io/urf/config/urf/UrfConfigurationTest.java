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

package io.urf.config.urf;

import static org.junit.Assert.*;

import java.util.*;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static org.hamcrest.Matchers.*;
import org.junit.*;

import io.urf.config.urf.UrfConfiguration;
import io.urf.model.UrfObject;

/**
 * Tests of {@link UrfConfiguration}.
 * @author Garret Wilson
 */
public class UrfConfigurationTest {

	@Test
	public void testRootUrfObject() {
		final UrfObject urfObject = new UrfObject("Configuration");
		urfObject.setPropertyValue("foo", "bar");
		urfObject.setPropertyValue("flag", Boolean.TRUE);

		final UrfObject org = new UrfObject("Company");
		org.setPropertyValue("name", "Acme Company");
		org.setPropertyValue("size", 123);

		final UrfObject address = new UrfObject();
		address.setPropertyValue("state", "NY");
		address.setPropertyValue("country", "USA");

		org.setPropertyValue("address", address);
		urfObject.setPropertyValue("organization", org);

		final UrfConfiguration urfConfiguration = new UrfConfiguration(urfObject);
		assertThat(urfConfiguration.hasConfigurationValue("foo"), is(true));
		assertThat(urfConfiguration.getString("foo"), is("bar"));
		assertThat(urfConfiguration.hasConfigurationValue("flag"), is(true));
		assertThat(urfConfiguration.getBoolean("flag"), is(true));
		assertThat(urfConfiguration.hasConfigurationValue("none"), is(false));
		assertThat(urfConfiguration.getOptionalObject("none"), isEmpty());

		assertThat(urfConfiguration.getString("organization.name"), is("Acme Company"));
		assertThat(urfConfiguration.getInt("organization.size"), is(123));
		assertThat(urfConfiguration.getString("organization.address.state"), is("NY"));
		assertThat(urfConfiguration.hasConfigurationValue("organization.address.country"), is(true));
		assertThat(urfConfiguration.getString("organization.address.country"), is("USA"));
	}

	@Test
	public void testRootMap() {
		final Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		map.put("flag", Boolean.TRUE);
		final UrfConfiguration urfConfiguration = new UrfConfiguration(map);
		assertThat(urfConfiguration.hasConfigurationValue("foo"), is(true));
		assertThat(urfConfiguration.getString("foo"), is("bar"));
		assertThat(urfConfiguration.hasConfigurationValue("flag"), is(true));
		assertThat(urfConfiguration.getBoolean("flag"), is(true));
		assertThat(urfConfiguration.hasConfigurationValue("none"), is(false));
		assertThat(urfConfiguration.getOptionalObject("none"), isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyKeySegmentError() {
		final Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		final UrfConfiguration urfConfiguration = new UrfConfiguration(map);
		urfConfiguration.getString("foo..bar");
	}

}
