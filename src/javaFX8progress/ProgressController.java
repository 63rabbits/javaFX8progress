package javaFX8progress;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class ProgressController {

	@FXML
	private Button btnLaunchProcess;

	@FXML
	private TextField txfResult;

	@FXML
	private AnchorPane rootPane;

	@FXML
	private CheckBox chkNoCheckExitDBox;

	@FXML
	void initialize() {
		assert btnLaunchProcess != null : "fx:id=\"btnLaunchProcess\" was not injected: check your FXML file 'Progress.fxml'.";
		assert txfResult != null : "fx:id=\"txfResult\" was not injected: check your FXML file 'Progress.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'Progress.fxml'.";
		assert chkNoCheckExitDBox != null : "fx:id=\"chkNoCheckExitDBox\" was not injected: check your FXML file 'Progress.fxml'.";

	}

	@FXML
	void btnLaunchProcessOnAction(ActionEvent event) {
		launchProcess();
	}

	protected void launchProcess() {
		this.txfResult.setText("");

		// * TASK
		Task<Void> task = new Task<Void>() {

			@Override
			public Void call() throws Exception {

				int time = 5; // sec
				int loopCount = 100;
				int sleepMSec = time * 1000 / loopCount;
				String s = null;

				for (int i = 1; i <= loopCount; i++) {
					if (isCancelled()) {
						s = "CANCELLED\n(receive cancel request #1)";
						updateMessage(s);
						// System.out.println(s);
						break;
					}

					updateMessage("count = " + String.valueOf(i) + "\nsleep = " + sleepMSec + " msec");
					updateProgress(i, loopCount);
					try {
						Thread.sleep(sleepMSec);
					}
					catch (InterruptedException e) {
						if (isCancelled()) {
							s = "CANCELLED\n(receive cancel request #2)";
							updateMessage(s);
							// System.out.println(s);
						}
						throw e;
					}
				}

				return null;
			}

			@Override
			protected void succeeded() {
				// updateMessage("SUCCEEDED");
				super.succeeded();
			}

			@Override
			protected void cancelled() {
				// updateMessage("CANCELLED");
				super.cancelled();
			}

			@Override
			protected void failed() {
				// updateMessage("FAILED");
				super.failed();
			}

		};

		// ProgressBox box = new ProgressBox(task);
		// box.setTitle("test");
		// box.execute(chkNoCheckExitDBox.isSelected());
		new ProgressBox(task).execute(chkNoCheckExitDBox.isSelected());

		txfResult.setText(task.getState().toString());

	}

}
