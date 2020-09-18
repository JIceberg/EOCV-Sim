package com.github.serivesmejia.eocvsim.pipeline;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvPipeline;

import com.github.serivesmejia.eocvsim.EOCVSim;
import com.github.serivesmejia.eocvsim.gui.Visualizer.AsyncPleaseWaitDialog;
import com.github.serivesmejia.eocvsim.util.Log;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class PipelineManager {

	public volatile ArrayList<Class<OpenCvPipeline>> pipelines = new ArrayList<Class<OpenCvPipeline>>();
	
	public OpenCvPipeline currentPipeline = null;
	public String currentPipelineName = "";

	public volatile Mat lastOutputMat = new Mat();
	
	public int lastFPS = 0;
	
	private int fpsC = 0;
	private long nextFPSUpdateMillis = 0;
	
	public void init(AsyncPleaseWaitDialog lookForPipelineAPWD) {
		
		Log.info("PipelineManager", "Initializing...");
		Log.white();
		
		lookForPipelines(lookForPipelineAPWD);
		
		nextFPSUpdateMillis = System.currentTimeMillis();
		
	}
	
	@SuppressWarnings("unchecked")
	public void lookForPipelines(AsyncPleaseWaitDialog lookForPipelineAPWD) {
		
		Log.info("PipelineManager", "Scanning for pipelines...");
		
		//Scan for all classes in the classpath
		ClassGraph classGraph = new ClassGraph().enableAllInfo().verbose();
		
		ScanResult scanResult = classGraph.scan();
		
		//iterate over the results of the scan
	    for (ClassInfo routeClassInfo : scanResult.getAllClasses()) {
	    	
	    	Class<?> foundClass = null;
	    	
			try {
				foundClass = Class.forName(routeClassInfo.getName());
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
				continue; //continue because we couldn't get the class...
			}

			//Scan recursively until we find a OpenCvPipeline superclass or we hit the Object superclass
			Class<?> superClass = foundClass.getSuperclass();
			while (superClass != null) {
				
				if(superClass == OpenCvPipeline.class){ //Yay we found a pipeline
					
					Log.info("PipelineManager", "Found pipeline " + routeClassInfo.getName());
					if(lookForPipelineAPWD != null) lookForPipelineAPWD.subMsg.setText("Found pipeline " + routeClassInfo.getSimpleName()); 
					
					addPipelineClass(foundClass);
					break;
					
				}
				
				//Didn't found a pipeline, continue searching...
				superClass = superClass.getSuperclass();
				
			}
		
	    }
	    
	    Log.info("PipelineManager", "Found " + pipelines.size() + " pipeline(s)");
	    Log.white();
		
	    changePipeline(0);
	    
	}
	
	private void addPipelineClass(Class<?> C) {
		try {
			pipelines.add((Class<OpenCvPipeline>) C);
		} catch (Throwable ex) {
			ex.printStackTrace();
			Log.error("PipelineManager", "Unable to cast " + C.getName() + " to OpenCvPipeline class.");
			Log.error("PipelineManager", "Remember that the pipeline class should extend OpenCvPipeline");
		}
	}
	
	public void update(Mat inputMat) {
		
		if(currentPipeline != null) {
			lastOutputMat = currentPipeline.processFrame(inputMat);
		} else {
			lastOutputMat = inputMat;
		}
		
		calcFPS();
		
	}
	
	private void calcFPS() {
		
		fpsC++;
		
		if(System.currentTimeMillis() >= nextFPSUpdateMillis) {
			
			nextFPSUpdateMillis = System.currentTimeMillis() + 1000;
			
			lastFPS = fpsC;
			fpsC = 0;
		
		}
		
	}
	
	public void changePipeline(int index) {
	
		Class<OpenCvPipeline> pipelineClass = pipelines.get(index);
	
		Log.info("PipelineManager", "Changing to pipeline " + pipelineClass.getName());
		
		Constructor<?> constructor;
		try {
			constructor = pipelineClass.getConstructor();
			currentPipeline = (OpenCvPipeline) constructor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			Log.error("PipelineManager", "Unable to instantiate class " + pipelineClass.getName());
		}
		
		Log.info("PipelineManager", "Instantiated pipeline class " + pipelineClass.getName());
		
		currentPipeline.init(EOCVSim.EMPTY_MAT);
		
		Log.info("PipelineManager", "Initialized pipeline " + pipelineClass.getName());
		Log.white();
		
		currentPipelineName = currentPipeline.getClass().getSimpleName();
		
	}
	
}
