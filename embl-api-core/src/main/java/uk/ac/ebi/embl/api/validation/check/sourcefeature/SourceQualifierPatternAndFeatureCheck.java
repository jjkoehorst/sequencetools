/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataRow;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@CheckDataSet("source-qualifier-pattern-feature.tsv")
@Description("Feature \\\"{0}\\\" is required when qualifier \\\"{1}\\\" matches pattern \\\"{2}\\\"")
public class SourceQualifierPatternAndFeatureCheck extends EntryValidationCheck {

	@CheckDataRow
	private DataRow dataRow;
	
	private final static String MESSAGE_ID = "SourceQualifierPatternAndFeatureCheck";

	public SourceQualifierPatternAndFeatureCheck() {
	}

	SourceQualifierPatternAndFeatureCheck(DataRow dataRow) {
		this.dataRow = dataRow;
	}

	public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        Collection<Feature> sources = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);

        String featureName = dataRow.getString(0);
		String qualifierName = dataRow.getString(1);
		String valuePattern = dataRow.getString(2);

		if (StringUtils.isEmpty(qualifierName)
				|| StringUtils.isEmpty(valuePattern)
				|| StringUtils.isEmpty(featureName)) {
			return result;
		}

		Pattern pattern = Pattern.compile(valuePattern);

		for (Feature source : sources) {
			Collection<Qualifier> qualifiers = source.getQualifiers(qualifierName);
			if (qualifiers.isEmpty()) {
				return result;
			}
			for (Qualifier qualifier : qualifiers) {
				String value = qualifier.getValue();
				if (value == null) {
					continue;
				}
				if (pattern.matcher(value).matches()) {
					if (!SequenceEntryUtils.isFeatureAvailable(featureName,entry)) {
						reportError(entry.getOrigin(), MESSAGE_ID, featureName, qualifierName, valuePattern);
					}
				}
			}
		}
		return result;
	}

}
