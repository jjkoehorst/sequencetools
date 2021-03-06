package uk.ac.ebi.embl.api.validation;

import java.sql.SQLException;

import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public interface EmblEntryValidationCheck<E> extends ValidationCheck<E>
{
	void setEmblEntryValidationPlanProperty(EmblEntryValidationPlanProperty property) throws SQLException;
    
    EmblEntryValidationPlanProperty getEmblEntryValidationPlanProperty();
    
    void setEntryDAOUtils(EntryDAOUtils daoUtils);
    
    EntryDAOUtils getEntryDAOUtils();

	EraproDAOUtils getEraproDAOUtils();
	
    void setEraproDAOUtils(EraproDAOUtils daoUtils);

}
