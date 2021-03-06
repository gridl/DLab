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

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.contracts.HealthChecker;
import com.mongodb.MongoException;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoHealthChecker implements HealthChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoHealthChecker.class);

    private MongoService mongoService;

    public MongoHealthChecker(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    @Override
    public boolean isAlive(@Auth UserInfo userInfo) {
        try {
            mongoService.ping();
            return true;
        } catch (MongoException e) {
            LOGGER.error("Mongo is not available");
            return false;
        }
    }
}
