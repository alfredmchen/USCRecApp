package com.example.uscrecsport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DBHelperRegister extends SQLiteOpenHelper {

    public DBHelperRegister(Context context){
        super(context, "recCenter.db",null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table users(username TEXT PRIMARY KEY, password TEXT NOT NULL)");
<<<<<<< HEAD
        db.execSQL("create table villageGym(appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, month INTEGER NOT NULL, " +
                "date INTEGER NOT NULL, time INTEGER NOT NULL)");
        db.execSQL("create table villageGymAppointment(appointment_id INTEGER PRIMARY KEY, " +
                "username TEXT NOT NULL, FOREIGN KEY(appointment_id) REFERENCES villageGym(appointment_id))");
        for(int i=1; i < 32; i++) {
            for (int j = 10; j < 25; j+=2) {
                db.execSQL("insert into villageGym(month, date, time) values(3," + i + ", " + j + ")");
            }
        }
=======
        db.execSQL("create table villageGym(time_id INTEGER PRIMARY KEY, time INTEGER NOT NULL)");
        db.execSQL("create table villageGymAppointment(appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "time_id INTEGER NOT NULL, username TEXT NOT NULL, FOREIGN KEY(time_id) REFERENCES villageGym(time_id))");
        admi

>>>>>>> 7965079dae162f263e13b0228c5c5374f5717326
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop Table if exists users");
    }

    public boolean insertUser(String un, String pw){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", un);
        cv.put("password", pw);
        boolean userExist = checkusername(un);
        if(userExist){
            return false;
        }
        long res = db.insert("users",null, cv);
        if(res == -1){
            return false;
        }else{
            return true;
        }
    }

    private boolean insertTime(Integer month, Integer date, Integer time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("month", month);
        cv.put("date", date);
        cv.put("time", time);
        long res = db.insert("villageGym",null, cv);
        if(res == -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean insertAppointment(Integer appointment_id, String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("appointment_id", appointment_id);
        long res = db.insert("villageGym" ,null, cv);
        if(res == -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean checkAppointment(Integer time_id, String username){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("select * from villageGynAppointments where username = ?", new String[] {username});
        if(cs.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }


    public boolean checkusername(String un){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("select * from users where username = ?", new String[] {un});
        if(cs.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkusernamepassword(String un, String pw){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("select * from users where username = ? and password = ? ", new String[] {un,pw});
        if(cs.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }
}
