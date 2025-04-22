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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wso2.carbon.module.core.SimpleMessageContext;
import org.wso2.carbon.module.core.collectors.CsvCollector;
import org.wso2.carbon.module.core.models.CsvPayloadInfo;
import org.wso2.carbon.module.csv.constant.Constants;
import org.wso2.carbon.module.csv.constant.ParameterKey;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvToCsvTransformerTest {

    @Mock
    private SimpleMessageContext mc;

    @Test
    void testMediate_noConfigurations_sameCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);
        when(mc.getCsvArrayStream(0, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_headerPresent_sameCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR);

        when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_headerPresentGivenCustomHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector =
                new CsvCollector(mc, new String[]{"p", "q", "r"}, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn("p,q,r");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(new String[]{"p", "q", "r"}, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "p,q,r\n" +
                "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_headerPresentSkipHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SKIP_HEADER)).thenReturn("true");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_headerPresentSkipHeaderCustomHeaderGiven_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector =
                new CsvCollector(mc, new String[]{"p", "q", "r"}, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SKIP_HEADER)).thenReturn("true");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn("p,q,r");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(new String[]{"p", "q", "r"}, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "p,q,r\n" +
                "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_headerAbsentCustomHeaderGiven_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector =
                new CsvCollector(mc, new String[]{"p", "q", "r"}, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Absent");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn("p,q,r");
        when(mc.getCsvArrayStream(0, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(new String[]{"p", "q", "r"}, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "p,q,r\n" +
                "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_headerAbsentCustomHeaderNotGiven_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Absent");
        when(mc.getCsvArrayStream(0, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_OrderByColumn_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.ORDER_BY_COLUMN)).thenReturn("2");
        when(mc.getCsvArrayStream(0, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "2,2,4\n" +
                "1,3,3\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_OrderByColumnOrderAscending_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.ORDER_BY_COLUMN)).thenReturn("2");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SORT_COLUMNS_BY_ORDERING)).thenReturn("Ascending");
        when(mc.getCsvArrayStream(0, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "2,2,4\n" +
                "1,3,3\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_OrderByColumnOrderDescending_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.ORDER_BY_COLUMN)).thenReturn("2");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SORT_COLUMNS_BY_ORDERING)).thenReturn("Descending");
        when(mc.getCsvArrayStream(0, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "1,3,3\n" +
                "2,2,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_OrderByColumnWithHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.ORDER_BY_COLUMN)).thenReturn("2");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "2,2,4\n" +
                "1,3,3\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_OrderByColumnWithHeaderWithSkipHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.ORDER_BY_COLUMN)).thenReturn("2");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SKIP_HEADER)).thenReturn("true");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "2,2,4\n" +
                "1,3,3\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_OrderByColumnNameWithHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.ORDER_BY_COLUMN)).thenReturn("\"b\"");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "2,2,4\n" +
                "1,3,3\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_OrderByInvalidColumnNameWithHeader_sameCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.ORDER_BY_COLUMN)).thenReturn("\"bbbb\"");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "1,3,3\n" +
                "2,2,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_OrderByColumnNameIntegerNameWithHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"2", "1", "3"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.ORDER_BY_COLUMN)).thenReturn("\"1\"");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SKIP_HEADER)).thenReturn("true");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "2,2,4\n" +
                "1,3,3\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipDataRows_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.DATA_ROWS_TO_SKIP)).thenReturn("1");
        when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(1, 2).stream());
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipDataRowsHeaderPresent_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.DATA_ROWS_TO_SKIP)).thenReturn("1");
        when(mc.getCsvArrayStream(2, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(2, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipDataRowsHeaderPresentSkipHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SKIP_HEADER)).thenReturn("true");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.DATA_ROWS_TO_SKIP)).thenReturn("1");
        when(mc.getCsvArrayStream(2, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(2, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipDataRowsHeaderPresentSkipHeaderCustomHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector =
                new CsvCollector(mc, new String[]{"p", "q", "r"}, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SKIP_HEADER)).thenReturn("true");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_HEADER)).thenReturn("p,q,r");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.DATA_ROWS_TO_SKIP)).thenReturn("1");
        when(mc.getCsvArrayStream(2, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.subList(2, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(new String[]{"p", "q", "r"}, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "p,q,r\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_SeparatorDefined_sameCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, ',');

        lenient().when(mc.lookupTemplateParameter(ParameterKey.VALUE_SEPARATOR)).thenReturn("#");
        when(mc.getCsvArrayStream(0, '#')).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(null, ',', false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_SeparatorDefinedWithHeader_sameCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), ',');

        lenient().when(mc.lookupTemplateParameter(ParameterKey.VALUE_SEPARATOR)).thenReturn("#");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        when(mc.getCsvArrayStream(1, '#')).thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo('#')).thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(csvPayload.get(0), ',', false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "1,2,3\n" +
                "2,3,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipColumns_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.COLUMNS_TO_SKIP)).thenReturn("2");
        when(mc.getCsvArrayStream(0, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "1,3\n" +
                "2,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipColumnsWithHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, new String[]{"a", "c"}, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.COLUMNS_TO_SKIP)).thenReturn("2");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(new String[]{"a", "c"}, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,c\n" +
                "1,3\n" +
                "2,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipColumnsWithHeaderWithSkipHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.COLUMNS_TO_SKIP)).thenReturn("2");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.SKIP_HEADER)).thenReturn("true");
        lenient().when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(null, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "1,3\n" +
                "2,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipColumnsWithHeaderWithHeaderNames_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, new String[]{"a", "c"}, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.COLUMNS_TO_SKIP)).thenReturn("\"b\"");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(new String[]{"a", "c"}, Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,c\n" +
                "1,3\n" +
                "2,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipColumnsWithHeaderComplexQuery_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c", "d", "e", "f", "g", "h"});
        csvPayload.add(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        csvPayload.add(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});

        final CsvCollector csvCollector =
                new CsvCollector(mc, new String[]{"b", "e", "f", "h"}, Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.COLUMNS_TO_SKIP)).thenReturn("\"a\":\"d\",!2,\"g\"");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 8, csvPayload));
        when(mc.collectToCsv(new String[]{"b", "e", "f", "h"}, Constants.DEFAULT_CSV_SEPARATOR, false,false))
                .thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "b,e,f,h\n" +
                "2,5,6,8\n" +
                "2,5,6,8\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipColumnsWithHeaderInvalidHeaderName_sameCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "3", "3"});
        csvPayload.add(new String[]{"2", "2", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.COLUMNS_TO_SKIP)).thenReturn("\"bb\"");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));
        when(mc.collectToCsv(csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c\n" +
                "1,3,3\n" +
                "2,2,4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_skipColumnsWithHeaderComplexQueryInvalidColumnName_sameCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c", "d", "e", "f", "g", "h"});
        csvPayload.add(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        csvPayload.add(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR);

        lenient().when(mc.lookupTemplateParameter(ParameterKey.COLUMNS_TO_SKIP)).thenReturn("\"a\":\"d\",!2,\"gg\"");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 8, csvPayload));
        when(mc.collectToCsv(csvPayload.get(0), Constants.DEFAULT_CSV_SEPARATOR, false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a,b,c,d,e,f,g,h\n" +
                "1,2,3,4,5,6,7,8\n" +
                "1,2,3,4,5,6,7,8\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_customSeparator_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, null, '#');

        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_VALUE_SEPARATOR)).thenReturn("#");
        when(mc.getCsvArrayStream(0, Constants.DEFAULT_CSV_SEPARATOR)).thenReturn(csvPayload.stream());
        when(mc.collectToCsv(null, '#', false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a#b#c\n" +
                "1#2#3\n" +
                "2#3#4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

    @Test
    void testMediate_customSeparatorWithHeader_correctCsvShouldSet() {

        final List<String[]> csvPayload = new ArrayList<>();
        csvPayload.add(new String[]{"a", "b", "c"});
        csvPayload.add(new String[]{"1", "2", "3"});
        csvPayload.add(new String[]{"2", "3", "4"});

        final CsvCollector csvCollector = new CsvCollector(mc, csvPayload.get(0), '#');

        lenient().when(mc.lookupTemplateParameter(ParameterKey.CUSTOM_VALUE_SEPARATOR)).thenReturn("#");
        lenient().when(mc.lookupTemplateParameter(ParameterKey.IS_HEADER_PRESENT)).thenReturn("Present");
        lenient().when(mc.getCsvArrayStream(1, Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(csvPayload.subList(1, 3).stream());
        when(mc.getCsvPayloadInfo(Constants.DEFAULT_CSV_SEPARATOR))
                .thenReturn(new CsvPayloadInfo(csvPayload.get(0), 3, csvPayload));

        when(mc.collectToCsv(csvPayload.get(0), '#', false,false)).thenReturn(csvCollector);
        ArgumentCaptor<String> payloadSetArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CsvToCsvTransformer csvToCsvTransformer = new CsvToCsvTransformer();
        csvToCsvTransformer.mediate(mc);

        verify(mc).setTextPayload(payloadSetArgumentCaptor.capture());
        String setPayload = payloadSetArgumentCaptor.getValue();

        final String expectedPayload = "a#b#c\n" +
                "1#2#3\n" +
                "2#3#4\n";
        Assertions.assertEquals(expectedPayload, setPayload);
    }

}
