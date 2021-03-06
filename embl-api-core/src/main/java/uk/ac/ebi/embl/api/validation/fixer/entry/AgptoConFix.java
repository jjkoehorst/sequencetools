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

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.AGPValidationCheck;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Description("")
public class AgptoConFix extends EntryValidationCheck
{
	static final HashMap<String, String> gapType= new HashMap<String, String>();
	static final HashMap<String, String> linkageEvidence= new HashMap<String, String>();

	static
	{
		gapType.put("unknown","unknown");
		gapType.put("repeatnoLinkage","repeat between scaffolds");
		gapType.put("scaffold","within scaffold");
		gapType.put("contig","between scaffolds");
		gapType.put("centromere","centromere");
		gapType.put("short_arm","short arm");
		gapType.put("heterochromatin","heterochromatin");
		gapType.put("telomere","telomere");
		gapType.put("repeatwithLinkage","repeat within scaffold");
		linkageEvidence.put("pcr","pcr");
		linkageEvidence.put("na","unspecified");
		linkageEvidence.put("paired-ends","paired-ends");
		linkageEvidence.put("align_genus","align genus");
		linkageEvidence.put("align_xgenus","align xgenus");
		linkageEvidence.put("align_trnscpt","align trnscpt");
		linkageEvidence.put("within_clone","within clone");
		linkageEvidence.put("clone_contig","clone contig");
		linkageEvidence.put("map","map");
		linkageEvidence.put("strobe","strobe");
		linkageEvidence.put("unspecified","unspecified");
    }

	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();
		FeatureFactory featureFactory=new FeatureFactory();
		QualifierFactory qualifierFactory=new QualifierFactory();
		LocationFactory locationFactory=new LocationFactory();
		ArrayList<Location> components=new ArrayList<Location>();

		if (entry == null||getEntryDAOUtils()==null||entry.getAgpRows().size()==0||!FileType.AGP.equals(getEmblEntryValidationPlanProperty().fileType.get()))
		{
			return result;
		}
		
        AGPValidationCheck check=new AGPValidationCheck();
        try{
        check.setEmblEntryValidationPlanProperty(getEmblEntryValidationPlanProperty());
        check.setEntryDAOUtils(getEntryDAOUtils());
        if(!check.check(entry).isValid())
        	return result;
        
		for(AgpRow agpRow:entry.getSortedAGPRows())
		{
			Long object_begin=agpRow.getObject_beg();
			Long object_end=agpRow.getObject_end();
			Long component_begin=agpRow.getComponent_beg();
			Long component_end=agpRow.getComponent_end();
			String orientation=agpRow.getOrientation();
			Long gap_length=agpRow.getGap_length();
			String gap_type=agpRow.getGap_type();
			List<String> linkage_evidences=agpRow.getLinkageevidence();
			String component_acc=agpRow.getComponent_acc();
			
		  if(agpRow.isGap())
		  {
			 Feature assembly_gapFeature=featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
			 Order<Location> locations=new Order<Location>();
			 LocalRange location=locationFactory.createLocalRange((long)object_begin,(long)object_end);
			 locations.addLocation(location);
			 locations.setSimpleLocation(true);
			 assembly_gapFeature.setLocations(locations);
			 Qualifier gap_typeQualifier=qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
			 if("repeat".equals(gap_type))
			 {
			 if(linkage_evidences==null||linkage_evidences.isEmpty())
			 {
				 gap_type="repeatnoLinkage";
			 }
			 else
			 {
				 gap_type="repeatwithLinkage";
			 }
			 }
			 String gapTypeValue=gapType.get(gap_type);
			 gap_typeQualifier.setValue(gapTypeValue);
			 assembly_gapFeature.addQualifier(gap_typeQualifier);
			 for(String linkage_evidence:linkage_evidences)
			 {
			 Qualifier linkage_evidenceQualifier=qualifierFactory.createQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME);
			 String linkage_evidenceQualifierValue=linkageEvidence.get(linkage_evidence);
			 linkage_evidenceQualifier.setValue(linkage_evidenceQualifierValue);
			 assembly_gapFeature.addQualifier(linkage_evidenceQualifier);
			 }
			 Qualifier estimated_lengthQualifier=qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
			 if("U".equals(agpRow.getComponent_type_id()))
			   {
				 estimated_lengthQualifier.setValue("unknown");
				 components.add(locationFactory.createUnknownGap(agpRow.getGap_length()));
			   }
			 else
			 {
				 estimated_lengthQualifier.setValue(new String(new Long(gap_length).toString()));
				 components.add(locationFactory.createGap(agpRow.getGap_length()));
			 }
			 assembly_gapFeature.addQualifier(estimated_lengthQualifier);
			 entry.addFeature(assembly_gapFeature); 			  
		  }
		  else
		  {
			  if(component_acc==null)
				  continue;
			  String[] accessionWithVersion=component_acc.split("\\.");
			  String accession=accessionWithVersion[0];
			  Integer version=new Integer(accessionWithVersion[1]);
			  Location remoteLocation=locationFactory.createRemoteRange(accession, version, (long)component_begin, (long)component_end);
			  if(orientation=="-"||orientation=="minus")
				  remoteLocation.setComplement(true);
			  components.add(remoteLocation);
		  }
			  
   		  //reportMessage(Severity.FIX, entry.getOrigin(), ACCESSION_FIX_ID, agpRow.getComponent_acc(),componentID,agpRow.getObject());
		}
		  		
		  entry.setDataClass(Entry.CON_DATACLASS);
		  if(entry.getSequence()==null)
		  {
			  SequenceFactory sequenceFactory=new SequenceFactory();
			  entry.setSequence(sequenceFactory.createSequence());
		  }
		  entry.getSequence().addContigs(components);
        }
        catch (SQLException e)
		{
			e.printStackTrace();
			throw new ValidationEngineException(e);
		}
		return result;
	}

}
