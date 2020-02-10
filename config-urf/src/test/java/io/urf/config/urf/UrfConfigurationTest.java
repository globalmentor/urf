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

import java.net.URI;
import java.nio.file.Paths;
import java.util.*;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.java.OperatingSystem.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.globalmentor.net.EmailAddress;

import io.confound.config.Configuration;
import io.confound.config.Section;
import io.urf.config.urf.UrfConfiguration;
import io.urf.model.UrfObject;

/**
 * Tests of {@link UrfConfiguration}.
 * @author Garret Wilson
 */
public class UrfConfigurationTest {

	/**
	 * @see UrfConfiguration#getSectionRoot()
	 * @see UrfConfiguration#getSectionName()
	 * @see UrfConfiguration#getSectionType()
	 */
	@Test
	public void testRootUrfObject() {
		final UrfObject urfObject = new UrfObject(null, "Configuration");
		urfObject.setPropertyValueByHandle("foo", "bar");
		urfObject.setPropertyValueByHandle("flag", Boolean.TRUE);

		final UrfObject org = new UrfObject(null, "Company");
		org.setPropertyValueByHandle("name", "Acme Company");
		org.setPropertyValueByHandle("size", 123);

		final UrfObject address = new UrfObject();
		address.setPropertyValueByHandle("state", "NY");
		address.setPropertyValueByHandle("country", "USA");

		org.setPropertyValueByHandle("address", address);
		urfObject.setPropertyValueByHandle("organization", org);

		final UrfConfiguration urfConfiguration = new UrfConfiguration(urfObject);
		assertThat(urfConfiguration.getSectionRoot(), is(sameInstance(urfConfiguration)));
		assertThat(urfConfiguration.getSectionType(), isPresentAndIs("Configuration"));
		assertThat(urfConfiguration.hasConfigurationValue("foo"), is(true));
		assertThat(urfConfiguration.getString("foo"), is("bar"));
		assertThat(urfConfiguration.hasConfigurationValue("flag"), is(true));
		assertThat(urfConfiguration.getBoolean("flag"), is(true));
		assertThat(urfConfiguration.hasConfigurationValue("none"), is(false));
		assertThat(urfConfiguration.findObject("none"), isEmpty());

		assertThat(urfConfiguration.getString("organization.name"), is("Acme Company"));
		assertThat(urfConfiguration.getInt("organization.size"), is(123));
		assertThat(urfConfiguration.getString("organization.address.state"), is("NY"));
		assertThat(urfConfiguration.hasConfigurationValue("organization.address.country"), is(true));
		assertThat(urfConfiguration.getString("organization.address.country"), is("USA"));
	}

	/** @see UrfConfiguration#getSectionType() */
	@Test
	public void testRootUrfObjectTypeTag() {
		final UrfObject urfObject = new UrfObject(null, URI.create("https://example.com/foo/Bar"));
		final UrfConfiguration urfConfiguration = new UrfConfiguration(urfObject);
		assertThat(urfConfiguration.getSectionType(), isPresentAndIs("https://example.com/foo/Bar"));
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
		assertThat(urfConfiguration.findObject("none"), isEmpty());
	}

	/**
	 * @see Configuration#findSection(String)
	 * @see Configuration#getSection(String)
	 */
	@Test
	public void testGetSectionObject() {
		final UrfObject root = new UrfObject();
		root.setPropertyValueByHandle("foo", "bar");

		//simulate the INI-like section of `.gitconfig`
		//```ini
		//[user]
		//  name = J. Doe
		//  email = jdoe@example.com
		//```
		//```turf
		//  user:
		//    name = "J. Doe"
		//    email = <jdoe@example.com>
		//  ;
		//```
		final UrfObject user = new UrfObject();
		user.setPropertyValueByHandle("name", "J. Doe");
		user.setPropertyValueByHandle("email", EmailAddress.fromString("jdoe@example.com"));
		root.setPropertyValueByHandle("user", user);

		root.setPropertyValueByHandle("flag", Boolean.TRUE);

		//add a typed section
		final UrfObject org = new UrfObject(null, "Company");
		org.setPropertyValueByHandle("name", "Acme Company");
		org.setPropertyValueByHandle("size", 123);
		root.setPropertyValueByHandle("org", org);

		final UrfConfiguration urfConfiguration = new UrfConfiguration(root);
		assertThat(urfConfiguration.hasConfigurationValue("foo"), is(true));
		assertThat(urfConfiguration.getString("foo"), is("bar"));
		assertThat(urfConfiguration.hasConfigurationValue("flag"), is(true));
		assertThat(urfConfiguration.getBoolean("flag"), is(true));
		assertThat(urfConfiguration.hasConfigurationValue("none"), is(false));
		assertThat(urfConfiguration.findObject("none"), isEmpty());

		assertThat(urfConfiguration.hasConfigurationValue("user"), is(true));
		assertThat(urfConfiguration.findSection("user"), isPresentAnd(instanceOf(Section.class)));
		assertThat(urfConfiguration.findObject("user", Section.class), isPresentAnd(instanceOf(Section.class)));
		assertThat(urfConfiguration.findObject("user", Object.class), isPresentAnd(instanceOf(Section.class)));
		final Section userSection = urfConfiguration.getSection("user");
		assertThat(userSection.getSectionRoot(), is(sameInstance(urfConfiguration)));
		assertThat(userSection.getSectionType(), isEmpty());
		assertThat(userSection.getString("name"), is("J. Doe"));
		assertThat(userSection.getObject("email", EmailAddress.class), is(EmailAddress.fromString("jdoe@example.com")));

		assertThat(urfConfiguration.hasConfigurationValue("org"), is(true));
		assertThat(urfConfiguration.findSection("org"), isPresentAnd(instanceOf(Section.class)));
		assertThat(urfConfiguration.findObject("org", Section.class), isPresentAnd(instanceOf(Section.class)));
		assertThat(urfConfiguration.findObject("org", Object.class), isPresentAnd(instanceOf(Section.class)));
		final Section orgSection = urfConfiguration.getSection("org");
		assertThat(orgSection.getSectionRoot(), is(sameInstance(urfConfiguration)));
		assertThat(orgSection.getSectionType(), isPresentAndIs("Company"));
		assertThat(orgSection.getString("name"), is("Acme Company"));
		assertThat(orgSection.getInt("size"), is(123));
	}

	public void testEmptyKeySegmentError() {
		final Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		final UrfConfiguration urfConfiguration = new UrfConfiguration(map);
		assertThrows(IllegalArgumentException.class, () -> urfConfiguration.getString("foo..bar"));
	}

	/** Test that strings are correctly converted to path objects. */
	@Test
	public void testFindPath() {
		final String tempDirectorySystemProperty = System.getProperty(JAVA_IO_TMPDIR_PROPERTY);
		final UrfObject urfObject = new UrfObject(null, "Configuration");
		urfObject.setPropertyValueByHandle("tempDir", tempDirectorySystemProperty);

		final UrfConfiguration urfConfiguration = new UrfConfiguration(urfObject);
		assertThat(urfConfiguration.hasConfigurationValue("tempDir"), is(true));
		assertThat(urfConfiguration.getPath("tempDir"), is(Paths.get(tempDirectorySystemProperty)));
	}

}
