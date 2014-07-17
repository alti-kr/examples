package *.designer;

import java.util.List;

import android.support.v4.app.Fragment;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;


public class ReportDesignerSettings extends Fragment implements OnDismissCallback {
	ListView listItems;
	List<ReportDesignerField> listFields;
	ListItemsAdapter sla;
	SwipeDismissAdapter swipeAdapter;

	public void refreshSwipeAdapter()
	{
		swipeAdapter = new SwipeDismissAdapter(sla, this);
		swipeAdapter.setAbsListView(listItems);		
		listItems.setAdapter(swipeAdapter);
	}
	
	public void setListFields(List<ReportDesignerField> listFields)
	{
		this.listFields = listFields;

	}
	public abstract class ListItemsAdapter extends BaseAdapter
	{
		@Override
		public int getCount() {
			return listFields.size();
		}

		@Override
		public Object getItem(int position) {
			if(position >= getCount()) {return null;}
			return listFields.get(position);
		}

		@Override
		public long getItemId(final int position) {
			if (getItem(position) == null) {return 0L;}
			return getItem(position).hashCode();
		}
        public void remove(int position) {

        }
	}

	public void refresh()
	{

		this.listItems.invalidateViews();
	}

	public void addItem(ReportDesignerField item)
	{
        item.setConditionValue("");
		this.listFields.add(item);
		refresh();
	}

	public void delItem(int position)
	{
		this.listFields.remove(position);
		refresh();
	}

	@Override
	public void onDismiss(AbsListView arg0, int[] reverseSortedPositions) {
        try {
            for (int position : reverseSortedPositions) {
                sla.remove(position);
            }
        }
        catch (Exception e){}
	}

    protected void moveItemTo(ReportDesignerSettings frg, int position) {
        frg.addItem(this.listFields.get(position));
        delItem(position);
        swipeAdapter.notifyDataSetChanged();
    }
}
