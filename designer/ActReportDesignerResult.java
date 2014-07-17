package *.designer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import *.Colors;
import *.R;

@SuppressLint("ValidFragment")
public class ActReportDesignerResult extends Fragment {

	private ReportDesigner rd = null;
	private View mainView;
	
	public ActReportDesignerResult()
	{
		super();
	}

	public ActReportDesignerResult(ReportDesigner currentReportDesigner) {
		super();
		rd = currentReportDesigner;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.act_designer_frg_result, container, false);
		if (rd == null)
		{
			return mainView;
		}
		ReportDesignerHelper.getInstance().generateReport(getActivity(), rd, this);	
		return mainView;
	}
	
	private void requestUpdateLayouts() {
		View v = getView();
		if ((v != null) && (rd != null))
			v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
					updateLayouts();
				}
			});
	}

	private void updateLayouts()
	{
		View mainView = this.getView();
		TableLayout tableViewHeader = (TableLayout) mainView.findViewById(R.id.act_designer_frg_result_table_header);
		TableRow tableRowHeaderTemp = (TableRow) tableViewHeader.getChildAt(0);
		TableRow.LayoutParams lParam;
		TextView textViewTemp;
		TextView textViewHeaderTemp;
		int offset;
		TableLayout tableView = (TableLayout) mainView.findViewById(R.id.act_designer_frg_result_table);
		TableRow tableRowTemp = (TableRow) tableView.getChildAt(0);
		for (int i = 0; i < tableRowTemp.getChildCount(); i++)
		{
			offset = 2;
			if ((i == 0) || (i == tableRowTemp.getChildCount() - 1)){offset = 1;}
			textViewTemp = (TextView) tableRowTemp.getChildAt(i);
			lParam = new TableRow.LayoutParams(textViewTemp.getWidth() - offset, LayoutParams.MATCH_PARENT);
			textViewHeaderTemp = ((TextView) tableRowHeaderTemp.getChildAt(i));
			textViewHeaderTemp.setBackgroundColor(Colors.COLOR_TABLE_DARK_GREY);
			if (i == 0)
			{
				lParam.setMargins(0, 0, 1, 0);
			}
			else if (i == tableRowTemp.getChildCount() - 1)
			{
				lParam.setMargins(1, 0, 0, 0);
			}
			else {
				lParam.setMargins(1, 0, 1, 0);
			}
			textViewHeaderTemp.setLayoutParams(lParam);
			textViewHeaderTemp.setPadding(5, 5, 5, 5);
		}
		tableRowHeaderTemp.requestLayout();
	}

	public void refreshView(TableLayout tViewHeader, TableLayout tView) {		
		tViewHeader.setId(R.id.act_designer_frg_result_table_header);
		tView.setId(R.id.act_designer_frg_result_table);
		LinearLayout llHeader = (LinearLayout) mainView.findViewById(R.id.act_designer_frg_result_ll_header);
		llHeader.addView(tViewHeader);
		ScrollView sv = (ScrollView) mainView.findViewById(R.id.act_designer_frg_result_sv);
		sv.addView(tView);
		requestUpdateLayouts();
	}

}
