package com.badlogic.invaders.android;

/**
 * Created by scott on 2016-05-11.
 */
public class AndroidGetQ implements com.badlogic.invaders.Invaders.InvaderInterface {

    public AndroidGetQ() {
    }

    @Override
    public double getQ0() {
        return NebDeviceDetailFragment.latest_Q0;
    }

    @Override
    public double getQ1() {
        return NebDeviceDetailFragment.latest_Q1;
    }

    @Override
    public double getQ2() {
        return NebDeviceDetailFragment.latest_Q2;
    }

    @Override
    public double getQ3() {
        return NebDeviceDetailFragment.latest_Q3;
    }

    @Override
    public long getTimestamp() {
        return NebDeviceDetailFragment.timestamp_N;
    }
}
