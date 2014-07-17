package *.periphery.printer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import *.App;
import *.R;
import *.Utils;
import *.db.Journal.JournalItemType;
import *.periphery.DeviceException;
import *.periphery.Periphery.DeviceFlavor;
import *.periphery.printer.AbsPrinter.PrinterModel;
import *.periphery.printer.PrintListener.OnPrintCancelledListener;
import *.periphery.printer.PrintListener.OnPrintFinishedListener;
import *.periphery.printer.PrintListener.OnPrintStartedListener;
import *.view.PrintDialogItem;

abstract public class PrintJob implements OnPrintStartedListener, OnPrintFinishedListener, OnPrintCancelledListener {

	private Handler handler = new Handler(Looper.getMainLooper());
	private DeviceFlavor<PrinterModel, PrinterConfiguration, PrinterWrapper<?>> printerFlavor;
	protected PrintJobThread printThread;
	private PrintDialogItem dialogItem;
	private OnPrintFinishedListener printFinished;
	private OnPrintStartedListener printStart;
	private OnPrintCancelledListener printCancelled;
	private boolean isFinished = false;
	private boolean isWorking = false;
	protected PrinterWrapper<?> wrapper;
	
	public PrintJob(DeviceFlavor<PrinterModel, PrinterConfiguration, PrinterWrapper<?>> printerFlavor, OnPrintStartedListener printStarted, OnPrintFinishedListener printFinished, OnPrintCancelledListener printCancelled) {
		this.printerFlavor = printerFlavor;
		this.printFinished = printFinished;
		this.printStart = printStarted;
		this.printCancelled = printCancelled;
		wrapper = printerFlavor.getWrapper();
	}
	
	public DeviceFlavor<PrinterModel, PrinterConfiguration, PrinterWrapper<?>> getPrinterFlavor() {
		return printerFlavor;
	}

	public PrintDialogItem getDialogItem() {
		return dialogItem;
	}

	public boolean isWorking() {
		return isWorking;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	public void execute(PrintDialogItem dialogItem) {
		this.dialogItem = dialogItem;
		if (printThread == null) {
			printThread = new PrintJobThread(this);
			printThread.start();
			isWorking = true;
		}
	}
	
	/**
	 * Override this method with what actually PrintJob will do between print started and print finished
	 */
	protected abstract void executeInternal() throws DeviceException;
	
	public void reset() {
		if (printThread != null) {
			printThread.dismissConfirmationDialog();
			printThread = null;
		}
		isFinished = false;
		isWorking = false;
	}

	@Override
	public void onPrintCancelled() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (printCancelled != null)
					printCancelled.onPrintCancelled();
			}
		});
	}

	@Override
	public void onPrintFinished() {
		isFinished = true;
		isWorking = false;

		handler.post(new Runnable() {

			@Override
			public void run() {
				if (dialogItem != null)
					dialogItem.setSucceed();
				if (printFinished != null)
					printFinished.onPrintFinished();
			}
		});
	}

	@Override
	public void onPrintStarted() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (dialogItem != null)
					dialogItem.setWorking(R.string.printing_printing, false);
				if (printStart != null)
					printStart.onPrintStarted();
			}
		});
	}

	public class PrintJobThread extends Thread {

		private PrintJob printJob;
		private AlertDialog confirmationDialog;

		private PrintJobThread(PrintJob printJob) {
			this.printJob = printJob;
		}

		synchronized void showConfirmationDialog(final String message) throws DeviceException, InterruptedException {
			App.getJournal().addItem(R.string.journal_error_print, JournalItemType.ERROR_PRINT);
			if (dialogItem != null) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						confirmationDialog = new AlertDialog.Builder(dialogItem.getContext()).setTitle(printJob.getPrinterFlavor().getModel().getName()).setMessage(message + "\n" + Utils.getString(R.string.printing_error_continue)).setPositiveButton(R.string.btn_yes, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								new Thread(new Runnable() {

									@Override
									public void run() {
										synchronized (printThread) {
											printThread.notify();
										}
									}
								}).start();
							}
						}).setNegativeButton(R.string.btn_cancel, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								printThread.interrupt();
							}
						}).setCancelable(false).show();
					}

				});

				synchronized (printThread) {
					printThread.wait();
				}

			} else
				throw new DeviceException();
		}
		
		private synchronized void dismissConfirmationDialog() {
			if (confirmationDialog != null) {
				confirmationDialog.dismiss();
				printThread.interrupt();
			}
		}
		
		synchronized void showToast(final String message) {
			App.getJournal().addItem(R.string.journal_error_print, JournalItemType.ERROR_PRINT);
			if (dialogItem != null) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						Utils.makeToast(App.getContext(), message, Toast.LENGTH_SHORT).show();
					}

				});
			}
		}
		
		@Override
		public void run() {
			printJob.onPrintStarted();
			try {
				executeInternal();
				printJob.onPrintFinished();
			} catch (final DeviceException e) {
				App.getJournal().addItem(R.string.journal_error_print, JournalItemType.ERROR_PRINT);
				if (dialogItem != null)
					handler.post(new Runnable() {
	
						@Override
						public void run() {
							switch(e.getType()) {
							case ERROR:
								dialogItem.setFailed(e.toString());
								break;
							case ERROR_RETRY:
								dialogItem.setRetry(e.toString());
								break;
							}
						}
					});
				else
					e.printStackTrace();
			}
		}
	}
}