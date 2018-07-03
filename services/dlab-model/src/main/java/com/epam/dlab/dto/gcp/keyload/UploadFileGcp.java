/*
 * Copyright (c) 2017, EPAM SYSTEMS INC
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

package com.epam.dlab.dto.gcp.keyload;

import com.epam.dlab.dto.base.keyload.UploadFile;
import com.epam.dlab.dto.gcp.edge.EdgeCreateGcp;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UploadFileGcp extends UploadFile {
    @JsonProperty
    private final EdgeCreateGcp edge;

	@JsonCreator
	public UploadFileGcp(@JsonProperty("edge") EdgeCreateGcp edge, @JsonProperty("content") String content) {
        super(content);
        this.edge = edge;
    }
}
