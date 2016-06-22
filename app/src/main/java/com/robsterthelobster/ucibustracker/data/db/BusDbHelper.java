package com.robsterthelobster.ucibustracker.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.robsterthelobster.ucibustracker.data.models.Route;

/**
 * Created by robin on 6/22/2016.
 */
public class BusDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UCIBustracker.db";

    private static final String SQL_DELETE = "DROP TABLE IF EXISTS ";

    final String SQL_CREATE_ROUTE_TABLE =
            "CREATE TABLE " + BusContract.RouteEntry.TABLE_NAME + " (" +
                    BusContract.RouteEntry.ROUTE_ID + " INTEGER NOT NULL, " +
                    BusContract.RouteEntry.ROUTE_NAME + " TEXT NOT NULL, " +
                    BusContract.RouteEntry.COLOR + " TEXT NOT NULL " +
                    " );";

    final String SQL_CREATE_STOP_TABLE =
            "CREATE TABLE " + BusContract.StopEntry.TABLE_NAME + " (" +
                    BusContract.StopEntry.STOP_ID + " INTEGER NOT NULL, " +
                    BusContract.StopEntry.STOP_NAME + " TEXT NOT NULL, " +
                    BusContract.StopEntry.LATITUDE + " TEXT NOT NULL, " +
                    BusContract.StopEntry.LONGITUDE + " TEXT NOT NULL " +
                    " );";

    final String SQL_CREATE_ARRIVAL_TABLE =
            "CREATE TABLE " + BusContract.ArrivalEntry.TABLE_NAME + " (" +
                    BusContract.ArrivalEntry.ROUTE_ID + " INTEGER NOT NULL, " +
                    BusContract.ArrivalEntry.ROUTE_NAME + " TEXT NOT NULL, " +
                    BusContract.ArrivalEntry.STOP_ID + " TEXT NOT NULL, " +
                    BusContract.ArrivalEntry.PREDICTION_TIME + " TEXT NOT NULL, " +
                    BusContract.ArrivalEntry.SECONDS_TO_ARRIVAL + " TEXT NOT NULL, " +
                    " FOREIGN KEY ("+ BusContract.ArrivalEntry.ROUTE_ID +
                    ") REFERENCES "+ BusContract.RouteEntry.TABLE_NAME +
                    "(" + BusContract.RouteEntry.ROUTE_ID + ")" +
                    " FOREIGN KEY ("+ BusContract.ArrivalEntry.STOP_ID +
                    ") REFERENCES "+ BusContract.StopEntry.TABLE_NAME +
                    "(" + BusContract.StopEntry.STOP_ID + ")" +
                    " );";

    final String SQL_CREATE_VEHICLE_TABLE =
            "CREATE TABLE " + BusContract.VehicleEntry.TABLE_NAME + " (" +
                    BusContract.VehicleEntry.ROUTE_ID + " INTEGER NOT NULL, " +
                    BusContract.VehicleEntry.BUS_NAME + " TEXT NOT NULL, " +
                    BusContract.VehicleEntry.LATITUDE + " TEXT NOT NULL, " +
                    BusContract.VehicleEntry.LONGITUDE + " TEXT NOT NULL, " +
                    BusContract.VehicleEntry.PERCENTAGE + " TEXT NOT NULL, " +
                    " FOREIGN KEY ("+ BusContract.VehicleEntry.ROUTE_ID +
                    ") REFERENCES "+ BusContract.RouteEntry.TABLE_NAME +
                    "(" + BusContract.RouteEntry.ROUTE_ID + ")" +
                    " );";

    public BusDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ROUTE_TABLE);
        db.execSQL(SQL_CREATE_STOP_TABLE);
        db.execSQL(SQL_CREATE_ARRIVAL_TABLE);
        db.execSQL(SQL_CREATE_VEHICLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE + BusContract.VehicleEntry.TABLE_NAME);
        db.execSQL(SQL_DELETE + BusContract.ArrivalEntry.TABLE_NAME);
        db.execSQL(SQL_DELETE + BusContract.StopEntry.TABLE_NAME);
        db.execSQL(SQL_DELETE + BusContract.RouteEntry.TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    final String TEST_QUERY = "SELECT * FROM " +
            BusContract.RouteEntry.TABLE_NAME + ", " +
            BusContract.StopEntry.TABLE_NAME + ", " +
            BusContract.ArrivalEntry.TABLE_NAME + ", " +
            BusContract.VehicleEntry.TABLE_NAME + "; ";

    public int testDb(){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(TEST_QUERY, null);
        int answer = cursor.getCount();
        db.close();

        return answer;
    }

    public long testDb2(){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BusContract.RouteEntry.ROUTE_ID, "123");
        values.put(BusContract.RouteEntry.ROUTE_NAME, "My Route Name");
        values.put(BusContract.RouteEntry.COLOR, "BLUE");

        long answer = db.insert(BusContract.RouteEntry.TABLE_NAME, null, values);
        db.close();

        return answer;
    }

    public int testDb3(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(TEST_QUERY, null);
        int answer = cursor.getCount();
        db.close();

        return answer;
    }

    public Cursor testDb4(){

        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + BusContract.RouteEntry.TABLE_NAME, null);
    }
}
