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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Description("RP line location range \"{0}\" - \"{1}\" has been fixed")
public class ReferencePositionFix extends EntryValidationCheck {

	private final static String FIX_ID = "ReferencePositionFix";

	public ReferencePositionFix() {
	}

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();
		if (entry == null||entry.getSequence()==null) {
			return result;
		}
		List<Reference> references = entry.getReferences();
		long sequenceLength = entry.getSequence().getLength();

		for (Reference reference : references) {
			List<LocalRange> locations = reference.getLocations()
					.getLocations();

			for (LocalRange location : locations) {
				boolean fixed = false;
				long beginPosition = location.getBeginPosition();
				long endPosition = location.getEndPosition();
				if (beginPosition < 1) {
					location.setBeginPosition(new Long(1));
					fixed = true;

				} else if (beginPosition > sequenceLength) {
					location.setBeginPosition(new Long(sequenceLength));
					fixed = true;
				}
				if (endPosition < 1) {
					location.setEndPosition((new Long(1)));
					fixed = true;
				} else if (endPosition > sequenceLength) {
					location.setEndPosition(new Long(sequenceLength));
					fixed = true;
				}
				if (fixed)
					reportMessage(Severity.FIX, reference.getOrigin(), FIX_ID,
							beginPosition, endPosition,
							location.getBeginPosition(),
							location.getEndPosition());
			}

		}

		return result;
	}

}
