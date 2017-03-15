package mx.com.sagaji.android.util;

import android.util.Log;
import android.util.Xml;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Numero;
import com.atcloud.android.util.Reflector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import mx.com.sagaji.android.to.ExistenciaTO;
import mx.com.sagaji.android.to.FacturaCobranzaTO;
import mx.com.sagaji.android.to.FacturaDevolucionDetalleTO;
import mx.com.sagaji.android.to.FacturaDevolucionTO;

public class XMLParser {
    public static String LOGTAG = XMLParser.class.getCanonicalName();

    private static final String ns = null;
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    //
    public ArrayList<ExistenciaTO> parseExistencia(String xml) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xml));
        parser.nextTag();

        ArrayList<ExistenciaTO> existencias = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "existencias");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.compareTo("detalles")==0) {
                existencias.add(readExistencias(parser));
            } else {
                skip(parser);
            }
        }

        return existencias;
    }

    private ExistenciaTO readExistencias(XmlPullParser parser) throws XmlPullParserException, IOException {
        ExistenciaTO existenciaTO = new ExistenciaTO();

        parser.require(XmlPullParser.START_TAG, ns, "detalles");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.compareTo("CODIGO")==0) {
                existenciaTO.CODIGO = readData(parser, name);
            } else if (name.compareTo("DESCRIPCION")==0) {
                existenciaTO.DESCRIPCION = readData(parser, name);
            } else if (name.compareTo("PRECIO")==0) {
                existenciaTO.PRECIO = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("UNIDAD")==0) {
                existenciaTO.UNIDAD = readData(parser, name);
            } else if (name.compareTo("MEXICO")==0) {
                existenciaTO.MEXICO = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("LEON")==0) {
                existenciaTO.LEON = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("PUEBLA")==0) {
                existenciaTO.PUEBLA = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("TUXTLA")==0) {
                existenciaTO.TUXTLA = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("OAXACA")==0) {
                existenciaTO.OAXACA = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("LINEA")==0) {
                existenciaTO.LINEA = readData(parser, name);
            } else {
                skip(parser);
            }
        }

        Log.d(LOGTAG, Reflector.toStringAllFields(existenciaTO));
        return existenciaTO;
    }
    //

    //
    public ArrayList<FacturaCobranzaTO> parseFacturasCobranza(String xml) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xml));
        parser.nextTag();

        ArrayList<FacturaCobranzaTO> facturas = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "documentos");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.compareTo("detalles")==0) {
                facturas.add(readFacturasCobranza(parser));
            } else {
                skip(parser);
            }
        }

        return facturas;
    }

    private FacturaCobranzaTO readFacturasCobranza(XmlPullParser parser) throws XmlPullParserException, IOException {
        FacturaCobranzaTO facturaCobranzaTO = new FacturaCobranzaTO();

        parser.require(XmlPullParser.START_TAG, ns, "detalles");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.compareTo("C2")==0) {
                facturaCobranzaTO.C2 = readData(parser, name);
            } else if (name.compareTo("Documento")==0) {
                facturaCobranzaTO.Documento = readData(parser, name);
            } else if (name.compareTo("Fecha_Factura")==0) {
                String date = readData(parser, name);
                try {
                    facturaCobranzaTO.Fecha_Factura = df.parse(date);
                } catch(Exception e) {
                    Log.d(LOGTAG, "error parsing date: "+date);
                }
            } else if (name.compareTo("Fecha_Vence")==0) {
                String date = readData(parser, name);
                try {
                    facturaCobranzaTO.Fecha_Vence = df.parse(date);
                } catch(Exception e) {
                    Log.d(LOGTAG, "error parsing date: "+date);
                }
            } else if (name.compareTo("Monto")==0) {
                facturaCobranzaTO.Monto = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("Saldo")==0) {
                facturaCobranzaTO.Saldo = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("MontoOrginal")==0) {
                facturaCobranzaTO.MontoOrginal = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("PLAZO")==0) {
                facturaCobranzaTO.PLAZO = Numero.getIntFromString(readData(parser, name));
            } else if (name.compareTo("DiasVencidos")==0) {
                facturaCobranzaTO.DiasVencidos = Numero.getIntFromString(readData(parser, name));
            } else if (name.compareTo("DiasTrascurridos")==0) {
                facturaCobranzaTO.DiasTrascurridos = Numero.getIntFromString(readData(parser, name));
            } else if (name.compareTo("Estatus")==0) {
                facturaCobranzaTO.Estatus = readData(parser, name);
            } else {
                skip(parser);
            }
        }

        Log.d(LOGTAG, Reflector.toStringAllFields(facturaCobranzaTO));
        return facturaCobranzaTO;
    }
    //

    //
    public ArrayList<FacturaDevolucionDetalleTO> parseFacturaDevolucionDetalles(String xml) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xml));
        parser.nextTag();

        ArrayList<FacturaDevolucionDetalleTO> detalles = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "facturas");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.compareTo("detalles")==0) {
                detalles.add(readFacturaDevolucionDetalle(parser));
            } else {
                skip(parser);
            }

        }

        return detalles;
    }

    private FacturaDevolucionDetalleTO readFacturaDevolucionDetalle(XmlPullParser parser) throws XmlPullParserException, IOException {
        FacturaDevolucionDetalleTO facturaDevolucionDetalleTO = new FacturaDevolucionDetalleTO();

        parser.require(XmlPullParser.START_TAG, ns, "detalles");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.compareTo("DOC")==0) {
                facturaDevolucionDetalleTO.DOC = readData(parser, name);
            } else if (name.compareTo("PARTIDA")==0) {
                facturaDevolucionDetalleTO.PARTIDA = Numero.getIntFromString(readData(parser, name));
            } else if (name.compareTo("CODPROD")==0) {
                facturaDevolucionDetalleTO.CODPROD = readData(parser, name);
            } else if (name.compareTo("CANTIDAD")==0) {
                facturaDevolucionDetalleTO.CANTIDAD = Numero.getIntFromString(readData(parser, name));
            } else if (name.compareTo("UM")==0) {
                facturaDevolucionDetalleTO.UM = readData(parser, name);
            } else if (name.compareTo("PU")==0) {
                facturaDevolucionDetalleTO.PU = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("SUBTOTAL")==0) {
                facturaDevolucionDetalleTO.SUBTOTAL = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("DESCRIP")==0) {
                facturaDevolucionDetalleTO.DESCRIP = readData(parser, name);
            } else {
                skip(parser);
            }
        }

        Log.d(LOGTAG, Reflector.toStringAllFields(facturaDevolucionDetalleTO));
        return facturaDevolucionDetalleTO;
    }
    //

    //
    public ArrayList<FacturaDevolucionTO> parseFacturasDevolucion(String xml) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xml));
        parser.nextTag();

        ArrayList<FacturaDevolucionTO> facturas = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "facturas");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.compareTo("detalles")==0) {
                facturas.add(readFacturasDevolucion(parser));
            } else {
                skip(parser);
            }
        }

        return facturas;
    }

    private FacturaDevolucionTO readFacturasDevolucion(XmlPullParser parser) throws XmlPullParserException, IOException {
        FacturaDevolucionTO facturaDevolucionTO = new FacturaDevolucionTO();

        parser.require(XmlPullParser.START_TAG, ns, "detalles");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.compareTo("DOCUMENTO")==0) {
                facturaDevolucionTO.DOCUMENTO = readData(parser, name);
            } else if (name.compareTo("MONEDA")==0) {
                facturaDevolucionTO.MONEDA = readData(parser, name);
            } else if (name.compareTo("FEC_FACTURA")==0) {
                String date = readData(parser, name).replace("T"," ");
                try {
                    facturaDevolucionTO.FEC_FACTURA = Fecha.getFechaHoraTZ(date);
                } catch (Exception e) {
                    Log.d(LOGTAG, "error parsing date: "+date);
                }
            } else if (name.compareTo("CLIENTE")==0) {
                facturaDevolucionTO.CLIENTE = readData(parser, name);
            } else if (name.compareTo("ESTATUS")==0) {
                facturaDevolucionTO.ESTATUS = readData(parser, name);
            } else if (name.compareTo("ASESOR")==0) {
                facturaDevolucionTO.ASESOR = readData(parser, name);
            } else if (name.compareTo("IVA")==0) {
                facturaDevolucionTO.IVA = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("IMPORTE")==0) {
                facturaDevolucionTO.IMPORTE = Numero.getDoubleFromString(readData(parser, name));
            } else if (name.compareTo("FILIAL")==0) {
                facturaDevolucionTO.FILIAL = readData(parser, name);
            } else {
                skip(parser);
            }
        }

        Log.d(LOGTAG, Reflector.toStringAllFields(facturaDevolucionTO));
        return facturaDevolucionTO;
    }
    //

    //
    //
    //

    private String readData(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String data = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return data.trim();
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
