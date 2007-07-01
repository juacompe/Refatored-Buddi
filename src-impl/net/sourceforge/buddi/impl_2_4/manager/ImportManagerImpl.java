package net.sourceforge.buddi.impl_2_4.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.sourceforge.buddi.api.exception.ValidationException;
import net.sourceforge.buddi.api.manager.ImportManager;
import net.sourceforge.buddi.api.model.ImmutableAccount;
import net.sourceforge.buddi.api.model.ImmutableCategory;
import net.sourceforge.buddi.api.model.ImmutableTransaction;
import net.sourceforge.buddi.api.model.ImmutableType;
import net.sourceforge.buddi.api.model.MutableAccount;
import net.sourceforge.buddi.api.model.MutableCategory;
import net.sourceforge.buddi.api.model.MutableTransaction;
import net.sourceforge.buddi.api.model.MutableType;
import net.sourceforge.buddi.impl_2_4.model.MutableAccountImpl;
import net.sourceforge.buddi.impl_2_4.model.MutableCategoryImpl;
import net.sourceforge.buddi.impl_2_4.model.MutableTransactionImpl;
import net.sourceforge.buddi.impl_2_4.model.MutableTypeImpl;

import org.homeunix.drummer.controller.SourceController;
import org.homeunix.drummer.controller.TypeController;
import org.homeunix.drummer.model.Account;
import org.homeunix.drummer.model.Category;
import org.homeunix.drummer.model.ModelFactory;
import org.homeunix.drummer.model.Transaction;
import org.homeunix.drummer.view.MainFrame;
import org.homeunix.drummer.view.TransactionsFrame;

public class ImportManagerImpl extends DataManagerImpl implements ImportManager {

    public ImportManagerImpl(Account selectedAccount, Category selectedCategory, Transaction selectedTransaction) {
        super(selectedAccount, selectedCategory, selectedTransaction);
    }

    private List<MutableTransactionImpl> temporaryTransactionList = new ArrayList<MutableTransactionImpl>();
    private List<MutableCategoryImpl> temporaryCategoryList = new ArrayList<MutableCategoryImpl>();
    private List<MutableAccountImpl> temporaryAccountList = new ArrayList<MutableAccountImpl>();
    private List<MutableTypeImpl> temporaryTypeList = new ArrayList<MutableTypeImpl>();

    public MutableTransaction createTransaction() {
        MutableTransactionImpl mutableTransactionImpl = new MutableTransactionImpl(ModelFactory.eINSTANCE.createTransaction());
        temporaryTransactionList.add(mutableTransactionImpl);
        return mutableTransactionImpl;
    }

    public MutableCategory createCategory() {
        MutableCategoryImpl mutableCategoryImpl = new MutableCategoryImpl(ModelFactory.eINSTANCE.createCategory());
        temporaryCategoryList.add(mutableCategoryImpl);
        return mutableCategoryImpl;
    }

    public MutableAccount createAccount() {
        MutableAccountImpl mutableAccountImpl = new MutableAccountImpl(ModelFactory.eINSTANCE.createAccount());
        temporaryAccountList.add(mutableAccountImpl);
        return mutableAccountImpl;
    }
    
    public MutableType createType() {
    	MutableTypeImpl mutableTypeImpl = new MutableTypeImpl();
    	temporaryTypeList.add(mutableTypeImpl);
    	return mutableTypeImpl;
    }

    public void saveChanges() throws ValidationException {
    	for (MutableTypeImpl mutableTypeImpl : temporaryTypeList) {
			mutableTypeImpl.validate();
			TypeController.addType(mutableTypeImpl.getImpl());
		}
    	
        for(MutableAccountImpl mutableAccountImpl: temporaryAccountList) {
            mutableAccountImpl.validate();
            SourceController.addAccount(mutableAccountImpl.getImpl());
        }
        
        for(MutableCategoryImpl mutableCategoryImpl: temporaryCategoryList) {
            mutableCategoryImpl.validate();
            SourceController.addCategory(mutableCategoryImpl.getImpl());
        }
        
        Collection<Transaction> transactionsToCommit = new HashSet<Transaction>();
        for(MutableTransactionImpl mutableTransactionImpl: temporaryTransactionList) {
            mutableTransactionImpl.validate();
//            TransactionController.addTransaction(mutableTransactionImpl.getImpl());O
            transactionsToCommit.add(mutableTransactionImpl.getImpl());
        }
        
        TransactionsFrame.addToTransactionListModel(transactionsToCommit);
        
        MainFrame.getInstance().getAccountListPanel().updateContent();
        MainFrame.getInstance().getCategoryListPanel().updateContent();
//        TransactionsFrame.updateAllTransactionWindows();
    }

    public void rollbackChanges() {
        temporaryTransactionList.clear();
        temporaryCategoryList.clear();
        temporaryAccountList.clear();
        temporaryTypeList.clear();
    }
    
    public void rollbackTransaction(ImmutableTransaction t){
    	temporaryTransactionList.remove(t);
    }
    
    public void rollbackAccount(ImmutableAccount a){
    	temporaryAccountList.remove(a);
    }
    
    public void rollbackCategory(ImmutableCategory c){
    	temporaryCategoryList.remove(c);
    }
    
    public void rollbackType(ImmutableType t){
    	temporaryTypeList.remove(t);
    }

    public ImmutableCategory findCategoryByName(String name) {
        if (null == name)
        {
            return null;
        }

        for(ImmutableCategory immutableCategory: temporaryCategoryList) {
            if(name.compareToIgnoreCase(immutableCategory.getName()) == 0)
                return immutableCategory;
        }

        return super.findCategoryByName(name);
    }

    public ImmutableAccount findAccountByName(String name) {
        if (null == name)
        {
            return null;
        }

        for(ImmutableAccount immutableAccount: temporaryAccountList) {
            if(name.compareToIgnoreCase(immutableAccount.getName()) == 0)
                return immutableAccount;
        }

        return super.findAccountByName(name);
    }

    public Collection<ImmutableCategory> getCategories() {
        Collection<ImmutableCategory> categories = super.getCategories();
        categories.addAll(temporaryCategoryList);
        return categories;
    }

    public Collection<ImmutableAccount> getAccounts() {
        Collection<ImmutableAccount> accounts = super.getAccounts();
        accounts.addAll(temporaryAccountList);
        return accounts;
    }

    public Collection<ImmutableTransaction> getTransactions() {
        Collection<ImmutableTransaction> transactions = super.getTransactions();
        transactions.addAll(temporaryTransactionList);
        return transactions;
    }
    
    public Collection<ImmutableType> getTypes() {
    	Collection<ImmutableType> types = super.getTypes();
    	types.addAll(temporaryTypeList);
    	return types;
    }

    public Collection<ImmutableTransaction> getTransactionsForAccount(ImmutableAccount account) {
        Collection<ImmutableTransaction> matchingTransactions = super.getTransactions(account);
        
        for (ImmutableTransaction transaction : temporaryTransactionList) {
            if (transaction.getFrom() != null && transaction.getTo() != null && 
                    (transaction.getFrom().equals(account) || transaction.getTo().equals(account)))
                matchingTransactions.add(transaction);
        }
        
        return matchingTransactions;
    }
}
