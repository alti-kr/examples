package *.designer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import *.App;
import *.R;

import java.util.List;

public class ActReportDesignerSelect extends ReportDesignerSettings {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		View mainView = inflater.inflate(R.layout.act_designer_frg_select, container, false);
		listItems = (ListView) mainView.findViewById(R.id.act_designer_frg_select_list);		
		sla = new ListItemsSelectAdapter();
		refreshSwipeAdapter();
		return mainView;
	}
	private class ListItemsSelectAdapter extends ListItemsAdapter 
	{
		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(R.layout.act_designer_frg_select_item, parent, false);
			}
			TextView textView = (TextView) view.findViewById(R.id.act_designer_frg_select_list_item);
			textView.setText(listFields.get(position).getNameHeader());
			return view;
		}

		@Override
        public void remove(final int position) {
			moveItemToStructure(position);

		}
	}

	private void moveItemToStructure(int position)
	{
        moveItemTo(((ActReportDesigner) getActivity()).frgProperties.frgStructure , position);
	}

	@Override
	public void addItem(ReportDesignerField item)
	{
		super.addItem(item);
		swipeAdapter.notifyDataSetChanged();
	}

    public static ActReportDesignerSelect makeInstance(List<ReportDesignerField> listFields)
    {
        ActReportDesignerSelect frgSelect = new ActReportDesignerSelect();
        frgSelect.setListFields(listFields);
        return frgSelect;
    }

}
