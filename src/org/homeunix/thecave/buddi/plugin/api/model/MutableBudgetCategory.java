/*
 * Created on Aug 22, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.plugin.api.model;

import org.homeunix.thecave.buddi.i18n.keys.BudgetPeriodType;

public interface MutableBudgetCategory extends ImmutableBudgetCategory, MutableSource {
	/**
	 * Sets the budget period type associated with this budget category.
	 * @return
	 */
	public void setBudgetPeriodType(BudgetPeriodType periodType);
	
	/**
	 * Sets whether this budget category represents income or not.
	 * @param income
	 */
	public void setIncome(boolean income);
	
	/**
	 * Sets the parent of this budget category.  Set to null for no parent.
	 * @param parent
	 */
	public void setParent(MutableBudgetCategory parent);
}