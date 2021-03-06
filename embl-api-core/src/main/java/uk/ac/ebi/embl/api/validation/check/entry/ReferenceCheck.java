/*******************************************************************************
 * Copyright 2012-13 EMBL-EBI, Hinxton outstation
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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.util.ArrayList;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Submitter references mandatory in embl-bank entries")
@ExcludeScope(validationScope={ValidationScope.EPO})
public class ReferenceCheck extends EntryValidationCheck
{
	private static String SUBMITTER_REFERENCECHECK = "ReferenceCheck_1";
	
	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();
		boolean isSubmission = false;
		if (entry == null || !getEmblEntryValidationPlanProperty().validationScope.get().equals(ValidationScope.EMBL) || entry.getDataClass() == Entry.PAT_DATACLASS || entry.getDataClass() == Entry.PRT_DATACLASS)
			return result;
		ArrayList<Reference> references = (ArrayList<Reference>) entry.getReferences();
		for (Reference ref : references)
		{
			Publication pub = ref.getPublication();
			if (pub instanceof Submission)
			{
				isSubmission = true;
			}
		}
		if (!isSubmission)
			reportError(entry.getOrigin(), SUBMITTER_REFERENCECHECK);

		return result;
	}
}
