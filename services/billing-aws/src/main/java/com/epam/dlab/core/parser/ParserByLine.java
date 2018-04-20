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

package com.epam.dlab.core.parser;

import com.epam.dlab.core.ModuleBase;
import com.epam.dlab.core.aggregate.AggregateGranularity;
import com.epam.dlab.exception.AdapterException;
import com.epam.dlab.exception.GenericException;
import com.epam.dlab.exception.InitializationException;
import com.epam.dlab.exception.ParseException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Abstract module of parser by the line.<br>
 * See description of {@link ModuleBase} how to create your own parser.
 */
public abstract class ParserByLine extends ParserBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParserByLine.class);

	/**
	 * Parse the header of source data and return it.
	 *
	 * @return the header of source data.
	 * @throws AdapterException is being thrown
	 * @throws ParseException is being thrown
	 */
	public abstract List<String> parseHeader() throws AdapterException, ParseException;

	/**
	 * Parse the row from source line and return result row.
	 *
	 * @param line the source line.
	 * @return the parsed row.
	 * @throws ParseException is being thrown
	 */
	public abstract List<String> parseRow(String line) throws ParseException;

	/**
	 * Read the line from adapter and return it.
	 *
	 * @return the parsed row from adapterIn.
	 * @throws AdapterException is being thrown
	 * @throws ParseException is being thrown
	 */
	@JsonIgnore
	protected String getNextRow() throws AdapterException {
		String line = getAdapterIn().readLine();
		if (line == null) {
			return null;
		}
		getCurrentStatistics().incrRowReaded();
		return line;
	}

	/**
	 * Initialize ParserBase.
	 *
	 * @throws InitializationException is being thrown
	 * @throws AdapterException is being thrown
	 * @throws ParseException is being thrown
	 */
	protected boolean init() throws InitializationException, AdapterException, ParseException {
		getAdapterIn().open();
		LOGGER.debug("Source data has multy entry {}", getAdapterIn().hasMultyEntry());
		if (!initEntry()) {
			return false;
		}
		getAdapterOut().open();
		return true;
	}

	/**
	 * Initialize for each entry ParserBase.
	 *
	 * @throws InitializationException is being thrown
	 * @throws AdapterException is being thrown
	 * @throws ParseException is being thrown
	 */
	private boolean initEntry() throws InitializationException, AdapterException, ParseException {
		if (getAdapterIn().hasMultyEntry() && !getAdapterIn().hasEntryData()) {
			return false;
		}
		addStatistics(getAdapterIn().getEntryName());
		getCurrentStatistics().start();

		super.init(parseHeader());
		initialize();
		if (getFilter() != null) {
			getFilter().initialize();
		}
		return true;
	}

	/**
	 * Close adapters.
	 *
	 * @throws AdapterException is being thrown
	 */
	private void closeAdapters(boolean silent) throws AdapterException {
		AdapterException ex = null;
		try {
			getAdapterIn().close();
		} catch (Exception e) {
			if (silent) {
				LOGGER.warn("Cannot close adapterIn. {}", e.getLocalizedMessage(), e);
			} else {
				ex = new AdapterException("Cannot close adapterIn. " + e.getLocalizedMessage(), e);
			}
		}
		try {
			getAdapterOut().close();
		} catch (Exception e) {
			if (silent || ex != null) {
				LOGGER.warn("Cannot close adapterOut. {}", e.getLocalizedMessage(), e);
			} else {
				ex = new AdapterException("Cannot close adapterOut. " + e.getLocalizedMessage(), e);
			}
		}
		if (!silent && ex != null) {
			throw ex;
		}
	}

	/**
	 * Parse the source data to common format and write it to output adapter.
	 *
	 * @throws InitializationException is being thrown
	 * @throws AdapterException is being thrown
	 * @throws ParseException is being thrown
	 */
	public void parse() throws InitializationException, AdapterException, ParseException {
		try {
			if (init()) {
				String line;
				List<String> row;
				ReportLine reportLine;
				do {
					while ((line = getNextRow()) != null) {
						if (getFilter() != null && (line = getFilter().canParse(line)) == null) {
							getCurrentStatistics().incrRowFiltered();
							continue;
						}

						row = parseRow(line);
						if ((getFilter() != null && (row = getFilter().canTransform(row)) == null)) {
							getCurrentStatistics().incrRowFiltered();
							continue;
						}
						if (isCheckedEvaluationCondition(row, line)) {
							continue;
						}
						reportLine = reportLineFromCommonFormat(row, line);

						if (getFilter() != null && (reportLine = getFilter().canAccept(reportLine)) == null) {
							getCurrentStatistics().incrRowFiltered();
							continue;
						}

						getCurrentStatistics().incrRowParsed();
						appendToAggregatorIfSatisfyCondition(reportLine);
					}

					writeRowIfSatisfyCondition();

					if (isAdapterMultyEntryConditionChecked()) {
						break;
					}

				} while (isAdapterDataConditionChecked());
			}
		} catch (GenericException e) {
			closeAdapters(true);
			stopStatisticsIfNotNull();
			throw e;
		} catch (Exception e) {
			closeAdapters(true);
			stopStatisticsIfNotNull();
			throw new ParseException("Unknown parser error. " + e.getLocalizedMessage(), e);
		}

		closeAdapters(false);
		stopStatisticsIfNotNull();
	}

	private boolean isCheckedEvaluationCondition(List<String> row, String line) throws ParseException {
		try {
			if (getCondition() != null && !getCondition().evaluate(row)) {
				getCurrentStatistics().incrRowFiltered();
				return true;
			}
		} catch (ParseException e) {
			throw new ParseException(e.getLocalizedMessage() + "\nEntry name: " + getCurrentStatistics
					().getEntryName() +
					"\nSource line[" +
					getCurrentStatistics().getRowReaded() + "]: " + line, e);
		} catch (Exception e) {
			throw new ParseException("Cannot evaluate condition " + getWhereCondition() + ". " +
					e.getLocalizedMessage() + "\nEntry name: " + getCurrentStatistics().getEntryName()
					+ "\nSource line[" + getCurrentStatistics().getRowReaded() + "]: " + line, e);
		}
		return false;
	}

	private ReportLine reportLineFromCommonFormat(List<String> row, String line) throws ParseException {
		ReportLine reportLine;
		try {
			reportLine = getCommonFormat().toCommonFormat(row);
		} catch (ParseException e) {
			throw new ParseException("Cannot cast row to common format. " +
					e.getLocalizedMessage() + "\nEntry name: " + getCurrentStatistics().getEntryName() +
					"\nSource line[" + getCurrentStatistics().getRowReaded() + "]: " + line, e);
		}
		return reportLine;
	}

	private void appendToAggregatorIfSatisfyCondition(ReportLine reportLine) throws AdapterException {
		if (getAggregate() != AggregateGranularity.NONE) {
			getAggregator().append(reportLine);
		} else {
			getAdapterOut().writeRow(reportLine);
			getCurrentStatistics().incrRowWritten();
		}
	}

	private void writeRowIfSatisfyCondition() throws AdapterException {
		if (getAggregate() != AggregateGranularity.NONE) {
			for (int i = 0; i < getAggregator().size(); i++) {
				getAdapterOut().writeRow(getAggregator().get(i));
				getCurrentStatistics().incrRowWritten();
			}
		}
	}

	private boolean isAdapterMultyEntryConditionChecked() throws AdapterException, ParseException,
			InitializationException {
		if (getAdapterIn().hasMultyEntry()) {
			if (getAdapterIn().openNextEntry()) {
				// Search entry with data
				while (!initEntry()) {
					if (!getAdapterIn().openNextEntry()) {
						break;
					}
				}
			} else {
				return true;
			}
		}
		return false;
	}

	private boolean isAdapterDataConditionChecked() {
		return getAdapterIn().hasMultyEntry() && getAdapterIn().hasEntryData();
	}

	private void stopStatisticsIfNotNull() {
		if (getCurrentStatistics() != null) {
			getCurrentStatistics().stop();
		}
	}
}
