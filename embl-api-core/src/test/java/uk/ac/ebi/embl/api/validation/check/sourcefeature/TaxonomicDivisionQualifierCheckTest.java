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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.taxonomy.Taxon;
import uk.ac.ebi.embl.api.taxonomy.TaxonFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.TaxonomicDivisionQualifierCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class TaxonomicDivisionQualifierCheckTest
{

	private Entry entry;
	private SourceFeature sourceFeature;
	private TaxonomicDivisionQualifierCheck check;
	private EmblEntryValidationPlanProperty property;
	private TaxonHelper taxonHelper;
	private FeatureFactory featureFactory;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();
		sourceFeature = featureFactory.createSourceFeature();
		property=new EmblEntryValidationPlanProperty();
		taxonHelper=createMock(TaxonHelper.class);
		property.taxonHelper.set(taxonHelper);
		DataSet dataSet = new DataSet();
		DataRow dataRow1 = new DataRow("cultivar", "PLN");
		DataRow dataRow2 = new DataRow("serovar", "PRO");
		DataRow dataRow3 = new DataRow("variety", "PLN,FUN");
		dataSet.addRow(dataRow1);
		dataSet.addRow(dataRow2);
		dataSet.addRow(dataRow3);
		check = new TaxonomicDivisionQualifierCheck(dataSet);
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoFeatures()
	{
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoPrimarysource()
	{
		entry.addFeature(featureFactory.createFeature(Feature.CDS_FEATURE_NAME));
		assertTrue(check.check(entry).isValid());
	}
	@Test
	public void testCheck_NoDivisionInvalidQualifier() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonsByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		Feature feature=featureFactory.createFeature("");
		feature.setSingleQualifierValue("cultivar", "Akio Tani");
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_DivisionNoQualifier() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		taxon.setDivision("PLN");
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonsByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.count("TaxonomicDivisionQualifierCheck_1", Severity.ERROR));
	}

	@Test
	public void testCheck_DivisionInvalidQualifier1() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		taxon.setDivision("HUM");
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonsByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		Feature feature=featureFactory.createFeature("");
		feature.setSingleQualifierValue("cultivar", "Akio Tani");
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("TaxonomicDivisionQualifierCheck_1", Severity.ERROR));
	}

	@Test
	public void testCheck_DivisionValidQualifier() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		taxon.setDivision("PLN");
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonsByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		Feature feature=featureFactory.createFeature("");
		feature.setSingleQualifierValue("cultivar", "Akio Tani");
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("TaxonomicDivisionNotQualifierCheck_1", Severity.ERROR));
	}
	
	@Test
	public void testCheck_DivisionInvalidQualifier2() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		taxon.setDivision("HUM");
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonsByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		Feature feature=featureFactory.createFeature("");
		feature.setSingleQualifierValue("variety", "Akio Tani");
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("TaxonomicDivisionQualifierCheck_1", Severity.ERROR));
		assertEquals("Qualifier \"variety\" can only exist if taxonomic division has one of the values \"PLN,FUN\".",
				result.getMessages().iterator().next().getMessage());	
	}
}
