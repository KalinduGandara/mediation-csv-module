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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
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

import javax.xml.stream.XMLStreamException;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class XmlToCsvTransformerTest {

    @Mock
    private SimpleMessageContext mc;

    @Test
    public void testMediate_validXmlInput_correctCsvOutputShouldSet() throws XMLStreamException {

        final String payloadXml = "<root>\n" +
                "    <child>\n" +
                "        <id>1</id>\n" +
                "        <firstName>Denise</firstName>\n" +
                "        <lastName>Callum</lastName>\n" +
                "    </child>\n" +
                "    <child>\n" +
                "        <id>2</id>\n" +
                "        <firstName>Randee</firstName>\n" +
                "        <lastName>Abbot-Doyle</lastName>\n" +
                "    </child>\n" +
                "    <child>\n" +
                "        <id>3</id>\n" +
                "        <firstName>Sandland</firstName>\n" +
                "        <lastName>Latashia</lastName>\n" +
                "    </child>\n" +
                "</root>";

        final OMElement xmlPayload = AXIOMUtil.stringToOM(payloadXml);
        Iterable<OMElement> iterable = xmlPayload::getChildElements;
        Stream<OMElement> childElementStream = StreamSupport.stream(iterable.spliterator(), false);
        final CsvCollector csvCollector = new CsvCollector(mc, null);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn("");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SUPPRESS_ESCAPE_CHARACTERS)).thenReturn("false");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.APPLY_QUOTES)).thenReturn("false");
        when(mc.getRootXmlElement()).thenReturn(xmlPayload.cloneOMElement());
        when(mc.getXmlChildElementsStream()).thenReturn(childElementStream);
        when(mc.collectToCsv(new String[]{"id", "firstName", "lastName"},false, false)).thenReturn(csvCollector);

        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        XmlToCsvTransformer xmlToCsvTransformer = new XmlToCsvTransformer();
        xmlToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload =
                "1,Denise,Callum\n" +
                        "2,Randee,Abbot-Doyle\n" +
                        "3,Sandland,Latashia\n";

        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    public void testMediate_validXmlInputWithHeaderProperty_correctCsvOutputWithHeaderShouldSet()
            throws XMLStreamException {

        final String payloadXml = "<root>\n" +
                "    <child>\n" +
                "        <id>1</id>\n" +
                "        <firstName>Denise</firstName>\n" +
                "        <lastName>Callum</lastName>\n" +
                "    </child>\n" +
                "    <child>\n" +
                "        <id>2</id>\n" +
                "        <firstName>Randee</firstName>\n" +
                "        <lastName>Abbot-Doyle</lastName>\n" +
                "    </child>\n" +
                "    <child>\n" +
                "        <id>3</id>\n" +
                "        <firstName>Sandland</firstName>\n" +
                "        <lastName>Latashia</lastName>\n" +
                "    </child>\n" +
                "</root>";

        final String header = "id,first,last";
        final String[] headerArray = new String[]{"id", "first", "last"};

        final OMElement xmlPayload = AXIOMUtil.stringToOM(payloadXml);
        Iterable<OMElement> iterable = xmlPayload::getChildElements;
        Stream<OMElement> childElementStream = StreamSupport.stream(iterable.spliterator(), false);
        final CsvCollector csvCollector = new CsvCollector(mc, headerArray);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn(header);
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SUPPRESS_ESCAPE_CHARACTERS)).thenReturn("false");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.APPLY_QUOTES)).thenReturn("false");
        when(mc.getXmlChildElementsStream()).thenReturn(childElementStream);
        when(mc.collectToCsv(headerArray,false, false)).thenReturn(csvCollector);

        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        XmlToCsvTransformer xmlToCsvTransformer = new XmlToCsvTransformer();
        xmlToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "id,first,last\n" +
                "1,Denise,Callum\n" +
                "2,Randee,Abbot-Doyle\n" +
                "3,Sandland,Latashia\n";

        Assertions.assertEquals(expectedPayload, setPayload);
    }

}
