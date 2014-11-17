package ke.co.ma3map.android.carriers;

import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

import ke.co.ma3map.android.helpers.Database;
import ke.co.ma3map.android.helpers.JSONObject;

/**
 * Created by jason on 21/09/14.
 */
public class Stop implements Parcelable{
    //stop_id text, stop_name text, stop_code text, stop_desc text, stop_lat text, stop_lon text, location_type int, parent_station text
    public static final String PARCELABLE_KEY = "Stop";
    public static final String[] ALL_COLUMNS = new String[]{"stop_id", "stop_name", "stop_code", "stop_desc", "stop_lat", "stop_lon", "location_type", "parent_station"};

    private String id;
    private String name;
    private String code;
    private String desc;
    private String lat;
    private String lon;
    private int locationType;
    private String parentStation;

    public Stop(){
        id = null;
        name = null;
        code = null;
        desc = null;
        lat = null;
        lon = null;
        locationType = -1;
        parentStation = null;
    }

    public Stop(Parcel source){
        this();
        readFromParcel(source);
    }

    public Stop(JSONObject stopData) throws JSONException{
        id = stopData.getString("stop_id");
        name = stopData.getString("stop_name");
        code = stopData.getString("stop_code");
        desc = stopData.getString("stop_desc");
        lat = stopData.getString("stop_lat");
        lon = stopData.getString("stop_lon");
        locationType = stopData.getInt("location_type");
        parentStation = stopData.getString("parent_station");
    }

    public Stop(Database database, SQLiteDatabase readableDB, String id){
        String query = "stop_id='"+id+"'";
        String[][] rows = database.runSelectQuery(readableDB, Database.TABLE_STOP, ALL_COLUMNS, query, null, null, null, null, null);
        if(rows != null && rows.length == 1){
            //{"stop_id", "stop_name", "stop_code", "stop_desc", "stop_lat", "stop_lon", "location_type", "parent_station"}
            name = rows[0][1];
            code = rows[0][2];
            desc = rows[0][3];
            lat = rows[0][4];
            lon = rows[0][5];
            locationType = Integer.parseInt(rows[0][6]);
            parentStation = rows[0][7];
        }
    }

    public void insertIntoDB(Database database, SQLiteDatabase writableDB, String lineID){
        String[] values = {id, name, code, desc, lat, lon, String.valueOf(locationType), parentStation};

        database.runInsertQuery(Database.TABLE_STOP, ALL_COLUMNS, values, 0, writableDB);

        database.runInsertQuery(Database.TABLE_STOP_LINE, new String[]{"stop_id", "line_id"}, new String[]{id, lineID}, -1, writableDB);
    }

    /**
     * This method returns the distance in metres between this stop and the provided point
     *
     * @param point The point you want to get the distance to
     *
     * @return The distance in metres between this point and the provided point. Will return -1 if
     *          unable to calculate the distance
     */
    public double getDistance(LatLng point){
        final int earthRadius = 6371;
        LatLng stopLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

        double latDiff = Math.toRadians(stopLocation.latitude - point.latitude);
        double lonDiff = Math.toRadians(stopLocation.longitude - point.longitude);

        double a = (Math.sin(latDiff/2) * Math.sin(latDiff/2))
                    + Math.sin(lonDiff/2)
                    * Math.sin(lonDiff/2)
                    * Math.cos(Math.toRadians(stopLocation.latitude))
                    * Math.cos(Math.toRadians(point.latitude));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = earthRadius * c;

        return d * 1609;//convert to metres
    }

    public String getId(){
        return this.id;
    }

    public String getLat(){
        return this.lat;
    }

    public String getLon(){
        return this.lon;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(code);
        parcel.writeString(desc);
        parcel.writeString(lat);
        parcel.writeInt(locationType);
        parcel.writeString(parentStation);
    }

    public void readFromParcel(Parcel in){
        id = in.readString();
        name = in.readString();
        code = in.readString();
        desc = in.readString();
        lat = in.readString();
        lon = in.readString();
        locationType = in.readInt();
        parentStation = in.readString();
    }

    /**
     * This static object is to facilitate for other parcelable objects to carry a Stop object
     */
    public static final Creator<Stop> CREATOR=new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel source)
        {
            return new Stop(source);
        }

        @Override
        public Stop[] newArray(int size)
        {
            return new Stop[size];
        }
    };
}
