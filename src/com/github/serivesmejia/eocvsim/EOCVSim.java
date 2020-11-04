package com.github.serivesmejia.eocvsim;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Mat;

import com.github.serivesmejia.eocvsim.gui.Visualizer;
import com.github.serivesmejia.eocvsim.gui.Visualizer.AsyncPleaseWaitDialog;
import com.github.serivesmejia.eocvsim.input.InputSourceManager;
import com.github.serivesmejia.eocvsim.pipeline.PipelineManager;
import com.github.serivesmejia.eocvsim.util.Log;
import com.github.serivesmejia.eocvsim.util.SysUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class EOCVSim {

	public volatile Visualizer visualizer = new Visualizer(this);
	public PipelineManager pipelineManager = null;

	public InputSourceManager inputSourceManager = new InputSourceManager(this);

	public static Mat EMPTY_MAT = null;
	public static String VERSION = "1.1.0";

	public static int DEFAULT_EOCV_WIDTH = 320;
	public static int DEFAULT_EOCV_HEIGHT = 240;

	private final ArrayList<Runnable> runnsOnMain = new ArrayList<>();

	public void init() {

		Log.info("EOCVSim", "Initializing EasyOpenCV Simulator v" + VERSION);
		Log.white();
		
		SysUtil.loadCvNativeLib();
		Log.white();
		
		EMPTY_MAT = new Mat();
		
		pipelineManager = new PipelineManager(this);
		
		visualizer.init();

		inputSourceManager.init();

		visualizer.updateSourcesList();
		visualizer.sourceSelector.setSelectedIndex(0);

		AsyncPleaseWaitDialog lookForPipelineAPWD = visualizer.asyncPleaseWaitDialog("Looking for pipelines...", "Scanning classpath", "Exit", new Dimension(300, 150), true);
		
		lookForPipelineAPWD.onCancel(new Runnable() {
			@Override
			public void run() {
				System.exit(0);
			}
		});
		
		pipelineManager.init(lookForPipelineAPWD);

		lookForPipelineAPWD.destroyDialog();

		visualizer.updatePipelinesList();
		visualizer.pipelineSelector.setSelectedIndex(0);
		
		beginLoop();
		
	}
	
	public void beginLoop() {
		
		Log.info("EOCVSim", "Begin EOCVSim loop");
		Log.white();

		inputSourceManager.inputSourceLoader.saveInputSourcesToFile();

		while(!Thread.interrupted()) {

			Telemetry telemetry = pipelineManager.currentTelemetry;

			//run all pending requested runnables
			for(Runnable runn : runnsOnMain.toArray(new Runnable[0])) {
				runn.run();
				runnsOnMain.remove(runn);
			}

			updateVisualizerTitle();

			inputSourceManager.update(pipelineManager.isPaused());

			//if we dont have a mat from the inputsource, we'll just skip this frame.
			if(inputSourceManager.lastMatFromSource == null || inputSourceManager.lastMatFromSource.empty()) continue;

			try {

				pipelineManager.update(inputSourceManager.lastMatFromSource);

				if(!pipelineManager.isPaused())
					visualizer.updateVisualizedMat(pipelineManager.lastOutputMat);

				if(telemetry != null) {
					telemetry.errItem.setCaption("");
					telemetry.errItem.setValue("");
				}

			} catch(Throwable ex) {

				Log.error("Error while processing pipeline", ex);

				if(telemetry != null) {
					telemetry.errItem.setCaption("[/!\\]");
					telemetry.errItem.setValue("Error while processing pipeline\nCheck console for details.");
					telemetry.update();
				}

			}

			visualizer.updateTelemetry(pipelineManager.currentTelemetry);

			System.gc(); //run JVM garbage collector
			
		}
		
	}
	
	public void updateVisualizerTitle() {
		
		String fpsMsg = " (" + String.valueOf(pipelineManager.lastFPS) + " FPS)";

		String isPaused = pipelineManager.isPaused() ? " (Paused)" : "";

		String memoryMsg = " (" + String.valueOf(SysUtil.getMemoryUsageMB()) + " MB memory used)";

		if(pipelineManager.currentPipeline == null) {
			visualizer.setTitleMessage("No pipeline" + fpsMsg + isPaused + memoryMsg);
		} else {
			visualizer.setTitleMessage(pipelineManager.currentPipelineName + fpsMsg + isPaused + memoryMsg);
		}
		
	}

	public void runOnMainThread(Runnable runn) {
		runnsOnMain.add(runn);
	}

}
