package uk.ac.ebi.embl.api.validation.fixer.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

public class ExclusiveQualifierTransformToNoteQualifierFix extends
		FeatureValidationCheck
{
	@CheckDataSet("exclusive-qualifiers-to-remove.tsv")
	private DataSet exclusiveQualifierSet;

	private final static String REMOVE_QUALIFIER_MESSAGE_ID = "ExclusiveQualifierTransformToNoteQualifierFix";

	public ExclusiveQualifierTransformToNoteQualifierFix()
	{
	}

	ExclusiveQualifierTransformToNoteQualifierFix(DataSet exclusiveQualifierSet)
	{
		this.exclusiveQualifierSet = exclusiveQualifierSet;
	}

	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();

		if (feature == null)
		{
			return result;
		}
		if(feature.getQualifiers().size()==0)
		{
			return result;
		}

		for (DataRow dataRow : exclusiveQualifierSet.getRows())
		{
			String qualifiertoRemove = dataRow.getString(0);
			String exclusiveQualifier = dataRow.getString(1);
			if (SequenceEntryUtils.isQualifierAvailable(qualifiertoRemove, feature) && SequenceEntryUtils.isQualifierAvailable(exclusiveQualifier, feature))
			{
				Qualifier qualifier = feature.getSingleQualifier(qualifiertoRemove);
				String texttoAppend = qualifier.getValue();
				if (SequenceEntryUtils.isQualifierAvailable(Qualifier.NOTE_QUALIFIER_NAME, feature))
				{
					feature.getSingleQualifier(Qualifier.NOTE_QUALIFIER_NAME).setValue(feature.getSingleQualifierValue(Qualifier.NOTE_QUALIFIER_NAME) + ";" + texttoAppend);
				} else
				{
					QualifierFactory qualifierFactory = new QualifierFactory();
					Qualifier noteQualifier = qualifierFactory.createQualifier(Qualifier.NOTE_QUALIFIER_NAME, texttoAppend);
					feature.addQualifier(noteQualifier);

				}
				feature.removeQualifier(qualifier);
				reportMessage(Severity.FIX, feature.getOrigin(), REMOVE_QUALIFIER_MESSAGE_ID, qualifiertoRemove);

			}
		}

		return result;
	}

}
