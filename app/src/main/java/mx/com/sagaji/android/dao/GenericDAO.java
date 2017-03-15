package mx.com.sagaji.android.dao;

import com.atcloud.android.dao.engine.DatabaseServices;
import mx.com.sagaji.android.dao.entity.FolioDAO;

/**
 * Created by jbecerra.
 */
public class GenericDAO {

    public static int obtenerSiguienteFolio(DatabaseServices ds, String tipo) throws Exception {
        FolioDAO foliosDAO = (FolioDAO)ds.first(new FolioDAO(), "tipo = '"+tipo+"'");
        if (foliosDAO == null) {
            foliosDAO = new FolioDAO();
            foliosDAO.tipo = tipo;
            foliosDAO.folio = 1;

            ds.insert(foliosDAO);
        } else {
            foliosDAO.folio ++;

            ds.update(foliosDAO);
        }
        return foliosDAO.folio;
    }
}