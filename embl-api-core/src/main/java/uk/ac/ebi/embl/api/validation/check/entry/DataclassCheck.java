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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.util.ArrayList;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.DataclassProvider;

@Description("Invalid ID Line dataclass \"{0}\""
		+ "\"{0}\" dataclass allowed only for Master entries"
		+ "Keyword dataclass \"{0}\" is not matching with ID line dataclass \"{1}\""
		+ "Accession dataclass \"{0}\" is not matching with ID line dataclass \"{1}\" ")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER})
public class DataclassCheck extends EntryValidationCheck {

	@CheckDataSet("dataclass.tsv")
	private DataSet dataclassDataSet;
	
	@CheckDataSet("keyword_dataclass.tsv")
	private DataSet keyworddataclassDataSet;

	private final static String INVALID_DATACLASS_MESSAGE_ID = "DataclassCheck1";
	private final static String MASTER_DATACLASS_MESSAGE_ID = "DataclassCheck2";
	private final static String KEYWORD_DATACLASS_MESSAGE_ID = "DataclassCheck3";
	private final static String ACCESSION_DATACLASS_MESSAGE_ID = "DataclassCheck4";


	public DataclassCheck() {

	}

	DataclassCheck(DataSet dataclassDataSet, DataSet keyworddataclassDataSet) {
		this.dataclassDataSet = dataclassDataSet;
		this.keyworddataclassDataSet= keyworddataclassDataSet;
	}

	public ValidationResult check(Entry entry) throws ValidationEngineException {
		boolean sflag = false;
		result = new ValidationResult();

		if (entry == null) {
			return result;
		}

		String entryDataclass = entry.getDataClass();

		if (entryDataclass == null) {
			return result;
		}

		for (DataRow row : dataclassDataSet.getRows()) {
			String validDataclass = row.getString(0);
			if (validDataclass.equals(entryDataclass))
				sflag = true;

		}
		if (!sflag) {
			reportError(entry.getOrigin(), INVALID_DATACLASS_MESSAGE_ID, entryDataclass);
		}
		if (entryDataclass.equals(Entry.SET_DATACLASS) && !entry.isMaster()) {
			reportError(entry.getOrigin(), MASTER_DATACLASS_MESSAGE_ID, entryDataclass);
		}
		try{
		String accessionDataclass=DataclassProvider.getAccessionDataclass(entry.getPrimaryAccession());
		ArrayList<String> keywordDataclasses =DataclassProvider.getKeywordDataclass(entry, keyworddataclassDataSet);
		
		if(keywordDataclasses.size()==1&&!keywordDataclasses.get(0).equals("XXX")&&!keywordDataclasses.get(0).equals(entryDataclass))
		{
			reportError(entry.getOrigin(), KEYWORD_DATACLASS_MESSAGE_ID,keywordDataclasses.get(0),entryDataclass );

		}
		
		if(accessionDataclass!=null &&!accessionDataclass.equals(entryDataclass))
		{
			reportError(entry.getOrigin(), ACCESSION_DATACLASS_MESSAGE_ID, accessionDataclass,entryDataclass);
		}
		}catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage());
		}
    	return result;
	}

}
