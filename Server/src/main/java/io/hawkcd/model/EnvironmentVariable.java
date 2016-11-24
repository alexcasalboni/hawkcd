/*
 * Copyright (C) 2016 R&D Solutions Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.hawkcd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.hawkcd.core.security.Authorization;
import io.hawkcd.model.enums.PermissionScope;
import io.hawkcd.model.enums.PermissionType;

@Authorization(scope = PermissionScope.PIPELINE, type = PermissionType.VIEWER)
public class EnvironmentVariable extends Entity {
    private String key;
    private String value;
    private boolean isSecured;
    private boolean isDeletable;

    public EnvironmentVariable() {
        this.setDeletable(true);
    }

    public EnvironmentVariable(String key, String value) {
        this.key = key;
        this.value = value;
        this.setDeletable(true);
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String value) {
        this.key = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSecured() {
        return this.isSecured;
    }

    @JsonProperty("isSecured")
    public void setSecured(boolean value) {
        this.isSecured = value;
    }

    public boolean isDeletable() {
        return this.isDeletable;
    }

    @JsonProperty("isDeletable")
    public void setDeletable(boolean deletable) {
        this.isDeletable = deletable;
    }
}
