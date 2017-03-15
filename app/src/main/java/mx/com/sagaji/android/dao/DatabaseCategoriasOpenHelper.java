package mx.com.sagaji.android.dao;

import java.io.File;
import com.atcloud.android.dao.engine.DatabaseServices;
import android.database.sqlite.SQLiteDatabase;

import mx.com.sagaji.android.AndroidApplication;

public class DatabaseCategoriasOpenHelper {
    private static DatabaseCategoriasOpenHelper singleton = null;
    private File database = null;

    private DatabaseCategoriasOpenHelper() {
        database = new File(AndroidApplication.getStorage(), "SQLiteCE.db");
        if (!database.exists()) {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(database.getPath(), null);
            onCreate(db);
            db.close();
        }
    }

    public static DatabaseCategoriasOpenHelper getInstance() {
        if(singleton==null)
            singleton = new DatabaseCategoriasOpenHelper();
        return singleton;
    }

    //public DatabaseServices getReadableDatabaseServices() {
    //	SQLiteDatabase db = SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.OPEN_READONLY);
    //	return new DatabaseServices(db);
    //}

    public DatabaseServices getWritableDatabaseServices() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        return new DatabaseServices(db);
    }

    private void onCreate(SQLiteDatabase db) {
    }
}