package javaFX8progress;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProgressBox extends Alert {
	private static final AlertType BOX_TYPE = AlertType.INFORMATION;
	private static final String BOX_TITLE = "Progress Box";
	private static final String HEADER_MESSAGE = "in progress ...";
	private static final long AUTO_CLOSE_DELAY_TIME = 1000L;
	private String endMessage = "done !";
	private boolean autoClose = false;
	private long autoCloseDelayTime = AUTO_CLOSE_DELAY_TIME;

	private ProgressBar progressBar = new ProgressBar();
	private Label contentMessage = new Label();
	// private final ButtonType ButtonTypeCancel = ButtonType.CANCEL; // <- If
	// you press the esc key, operation is funny.
	private final ButtonType ButtonTypeCancel = new ButtonType(ButtonType.CANCEL.getText());
	private Alert comfBox = null;
	private Task<?> task = null;

	public ProgressBox(Task<?> task) {
		super(BOX_TYPE);
		this.task = task;
		makeProgressBox();
		makeComfBox();
	}

	void setAutoClose(boolean b, long msec) {
		this.autoClose = b;
		autoCloseDelayTime = msec;
	}

	void setEndMessage(String msg) {
		this.endMessage = msg;
	}

	private void makeProgressBox() {
		// this.initModality(Modality.NONE);

		// title
		setTitle(BOX_TITLE);

		// head
		setHeaderText(HEADER_MESSAGE);

		// content
		Label progressValue = new Label();
		HBox hbox = new HBox(10, progressBar, progressValue);
		progressBar.setPrefWidth(280.0);

		contentMessage.setPrefWidth(300.0);
		contentMessage.setPrefHeight(30);
		contentMessage.setAlignment(Pos.TOP_LEFT);
		contentMessage.setWrapText(true);
		VBox vbox = new VBox(5, hbox, contentMessage);
		vbox.setPrefWidth(360);
		getDialogPane().setContent(vbox);

		// button
		getButtonTypes().clear();
		getButtonTypes().setAll(ButtonTypeCancel);
		Button cancelButton = (Button) getDialogPane().lookupButton(ButtonTypeCancel);
		cancelButton.setDefaultButton(true);

		// progress value
		progressValue.textProperty().bind(observer(progressBar));
	}

	private ObjectBinding<String> observer(ProgressBar p) {
		final ProgressBar pbx = p;
		ObjectBinding<String> sBinding = new ObjectBinding<String>() {
			{
				super.bind(pbx.progressProperty());
			}

			@Override
			protected String computeValue() {
				return (pbx.getProgress() < 0.0)
						? "    %"
						: String.valueOf(Math.round(pbx.getProgress() * 100.0)) + " %";
			}
		};
		return sBinding;
	}

	private void makeComfBox() {
		comfBox = new Alert(AlertType.CONFIRMATION);
		comfBox.setTitle(this.getTitle());
		DialogPane pane = comfBox.getDialogPane();
		pane.getButtonTypes().clear();
		pane.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
		for (ButtonType bt : comfBox.getButtonTypes()) {
			((Button) pane.lookupButton(bt)).setDefaultButton(bt == ButtonType.NO);
		}
	}

	Task<?> execute(boolean autoclose, long delaytime) {
		this.autoClose = autoclose;
		this.autoCloseDelayTime = delaytime;
		return execute();
	}

	Task<?> execute(boolean autoclose) {
		this.autoClose = autoclose;
		this.autoCloseDelayTime = AUTO_CLOSE_DELAY_TIME;
		return execute();
	}

	Task<?> execute() {

		// set handler
		task.setOnSucceeded(e -> closeBySucceeded());
		task.setOnCancelled(e -> closeByCancelled());
		task.setOnFailed(e -> closeByFailed());

		// synchronize the progress bar to task state
		progressBar.progressProperty().bind(task.progressProperty());
		contentMessage.textProperty().bind(task.messageProperty());

		// start task
		ExecutorService svc = Executors.newSingleThreadExecutor();
		svc.submit(task);

		// dialog
		do {
			Optional<ButtonType> result = showAndWait();
			if (result.isPresent() && (result.get() == ButtonTypeCancel)
					&& (showComfBox("Are you sure you want to cancel ?", ""))) {
				task.cancel();
				break;
			}
			setResult(null); // clear the result before calling showAndWait().
		} while (task.isRunning());

		svc.shutdown();

		return task;
	}

	private void closeBySucceeded() {
		closeCommon();
		if (!autoClose) showResultBox(AlertType.INFORMATION, endMessage, "");
	}

	private void closeByCancelled() {
		closeCommon();
		if (!autoClose) showResultBox(AlertType.WARNING, "CANCELLED !!", "");
	}

	private void closeByFailed() {
		closeCommon();
		if (!autoClose) showResultBox(AlertType.ERROR, "FAILED !!", "");
	}

	private void closeCommon() {
		comfBox.close();

		if (autoClose) {
			if (!isShowing()) show();

			try {
				Thread.sleep(autoCloseDelayTime);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Platform.runLater(() -> close());
	}

	private boolean showComfBox(String headerMessage, String contentMessage) {
		boolean rtn = false;

		comfBox.setHeaderText(headerMessage);
		comfBox.setContentText(contentMessage);

		Optional<ButtonType> result = comfBox.showAndWait();
		if (result.isPresent() && (comfBox.getResult() == ButtonType.YES)) rtn = true;

		return rtn;
	}

	private void showResultBox(AlertType type, String headMessage, String detailMessage) {
		Alert dbox = new Alert(type);
		dbox.setTitle(this.getTitle());
		dbox.setHeaderText(headMessage);
		dbox.setContentText(detailMessage);
		dbox.showAndWait();
	}
}