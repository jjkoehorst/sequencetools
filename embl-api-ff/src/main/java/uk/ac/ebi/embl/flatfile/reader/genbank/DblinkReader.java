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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.DblinkXRefMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file DBlink lines.
 */
public class DblinkReader extends MultiLineBlockReader {
	
	public DblinkReader(LineReader lineReader) {
		super(lineReader, ConcatenateType.CONCATENATE_BREAK);
	}

	@Override
	public String getTag() {
		return GenbankTag.DBLINK_TAG;
	}

	@Override
	protected void read(String block) 
	{
		DblinkXRefMatcher xRefMatcher = new DblinkXRefMatcher(this);
		
		String[] xrefStrings=block.split("\\n");
		
		for(String xrefString :xrefStrings )
		{
		
		if(!xRefMatcher.match(xrefString)) {
			error("FF.1", getTag());
			return;
		}
		XRef xref = xRefMatcher.getXRef();
		xref.setOrigin(getOrigin());				
		entry.addXRef(xref);
		}
	}

}
