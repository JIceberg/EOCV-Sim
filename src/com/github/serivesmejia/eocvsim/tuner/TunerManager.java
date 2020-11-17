package com.github.serivesmejia.eocvsim.tuner;

import com.github.serivesmejia.eocvsim.EOCVSim;
import com.github.serivesmejia.eocvsim.gui.tuner.TunableFieldPanel;
import com.github.serivesmejia.eocvsim.tuner.field.IntegerField;
import com.github.serivesmejia.eocvsim.tuner.field.ScalarField;
import com.github.serivesmejia.eocvsim.util.Log;
import org.opencv.core.Scalar;
import org.openftc.easyopencv.OpenCvPipeline;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TunerManager {

    private final EOCVSim eocvSim;

    private final List<TunableField> fields = new ArrayList<>();

    private boolean firstInit = true;

    public TunerManager(EOCVSim eocvSim) {
        this.eocvSim = eocvSim;
    }

    public void init() {

        if(firstInit) {
            eocvSim.pipelineManager.runOnChange(this::reset);
            firstInit = false;
        }

        if(eocvSim.pipelineManager.currentPipeline == null) return;

        addFieldsFrom(eocvSim.pipelineManager.currentPipeline);
        eocvSim.visualizer.updateTunerFields(getTunableFieldPanels());

    }

    public void update() {
        //update all fields
        for(TunableField field : fields) {
            field.update();
        }
    }

    public void reset() {
        fields.clear();
        init();
    }

    public void addFieldsFrom(OpenCvPipeline pipeline) {

        if(pipeline == null) return;

        Field[] fields = pipeline.getClass().getFields();

        for(Field field : fields) {

            TunableField toAddField = null; //for code simplicity

            try {

                if(!field.canAccess(pipeline) || Modifier.isFinal(field.getModifiers())) continue;

                if (field.getType() == Scalar.class) {
                    toAddField = new ScalarField(pipeline, field, eocvSim);
                } else if (field.getType() == int.class) {
                    toAddField = new IntegerField(pipeline, field, eocvSim);
                }

            } catch (Exception ex) {
                Log.error("TunerManager", "Reflection error while processing field: " + field.getName(), ex);
            }

            if(toAddField != null) { this.fields.add(toAddField); }

        }

    }

    public List<TunableFieldPanel> getTunableFieldPanels() {

        List<TunableFieldPanel> panels = new ArrayList<>();

        for(TunableField field : fields) {
            panels.add(new TunableFieldPanel(field, eocvSim));
        }

        return panels;

    }

}
