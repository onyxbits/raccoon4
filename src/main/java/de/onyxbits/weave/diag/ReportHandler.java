/*
 * Copyright 2016 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.weave.diag;

/**
 * Knows how to process {@link Report}S.
 * 
 * @author patrick
 * 
 */
public interface ReportHandler {

	/**
	 * File a report.
	 * 
	 * @param report
	 *          the report to file.
	 */
	public void handle(Report report);

	/**
	 * Check if a report has already been filed.
	 * 
	 * @param report
	 *          the report in question.
	 * @return true if the handler is confident that it already filed a report
	 *         under its fingerprint.
	 */
	public boolean isDuplicate(Report report);
}
