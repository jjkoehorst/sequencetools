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
package uk.ac.ebi.embl.flatfile.writer.genomeassembly;

import java.io.Writer;
import java.util.ArrayList;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.Field;

public class UnplacedFileWriter extends GenomeAssemblyFileWriter
{
	GenomeAssemblyRecord gaRecord;

	protected UnplacedFileWriter(GenomeAssemblyRecord gaRecord)
	{
		this.gaRecord = gaRecord;
	}

	@Override
	public boolean write(Writer writer)
	{
		if (gaRecord == null)
			return false;
		@SuppressWarnings("unchecked")
		ArrayList<Field> fields = (ArrayList<Field>) gaRecord.getFields();
		for (Field field : fields)
		{
			writeRow(field.getValue(), null, null, null, writer);
		}
		return true;
	}
}
