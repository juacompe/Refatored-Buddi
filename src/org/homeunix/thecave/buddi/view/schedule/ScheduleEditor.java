/*
 * Created on May 6, 2006 by wyatt
 */
package org.homeunix.thecave.buddi.view.schedule;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.homeunix.thecave.buddi.Const;
import org.homeunix.thecave.buddi.i18n.BuddiKeys;
import org.homeunix.thecave.buddi.i18n.keys.ButtonKeys;
import org.homeunix.thecave.buddi.i18n.keys.ScheduleFrequency;
import org.homeunix.thecave.buddi.model.DataModel;
import org.homeunix.thecave.buddi.model.ScheduledTransaction;
import org.homeunix.thecave.buddi.model.Transaction;
import org.homeunix.thecave.buddi.model.prefs.PrefsModel;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.TransactionEditor;
import org.homeunix.thecave.buddi.view.swing.TranslatorListCellRenderer;
import org.homeunix.thecave.moss.swing.components.JScrollingComboBox;
import org.homeunix.thecave.moss.swing.hint.JHintTextArea;
import org.homeunix.thecave.moss.swing.hint.JHintTextField;
import org.homeunix.thecave.moss.swing.window.MossDocumentFrame;
import org.homeunix.thecave.moss.swing.window.MossPanel;
import org.homeunix.thecave.moss.util.Log;
import org.homeunix.thecave.moss.util.OperatingSystemUtil;
import org.jdesktop.swingx.JXDatePicker;

public class ScheduleEditor extends MossPanel {
	public static final long serialVersionUID = 0;

	private ScheduledTransaction schedule = null;

	private final JHintTextField scheduleName;
	private final JHintTextArea message;
	private final JScrollingComboBox frequencyPulldown;
	private final JXDatePicker startDateChooser;
	private final TransactionEditor transactionEditor;

	//Each card
	private final MonthlyByDateCard monthly;
	private final OneDayEveryMonthCard oneDayMonthly;
	private final WeeklyCard weekly;
	private final BiWeeklyCard biWeekly;
	private final WeekdayCard everyWeekday;
	private final DailyCard everyDay;
	private final MultipleWeeksEachMonthCard multipleWeeksMonthly;
	private final MultipleMonthsEachYearCard multipleMonthsYearly;

	private final Map<String, ScheduleCard> cardMap = new HashMap<String, ScheduleCard>();
	
	private final CardLayout cardLayout;
	private final JPanel cardHolder;

	public ScheduleEditor(MossDocumentFrame parentFrame){
		super(true);

		startDateChooser = new JXDatePicker();
		transactionEditor = new TransactionEditor((DataModel) parentFrame.getDocument(), null, true);

		scheduleName = new JHintTextField(TextFormatter.getTranslation(BuddiKeys.SCHEDULED_ACTION_NAME));

		message = new JHintTextArea(TextFormatter.getTranslation(BuddiKeys.HINT_MESSAGE));

		//This is where we create all the check boxes

		//Create the check boxes for Multiple Weeks each Month.


		//This is where we give the Frequency dropdown options
		frequencyPulldown = new JScrollingComboBox(ScheduleFrequency.values());
		
		
		monthly = new MonthlyByDateCard();
		oneDayMonthly = new OneDayEveryMonthCard();
		weekly = new WeeklyCard();
		biWeekly = new BiWeeklyCard();
		everyWeekday = new WeekdayCard();
		everyDay = new DailyCard();
		multipleWeeksMonthly = new MultipleWeeksEachMonthCard();
		multipleMonthsYearly = new MultipleMonthsEachYearCard();

		cardLayout = new CardLayout();
		cardHolder = new JPanel(cardLayout);
		
		open();
	}

	protected String getType(){
		return TextFormatter.getTranslation(BuddiKeys.ACCOUNT);
	}

