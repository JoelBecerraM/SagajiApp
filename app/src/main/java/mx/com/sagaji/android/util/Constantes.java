package mx.com.sagaji.android.util;

import android.graphics.Color;

/**
 * Created by jbecerra.
 */
public class Constantes {
    public static String ESTADO_PENDIENTE = "PE";
    public static String ESTADO_TERMINADO = "TE";
    public static String ESTADO_ENVIADO = "EN";
    public static String ESTADO_BORRADO = "BR";

    public static String ACCION_ABRIR = "accion_abrir";
    public static String ACCION_IMPRIMIR = "accion_imprimir";
    public static String ACCION_BORRAR = "accion_borrar";
    public static String ACCION_REENVIAR = "accion_reenviar";

    public static int THREAD_DONE = 0;
    public static int THREAD_INIT = 1;
    public static int THREAD_RUNNING = 2;

    public static String ESTADO_PEDIDO_AUTORIZACION_INVERSION = "AI";

    public static int ESTADO_AUTORIZA_INVERSION_PENDIENTE = 0;
    public static int ESTADO_AUTORIZA_INVERSION_APROBADA = 1;
    public static int ESTADO_AUTORIZA_INVERSION_CANCELADA = 2;

    public static String FOLIO_VISITA = "visita";
    public static String FOLIO_PEDIDO = "pedidos";
    public static String FOLIO_DEVOLUCION = "devolucion";
    public static String FOLIO_COBRANZA = "cobranza";
    public static String FOLIO_UBICACION = "ubicacion";
    public static String FOLIO_TRANSMISION_VISITA = "transmisionvisita";
    public static String FOLIO_TRANSMISION_PEDIDO = "transmisionpedidos";
    public static String FOLIO_TRANSMISION_UBICACION = "transmisionubicacion";

    public static int ACTIVITY_CLIENTE = 0;
    public static int ACTIVITY_PEDIDOS = 1;
    public static int ACTIVITY_PEDIDOS_GUARDAR = 2;
    public static int ACTIVITY_DEVOLUCIONES = 3;
    public static int ACTIVITY_DEVOLUCIONES_GUARDAR = 4;
    public static int ACTIVITY_COBRANZA = 5;
    public static int ACTIVITY_COBRANZA_GUARDAR = 6;
    public static int ACTIVITY_SINCRONIZACION_CATALOGOS = 7;
    public static int ACTIVITY_ENVIA_OPERACIONES = 8;
    public static int ACTIVITY_CONFIGURACION = 9;
    public static int ACTIVITY_ANTERIORES = 10;
    public static int ACTIVITY_BUSQUEDA = 11;
    public static int ACTIVITY_CATEGORIAS = 12;
    public static int ACTIVITY_REFERENCIA = 13;

    public static String PARAMETRO_WEBSERVICEURI = "WebServiceURI";
    public static String PARAMETRO_ESPERAUBICACION = "EsperaUbicacion";
    public static String PARAMETRO_CONTRASENACONFIGURACION = "ContrasenaConfiguracion";
    public static String PARAMETRO_ESPERAMAXSINCRONIZACION = "EsperaSincronizacion";

    public static long ESPERA_MAX_SINCRONIZACION = 1000 * 60 * 60 * 24 * 3;

    public static String EXTRA_ID = "id";
    public static String EXTRA_CONFIGURACION = "configuracion";
    public static String EXTRA_CLIENTE = "cliente";
    public static String EXTRA_FOLIO = "folio";
    public static String EXTRA_INICIOVISTA = "iniciovisita";
    public static String EXTRA_UBICACION = "ubicacion";
    public static String EXTRA_FECHAENTREGA = "fechaentrega";
    public static String EXTRA_DIRECCION = "direccion";

    public static long ENVIAR_PEDIDOS      = 0x00000001l;
    public static long ENVIAR_VISITAS      = 0x00000010l;
    public static long ENVIAR_MENSAJES     = 0x00000100l;
    public static long ENVIAR_UBICACIONES  = 0x00001000l;
    public static long ENVIAR_DEVOLUCIONES = 0x00010000l;
    public static long ENVIAR_COBRANZAS    = 0x00100000l;

    public static int FAMILIA_COLOR_NORMAL = Color.TRANSPARENT;
    public static int FAMILIA_COLOR_AGREGADA = Color.rgb(0xFF, 0xFE, 0xB2);
    public static int PRODUCTO_COLOR_NORMAL = Color.TRANSPARENT;
    public static int PRODUCTO_COLOR_AGREGADO = Color.rgb(0xFF, 0xFE, 0xB2);
    public static int PRODUCTO_COLOR_PROMOCION = Color.rgb(0xA3, 0xCA, 0xDC);

    public static String SINCRONIZACION_LOG = "Sincronizacion.log";
}