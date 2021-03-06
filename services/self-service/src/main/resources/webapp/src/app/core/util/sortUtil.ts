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

export class SortUtil {
  public static statusSort(arg1: string, arg2: string): number {
    const order = ['creating', 'running', 'stopping', 'stopped', 'terminating', 'terminated', 'failed'];

    return order.indexOf(arg1) - order.indexOf(arg2);
  }

  public static shapesSort(shapesJson) {
    const sortOrder = ['For testing', 'Memory optimized', 'GPU optimized', 'Compute optimized'];
    const sortedShapes = {};

    Object.keys(shapesJson)
      .sort((a, b) => sortOrder.indexOf(a) - sortOrder.indexOf(b))
      .forEach(key => { sortedShapes[key] = shapesJson[key]; });

    return sortedShapes;
  }

  public static libGroupsSort(groups) {
    const sortOrder = ['os_pkg', 'pip2', 'pip3', 'r_pkg', 'others'];

    return groups.sort((arg1, arg2) => sortOrder.indexOf(arg1) - sortOrder.indexOf(arg2));
  }
}
