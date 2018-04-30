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

package com.epam.dlab.mongo;

import com.epam.dlab.billing.DlabResourceType;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Objects;

/** The resource of DLab environment.
 */
public class ResourceItem implements Comparable<ResourceItem> {
	
	/** Resource ID. */
	String resourceId;
	
	/** User friendly name of resource.*/
	private String resourceName;
	
	/** Type of resource. */
	private DlabResourceType type;
	
	/** Name of user. */
	private String user;
	
	/** Name of exploratory.*/
	private String exploratoryName;
	
	/** Instantiate resource of DLab environment.
	 * @param resourceId resource id.
	 * @param type the type of resource.
	 * @param user the name of user.
	 * @param exploratoryName the name of exploratory.
	 */
	ResourceItem(String resourceId, String resourceName, DlabResourceType type,
				 String user, String exploratoryName) {
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.type = type;
		this.user = user;
		this.exploratoryName = exploratoryName;
	}

	@Override
	public int compareTo(@Nullable ResourceItem o) {
		if (o == null) {
			return -1;
		}
		int result = StringUtils.compare(resourceId, o.resourceId);
		if (result == 0) {
			result = StringUtils.compare(exploratoryName, o.exploratoryName);
			if (result == 0) {
				result = StringUtils.compare(type.name(), o.type.name());
				if (result == 0) {
					return StringUtils.compare(user, o.user);
				}
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ResourceItem ri = (ResourceItem) o;
		return resourceId.equals(ri.resourceId) && exploratoryName.equals(ri.exploratoryName) &&
				type.name().equals(ri.type.name()) && user.equals(ri.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceId, exploratoryName, type.name(), user);
	}


	/** Returns the resource id. */
	public String getResourceId() {
		return resourceId;
	}

	/** Return user friendly name of resource.*/
	public String getResourceName() {
		return resourceName;
	}
	
	/** Returns the type of resource. */
	public DlabResourceType getType() {
		return type;
	}
	
	/** Returns the name of user. */
	public String getUser() {
		return user;
	}

	/** Returns the name of exploratory. */
	public String getExploratoryName() {
		return exploratoryName;
	}


	
	/** Returns a string representation of the object.
	 * @param self the object to generate the string for (typically this), used only for its class name.
	 */
	public ToStringHelper toStringHelper(Object self) {
    	return MoreObjects.toStringHelper(self)
    			.add("resourceId",  resourceId)
    			.add("resourceName", resourceName)
    			.add("type",  type)
    			.add("user",  user)
    			.add("exploratoryName", exploratoryName);
    }

	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}
}
