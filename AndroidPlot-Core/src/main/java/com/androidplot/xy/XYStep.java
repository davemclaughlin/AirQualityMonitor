/*
 * Copyright 2012 AndroidPlot.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.androidplot.xy;

/**
 * An immutable object generated by XYStepCalculator representing
 * a stepping model to be used by an XYPlot.
 */
public class XYStep {

    private final float stepCount;
    private final float stepPix;
    private final double stepVal;

    //public XYStep() {}

    public XYStep(float stepCount, float stepPix, double stepVal) {
        this.stepCount = stepCount;
        this.stepPix = stepPix;
        this.stepVal = stepVal;
    }

    public double getStepCount() {
        return stepCount;
    }

    /*public void setStepCount(double stepCount) {
        this.stepCount = stepCount;
    }*/

    public float getStepPix() {
        return stepPix;
    }

    /*public void setStepPix(float stepPix) {
        this.stepPix = stepPix;
    }*/

    public double getStepVal() {
        return stepVal;
    }

    /*public void setStepVal(double stepVal) {
        this.stepVal = stepVal;
    }*/
}
