package *.designer;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import *.App;
import *.FrgDialog;
import *.R;
import *.Utils;

public class ActReportDesignerMain extends Fragment implements OnClickListener {
    private Button buttonForm;
	private int lastSelectedPosition;
	private  List<ReportDesigner> reports;
	private ListView reportslist;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		lastSelectedPosition = -1;
		reports = ((ActReportDesigner) getActivity()).repDesRep.getReports();
		View mainView = inflater.inflate(R.layout.act_designer_frg_main, container, false);
		reportslist = (ListView) mainView.findViewById(R.id.act_designer_frg_main_list_reports);
		ReportsListAdapter rla = new ReportsListAdapter();
		reportslist.setAdapter(rla);
        Button buttonCreate = (Button) mainView.findViewById(R.id.act_designer_frg_main_button_create);
		buttonCreate.setOnClickListener(this);
		buttonForm = (Button) mainView.findViewById(R.id.act_designer_frg_main_button_form);
		buttonForm.setOnClickListener(this);

		setDefaultDesigner();

		if (Utils.isLandscape())
		{
			((ActReportDesigner) getActivity()).enableChangeView(false);
		}
		return mainView;
	}

	private void setDefaultDesigner() {
		this.lastSelectedPosition = -1;	
		if (reports.size() > 0)
		{
			((ActReportDesigner) getActivity()).setCurrentReportDesigner(reports.get(0));
			this.lastSelectedPosition = 0;
		}
		refreshEnableButton();
	}

	public void setLastReportInList()
	{
		if (reports.size() > 0)
		{
			lastSelectedPosition = reports.size() - 1;
		}
	}

	public void setLastselectedPosition(int position)
	{
		lastSelectedPosition = (position > reports.size() - 1) ? -1 : position;
	}

	private class ReportsListAdapter extends BaseAdapter
	{
		@Override
		public int getCount() {
			return reports.size();
		}

		@Override
		public Object getItem(int position) {
			return reports.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(R.layout.act_designer_frg_main_list_reports_listitem, parent, false);
			}
			TextView textView = (TextView) view.findViewById(R.id.act_designer_frg_main_list_report);
			textView.setText(reports.get(position).getName());
			TextView textViewCaption = (TextView) view.findViewById(R.id.act_designer_frg_main_list_report_caption);
			textViewCaption.setText(reports.get(position).getCaption());
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickReportsList(position);
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return onLongClickReportsList(position);
				}
			});
			boolean selected = lastSelectedPosition == position;
			view.setBackgroundColor(selected ? getResources().getColor(R.color.genericListItemPressed) : getResources().getColor(R.color.background));

			return view;
		}
	}

	public void refresh()
	{
		reports = ((ActReportDesigner) getActivity()).getReports();
		this.reportslist.invalidateViews();
		refreshEnableButton();
	}

	@Override
	public void onClick(View button) {
		switch (button.getId()) {
		case R.id.act_designer_frg_main_button_create: {
			((ActReportDesigner) getActivity()).enableChangeView(true);
			setLastselectedPosition(-1);
			refresh();
			break;
		}
		case R.id.act_designer_frg_main_button_form: {
			commonOnClick(lastSelectedPosition);
			((ActReportDesigner) getActivity()).formReport();
			break;
		}
		default:
			break;
		}

	}

	private void refreshEnableButton()
	{
		buttonForm.setEnabled(false);
		if (this.lastSelectedPosition > -1)
		{
			buttonForm.setEnabled(true);
		}

	}

	public int getLastSelectedPosition()
	{
		return this.lastSelectedPosition;
	}

	
	private void commonOnClick(int position)
	{	
		refresh();		
		lastSelectedPosition = (position < reports.size()? position : (reports.size()-1));		
		((ActReportDesigner) getActivity()).setCurrentReportDesigner(lastSelectedPosition > -1? reports.get(lastSelectedPosition):null);
		refreshEnableButton();
	}
	private void commonOnClickShow()
	{
		((ActReportDesigner) getActivity()).enableChangeView(false);
	}
	private void onClickReportsList(int position)
	{
		commonOnClick(position);
		if (Utils.isLandscape())
		{
			commonOnClickShow();
		}
	}

	private boolean onLongClickReportsList(final int position)
	{
		if (Utils.isPortrait())
		{
			FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			longClickDialog longClickDialogFrg = new longClickDialog(position);			
			longClickDialogFrg.show(ft, "dialog");
		}
		else
		{
			deleteDialog(position);
		}
		return false;
	}

	private void deleteDialog(final int position)
	{
		FrgDialog.fragmentize(
			new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.confirmation)).setMessage(String.format(getString(R.string.rd_frg_main_dialog_del_confirm), reports.get(position).getName()) ).setCancelable(true)
				.setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						lastSelectedPosition = position;
						((ActReportDesigner) getActivity()).setCurrentReportDesigner(reports.get(position));
						((ActReportDesigner) getActivity()).delReportDesigner();
						setDefaultDesigner();
						if (Utils.isLandscape())
						{
							((ActReportDesigner) getActivity()).enableChangeView(false);
						}
					}
				}).setNegativeButton(getString(R.string.btn_cancel), null)).show(getActivity().getSupportFragmentManager(), null);
	}

	@SuppressLint("ValidFragment")
	public  class longClickDialog extends DialogFragment {
		private final int position;

		public longClickDialog(int pos) {
			this.position = pos;
		}
		@NonNull
        @Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String items[] = { Utils.getString(R.string.rd_frg_main_dialog_change), Utils.getString(R.string.rd_frg_main_dialog_del) };
			return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.rd_frg_main_dialog_header)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
						{
							commonOnClick(position);
							commonOnClickShow();
							break;
						}
						case 1:
						{
							deleteDialog(position);
							break;
						}						
						}
						dismiss();
					}
				})
                .setNegativeButton(R.string.btn_cancel,null)
				.create();
		}
	}
	
	String generateNameNewReport()
	{
		if (reports == null){return ActReportDesigner.titleRDNew;}
		return checkNewName(0);
		
	}
	private String checkNewName(int index)
	{
		for (ReportDesigner rd : reports)
		{
			if(rd.getName().equals((ActReportDesigner.titleRDNew)+ (index==0? "":"_"+String.valueOf(index))))
			{
				return checkNewName(index+1);
			}
		}
		return (ActReportDesigner.titleRDNew)+(index==0? "":"_"+String.valueOf(index));
	}
}
