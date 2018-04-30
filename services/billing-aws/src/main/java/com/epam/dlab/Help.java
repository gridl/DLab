/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab;

import com.epam.dlab.core.BillingUtils;
import com.epam.dlab.core.ModuleType;
import com.epam.dlab.exception.InitializationException;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Print help for billing tool.
 */
@Slf4j
public class Help {

	private Help() {
	}

	/** Print help to console.
	 * @param resourceName the name of resource.
	 * @param substitute - map for substitution in help content.
	 * @throws InitializationException in case of exception
	 */
	private static void printHelp(String resourceName, Map<String, String> substitute) throws InitializationException {
		List<String> list = BillingUtils.getResourceAsList("/" + Help.class.getName() + "." + resourceName + ".txt");
		String help = StringUtils.join(list, System.lineSeparator());
		
		if (substitute == null) {
			substitute = new HashMap<>();
		}
		substitute.put("classname", BillingScheduler.class.getName());

		for (Map.Entry<String, String> entry : substitute.entrySet()) {
			help = StringUtils.replace(help, "${" + entry.getKey().toUpperCase() + "}", entry.getValue());
		}
		log.debug(help);
	}

	/** Create and return substitutions for names of modules.
	 * @return map
	 * @throws InitializationException in case of exception
	 */
	private static Map<String, String> findModules() throws InitializationException {
		List<Class<?>> modules = BillingUtils.getModuleClassList();
		Map<String, String> substitute = new HashMap<>();
		
		for (Class<?> module : modules) {
			ModuleType type = BillingUtils.getModuleType(module);
			JsonTypeName typeName = module.getAnnotation(JsonTypeName.class);
			if (typeName != null && type != null) {
				String typeNames = substitute.get(type.toString() + "s");
				typeNames = (typeNames == null ? typeName.value() : typeNames + ", " + typeName.value());
				substitute.put(type.toString() + "s", typeNames);
			}
		}
		
		return substitute;
	}

	/** Find and return help for module.
	 * @param type the type of module.
	 * @param name the name of module.
	 * @throws InitializationException in case of exception
	 */
	private static String findModuleHelp(ModuleType type, String name) throws InitializationException {
		List<Class<?>> modules = BillingUtils.getModuleClassList();
		StringBuilder typeNames = new StringBuilder();
		for (Class<?> module : modules) {
			ModuleType t = BillingUtils.getModuleType(module);
			if (t == type) {
				JsonTypeName typeName = module.getAnnotation(JsonTypeName.class);
				if (typeName != null ) {
					if (name.equals(typeName.value())) {
						return descriptionValue(module, type, name);
					} else {
						typeNames.append(typeName.value()).append(", ");
					}
				}
			}
		}
		throw new InitializationException("Module for " + type + " " + name + " not found." +
				(typeNames.toString().isEmpty() ? "" : " Module type must be one of next: " +
						typeNames.toString().substring(0, typeNames.toString().lastIndexOf(", "))));
	}

	private static String descriptionValue(Class<?> module, ModuleType type, String name) throws
			InitializationException {
		JsonClassDescription description = module.getAnnotation(JsonClassDescription.class);
		if (description != null) {
			return description.value();
		}
		throw new InitializationException("Help for " + type + " " + name + " not found");
	}

	/** Print help screen for billing tool. 
	 * @throws InitializationException in case of exception*/
	public static void usage(String ... args) throws InitializationException {
		if (args == null || args.length == 0) {
			printHelp("usage", null);
		} else if (args[0].equalsIgnoreCase("conf")) {
			printHelp("conf", findModules());
		} else {
			ModuleType type = ModuleType.of(args[0]);
			if (type == null) {
				log.error("Unknown --help " + args[0] + " command.");
			} else if (args.length < 2) {
				log.error("Missing the type of module.");
				String typeNames = findModules().get(type.toString() + "s");
				if (typeNames != null) {
					log.error("Must be one of next: " + typeNames);
				}
			} else if (args.length > 2) {
				log.error("Extra arguments in command: " +
					StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " "));
			} else {
				log.debug(findModuleHelp(type, args[1]));
			}
		}
	}
}
