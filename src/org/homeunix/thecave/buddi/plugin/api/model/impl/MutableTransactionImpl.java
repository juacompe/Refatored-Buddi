/*
 * Created on Aug 23, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.plugin.api.model.impl;

import java.util.Date;

import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.Transaction;
import org.homeunix.thecave.buddi.plugin.api.model.ImmutableTransaction;
import org.homeunix.thecave.buddi.plugin.api.model.MutableSource;
import org.homeunix.thecave.buddi.plugin.api.model.MutableTransaction;

public class MutableTransactionImpl extends MutableModelObjectImpl implements MutableTransaction {

	public MutableTransactionImpl(Transaction transaction) {
		super(transaction);
	}
	
	public Transaction getTransaction(){
		return (Transaction) getRaw();
	}
	
	public boolean isCleared() {
		return getTransaction().isCleared();
	}
	public Date getDate() {
		return getTransaction().getDate();
	}
	public String getDescription() {
		return getTransaction().getDescription();
	}
	public String getMemo() {
		return getTransaction().getMemo();
	}
	public String getNumber() {
		return getTransaction().getNumber();
	}
	public boolean isReconciled() {
		return getTransaction().isReconciled();
	}
	public boolean isScheduled() {
		return getTransaction().isScheduled();
	}
	public ImmutableSourceImpl getFrom(){
		if (getTransaction().getFrom() instanceof Account)
			return new ImmutableAccountImpl((Account) getTransaction().getFrom());
		if (getTransaction().getFrom() instanceof BudgetCategory)
			return new ImmutableBudgetCategoryImpl((BudgetCategory) getTransaction().getFrom());
		return null;
	}
	public ImmutableSourceImpl getTo(){
		if (getTransaction().getTo() instanceof Account)
			return new ImmutableAccountImpl((Account) getTransaction().getTo());
		if (getTransaction().getTo() instanceof BudgetCategory)
			return new ImmutableBudgetCategoryImpl((BudgetCategory) getTransaction().getTo());
		return null;
	}
	public long getBalanceFrom() {
		return getTransaction().getBalanceFrom();
	}
	public long getBalanceTo() {
		return getTransaction().getBalanceTo();
	}
	public boolean isInflow(){
		return getTransaction().isInflow();
	}
	public long getAmount(){
		return getTransaction().getAmount();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ImmutableTransaction)
			return getUid().equals(((ImmutableTransaction) obj).getUid());
		return false;
	}

	public void setAmount(long amount) {
		getTransaction().setAmount(amount);
	}

	public void setCleared(boolean cleared) {
		getTransaction().setCleared(cleared);
	}

	public void setDate(Date date) {
		getTransaction().setDate(date);
	}

	public void setDescription(String description) {
		getTransaction().setDescription(description);
	}

	public void setFrom(MutableSource from) {
		if (from == null)
			getTransaction().setFrom(null);
		else
			getTransaction().setFrom(from.getSource());
	}

	public void setMemo(String memo) {
		getTransaction().setMemo(memo);
	}

	public void setNumber(String number) {
		getTransaction().setNumber(number);
	}

	public void setReconciled(boolean reconciled) {
		getTransaction().setReconciled(reconciled);
	}

	public void setTo(MutableSource to) {
		if (to == null)
			getTransaction().setTo(null);
		else
			getTransaction().setTo(to.getSource());
	}
}
