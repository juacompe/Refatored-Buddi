/*
 * Created on May 6, 2006 by wyatt
 */
package org.homeunix.drummer.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.homeunix.drummer.Buddi;
import org.homeunix.drummer.controller.Translate;
import org.homeunix.drummer.controller.TranslateKeys;
import org.homeunix.drummer.model.Account;
import org.homeunix.drummer.view.components.EditableTransaction;
import org.homeunix.drummer.view.components.TransactionCellRenderer;
import org.homeunix.drummer.view.components.text.JHintTextField;

public abstract class TransactionsFrameLayout extends AbstractFrame {
	public static final long serialVersionUID = 0;

	protected static final Map<Account, TransactionsFrameLayout> transactionInstances = new HashMap<Account, TransactionsFrameLayout>();
	
	protected final JList list;
	protected final JScrollPane listScroller;
	
	protected final EditableTransaction editableTransaction;
	protected final JButton recordButton;
	protected final JButton clearButton;
	protected final JButton deleteButton;
	protected final JHintTextField searchField;
	protected final JButton clearSearchField;
	protected final JLabel creditRemaining;
	
	public TransactionsFrameLayout(Account account){
		if (transactionInstances.get(account) != null)
			transactionInstances.get(account).setVisible(false);

		transactionInstances.put(account, this);
		
		//Set up the transaction list
		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		creditRemaining = new JLabel();
		
		TransactionCellRenderer renderer = new TransactionCellRenderer();
		renderer.setAccount(account);
		list.setCellRenderer(renderer);
		
		listScroller = new JScrollPane(list);
		listScroller.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 10, 5, 10),
				listScroller.getBorder()));
						
		JPanel scrollPanel = new JPanel(new BorderLayout());
		scrollPanel.setBorder(BorderFactory.createTitledBorder(""));		
		scrollPanel.add(listScroller, BorderLayout.CENTER);
		
		//Set up the editing portion
		editableTransaction = new EditableTransaction(this);
		editableTransaction.updateContent();
		
		recordButton = new JButton(Translate.getInstance().get(TranslateKeys.RECORD));
		clearButton = new JButton(Translate.getInstance().get(TranslateKeys.CLEAR));
		deleteButton = new JButton(Translate.getInstance().get(TranslateKeys.DELETE));
		searchField = new JHintTextField(Translate.getInstance().get(TranslateKeys.DEFAULT_SEARCH));
		clearSearchField = new JButton("x");
		
		recordButton.setPreferredSize(new Dimension(Math.max(100, recordButton.getPreferredSize().width), recordButton.getPreferredSize().height));
		clearButton.setPreferredSize(new Dimension(Math.max(100, clearButton.getPreferredSize().width), clearButton.getPreferredSize().height));
		deleteButton.setPreferredSize(new Dimension(Math.max(100, deleteButton.getPreferredSize().width), deleteButton.getPreferredSize().height));
		searchField.setPreferredSize(new Dimension(200, searchField.getPreferredSize().height));

		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		searchPanel.add(searchField);
		searchPanel.add(clearSearchField);
		
		JPanel creditRemainingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		creditRemainingPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		creditRemainingPanel.add(creditRemaining);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(searchPanel, BorderLayout.EAST);
		topPanel.add(creditRemainingPanel, BorderLayout.WEST);
		
		this.getRootPane().setDefaultButton(recordButton);
		
		JPanel buttonPanelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanelRight.add(clearButton);
		buttonPanelRight.add(recordButton);

		JPanel buttonPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanelLeft.add(deleteButton);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(buttonPanelRight, BorderLayout.EAST);
		buttonPanel.add(buttonPanelLeft, BorderLayout.WEST);

		editableTransaction.setBorder(BorderFactory.createEmptyBorder(2, 8, 5, 8));
		
		scrollPanel.add(topPanel, BorderLayout.NORTH);
		scrollPanel.add(editableTransaction, BorderLayout.SOUTH);
		
		JPanel mainPanel = new JPanel(); 
		mainPanel.setLayout(new BorderLayout());

		mainPanel.add(scrollPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
		
		if (Buddi.isMac()){
			list.putClientProperty("Quaqua.List.style", "striped");
			listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			clearSearchField.putClientProperty("Quaqua.Button.style", "square");
			mainPanel.setBorder(BorderFactory.createEmptyBorder(7, 17, 17, 17));
		}
		
		//Call the method to add actions to the buttons
		initActions();
		
		//Show the window
		openWindow();
	}
		
	public abstract Account getAccount();
}
