package com.robsterthelobster.ucibustracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.robsterthelobster.ucibustracker.data.db.BusContract;
import com.robsterthelobster.ucibustracker.data.db.BusDbHelper;

import org.junit.Before;

/**
 * Created by robin on 6/22/2016.
 */
public class DatabaseTest extends AndroidTestCase {

    final String TEST_QUERY = "SELECT * FROM " +
            BusContract.RouteEntry.TABLE_NAME + ", " +
            BusContract.StopEntry.TABLE_NAME + ", " +
            BusContract.ArrivalEntry.TABLE_NAME + ", " +
            BusContract.VehicleEntry.TABLE_NAME + "; ";

    BusDbHelper helper;

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        helper = new BusDbHelper(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        helper.close();
    }

    public void testDatabaseInitialization(){

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(TEST_QUERY, null);
        int answer = cursor.getCount();
        db.close();

        assertEquals(answer, 0);
    }

    public void testDataBaseInsert(){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BusContract.RouteEntry.ROUTE_ID, "123");
        values.put(BusContract.RouteEntry.ROUTE_NAME, "My Route Name");
        values.put(BusContract.RouteEntry.COLOR, "BLUE");

        long answer = db.insert(BusContract.RouteEntry.TABLE_NAME, null, values);

        db.close();

        assertFalse(answer == -1);
    }

    public void testDataBeingStored(){

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + BusContract.RouteEntry.TABLE_NAME, null);

        while(c.moveToNext()){
            assertEquals(c.getInt(0), 123);
            assertEquals(c.getString(1), "My Route Name");
            assertEquals(c.getString(2), "BLUE");
        }

        db.close();
    }

}
