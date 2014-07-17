package *.designer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.nhaarman.listviewanimations.widget.DynamicListView;
import *.Colors;
import *.FrgInput.OnInputFinishedListener;
import *.FrgKeyboard;
import *.R;
import *.Utils;
import *.UtilsDate;
import *.designer.ReportDesignerHelper.Condition;
import *.view.EditText;

public class ActReportDesignerStructure extends ReportDesignerSettings {	
	private int structureType = 0; // 0 - lines 1-columns 2 - cond , 3 - sort

	private static int conditionValuesSelected;
	private int lastSelectedPosition;



    public static ActReportDesignerStructure makeInstance(List<ReportDesignerField> listFields, int type)
    {
        ActReportDesignerStructure frgStructure = new ActReportDesignerStructure();
        frgStructure.setListFields(listFields);
        frgStructure.setStructureType(type);
        return frgStructure;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.act_designer_frg_structure, container, false);
		listItems = (DynamicListView) mainView.findViewById(R.id.act_designer_frg_structure_list);		
		final OnItemLongClickListener itemLongClickListener =  listItems.getOnItemLongClickListener();
		listItems.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				itemLongClickListener.onItemLongClick(arg0, arg1, arg2, arg3);
				Utils.vibrateShort();
				return true;
			}
		});

		sla = new ListItemsSelectAdapter();
		refreshSwipeAdapter();
		lastSelectedPosition = -1;
		return mainView;
	}
	
	


	private class ListItemsSelectAdapter extends ListItemsAdapter implements DynamicListView.Swappable
	{
				
		@Override
		public void swapItems(final int locationOne, final int locationTwo) {
			ReportDesignerField temp = (ReportDesignerField) getItem(locationOne);
			listFields.set(locationOne, (ReportDesignerField) getItem(locationTwo));
			listFields.set(locationTwo, temp);
		}

		@Override
        public void remove(int position) {
			moveItemToSelect(position);

		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (lastSelectedPosition > listFields.size() - 1) {
				lastSelectedPosition = listFields.size() - 1;
			}

			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = convertView;
			if (view == null) {
				view = inflater.inflate(R.layout.act_designer_frg_structure_item, parent, false);
			}

			TextView textViewName = (TextView) view.findViewById(R.id.act_designer_frg_structure_list_item);
			textViewName.setText(listFields.get(position).getNameHeader());			
			ImageView imageSort = (ImageView) view.findViewById(R.id.act_designer_frg_structure_sort);
			if (listFields.get(position).isNumeric())
			{
				imageSort.setImageDrawable(listFields.get(position).getSortDirection() ? getResources().getDrawable(R.drawable.icon_asc_num) : getResources().getDrawable(R.drawable.icon_desc_num));
			}
			else
			{
				imageSort.setImageDrawable(listFields.get(position).getSortDirection() ? getResources().getDrawable(R.drawable.icon_asc) : getResources().getDrawable(R.drawable.icon_desc));
			}
			imageSort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastSelectedPosition = position;
                    changeSortDirection(listFields.get(position));
                }
            });

			final EditText textViewValue = (EditText) view.findViewById(R.id.act_designer_frg_structure_condition_value);

			if (lastSelectedPosition == position)
			{
				textViewValue.setFocusable(true);
			}
			textViewValue.setOnInputFinishedListener(null);
			textViewValue.clearTextChangedListeners();
			textViewValue.setText("");
			if (listFields.get(position).isDate() && structureType==2) // only for conditions
			{
                textViewValue.setInputMode(EditText.INPUT_MODE_DIALOG);
                textViewValue.setDateFormat(ReportDesignerHelper.showDateFormat);
                String tempDate = UtilsDate.getFormatedDateAsString(ReportDesignerHelper.showDateFormat, listFields.get(position).getConditionValue());
                if (tempDate.isEmpty()) {
                    if (listFields.get(position).isStartDay()) {
                        tempDate = UtilsDate.getFormatedDateAsString(ReportDesignerHelper.showDateFormat, UtilsDate.getTimestampStartCurrentDay());
                    } else {
                        tempDate = UtilsDate.getFormatedDateAsString(ReportDesignerHelper.showDateFormat, UtilsDate.getTimestampEndCurrentDay());
                    }
                }
                textViewValue.setText(tempDate);
            }
			else {
                textViewValue.setInputMode(EditText.INPUT_MODE_KEYBOARD);
                if ((listFields.get(position).isNumeric()) && (listFields.get(position).getReportFields().getformatType() != 1)) {
                    textViewValue.setKeyboardDefaultLayout(FrgKeyboard.KEYBOARD_NUMERIC_FLOAT);
                    textViewValue.setKeyboardLayouts(FrgKeyboard.KEYBOARD_NUMERIC_FLOAT);

                } else if (listFields.get(position).getReportFields().getformatType() == 1) {
                    textViewValue.setKeyboardDefaultLayout(FrgKeyboard.KEYBOARD_NUMERIC);
                    textViewValue.setKeyboardLayouts(FrgKeyboard.KEYBOARD_NUMERIC);
                } else {
                    textViewValue.setKeyboardDefaultLayout(FrgKeyboard.KEYBOARD_CYRILLIC);
                    textViewValue.setKeyboardLayouts(FrgKeyboard.KEYBOARD_CYRILLIC | FrgKeyboard.KEYBOARD_LATIN | FrgKeyboard.KEYBOARD_NUMERIC_FLOAT);
                }

                textViewValue.setText(listFields.get(position).getFormated(listFields.get(position).getConditionValue(), false));
				textViewValue.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
                        if (inputChecked(listFields.get(position),textViewValue.getEditableText().toString())) {
                            textViewValue.setTextColor(Colors.COLOR_BLACK);
                            changeConditionValue(listFields.get(position), textViewValue.getEditableText().toString());
                        }
                        else {textViewValue.setTextColor(Colors.COLOR_TEXT_ERROR);}
					}
				});

				
			}

			textViewValue.setOnInputFinishedListener(new OnInputFinishedListener() {
				@Override
				public boolean onInputFinished() {
					changeConditionValue(listFields.get(position), textViewValue.getEditableText().toString());
					((ActReportDesigner) getActivity()).tryCloseKeyboard();
					return true;
				}
			});

			TextView textViewCondition = (TextView) view.findViewById(R.id.act_designer_frg_structure_condition);
			textViewCondition.setText(listFields.get(position).getCondition().representView);

			textViewCondition.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    lastSelectedPosition = position;
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    // Create and show the dialog.
                    conditionDialog newFragment = new conditionDialog();
                    newFragment.setReportFieldsCond(listFields.get(position));
                    newFragment.show(ft, "dialog");

                }
            });

			textViewValue.setVisibility(View.VISIBLE);
			textViewCondition.setVisibility(View.VISIBLE);
			imageSort.setVisibility(View.VISIBLE);

			switch (structureType) {
			case 1: // 1-columns
			{
				textViewValue.setVisibility(View.GONE);
				textViewCondition.setVisibility(View.GONE);
				imageSort.setVisibility(View.GONE);
				break;
			}

			case 2: // 2 - cond
			{
				imageSort.setVisibility(View.GONE);
				break;
			}
			case 3: // 3 - sort
			{
				textViewValue.setVisibility(View.GONE);
				textViewCondition.setVisibility(View.GONE);
				break;
			}
			default: // 0 - lines
				textViewValue.setVisibility(View.GONE);
				textViewCondition.setVisibility(View.GONE);
				imageSort.setVisibility(View.GONE);
				break;
			}			
			return view;
		}
	}

    private boolean inputChecked(ReportDesignerField rdField, String inputString) {
        if (inputString == null) {
            return true;
        }
        if (inputString.length() == 0) {
            return true;
        }
        String patternString = "";
        if (rdField.isNumeric()&& (rdField.getReportFields().getformatType() != 1)) {
            if (rdField.getReportFields().getformatType() == 3) // price
            {
                patternString = "\\d*\\.{0,1}\\d{0,2}";
            } else if (rdField.getReportFields().getformatType() == 4) // quantity
            {
                patternString = "\\d*\\.{0,1}\\d{0,3}";
            }
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(inputString);
            boolean valid = matcher.matches();
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    private void changeSortDirection(ReportDesignerField reportFields)
	{
		reportFields.setSortDirection(!reportFields.getSortDirection());
		refresh();
	}

	private void changeConditionValue(ReportDesignerField reportFields, String string)
	{		
		reportFields.setConditionValue(reportFields.getFormatedSQL(string));
	}

	@Override
	public void setListFields(List<ReportDesignerField> listFields)
	{
		super.setListFields(listFields);
		lastSelectedPosition = -1;
	}

    private void moveItemToSelect(int position) {
        moveItemTo(((ActReportDesigner) getActivity()).frgProperties.frgSelect, position);
    }
	

	@Override
	public void addItem(ReportDesignerField item)
	{
		if (structureType == 1 && ((item.getReportFields().getformatType() == 3) || (item.getReportFields().getformatType() == 4))) {
			item.setCountResult(true);
		}
		super.addItem(item);
        listItems.smoothScrollToPosition(listFields.size()-1);
		swipeAdapter.notifyDataSetChanged();		
	}

	public void setStructureType(int type)
	{
		this.structureType = type;
	}



	@SuppressLint("ValidFragment")
	public class conditionDialog extends DialogFragment {
		private ReportDesignerField reportFieldsCond;

		public void setReportFieldsCond(ReportDesignerField reportFieldsCond)
		{
			this.reportFieldsCond = reportFieldsCond;
		}

		@NonNull
        @Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.rd_frg_structure_conditions_dialog_header)
				.setSingleChoiceItems(Condition.getRepresent(), 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						conditionValuesSelected = which;
						reportFieldsCond.setCondition(Condition.values()[conditionValuesSelected]);
						refresh();
						dismiss();
					}
				})
                .setNegativeButton(getString(R.string.btn_cancel), null)
				.create();
		}
	}

}