	public void init() {
		message.setToolTipText(TextFormatter.getTranslation(BuddiKeys.TOOLTIP_SCHEDULED_MESSAGE));
		
		JScrollPane messageScroller = new JScrollPane(message);
		messageScroller.setPreferredSize(new Dimension(100, 100));

		startDateChooser.setEditor(new JFormattedTextField(new SimpleDateFormat(PrefsModel.getInstance().getDateFormat())));
		startDateChooser.setDate(new Date());

		Dimension textSize = new Dimension(Math.max(250, scheduleName.getPreferredSize().width), scheduleName.getPreferredSize().height);
		startDateChooser.setPreferredSize(textSize);
		scheduleName.setPreferredSize(textSize);

		//The namePanel is where we keep the Schedule Name.
		JPanel namePanel = new JPanel(new BorderLayout());
		namePanel.add(scheduleName, BorderLayout.EAST);
		namePanel.add(messageScroller, BorderLayout.SOUTH);

		//The schedulePanel is where we keep all the options for scheduling
		JPanel schedulePanel = new JPanel(new BorderLayout());
		schedulePanel.setBorder(BorderFactory.createTitledBorder((String) null));

		//The top part of the schedule information
		JLabel intro = new JLabel(TextFormatter.getTranslation(BuddiKeys.REPEAT_THIS_ACTION));
		JPanel introHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
		introHolder.add(intro);
		introHolder.add(frequencyPulldown);

		//The middle part of the schedule information
		JPanel startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		startDatePanel.add(new JLabel(TextFormatter.getTranslation(BuddiKeys.STARTING_ON)));
		startDatePanel.add(startDateChooser);

		//Put the schedule panel together properly...
		schedulePanel.add(introHolder, BorderLayout.NORTH);
		schedulePanel.add(startDatePanel, BorderLayout.CENTER);
		schedulePanel.add(cardHolder, BorderLayout.SOUTH);

		//We new define each 'card' separately, with the correct
		// options for each frequency.  This is then added to the
		// card holder for quick random access when the frequency 
		// pulldown changes.
		cardMap.put(ScheduleFrequency.SCHEDULE_FREQUENCY_MONTHLY_BY_DATE.toString(), monthly);
		cardMap.put(ScheduleFrequency.SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK.toString(), oneDayMonthly);
		cardMap.put(ScheduleFrequency.SCHEDULE_FREQUENCY_WEEKLY.toString(), weekly);
		cardMap.put(ScheduleFrequency.SCHEDULE_FREQUENCY_BIWEEKLY.toString(), biWeekly);
		cardMap.put(ScheduleFrequency.SCHEDULE_FREQUENCY_EVERY_WEEKDAY.toString(), everyWeekday);
		cardMap.put(ScheduleFrequency.SCHEDULE_FREQUENCY_EVERY_DAY.toString(), everyDay);
		cardMap.put(ScheduleFrequency.SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH.toString(), multipleWeeksMonthly);
		cardMap.put(ScheduleFrequency.SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR.toString(), multipleMonthsYearly);
		
		cardHolder.add(monthly, ScheduleFrequency.SCHEDULE_FREQUENCY_MONTHLY_BY_DATE.toString());
		cardHolder.add(oneDayMonthly, ScheduleFrequency.SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK.toString());
		cardHolder.add(weekly, ScheduleFrequency.SCHEDULE_FREQUENCY_WEEKLY.toString());
		cardHolder.add(biWeekly, ScheduleFrequency.SCHEDULE_FREQUENCY_BIWEEKLY.toString());
		cardHolder.add(new JPanel(), ScheduleFrequency.SCHEDULE_FREQUENCY_EVERY_WEEKDAY.toString());
		cardHolder.add(new JPanel(), ScheduleFrequency.SCHEDULE_FREQUENCY_EVERY_DAY.toString());
		cardHolder.add(multipleWeeksMonthly, ScheduleFrequency.SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH.toString());
		cardHolder.add(multipleMonthsYearly, ScheduleFrequency.SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR.toString());


		//Done all the fancy stuff... now just put all the panels together
		JPanel transactionPanel = new JPanel(new BorderLayout());
		transactionPanel.setBorder(BorderFactory.createEmptyBorder(7, 0, 10, 0));
		transactionPanel.add(transactionEditor, BorderLayout.NORTH);
		transactionPanel.add(schedulePanel, BorderLayout.CENTER);

//		JPanel textPanelSpacer = new JPanel(new BorderLayout());

		this.setLayout(new BorderLayout());
		this.add(namePanel, BorderLayout.NORTH);
		this.add(transactionPanel, BorderLayout.CENTER);


//		JPanel mainBorderPanel = new JPanel();
//		mainBorderPanel.setLayout(new BorderLayout());
//		mainBorderPanel.setBorder(BorderFactory.createTitledBorder((String) null));
//		mainBorderPanel.add(textPanelSpacer);

		if (OperatingSystemUtil.isMac()){
//			textPanelSpacer.setBorder(BorderFactory.createEmptyBorder(7, 17, 17, 17));
			messageScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			messageScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}

//		this.add(mainPanel, BorderLayout.CENTER);

		//If we are viewing existing transactions, we cannot 
		// modify the schedule at all.
		//TODO Make a more intelligent enabled check thingy
		startDateChooser.setEnabled(schedule == null);
		frequencyPulldown.setEnabled(schedule == null);
		monthly.setEnabled(schedule == null);
		oneDayMonthly.setEnabled(schedule == null);
		weekly.setEnabled(schedule == null);
		biWeekly.setEnabled(schedule == null);
		everyWeekday.setEnabled(schedule == null);
		everyDay.setEnabled(schedule == null);
		multipleWeeksMonthly.setEnabled(schedule == null);
		multipleMonthsYearly.setEnabled(schedule == null);

		updateSchedulePulldown();

		frequencyPulldown.setRenderer(new TranslatorListCellRenderer());
		frequencyPulldown.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				ScheduleEditor.this.updateSchedulePulldown();
			}
		});
	}

	private void updateSchedulePulldown(){
		if (frequencyPulldown.getSelectedItem() != null) {
			cardLayout.show(cardHolder, frequencyPulldown.getSelectedItem().toString());

			if (Const.DEVEL) Log.info("Showing card " + frequencyPulldown.getSelectedItem().toString());
		}
	}

	private boolean ensureInfoCorrect(){

		//System.out.println(Calendar.getInstance().get(startDateChooser.getDate()));


		//We must have filled in at least the name and the date.
		if ((scheduleName.getValue().toString().length() == 0)
				|| (startDateChooser.getDate() == null)){
			return false;
		}

		//If we're just fillinf in the transaction, we need at least
		// amount, description, to, and from.
		if ((transactionEditor.getAmount() != 0)
				&& (transactionEditor.getDescription().length() > 0)
				&& (transactionEditor.getTo() != null)
				&& (transactionEditor.getFrom() != null)){
			return true;
		}

		//If the message is filled in, we can let the action succeed
		// without the transaction being filled out.  However, if any
		// part of the transaction is filled in, it all must be.
		if ((message.getValue().toString().length() > 0)
				&& (transactionEditor.getAmount() == 0)
				&& (transactionEditor.getDescription().length() == 0)
				&& (transactionEditor.getTo() == null)
				&& (transactionEditor.getFrom() == null)){
			return true;
		}

		return false;
	}

	public ScheduledTransaction getUpdatedSchedule(){
		//TODO Save schedule

		if (ScheduleEditor.this.ensureInfoCorrect()){
			String[] options = new String[2];
			options[0] = TextFormatter.getTranslation(ButtonKeys.BUTTON_OK);
			options[1] = TextFormatter.getTranslation(ButtonKeys.BUTTON_CANCEL);

			if (!ScheduleEditor.this.startDateChooser.getDate().before(new Date())
//					|| schedule != null		//If the schedule has already been defined, we won't bother people again 
					|| JOptionPane.showOptionDialog(ScheduleEditor.this, 
							TextFormatter.getTranslation(BuddiKeys.START_DATE_IN_THE_PAST), 
							TextFormatter.getTranslation(BuddiKeys.START_DATE_IN_THE_PAST_TITLE), 
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE,
							null,
							options,
							options[0]) == JOptionPane.OK_OPTION){
//				schedule.set

				schedule.setScheduleName(scheduleName.getValue().toString());
				schedule.setMessage(message.getValue().toString());
				schedule.setAmount(transactionEditor.getAmount());
				schedule.setDescription(transactionEditor.getDescription());
				schedule.setNumber(transactionEditor.getNumber());
				schedule.setMemo(transactionEditor.getMemo());
				schedule.setTo(transactionEditor.getTo());
				schedule.setFrom(transactionEditor.getFrom());
				schedule.setCleared(transactionEditor.isCleared());
				schedule.setReconciled(transactionEditor.isReconciled());

				// We should not have to save this, as it cannot be modified.
				schedule.setStartDate(startDateChooser.getDate());
				schedule.setFrequencyType(frequencyPulldown.getSelectedItem().toString());
				schedule.setScheduleDay(getSelectedCard().getScheduleDay());
				schedule.setScheduleWeek(getSelectedCard().getScheduleWeek());
				schedule.setScheduleMonth(getSelectedCard().getScheduleMonth());
//				DataInstance.getInstance().saveDataModel();

//				ScheduleEditor.this.saveSchedule();
//				ScheduleController.checkForScheduledActions();  //TODO
//				MainFrame.getInstance().updateContent();
//				TransactionsFrame.updateAllTransactionWindows();
//				ScheduleModifyDialog.this.closeWindow();
				
				return schedule;
			}
			else
				if (Const.DEVEL) Log.debug("Cancelled from either start date in the past, or info not correct");
		}
		else {
			String[] options = new String[1];
			options[0] = TextFormatter.getTranslation(ButtonKeys.BUTTON_OK);

			JOptionPane.showOptionDialog(ScheduleEditor.this, 
					TextFormatter.getTranslation(BuddiKeys.SCHEDULED_NOT_ENOUGH_INFO),
					TextFormatter.getTranslation(BuddiKeys.SCHEDULED_NOT_ENOUGH_INFO_TITLE),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE,
					null,
					options,
					options[0]);
		}

		return null;
	}
	
	private ScheduleCard getSelectedCard(){
		if (frequencyPulldown.getSelectedItem() == null)
			return null;
		return cardMap.get(frequencyPulldown.getSelectedItem().toString());
	}
	
	public void loadSchedule(ScheduledTransaction s){
		if (s != null){
			transactionEditor.updateContent();
			updateSchedulePulldown();

			//Load the changeable fields, including Transaction
			scheduleName.setValue(s.getScheduleName());
			if (s.getMessage() != null)
				message.setValue(s.getMessage());

			Transaction t = (Transaction) s;

			if (s.getDescription() != null
					&& s.getTo() != null
					&& s.getFrom() != null){
				transactionEditor.setTransaction(t, true);
			}
			else {
				transactionEditor.setVisible(false);
			}

			//Load the schedule pulldowns, based on which type of 
			// schedule we're following.
			startDateChooser.setDate(s.getStartDate());
			frequencyPulldown.setSelectedItem(s.getFrequencyType());
			
			//Load the schedule in the selected card.
			if (getSelectedCard() != null)
				getSelectedCard().loadSchedule(s);
		}
	}
}