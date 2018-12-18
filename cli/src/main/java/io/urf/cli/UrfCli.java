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

package io.urf.cli;

import java.nio.file.Path;
import java.util.List;

import javax.annotation.*;

import org.fusesource.jansi.AnsiConsole;

import io.confound.config.file.ResourcesConfigurationManager;
import picocli.CommandLine;
import picocli.CommandLine.*;

/**
 * Command-line interface for working with URF data.
 * @author Garret Wilson
 */
@Command(name = "urf", description = "Command-line interface for working with URF data.", versionProvider = UrfCli.VersionProvider.class, mixinStandardHelpOptions = true)
public class UrfCli implements Runnable {

	/**
	 * Main program entry method.
	 * @param args Program arguments.
	 */
	public static void main(@Nonnull final String[] args) {
		AnsiConsole.systemInstall();
		CommandLine.run(new UrfCli(), args);
		AnsiConsole.systemUninstall();
	}

	@Override
	public void run() {
		CommandLine.usage(this, System.out);
	}

	@Command(description = "Converts one or more files to some URF file format.")
	public void convert(@Parameters(paramLabel = "<file>", arity = "1..*") Path[] paths) {
		System.out.println(List.of(paths)); //TODO implement
	}

	/**
	 * Strategy for retrieving a version from the configuration.
	 * @author Garret Wilson
	 */
	static class VersionProvider implements IVersionProvider {

		/** The configuration key containing the version of the program. */
		public static final String CONFIG_KEY_VERSION = "version";

		@Override
		public String[] getVersion() throws Exception {
			return new String[] {ResourcesConfigurationManager.loadConfigurationForClass(UrfCli.class)
					.orElseThrow(ResourcesConfigurationManager::createConfigurationNotFoundException).getString(CONFIG_KEY_VERSION)};
		}

	}
}
