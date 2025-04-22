/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.module.csv.constant;

/**
 * Keys used in the parameters
 */
public class ParameterKey {

    public static final String LINES_TO_SKIP = "linesToSkip";
    public static final String DATA_TYPES = "dataTypes";
    public static final String IS_HEADER_PRESENT = "headerPresent";
    public static final String VALUE_SEPARATOR = "valueSeparator";
    public static final String CUSTOM_VALUE_SEPARATOR = "customValueSeparator";
    public static final String SKIP_HEADER = "skipHeader";
    public static final String DATA_ROWS_TO_SKIP = "dataRowsToSkip";
    public static final String COLUMNS_TO_SKIP = "columnsToSkip";
    public static final String CUSTOM_HEADER = "customHeader";
    public static final String SORT_COLUMNS_BY_ORDERING = "columnOrdering";
    public static final String ORDER_BY_COLUMN = "orderByColumn";
    public static final String CSV_EMPTY_VALUES = "csvEmptyValues";
    public static final String JSON_KEYS = "jsonKeys";
    public static final String ROOT_JSON_KEY = "rootJsonKey";
    public static final String XML_TAG_NAMES = "tagNames";
    public static final String ROOT_ELEMENT_TAG_NAME = "rootElementTag";
    public static final String ROOT_ELEMENT_NAMESPACE = "rootElementNamespace";
    public static final String ROOT_ELEMENT_NAMESPACE_URI = "rootElementNamespaceURI";
    public static final String GROUP_ELEMENT_TAG_NAME = "groupElementTag";
    public static final String GROUP_ELEMENT_NAMESPACE = "groupElementNamespace";
    public static final String GROUP_ELEMENT_NAMESPACE_URI = "groupElementNamespaceURI";

    public static final String SUPPRESS_ESCAPE_CHARACTERS = "suppressEscaping";
    public static final String APPLY_QUOTES = "applyQuotes";
    private ParameterKey() {

    }

}
