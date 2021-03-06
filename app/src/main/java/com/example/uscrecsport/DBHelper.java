package com.example.uscrecsport;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context){
        super(context, "recCenter.db",null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table users(username TEXT PRIMARY KEY, password TEXT NOT NULL, picture_url TEXT, student_ID TEXT NOT NULL)");

        db.execSQL("create table villageGym(appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, month TEXT NOT NULL, " +
                "date TEXT NOT NULL, time TEXT NOT NULL)");
        db.execSQL("create table villageGymAppointment(appointment_id INTEGER, " +
                "username TEXT NOT NULL, FOREIGN KEY(appointment_id) REFERENCES villageGym(appointment_id))");
        db.execSQL("create table villageGymWaitlist(appointment_id INTEGER, " +
                "username TEXT NOT NULL, FOREIGN KEY(appointment_id) REFERENCES villageGym(appointment_id))");

        db.execSQL("create table lyonGym(appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, month TEXT NOT NULL, " +
                "date TEXT NOT NULL, time TEXT NOT NULL)");
        db.execSQL("create table lyonGymAppointment(appointment_id INTEGER, " +
                "username TEXT NOT NULL, FOREIGN KEY(appointment_id) REFERENCES villageGym(appointment_id))");
        db.execSQL("create table lyonGymWaitlist(appointment_id INTEGER, " +
                "username TEXT NOT NULL, FOREIGN KEY(appointment_id) REFERENCES villageGym(appointment_id))");

        db.execSQL("create table notificationList(user TEXT, gym TEXT, appointment_id INTEGER)");

        for(int k = 3; k <6; k++){
            for(int i=1; i < 32; i++) {
                for (int j = 8; j < 24; j+=2) {
                    db.execSQL("insert into villageGym(month, date, time) values(" + k   +  "," + i  + ", " + j + ")");
                }
            }
        }
        for(int k = 3; k <6; k++) {
            for(int i=1; i < 32; i++) {
                for (int j = 8; j < 24; j+=2) {
                    db.execSQL("insert into lyonGym(month, date, time) values(" + k   + "," + i + ", " + j + ")");
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop Table if exists users");
    }

    public boolean insertUser(String un, String pw, String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", un);
        cv.put("password", pw);
        cv.put("student_id",id);
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

    public boolean insertNotification(String gym, Integer appointment_id, String user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("gym", gym);
        cv.put("appointment_id", appointment_id);
        cv.put("user", user);
        long res = db.insert("notificationList",null, cv);
        if(res == -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean cancelAppointment(String gym, Integer appointment_id, String username){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "";
        String getTimeString = "";
        if(gym.equals("Village gym")){
            queryString = "DELETE FROM villageGymAppointment " +
                    "WHERE appointment_id =" + appointment_id + " and username = '" + username + "'";
//            getTimeString = "SELECT * FROM villageGym WHERE appointment_id =" + appointment_id;
        }else{
            queryString = "DELETE FROM lyonGymAppointment " +
                    "WHERE appointment_id =" + appointment_id + " and username = '" + username + "'";
//            getTimeString = "SELECT * FROM lyonGym WHERE appointment_id =" + appointment_id;
        }

//        Cursor getTime = db.rawQuery(getTimeString, null);
//        boolean was_unavailable = false;
//
//        if(getTime.moveToFirst()) {
//            if (!checkAppointmentAvailability(gym, getTime.getString(1), getTime.getString(2), getTime.getString(3))) {
//                was_unavailable = true;
//            }
//        }

        Cursor cursor = db.rawQuery(queryString, null);
        moveWaitlistToNotificationList(gym, appointment_id);
        deleteWaitlist(gym, appointment_id);
        if(cursor.moveToFirst()){
//            if(was_unavailable && checkAppointmentAvailability(gym, getTime.getString(1), getTime.getString(2), getTime.getString(3))) {
//                moveWaitlistToNotificationList(gym, appointment_id);
//                deleteWaitlist(gym, appointment_id);
//            }
            return true;
        }else{
            return false;
        }
    }

    public String getImageUrl(String un){
        SQLiteDatabase db = this.getReadableDatabase();
        String imgurl = "";
        Cursor cs = db.rawQuery("select * from users where username = ?", new String[]{un});
        if(cs.getCount() == 1 && cs.moveToFirst()){
            imgurl = cs.getString(cs.getColumnIndexOrThrow("picture_url"));
        }
        return imgurl;
    }

    public boolean setImageUrl(String un,String url){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("picture_url", url);

        long res = db.update("users", cv, "username = ?", new String []{un});
        if(res == -1){
            return false;
        }else{
            return true;
        }
    }

    public void moveWaitlistToNotificationList(String gym, Integer appointment_id){
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "";
        if(gym.equals("Village gym")){
            queryString = "SELECT * FROM villageGymWaitlist WHERE appointment_id = " + appointment_id;
        }else{
            queryString = "SELECT * FROM lyonGymWaitlist WHERE appointment_id = " + appointment_id;;
        }
        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()){
            String input_gym = "";
            do{
                if(gym.equals("Village gym")){
                    input_gym = "village";
                }else {
                    input_gym = "lyon";
                }
                insertNotification(input_gym, appointment_id, cursor.getString(1));
            }while(cursor.moveToNext());
        }

    }

    public void deleteWaitlist(String gym, Integer appointment_id){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "";

        if(gym.equals("Village gym")){
            queryString = "DELETE FROM villageGymWaitlist WHERE appointment_id =" + appointment_id;
        }else{
            queryString = "DELETE FROM lyonGymWaitlist WHERE appointment_id =" + appointment_id;;
        }

        Cursor cursor = db.rawQuery(queryString, null);
        cursor.moveToFirst();
    }

    public boolean insertAppointment(String gym, Integer appointment_id, String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("appointment_id", appointment_id);
        cv.put("username", username);

        boolean appointment = checkAppointment(gym, appointment_id, username);
        if(appointment){
            return false;
        }

        long res;
        if(gym.equals("village")){
            res = db.insert("villageGymAppointment" ,null, cv);
        }else{
            res = db.insert("lyonGymAppointment" ,null, cv);
        }

        if(res == -1){
            return false;
        }else return true;
    }

    public boolean insertWaitlist(String gym, Integer appointment_id, String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("appointment_id", appointment_id);
        cv.put("username", username);

        boolean waitlist = checkWaitlist(gym, appointment_id, username);
        if(waitlist){
            return false;
        }

        long res;
        if(gym.equals("village")){
            res = db.insert("villageGymWaitlist" ,null, cv);
        }else{
            res = db.insert("lyonGymWaitlist" ,null, cv);
        }

        if(res == -1){
            return false;
        }else return true;
    }

    public String getUser(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs;
        cs = db.rawQuery("select * from users where student_ID = ?",
                new String[]{id});
        if (cs.moveToFirst()) {
            return cs.getString(0);
        }
        return null;
    }

    public Integer getAppointmentId(String gym, String month, String date, String time){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs;

        if(gym.equals("village")){
            cs = db.rawQuery("select * from villageGym where month = ? and date = ? and time = ?",
                    new String[] {month, date, time});
        }else{
            cs = db.rawQuery("select * from lyonGym where month = ? and date = ? and time = ?",
                    new String[] {month, date, time});
        }
        if(cs.moveToFirst()){
            return cs.getInt(0);
        }
        return -1;
    }

    public List<String> getTime(String gym, Integer appointment_id){ // return string of time in month date time order
        List<String> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs;

        if(gym.equals("village")){
            cs = db.rawQuery("select * from villageGym where appointment_id = ?",
                    new String[] {appointment_id.toString()});
        }else{
            cs = db.rawQuery("select * from lyonGym where appointment_id = ?",
                    new String[] {appointment_id.toString()});
        }
        if(cs.moveToFirst()){
            do{
               returnList.add(cs.getString(1)) ;
                returnList.add(cs.getString(2)) ;
                returnList.add(cs.getString(3)) ;
            }while(cs.moveToNext());
        }
        return returnList;
    }

    public boolean checkAppointmentAvailability(String gym, String month, String date, String time){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs;
        if(gym.equals("village")){
            cs = db.rawQuery("select * from villageGymAppointment where appointment_id = ?",
                    new String[]{getAppointmentId(gym, month, date, time).toString()});
        }else{
            cs = db.rawQuery("select * from lyonGymAppointment where appointment_id = ?",
                    new String[]{getAppointmentId(gym, month, date, time).toString()});
        }
        if(cs.getCount() > 2){
            return false;
        }else{
            return true;
        }
    }

    public boolean checkAppointment(String gym, Integer appointment_id, String username){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs;
        if(gym.equals("village")){
            cs = db.rawQuery("select * from villageGymAppointment where appointment_id = ? and username = ?", new String[] {appointment_id.toString(), username});
        }else{
            cs = db.rawQuery("select * from lyonGymAppointment where appointment_id = ? and username = ?", new String[] {appointment_id.toString(), username});
        }
        if(cs.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkWaitlist(String gym, Integer appointment_id, String username){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs;
        if(gym.equals("village")){
            cs = db.rawQuery("select * from villageGymWaitlist where appointment_id = ? and username = ?", new String[] {appointment_id.toString(), username});
        }else{
            cs = db.rawQuery("select * from lyonGymWaitlist where appointment_id = ? and username = ?", new String[] {appointment_id.toString(), username});
        }
        if(cs.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkusername(String un){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs = db.rawQuery("select * from users where username = ?", new String[] {un});
        if(cs.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkStudentID(String un){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs = db.rawQuery("select * from users where student_ID = ?", new String[] {un});
        if(cs.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkusernamepassword(String un, String pw){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cs = db.rawQuery("select * from users where username = ? and password = ? ", new String[] {un,pw});
        if(cs.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

    public List<Appointment> getPastAppointments(String un, int currmonth, int currday, int currhour) {
        List<Appointment> result = new ArrayList<Appointment>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor lcs = db.rawQuery("select * from lyonGymAppointment where username = ?", new String[]{un});
        if (lcs.getCount() > 0){
            while (lcs.moveToNext()) {
                String time = lcs.getString(0);
                if(!isCurrentAppt(currmonth,currday,currhour,time)) {
                    Cursor cs = db.rawQuery("select * from lyonGym where appointment_id = ?", new String[]{time});
                    cs.moveToFirst();
                    String month = cs.getString(cs.getColumnIndexOrThrow("month"));
                    String day = cs.getString(cs.getColumnIndexOrThrow("date"));
                    String hour = cs.getString(cs.getColumnIndexOrThrow("time"));
                    Appointment temp = new Appointment("Lyon Center", time,month,day,hour);
                    result.add(temp);
                }
            }
            lcs.close();
        }
        Cursor vcs = db.rawQuery("select * from villageGymAppointment where username = ?", new String[]{un});
        if (vcs.getCount() > 0){
            while (vcs.moveToNext()) {
                String time = vcs.getString(0);
                if(!isCurrentAppt(currmonth,currday,currhour,time)) {
                    Cursor cs = db.rawQuery("select * from lyonGym where appointment_id = ?", new String[]{time});
                    cs.moveToFirst();
                    String month = cs.getString(cs.getColumnIndexOrThrow("month"));
                    String day = cs.getString(cs.getColumnIndexOrThrow("date"));
                    String hour = cs.getString(cs.getColumnIndexOrThrow("time"));
                    Appointment temp = new Appointment("Village gym", time,month,day,hour);
                    result.add(temp);
                }
            }
            vcs.close();
        }
        return result;
    }
    public List<Appointment> getCurrentAppointments(String un, int currmonth, int currday, int currhour) {
        List<Appointment> result = new ArrayList<Appointment>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor lcs = db.rawQuery("select * from lyonGymAppointment where username = ?", new String[]{un});
        if (lcs.getCount() > 0){
            while (lcs.moveToNext()) {
                String time = lcs.getString(0);
                if(isCurrentAppt(currmonth,currday,currhour,time)) {
                    Cursor cs = db.rawQuery("select * from lyonGym where appointment_id = ?", new String[]{time});
                    cs.moveToFirst();
                    String month = cs.getString(cs.getColumnIndexOrThrow("month"));
                    String day = cs.getString(cs.getColumnIndexOrThrow("date"));
                    String hour = cs.getString(cs.getColumnIndexOrThrow("time"));
                    Appointment temp = new Appointment("Lyon Center", time,month,day,hour);
                    result.add(temp);
                }
            }
            lcs.close();
        }
        Cursor vcs = db.rawQuery("select * from villageGymAppointment where username = ?", new String[]{un});
        if (vcs.getCount() > 0){
            while (vcs.moveToNext()) {
                String time = vcs.getString(0);
                if(isCurrentAppt(currmonth,currday,currhour,time)) {
                    Cursor cs = db.rawQuery("select * from lyonGym where appointment_id = ?", new String[]{time});
                    cs.moveToFirst();
                    String month = cs.getString(cs.getColumnIndexOrThrow("month"));
                    String day = cs.getString(cs.getColumnIndexOrThrow("date"));
                    String hour = cs.getString(cs.getColumnIndexOrThrow("time"));
                    Appointment temp = new Appointment("Village gym", time,month,day,hour);
                    result.add(temp);
                }
            }
            vcs.close();
        }
        return result;
    }
    public boolean isCurrentAppt(int cm, int cd, int ch, String timeid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("select month, date, time from lyonGym where appointment_id = ?", new String[]{timeid});
        cs.moveToFirst();
        if(cs.getCount() == 1) {
            int month = Integer.parseInt(cs.getString(cs.getColumnIndexOrThrow("month")));
            int day = Integer.parseInt(cs.getString(cs.getColumnIndexOrThrow("date")));
            int hour = Integer.parseInt(cs.getString(cs.getColumnIndexOrThrow("time")));
            if(month >= cm){
                if(day > cd){
                    return true;
                }else if (day == cd){
                    if(hour >= ch){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public List<Appointment> getNotification(String un){
        List<Appointment> result = new ArrayList<Appointment>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery("select * from notificationList where user = ?", new String[]{un});
        while(cs.moveToNext()){
            String apptid = cs.getString(cs.getColumnIndexOrThrow("appointment_id"));
            String rec = cs.getString(cs.getColumnIndexOrThrow("gym"));
            System.out.println(rec);
            Cursor idcs = db.rawQuery("select month, date, time from lyonGym where appointment_id = ?", new String[]{apptid});
            String month = "";
            String day = "";
            String hour = "";
            if(idcs.getCount() == 1 && idcs != null && idcs.moveToFirst()) {
                 month = idcs.getString(idcs.getColumnIndexOrThrow("month"));
                 day = idcs.getString(idcs.getColumnIndexOrThrow("date"));
                 hour = idcs.getString(idcs.getColumnIndexOrThrow("time"));
            }
            Appointment temp = new Appointment(rec, apptid, month, day, hour);
            result.add(temp);
        }

        db.delete("notificationList","user=?",new String[]{un});
        cs.close();
        return result;
    }
}
