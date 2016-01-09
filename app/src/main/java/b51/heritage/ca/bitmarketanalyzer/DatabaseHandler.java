package b51.heritage.ca.bitmarketanalyzer;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mbassett on 12/7/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper{
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "BitmarketDB";

    // Labels table name
    private static final String TABLE_CURRENCY = "Currency";

    // Labels Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_CODE = "code";
    private static final String KEY_SYMBOL = "symbol";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Category table create query
        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CURRENCY + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CODE + " TEXT," + KEY_SYMBOL + " TEXT)";
        db.execSQL(CREATE_CATEGORIES_TABLE);

        ContentValues vals = new ContentValues();
        vals.put(KEY_CODE, "CAD");
        vals.put(KEY_SYMBOL, "$");

        db.insert(TABLE_CURRENCY, null, vals);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCY);

        // Create tables again
        onCreate(db);
    }

    public boolean addCurrency(Currency curr){

        List<Currency> currencies = getCurrencies();

        SQLiteDatabase db = this.getWritableDatabase();

        for(Currency currency : currencies){
            if(curr.getCode().equals(currency.getCode())){
                return false;
            }
        }

        ContentValues vals = new ContentValues();
        vals.put(KEY_CODE, curr.getCode());
        vals.put(KEY_SYMBOL, curr.getSymbol());

        db.insert(TABLE_CURRENCY, null, vals);

        db.close();

        return true;
    }

    public List<Currency> getCurrencies(){
        List<Currency> currencies = new ArrayList<Currency>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_CODE + ", " + KEY_SYMBOL + " FROM " + TABLE_CURRENCY;

        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                String code = cursor.getString(0);
                String symbol = cursor.getString(1);
                Currency curr = new Currency(code, symbol);

                currencies.add(curr);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return currencies;
    }
}
