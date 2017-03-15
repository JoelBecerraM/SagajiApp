package mx.com.sagaji.android.to;

import com.atcloud.android.dao.engine.DatabaseRecord;
import java.io.File;

/**
 * Created by jbecerra.
 */
public class CatalogoTO {
    public DatabaseRecord dao;
    public File file;
    public boolean delete;

    public CatalogoTO(DatabaseRecord dao, File file) {
        this.dao = dao;
        this.file = file;
        this.delete = true;
    }

    public CatalogoTO(DatabaseRecord dao, File file, boolean delete) {
        this.dao = dao;
        this.file = file;
        this.delete = delete;
    }

    @Override
    public String toString() {
        return dao.toString()+";"+file.getAbsolutePath();
    }
}