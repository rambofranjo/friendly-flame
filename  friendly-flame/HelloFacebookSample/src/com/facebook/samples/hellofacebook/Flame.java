package com.facebook.samples.hellofacebook;

//class to controll the status of the flame
public class Flame {
	
	//to calculate the outgoingness of a user
    public float[] calculateOutgoingness() {
    	
    	//outgoingness color is yellow
    	//intensity is defined by the outgoingness of the user
    	//and calculate from his events
    	float[] hsv = new float[3];
    	
    	//change yellow to HSV value
    	android.graphics.Color.RGBToHSV(255, 255, 0, hsv);
    	
    	//change the value according to the outgoingness
    	//currently it's hardcoded, but it should depend
    	//on the value of the events
    	
    	float outgoingness = 0.8f;
    	//calculate outgoingness by events;
    	hsv[2] = outgoingness;
    	
    	return hsv;
    	
    	//TODO: WERT an Arduino Ÿbermitteln
    }

}
