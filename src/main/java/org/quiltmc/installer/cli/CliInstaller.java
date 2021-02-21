/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.installer.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.installer.action.Action;

/**
 * The main entrypoint when installing from the command line.
 */
public final class CliInstaller {
	// The value in this variable will be set by blossom at compile time.
	public static final String INSTALLER_VERSION = "__INSTALLER_VERSION";
	// TODO: Add installation dir option
	static final String USAGE = "help | listVersions [--snapshots] | install (client | server ) <minecraft-version> [loader-version] [--no-profile] [--install-dir=<dir>]";

	public static void run(String[] args) {
		UsageParser usageParser = new UsageParser();
		Node node = usageParser.parse(USAGE, false);

		// Assemble the array of args back into a single string
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < args.length; i++) {
			if (i != 0) {
				builder.append(' ');
			}

			builder.append(args[i]);
		}

		Action<?> action = parse(builder.toString(), node);

		action.run(msg -> {
			if (action != Action.DISPLAY_HELP) {
				// TODO: Do we want to add some sort of hanging carriage percentage tracker?
				// TODO: Implement CLI tracker
			}

			// Help shouldn't need a progress bar
		});
	}

	/**
	 * Parse the inputted arguments and return the appropriate action that should be run.
	 *
	 * @param input the input
	 * @param node the input parser tree
	 * @return the action, defaulting to {@link Action#DISPLAY_HELP} if the arguments were improperly parsed.
	 */
	private static Action<?> parse(String input, Node node) {
		Map<String, String> args = new LinkedHashMap<>();
		InputParser inputParser = new InputParser();

		if (!inputParser.parse(input, node, args)) {
			return Action.DISPLAY_HELP;
		}

		switch (args.get("unnamed_0")) {
		case "help":
			return Action.DISPLAY_HELP;
		case "listVersions":
			boolean snapshots = args.containsKey("snapshots");

			return Action.listVersions(snapshots);
		case "install":
			String minecraftVersion = args.get("minecraft-version");

			// The loader version, if null lookup the latest
			@Nullable
			String loaderVersion = args.get("loader-version");

			@Nullable
			String installDir = args.get("installer-dir");

			switch (args.get("unnamed_1")) {
			case "client":
				return Action.installClient(minecraftVersion, loaderVersion, installDir, !args.containsKey("no-profile"));
			case "server":
				return Action.installServer(minecraftVersion, loaderVersion, installDir);
			}

			break;
		}

		return Action.DISPLAY_HELP;
	}
}
