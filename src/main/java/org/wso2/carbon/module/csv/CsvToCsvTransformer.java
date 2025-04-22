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

import org.wso2.carbon.module.core.SimpleMediator;
import org.wso2.carbon.module.core.SimpleMessageContext;
import org.wso2.carbon.module.core.models.CsvPayloadInfo;
import org.wso2.carbon.module.csv.constant.Constants;
import org.wso2.carbon.module.csv.constant.HeaderAvailability;
import org.wso2.carbon.module.csv.constant.OrderingType;
import org.wso2.carbon.module.csv.constant.ParameterKey;
import org.wso2.carbon.module.csv.util.CsvTransformer;

import java.util.Optional;
import java.util.stream.Stream;

import static org.wso2.carbon.module.csv.util.CsvTransformer.resolveColumnIndex;
import static org.wso2.carbon.module.csv.util.CsvTransformer.skipColumnsSingleRow;
import static org.wso2.carbon.module.csv.util.PropertyReader.*;

/**
 * Transformer to transform CSV content to another CSV content.
 */
public class CsvToCsvTransformer extends SimpleMediator {

    @Override
    public void mediate(SimpleMessageContext mc) {

        final HeaderAvailability headerAvailability =
                getEnumParam(mc, ParameterKey.IS_HEADER_PRESENT, HeaderAvailability.class, HeaderAvailability.ABSENT);
        final Optional<Character> valueSeparatorOptional = getCharParam(mc, ParameterKey.VALUE_SEPARATOR);
        final char valueSeparator = valueSeparatorOptional.orElse(Constants.DEFAULT_CSV_SEPARATOR);
        final boolean skipHeader = getBooleanParam(mc, ParameterKey.SKIP_HEADER);
        final Optional<Integer> dataRowsToSkip = getIntegerParam(mc, ParameterKey.DATA_ROWS_TO_SKIP);
        final Optional<String> columnsToSkipQuery = getStringParam(mc, ParameterKey.COLUMNS_TO_SKIP);
        final Optional<String> orderByColumnQuery = getStringParam(mc, ParameterKey.ORDER_BY_COLUMN);
        final OrderingType columnOrdering =
                getEnumParam(mc, ParameterKey.SORT_COLUMNS_BY_ORDERING, OrderingType.class, OrderingType.ASCENDING);
        final Optional<String> customHeader = getStringParam(mc, ParameterKey.CUSTOM_HEADER);
        final Optional<Character> customValueSeparator = getCharParam(mc, ParameterKey.CUSTOM_VALUE_SEPARATOR);
        final boolean suppressEscapeCharacters = getBooleanParam(mc, ParameterKey.SUPPRESS_ESCAPE_CHARACTERS);
        final boolean applyQuotes = getBooleanParam(mc, ParameterKey.APPLY_QUOTES);

        CsvPayloadInfo payloadInfo = new CsvPayloadInfo();
        if (headerAvailability == HeaderAvailability.PRESENT || customHeader.isPresent()) {
            payloadInfo = mc.getCsvPayloadInfo(valueSeparator);
        }
        String[] header = CsvTransformer.getHeader(payloadInfo, headerAvailability);
        int linesToSkip = CsvTransformer.getLinesToSkip(headerAvailability, dataRowsToSkip.orElse(0));
        Stream<String[]> csvArrayStream = mc.getCsvArrayStream(linesToSkip, valueSeparator);
        if (orderByColumnQuery.isPresent()) {
            csvArrayStream =
                    reorder(resolveColumnIndex(orderByColumnQuery.get(), header), csvArrayStream, columnOrdering);
        }
        if (columnsToSkipQuery.isPresent()) {
            csvArrayStream = CsvTransformer
                    .skipColumns(payloadInfo.getNumberOfColumns(), columnsToSkipQuery.get(), csvArrayStream, header);
        }

        String[] resultHeader = null;
        if (headerAvailability == HeaderAvailability.PRESENT) {
            if (customHeader.isPresent()) {
                resultHeader = getCustomHeader(customHeader.get());
            } else if (!skipHeader) {
                resultHeader = header;
            }
        } else if (customHeader.isPresent()) {
            resultHeader = getCustomHeader(customHeader.get());
        }
        if (columnsToSkipQuery.isPresent() && resultHeader != null && !customHeader.isPresent()) {
            resultHeader = skipColumnsSingleRow(payloadInfo.getNumberOfColumns(), columnsToSkipQuery.get(),
                    payloadInfo.getFirstRow(), resultHeader);
        }
        csvArrayStream
                .collect(mc.collectToCsv(resultHeader, customValueSeparator.orElse(Constants.DEFAULT_CSV_SEPARATOR), suppressEscapeCharacters, applyQuotes));
    }

    /**
     * Generate header from the given custom header
     * @param customHeader Custom header to use.
     * @return Generated header.
     */
    private String[] getCustomHeader(String customHeader) {

        return customHeader.split(Constants.DEFAULT_EXPRESSION_SPLITTER);
    }

    /**
     * Reorder the given CSV.
     *
     * @param orderByColumn  Ordering column index.
     * @param csvArrayStream CSV array stream.
     * @param orderingType   Ordering type.
     * @return Ordered CSV array stream.
     */
    private Stream<String[]> reorder(int orderByColumn, Stream<String[]> csvArrayStream, OrderingType orderingType) {
        if (orderByColumn >= 0) {
            csvArrayStream = csvArrayStream.sorted((row1, row2) -> {
                String val1 = getCellValue(row1, orderByColumn);
                String val2 = getCellValue(row2, orderByColumn);
                int comparisonResult = val1.compareTo(val2);
                if (orderingType == OrderingType.DESCENDING) {
                    comparisonResult = -comparisonResult;
                }
                return comparisonResult;
            });
        }
        return csvArrayStream;
    }

    /**
     * Returns the value for the given cell. Returns empty if cell not found.
     *
     * @param row   Row of the cell.
     * @param index Index of the cell in the given row.
     * @return Value of the given cell.
     */
    private String getCellValue(String[] row, int index) {
        final int rowLength = row.length;
        if (index >= rowLength) {
            return "";
        } else {
            return row[index];
        }
    }

}
