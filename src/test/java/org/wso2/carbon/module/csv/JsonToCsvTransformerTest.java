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

package org.wso2.carbon.module.csv;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wso2.carbon.module.core.SimpleMessageContext;
import org.wso2.carbon.module.core.collectors.CsvCollector;
import org.wso2.carbon.module.csv.constant.ParameterKey;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JsonToCsvTransformerTest {

    @Mock
    private SimpleMessageContext mc;

    @Test
    public void testMediate_validJsonInput_correctCsvOutputShouldSet() {

        final String jsonPayload = "[\n" +
                "    {\n" +
                "        \"id\": \"1\",\n" +
                "        \"firstName\": \"Denise\",\n" +
                "        \"lastName\": \"Callum\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"2\",\n" +
                "        \"firstName\": \"Randee\",\n" +
                "        \"lastName\": \"Abbot-Doyle\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"3\",\n" +
                "        \"firstName\": \"Sandland\",\n" +
                "        \"lastName\": \"Latashia\"\n" +
                "    }\n" +
                "]";

        JsonParser parser = new JsonParser();
        final JsonElement payloadJsonElement = parser.parse(jsonPayload);
        final Stream<JsonElement> payloadJsonStream =
                StreamSupport.stream(payloadJsonElement.getAsJsonArray().spliterator(), false);
        final CsvCollector csvCollector = new CsvCollector(mc, null);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn("");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SUPPRESS_ESCAPE_CHARACTERS)).thenReturn("false");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.APPLY_QUOTES)).thenReturn("false");
        when(mc.getJsonElement()).thenReturn(payloadJsonElement);
        when(mc.getJsonArrayStream()).thenReturn(payloadJsonStream);
        when(mc.collectToCsv(new String[]{"id", "firstName", "lastName"}, ',', false, false)).thenReturn(csvCollector);

        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        JsonToCsvTransformer jsonToCsvTransformer = new JsonToCsvTransformer();
        jsonToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "1,Denise,Callum\n" +
                "2,Randee,Abbot-Doyle\n" +
                "3,Sandland,Latashia\n";

        Assertions.assertEquals(expectedPayload, setPayload);

    }

    @Test
    public void testMediate_validJsonInputWithHeaderSet_correctCsvShouldReturnWithHeader() {

        final String jsonPayload = "[\n" +
                "    {\n" +
                "        \"id\": \"1\",\n" +
                "        \"firstName\": \"Denise\",\n" +
                "        \"lastName\": \"Callum\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"2\",\n" +
                "        \"firstName\": \"Randee\",\n" +
                "        \"lastName\": \"Abbot-Doyle\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"3\",\n" +
                "        \"firstName\": \"Sandland\",\n" +
                "        \"lastName\": \"Latashia\"\n" +
                "    }\n" +
                "]";

        final String header = "id,first,last";
        final String[] headerArray = new String[]{"id", "first", "last"};

        JsonParser parser = new JsonParser();
        final JsonElement payloadJsonElement = parser.parse(jsonPayload);
        final Stream<JsonElement> payloadJsonStream =
                StreamSupport.stream(payloadJsonElement.getAsJsonArray().spliterator(), false);
        final CsvCollector csvCollector = new CsvCollector(mc, headerArray);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn(header);
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SUPPRESS_ESCAPE_CHARACTERS)).thenReturn("false");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.APPLY_QUOTES)).thenReturn("false");
        when(mc.getJsonArrayStream()).thenReturn(payloadJsonStream);
        when(mc.collectToCsv(headerArray, ',', false, false)).thenReturn(csvCollector);

        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        JsonToCsvTransformer jsonToCsvTransformer = new JsonToCsvTransformer();
        jsonToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "id,first,last\n" +
                "1,Denise,Callum\n" +
                "2,Randee,Abbot-Doyle\n" +
                "3,Sandland,Latashia\n";

        Assertions.assertEquals(expectedPayload, setPayload);

    }

    @Test
    public void testMediate_applyQuotes_correctlyHandlesSpecialCharacters() {
        final String jsonPayload = "[\n" +
                "    {\n" +
                "        \"id\": \"1\",\n" +
                "        \"text\": \"Normal text\",\n" +
                "        \"with_comma\": \"Hello, world\",\n" +
                "        \"with_quote\": \"She said \\\"Hello\\\"\",\n" +
                "        \"with_newline\": \"Line one\\nLine two\",\n" +
                "        \"with_all\": \"\\\"Quoted text\\\", with comma\\nand newline\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"2\",\n" +
                "        \"text\": \"\",\n" +
                "        \"with_comma\": \"Smith, John\",\n" +
                "        \"with_quote\": \"Double quotes: \\\"\\\"\",\n" +
                "        \"with_newline\": \"Multiple\\nNew\\nLines\",\n" +
                "        \"with_all\": \"Comma, \\\"quote\\\" and\\nnewline combined\"\n" +
                "    }\n" +
                "]";

        final String[] headerArray = new String[]{"id", "text", "with_comma", "with_quote", "with_newline", "with_all"};
        final String header = "id,text,with_comma,with_quote,with_newline,with_all";

        JsonParser parser = new JsonParser();
        final JsonElement payloadJsonElement = parser.parse(jsonPayload);
        final Stream<JsonElement> payloadJsonStream =
                StreamSupport.stream(payloadJsonElement.getAsJsonArray().spliterator(), false);
        final CsvCollector csvCollector = new CsvCollector(mc, headerArray, ',', false, true);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn(header);
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SUPPRESS_ESCAPE_CHARACTERS)).thenReturn("false");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.APPLY_QUOTES)).thenReturn("true");
        when(mc.getJsonArrayStream()).thenReturn(payloadJsonStream);
        when(mc.collectToCsv(headerArray, ',', false, true)).thenReturn(csvCollector);

        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        JsonToCsvTransformer jsonToCsvTransformer = new JsonToCsvTransformer();
        jsonToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "id,text,with_comma,with_quote,with_newline,with_all\n" +
                "1,Normal text,\"Hello, world\",\"She said \"\"Hello\"\"\",\"Line one\n" +
                "Line two\",\"\"\"Quoted text\"\", with comma\n" +
                "and newline\"\n" +
                "2,,\"Smith, John\",\"Double quotes: \"\"\"\"\",\"Multiple\n" +
                "New\n" +
                "Lines\",\"Comma, \"\"quote\"\" and\n" +
                "newline combined\"\n";

        Assertions.assertEquals(expectedPayload, setPayload);
    }
}
