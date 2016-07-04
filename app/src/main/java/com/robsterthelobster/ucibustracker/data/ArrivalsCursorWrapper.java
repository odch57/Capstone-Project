package com.robsterthelobster.ucibustracker.data;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.location.Location;
import android.util.Log;

import com.robsterthelobster.ucibustracker.ArrivalsFragment;
import com.robsterthelobster.ucibustracker.Utility;

/**
 * Created by robin on 7/4/2016.
 */
public class ArrivalsCursorWrapper extends CursorWrapper{

    private int[] index;
    private int count=0;
    private int pos=0;

    public ArrivalsCursorWrapper(Cursor cursor, Location location, int latColumn, int longColumn) {
        super(cursor);
        this.count = super.getCount();
        this.index = new int[this.count];
        for (int i=0;i<this.count;i++) {
            super.moveToPosition(i);

            double distance = Utility.getDistanceBetweenTwoPoints(
                    location, getDouble(latColumn), getDouble(longColumn));
            // 500 meters
            Log.d("distance", cursor.getString(ArrivalsFragment.C_STOP_NAME) + ": " + distance );
            if (distance < 500)
                this.index[this.pos++] = i;
        }
        this.count = this.pos;
        this.pos = 0;
        super.moveToFirst();
    }

    @Override
    public boolean move(int offset) {
        return this.moveToPosition(this.pos+offset);
    }

    @Override
    public boolean moveToNext() {
        return this.moveToPosition(this.pos+1);
    }

    @Override
    public boolean moveToPrevious() {
        return this.moveToPosition(this.pos-1);
    }

    @Override
    public boolean moveToFirst() {
        return this.moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {
        return this.moveToPosition(this.count-1);
    }

    @Override
    public boolean moveToPosition(int position) {
        this.pos = position;
        if (position >= this.count || position < 0)
            return false;
        return super.moveToPosition(this.index[position]);
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public int getPosition() {
        return this.pos;
    }
}
