package *.designer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import *.Files;
import *.Utils;

public final class ReportDesignerRepository implements Serializable {
	private volatile static ReportDesignerRepository instance = null;
	private static final long serialVersionUID = 1485529980713159649L;
	private final List<ReportDesigner> list;

	private ReportDesignerRepository() {
		list = new ArrayList<>();
	}

	public List<ReportDesigner> getReports() {
		return list;
	}

	public int size() {
		return list.size();
	}

	public boolean remove(ReportDesigner rd) {
		return list.remove(rd);
	}

	public boolean add(ReportDesigner rd) {
		return list.add(rd);
	}

	public ReportDesigner get(int location) {
		return list.get(location);
	}

	public synchronized static ReportDesignerRepository getInstance() {
		instance = (ReportDesignerRepository) Files.REP_DES_REPOSITORY.readSerializable();
		if (instance == null)
		{
			instance = new ReportDesignerRepository();
		}

		if (instance.getReports() == null)
		{
			instance = new ReportDesignerRepository();
		}

		return instance;
	}

	public void save() {
		prepareToSave();
		Files.REP_DES_REPOSITORY.writeSerializable(this);

	}

	private void prepareToSave()
	{
		for (ReportDesigner rd : list)
		{
			rd.check();
		}
	}

	public synchronized static void kill() {
		Files.REP_DES_REPOSITORY.delete();
		instance = null;
	}

	public void refreshReportDesigners()
	{
		for (ReportDesigner rd : list)
		{
			rd.refreshAfterSerializable();
		}
	}
}
