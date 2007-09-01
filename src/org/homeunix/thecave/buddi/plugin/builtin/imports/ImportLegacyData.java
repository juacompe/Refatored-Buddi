/*
 * Created on Aug 26, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.plugin.builtin.imports;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.homeunix.drummer.model.Account;
import org.homeunix.drummer.model.Category;
import org.homeunix.drummer.model.DataInstance;
import org.homeunix.drummer.model.Schedule;
import org.homeunix.drummer.model.Source;
import org.homeunix.drummer.model.Transaction;
import org.homeunix.drummer.model.Type;
import org.homeunix.drummer.model.impl.DataModelImpl;
import org.homeunix.thecave.buddi.i18n.BuddiKeys;
import org.homeunix.thecave.buddi.model.periods.BudgetPeriodMonthly;
import org.homeunix.thecave.buddi.plugin.api.BuddiImportPlugin;
import org.homeunix.thecave.buddi.plugin.api.exception.ModelException;
import org.homeunix.thecave.buddi.plugin.api.model.MutableAccount;
import org.homeunix.thecave.buddi.plugin.api.model.MutableAccountType;
import org.homeunix.thecave.buddi.plugin.api.model.MutableBudgetCategory;
import org.homeunix.thecave.buddi.plugin.api.model.MutableModel;
import org.homeunix.thecave.buddi.plugin.api.model.MutableModelFactory;
import org.homeunix.thecave.buddi.plugin.api.model.MutableScheduledTransaction;
import org.homeunix.thecave.buddi.plugin.api.model.MutableSource;
import org.homeunix.thecave.buddi.plugin.api.model.MutableTransaction;
import org.homeunix.thecave.moss.exception.DocumentLoadException;
import org.homeunix.thecave.moss.util.Log;
import org.homeunix.thecave.moss.util.Version;

public class ImportLegacyData extends BuddiImportPlugin {

	@Override
	public void importData(MutableModel model, File file) {
		try {
			if (file == null)
				return;

			convert(model, file);
		}
		catch (DocumentLoadException dle){
			Log.error("Error converting data file: ", dle);
		}
	}

	public Version getMaximumVersion() {
		return null;
	}

	public Version getMinimumVersion() {
		return null;
	}

	public String getName() {
		return BuddiKeys.IMPORT_LEGACY_BUDDI_FORMAT.toString();
	}
	
	public void convert(MutableModel model, File oldFile) throws DocumentLoadException {
		try {
			DataInstance.getInstance().loadDataFile(oldFile);
			DataModelImpl oldModel = (DataModelImpl) DataInstance.getInstance().getDataModel();

			Map<Type, MutableAccountType> typeMap = new HashMap<Type, MutableAccountType>();
			Map<MutableAccountType, List<MutableAccount>> typeAccountMap = new HashMap<MutableAccountType, List<MutableAccount>>();
			Map<Category, MutableBudgetCategory> categoryMap = new HashMap<Category, MutableBudgetCategory>();
			Map<Source, MutableSource> sourceMap = new HashMap<Source, MutableSource>();

			for (Object oldTypeObject : oldModel.getAllTypes().getTypes()){
				org.homeunix.drummer.model.Type oldType = (org.homeunix.drummer.model.Type) oldTypeObject;

				if (model.getType(oldType.getName()) == null){
					MutableAccountType newType = MutableModelFactory.createMutableType(model, oldType.getName(), oldType.isCredit());

					typeMap.put(oldType, newType);
					typeAccountMap.put(newType, new LinkedList<MutableAccount>());

					model.addAccountType(newType);
				}
				else {
					typeMap.put(oldType, (MutableAccountType) model.getType(oldType.getName()));
					typeAccountMap.put((MutableAccountType) model.getType(oldType.getName()), new LinkedList<MutableAccount>());				
				}
			}

			
			for (Object oldAccountObject : oldModel.getAllAccounts().getAccounts()) {
				Account oldAccount = (Account) oldAccountObject;

				if (model.getAccount(oldAccount.getName()) == null){
					MutableAccount newAccount = MutableModelFactory.createMutableAccount(model, oldAccount.getName(), oldAccount.getStartingBalance(), typeMap.get(oldAccount.getAccountType()));
					newAccount.setStartingBalance(oldAccount.getStartingBalance());
					newAccount.setDeleted(oldAccount.isDeleted());
					
					model.addAccount(newAccount);
					sourceMap.put(oldAccount, newAccount);
				}
				else {
					System.out.println(oldAccount.getName());
					System.out.println(model.getAccount(oldAccount.getName()));
					sourceMap.put(oldAccount, (MutableAccount) model.getAccount(oldAccount.getName()));
				}
			}

			
			for (Object oldCategoryObject : oldModel.getAllCategories().getCategories()){
				Category oldCategory = (Category) oldCategoryObject;

				if (model.getBudgetCategory(oldCategory.getFullName()) == null){
					MutableBudgetCategory newBudgetCategory = MutableModelFactory.createMutableBudgetCategory(model, oldCategory.getName(), new BudgetPeriodMonthly(), oldCategory.isIncome());
					newBudgetCategory.setDeleted(oldCategory.isDeleted());
					newBudgetCategory.setAmount(new Date(), oldCategory.getBudgetedAmount());

					categoryMap.put(oldCategory, newBudgetCategory);
					sourceMap.put(oldCategory, newBudgetCategory);

					model.addBudgetCategory(newBudgetCategory);
				}
				else {
					categoryMap.put(oldCategory, (MutableBudgetCategory) model.getBudgetCategory(oldCategory.getFullName()));
					sourceMap.put(oldCategory, (MutableBudgetCategory) model.getBudgetCategory(oldCategory.getFullName()));
				}
			}

 
			for (Object oldCategoryObject : oldModel.getAllCategories().getCategories()){
				Category oldCategory = (Category) oldCategoryObject;

				if (oldCategory.getParent() != null){
					Category oldParent = oldCategory.getParent();
					if (model.getBudgetCategory(oldCategory.getName()) != null){
						System.out.println(model.getBudgetCategory(oldCategory.getName()));						
						((MutableBudgetCategory) model.getBudgetCategory(oldCategory.getName())).setParent((MutableBudgetCategory) model.getBudgetCategory(oldParent.getName()));
					}
				}
			}

			
			for (Object oldTransactionObject : oldModel.getAllTransactions().getTransactions()){
				Transaction oldTransaction = (Transaction) oldTransactionObject;

				MutableTransaction newTransaction = MutableModelFactory.createMutableTransaction(
						model, 
						oldTransaction.getDate(), 
						oldTransaction.getDescription(), 
						oldTransaction.getAmount(), 
						sourceMap.get(oldTransaction.getFrom()),
						sourceMap.get(oldTransaction.getTo())
				);
				newTransaction.setClearedFrom(oldTransaction.isCleared());
				newTransaction.setClearedTo(oldTransaction.isCleared());
				newTransaction.setMemo(oldTransaction.getMemo());
				newTransaction.setNumber(oldTransaction.getNumber());
				newTransaction.setReconciledFrom(oldTransaction.isReconciled());
				newTransaction.setReconciledTo(oldTransaction.isReconciled());
				newTransaction.setFrom(sourceMap.get(oldTransaction.getFrom()));
				newTransaction.setTo(sourceMap.get(oldTransaction.getTo()));
				newTransaction.setScheduled(oldTransaction.isScheduled());

				model.addTransaction(newTransaction);
			}

			
			for (Object oldScheduledTransactionObject : oldModel.getAllTransactions().getScheduledTransactions()){
				Schedule oldScheduledTransaction = (Schedule) oldScheduledTransactionObject;

				MutableScheduledTransaction newScheduledTransaction = MutableModelFactory.createMutableScheduledTransaction(model);
				newScheduledTransaction.setClearedFrom(oldScheduledTransaction.isCleared());
				newScheduledTransaction.setClearedTo(oldScheduledTransaction.isCleared());
				newScheduledTransaction.setDate(oldScheduledTransaction.getDate());
				newScheduledTransaction.setDescription(oldScheduledTransaction.getDescription());
				newScheduledTransaction.setEndDate(oldScheduledTransaction.getEndDate());
				newScheduledTransaction.setFrequencyType(oldScheduledTransaction.getFrequencyType());
				newScheduledTransaction.setLastDayCreated(oldScheduledTransaction.getLastDateCreated());
				newScheduledTransaction.setMemo(oldScheduledTransaction.getMemo());
				newScheduledTransaction.setMessage(oldScheduledTransaction.getMessage());
				newScheduledTransaction.setNumber(oldScheduledTransaction.getNumber());
				newScheduledTransaction.setReconciledFrom(oldScheduledTransaction.isReconciled());
				newScheduledTransaction.setReconciledTo(oldScheduledTransaction.isReconciled());
				newScheduledTransaction.setScheduled(oldScheduledTransaction.isScheduled());
				newScheduledTransaction.setScheduleDay(oldScheduledTransaction.getScheduleDay());
				newScheduledTransaction.setScheduleWeek(oldScheduledTransaction.getScheduleWeek());
				newScheduledTransaction.setScheduleMonth(oldScheduledTransaction.getScheduleMonth());
				newScheduledTransaction.setScheduleName(oldScheduledTransaction.getScheduleName());
				newScheduledTransaction.setStartDate(oldScheduledTransaction.getStartDate());
				newScheduledTransaction.setAmount(oldScheduledTransaction.getAmount());
				newScheduledTransaction.setFrom(sourceMap.get(oldScheduledTransaction.getFrom()));
				newScheduledTransaction.setTo(sourceMap.get(oldScheduledTransaction.getTo()));

				model.addScheduledTransaction(newScheduledTransaction);
			}
		}
		catch (ModelException me){
			throw new DocumentLoadException(me);
		}
	}
}