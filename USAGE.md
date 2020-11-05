# Usage Explaination

The purpose of this software is to *simulate the package & class structure of OpenFTC's EasyOpenCV and a little bit of the FTC SDK*,
while also providing OpenCV functionality and a simple GUI. By simulating the aforementioned structure, it allows the imports, class names, etc.
to be the same as they would if you were using the FTC SDK with EasyOpenCV, allowing you to simply copy paste your vision code
onto your Android Studio project once you want to transfer it to a robot.<br/>

## Table of Contents
- Pipelines
    - Sample Pipeline
- Input Sources

## Pipelines

All of the pipeline classes **should be** placed under the *org.firstinspires.ftc.teamcode* package, in the *TeamCode* module. This way, they will be
automatically detected by the simulator and will be selectionable from the GUI.

<img src='images/eocvsim_screenshot_structure.png' width='301' height='183'><br/>

*(Also, the simulator already comes by default with some EasyOpenCV samples)*<br/>

To create a new java class, follow these steps:<br/>
1) In the project files menu, open the TeamCode module
2) Find the *org.firstinspires.ftc.teamcode* package and right click on it
3) On the context menu, click on *New > Java Class*
4) A new menu will appear, type a name and make sure the *Class* option is selected
5) Once you have typed a name, press enter and the class will be created

Here's a quick gif illustrating these steps:<br/>

<img src='images/eocvsim_usage_createclass.gif' width='512' height='288'><br/>

If you want your class to be a pipeline, it **should also** extend the EOCV's OpenCvPipeline abstract class and override the processFrame() method.<br/><br/>
Here's a empty pipeline template, with the SamplePipeline class we created before:

```java
package org.firstinspires.ftc.teamcode;

import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvPipeline;

public class SamplePipeline extends OpenCvPipeline {

    @Override
    public void init(Mat input) {
        /* Executed once, when the pipeline is selected */
    }

    @Override
    public Mat processFrame(Mat input) {
        /* Executed each frame, the returned mat will be the one displayed */
        /* Processing and detection stuff */
        return input; // Return the input mat
                      // (Or a new, processed mat)
    }

    @Override
    public void onViewportTapped() {
        /*
         * Executed everytime when the pipeline view is tapped/clicked.
         * This is executed from the UI thread, so whatever we do here
         * we must do it quickly.
         */
    }

}
```

## Input Sources

To allow multiple ways to test your pipeline, the simulator comes with so called *Input Sources*, which are the ones that will give your pipeline the input mats, As of right now, the sim has two types of Input Sources:

- Image Source:</br></br>
    These will feed your pipeline with a static Mat from an image loaded in your computer hard drive.</br></br>
    To save resources, your pipeline will just run once when you select an image source, but you can optionally resume the pipeline execution by clicking the           "Pause" button under the pipeline selector.</br></br>
- Camera Source:</br></br>
    These will feed your pipeline with a constantly changing Mat from a specified camera plugged in your computer.</br></br>
    Unlike the image sources, these will not pause the execution of you pipeline by default, but you can click the "Pause" button to pause it at any time.
    
### Creating an Input Source

