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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;

@Description("protein_id \"{0}\" has been deleted for feature \"{1}\", as protein_ids can only be assigned by EMBL")
@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
public class ProteinIdRemovalFix extends EntryValidationCheck {

	private final static String FIX_ID = "ProteinIdRemovalFix_1";

	public ProteinIdRemovalFix() {
	}

	public ValidationResult check(Entry entry) throws ValidationEngineException {
		result = new ValidationResult();

		if (entry == null || entry.getFeatures() == null
				|| entry.getFeatures().size() == 0) {
			return result;
		}
		Integer assemblyLevel = ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 0 : ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 1 : ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 2 :-1;

		if(getEmblEntryValidationPlanProperty().analysis_id.get()==null||assemblyLevel==-1)
		{
			return result;
		}

		EntryDAOUtils entryDAOUtils = getEntryDAOUtils();
		boolean isEntryUpdate=false;
		try{
			isEntryUpdate=entryDAOUtils.isAssemblyEntryUpdate(
				getEmblEntryValidationPlanProperty().analysis_id.get(),
				entry.getSubmitterAccession(),assemblyLevel);
		}catch(SQLException e)
		{
			throw new ValidationEngineException(e);
		}

		if (isEntryUpdate)
			return result;
		
			for (Feature feature : entry.getFeatures()) {

				if (feature.getQualifiers(Qualifier.PROTEIN_ID_QUALIFIER_NAME) != null
						& feature.getQualifiers(
								Qualifier.PROTEIN_ID_QUALIFIER_NAME).size() != 0) {
					for(Qualifier qualifier:feature.getQualifiers(Qualifier.PROTEIN_ID_QUALIFIER_NAME))
					{
					feature.removeQualifier(qualifier);
					reportMessage(Severity.FIX, feature.getOrigin(), FIX_ID,
							qualifier.getValue(),feature.getName());
					}
				}
			}
				return result;
	}

}
