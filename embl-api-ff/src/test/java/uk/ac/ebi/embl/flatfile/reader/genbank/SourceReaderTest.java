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

import java.io.IOException;

import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class SourceReaderTest extends GenbankReaderTest {

	public void testReadWithoutCommonName() throws IOException {
		initLineReader(
			"SOURCE      dsfdsfjsdlkfslkdfjdsl\n"
		);
		ValidationResult result = (new SourceReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
    }

	public void testReadWithCommonNameWrapped() throws IOException {
		initLineReader(
			"SOURCE      hello (you\n" +
			"            too)\n"
		);
		SourceReader reader = (new SourceReader(lineReader));
		ValidationResult result = reader.read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				"you too",
				reader.getCache().getCommonName("hello"));
    }	

	public void testReadWithCommonNameNoWrapped() throws IOException {
		initLineReader(
			"SOURCE      hello (A/equine/Ibadan/6/91(H3N8))\n"
		);
		SourceReader reader = (new SourceReader(lineReader));
		ValidationResult result = reader.read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				"A/equine/Ibadan/6/91(H3N8)",
				reader.getCache().getCommonName("hello"));
    }		
	
	public void testRead_EmptyLine() throws IOException {
		initLineReader(
				"SOURCE\n"
			);
			ValidationResult result = (new SourceReader(lineReader)).read(entry);
			assertEquals(0, result.count(Severity.ERROR));
	}	
}
