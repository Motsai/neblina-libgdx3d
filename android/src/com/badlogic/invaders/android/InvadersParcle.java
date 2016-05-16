package com.badlogic.invaders.android;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by scott on 2016-05-09.
 */
// simple class that just has one member property as an example
public class InvadersParcle implements Parcelable {
    private int mData;

    /* everything below here is for implementing Parcelable */

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<InvadersParcle> CREATOR = new Parcelable.Creator<InvadersParcle>() {
        public InvadersParcle createFromParcel(Parcel in) {
            return new InvadersParcle(in);
        }

        public InvadersParcle[] newArray(int size) {
            return new InvadersParcle[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private InvadersParcle(Parcel in) {
        mData = in.readInt();
    }
}
