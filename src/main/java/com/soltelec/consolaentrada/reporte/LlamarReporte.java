/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soltelec.consolaentrada.reporte;

import com.soltelec.consolaentrada.models.entities.Medida;
import com.soltelec.consolaentrada.models.entities.Reinspeccion;
import com.soltelec.consolaentrada.models.entities.Cda;
import com.soltelec.consolaentrada.models.entities.Certificado;
import com.soltelec.consolaentrada.models.entities.Prueba;
import com.soltelec.consolaentrada.models.entities.HojaPruebas;
import com.soltelec.consolaentrada.configuration.Conexion;
import com.soltelec.consolaentrada.custom.ModeloTablaImpresionFecha;
import com.soltelec.consolaentrada.models.controllers.CdaJpaController;
import com.soltelec.consolaentrada.models.controllers.EquiposJpaController1;
import com.soltelec.consolaentrada.models.controllers.HojaPruebasJpaController;
import com.soltelec.consolaentrada.models.controllers.ReinspeccionJpaController;
import com.soltelec.consolaentrada.models.controllers.ReporteJpaController;
import com.soltelec.consolaentrada.models.entities.AuditoriaSicov;
import com.soltelec.consolaentrada.models.entities.Equipo;
import com.soltelec.consolaentrada.models.entities.TipoVehiculo;
import com.soltelec.consolaentrada.utilities.CargarArchivos;
import com.soltelec.consolaentrada.utilities.Mensajes;
import com.soltelec.consolaentrada.utilities.SepararImagenes;
import com.soltelec.consolaentrada.utilities.UtilConexion;
import com.soltelec.consolaentrada.utilities.Validaciones;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Esta clase se encarga de llenar el reporte en la base de datos. En esta clase
 * esta la logica de la evaluacion de la prueba
 *
 * @author Gerencia TIC
 */
public class LlamarReporte 
{

    Map parametros = null;
    private boolean imprimirPdf;
    JasperReport report, report2;
    public int dve;
    final String rutaReporte = "FUR03625.jasper";
    final String rutaReporte2 = "report2.jasper";
    ConsultasCertificados consultasCertificados;
    Consultas consultas;
    private ListenerPrimerCertificado listenerPrimerCertificado;
    private double ang, Int, angv, Intv, angv_izq, Intv_izq;
    public boolean int1, int2;
    private Reinspeccion reinspecionActual;
    private boolean reinspeccion;
    private HojaPruebas ctxHojaPrueba;
    private Cda ctxCDA;
    
    private int idHojaPrueba;
    private boolean isReinspecion;
    
    public LlamarReporte() 
    {
        try {
            consultasCertificados = new ConsultasCertificados();
            String strConsulta = "SELECT DATEDIFF((SELECT h.Fecha_ingreso_vehiculo from hoja_pruebas WHERE h.TESTSHEET=?),(SELECT s.fecha_actualizacion	FROM software s WHERE s.Id_sw=1))";
            report = (JasperReport) JRLoader.loadObject(CargarArchivos.cargarArchivo(rutaReporte));
            report2 = (JasperReport) JRLoader.loadObject(CargarArchivos.cargarArchivo(rutaReporte2));
            consultas = new Consultas();
        } catch (Throwable ex) {
            int even = 0;
        }
    }

    public LlamarReporte(boolean imprimirPdf) 
    {
        this.imprimirPdf = imprimirPdf;
        try {
            consultasCertificados = new ConsultasCertificados();
            report = (JasperReport) JRLoader.loadObject(CargarArchivos.cargarArchivo(rutaReporte));
            report2 = (JasperReport) JRLoader.loadObject(CargarArchivos.cargarArchivo(rutaReporte2));
            consultas = new Consultas();
        } catch (Throwable ex) {
            int even = 0;
        }
    }

    /**
     * Metodo de entrada o de inicio a partir de este se llaman todos los demas
     * metodos de la clase se abre solamente una conexion
     *
     * Se modifica para hacerlo mas legible y que no cargue tanta informacion
     * que no ira a utilizar.
     *
     * @param hojaPruebas
     * @param consecutivoRUNT
     * @throws JRException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void cargarReporte(HojaPruebas ctxHojaPrueba, Cda ctxCDA, long consecutivoRUNT, String txtPlaca) throws JRException, SQLException, ClassNotFoundException 
    {
        this.ctxHojaPrueba = ctxHojaPrueba;
        this.ctxCDA = ctxCDA;
        idHojaPrueba=ctxHojaPrueba.getId();
        List<Prueba> ctxIndPruebas = null;

        try (Connection cn = cargarConexion())
        {
            llamarProcedimientoMedidas(cn, this.ctxHojaPrueba.getId());
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
            }
            parametros = new HashMap();

            parametros.put("ConsecutivoFecha","");
            parametros.put("Aprobado", "");
            parametros.put("codigoPrueba","");
            parametros.put("MarcaDesvia", "");
            parametros.put("InstDesvia", "");
            parametros.put("InstSusp", "");
            parametros.put("MarcaSusp", "");
        
            if (this.ctxHojaPrueba.getIntentos()>1)
            {
                parametros.put("ConsecutivoFecha",this.ctxHojaPrueba.getCon_hoja_prueba()+" - 1 , "+ new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(this.ctxHojaPrueba.getFechaIngreso()));
            }
               
            reinspeccion = this.ctxHojaPrueba.getReinspeccionList().size() > 0;  //consultas.isReinspeccion(this.ctxHojaPrueba.getId() , cn);            
            if (reinspeccion) 
            {
                parametros.put("codigoPrueba",this.ctxHojaPrueba.getCon_hoja_prueba()+" - 2"); 
                reinspecionActual = this.ctxHojaPrueba.getReinspeccionList().iterator().next();
                parametros.put("fecha", new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(reinspecionActual.getFechaSiguiente()));
                parametros.put("isReinspeccion", "X");
                parametros.put("idReinspeccion", reinspecionActual.getId());
                ctxIndPruebas = cargarIdPruebasNoDuplicadas(this.ctxHojaPrueba, "R");
                cargarDilucion("R");
                cargarInfo();
                cargarMedidas(ctxIndPruebas);
                parametros.put("Comentarios", cargarComentariosFUR(this.ctxHojaPrueba, "R", ctxIndPruebas));
                parametros.put("Comentarios", cargarObservacionesDB(this.ctxHojaPrueba, ctxIndPruebas));

                cargarSeriales(this.ctxHojaPrueba.getId(), cn);

            } else {
                parametros.put("codigoPrueba",this.ctxHojaPrueba.getCon_hoja_prueba()+" - 1"); 
                parametros.put("fecha", new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(this.ctxHojaPrueba.getFechaIngreso()));
                parametros.put("isReinspeccion", "NO");
                ctxIndPruebas = cargarIdPruebasNoDuplicadas(this.ctxHojaPrueba, "I");
                cargarDilucion("I");
                cargarInfo();
                cargarMedidas(ctxIndPruebas);
                cargarSeriales(this.ctxHojaPrueba.getId(), cn);
                parametros.put("Comentarios",cargarComentariosFUR(this.ctxHojaPrueba, "I", ctxIndPruebas));
                parametros.put("Comentarios", cargarObservacionesDB(this.ctxHojaPrueba, ctxIndPruebas));
            }

            configurarPermisibles();
            try {
                cargarImagen(reinspeccion, parametros, reinspeccion ? 1 : 0,false);
            } catch (Exception ex) {
            }

            cargarDefectos(cn, this.ctxHojaPrueba.getId(), txtPlaca, this.ctxHojaPrueba.getNroPruebasRegistradas());//Este evalua la prueba
            HojaPruebasJpaController hpContJpa = new HojaPruebasJpaController();
            String edoHP = (hpContJpa.verificarHojaFinalizada(ctxHojaPrueba));
            parametros.put("Aprobado", ""); 
            parametros.put("Reprobado", "");
            if (edoHP.equalsIgnoreCase("APROBADA")) {
                parametros.put("Aprobado", "X");
            }
            if (edoHP.equalsIgnoreCase("REPROBADA")) {
                parametros.put("Reprobado", "X");
                if (ctxHojaPrueba.getIntentos() == 1) {
                    Calendar calDias = Calendar.getInstance();
                    calDias.setTime(ctxHojaPrueba.getFechaIngreso());
                    calDias.add(Calendar.DATE, 14);
                    int m = calDias.get(Calendar.MONTH) + 1;
                    String mS;
                    if (m <= 9) {
                        mS = "0" + m;
                    } else {
                        mS = String.valueOf(m);
                    }
                    parametros.put("fechaFinRep", calDias.get(Calendar.DATE) + "/" + mS + "/" + calDias.get(Calendar.YEAR) + " Hora:" + calDias.get(Calendar.HOUR_OF_DAY) + ":" + calDias.get(Calendar.MINUTE) + ":" + calDias.get(Calendar.SECOND));
                } else {
                    parametros.put("fechaFinRep", " ");
                }
            }
            JasperPrint fillReport;

            boolean preventiva = consultas.isRevisionPreventiva(this.ctxHojaPrueba);
            if (preventiva) {
                fillReport = JasperFillManager.fillReport(report2, parametros, cn);
            } else {
                fillReport = JasperFillManager.fillReport(report, parametros, cn);
            }
//          JasperPrint fillReport = JasperFillManager.fillReport(report, parametros, cn);
//          JasperPrint fillReport2 = JasperFillManager.fillReport(report2, parametros, cn);
//          String destFileNamePdf="./certificado"+hojaPruebas+".pdf";

            File carpetaReportes = new File("C:\\Reportes");

            if (!carpetaReportes.exists()) {
                carpetaReportes.mkdir();
            }

            HojaPruebasJpaController hpControler = new HojaPruebasJpaController();
            List<AuditoriaSicov> lstTramas = hpControler.recogerTramasExist(this.ctxHojaPrueba);
            ListenerEnvioFUR listenerEnvioFUR = new ListenerEnvioFUR(this.ctxHojaPrueba.getId().longValue(), lstTramas);
            JRViewerModificado jvModificado = new JRViewerModificado(fillReport);
            jvModificado.setListenerImprimir(listenerEnvioFUR);
            JFrame app = new JFrame("TEST");
            app.setTitle("Impresion del FUR");
            app.setContentPane(jvModificado);

            String destFileNamePdf = "C:\\Reportes\\reporte" + this.ctxHojaPrueba.getId() + ".pdf";

            if (imprimirPdf) {
                JasperExportManager.exportReportToPdfFile(fillReport, destFileNamePdf);
                try {
                    File path = new File(destFileNamePdf);
                    Desktop.getDesktop().open(path);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                //Cambio FUR Preventiva
//                if (consultas.isRevisionPreventiva(hojaPruebas, cn)) {
//                    JasperViewer jasperViewer2 = new JasperViewer(fillReport2, false);
//                    jasperViewer2.setVisible(true);
//                } else {
                JasperViewer jasperViewer = new JasperViewer(fillReport, false);
                jasperViewer.setContentPane(jvModificado);
                if (preventiva) {
                    jasperViewer.setTitle("Impresion PREVENTIVA");
                } else {
                    jasperViewer.setTitle("Impresion FUR");
                }
                jasperViewer.setVisible(true);
            }

            //VERIFICAR QUE NO SEA UNA PREVENTIVA
//            consultas = new Consultas();
//            if (consultas.isRevisionPreventiva(hojaPruebas, cn)) {
            if (preventiva) {
                JOptionPane.showMessageDialog(null, "REVISION PREVENTIVA NO SE IMPRIME CERTIFICADO");
            } else if ((parametros.get("Aprobado")) != null && parametros.get("Aprobado").equals("X") && consecutivoRUNT > 0) {
                imprimirCertificado(this.ctxHojaPrueba.getId(), consecutivoRUNT, cn);
            }
        }
    }

    
    
    public void cargaEmegencia() {
        try {
            consultasCertificados = new ConsultasCertificados();
//            report = (JasperReport) JRLoader.loadObjectFromFile(rutaReporte);
            report = (JasperReport) JRLoader.loadObject(CargarArchivos.cargarArchivo(rutaReporte));
//            report2 = (JasperReport) JRLoader.loadObjectFromFile(rutaReporte2);
            consultas = new Consultas();
            report2 = (JasperReport) JRLoader.loadObject(CargarArchivos.cargarArchivo(rutaReporte2));
        } catch (Throwable ex) {
            //  Mensajes.mostrarExcepcion(ex);
            int eve = 0;
        }
    }

    /**
     * Este mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©todo
     * cargarÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ un reporte pero de
     * reinspecciÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³n, es decir un
     * reporte que se desea mantener en la base de datos
     *
     * @param reinspeccion
     * @throws java.lang.Exception
     */
    public void cargarReporteReinspeccion(Reinspeccion Reinspeccion) throws Exception {

        this.isReinspecion=true;
        parametros = new HashMap();
        //cargar la hoja de prueba con la que esta asociada la reinspeccion
        parametros.put("idReinspeccion", Reinspeccion.getId());
        parametros.put("Aprobado", "");
        parametros.put("Reprobado", "");
        parametros.put("ConsecutivoFecha","");
        parametros.put("MarcaDesvia", "");
        parametros.put("InstDesvia", "");
        parametros.put("InstSusp", "");
        parametros.put("MarcaSusp", "");
        
        parametros.put("codigoPrueba",Reinspeccion.getHojaPruebas().getCon_hoja_prueba() +" - 1");

        //no llama al procedimiento de medidas, puede ser necesario crear otro procedimiento pero para
        //reinspeccion
        try (Connection cn = cargarConexion()) {
            //no llama al procedimiento de medidas, puede ser necesario crear otro procedimiento pero para
            //reinspeccio
            ConsultasReinspeccion crei = new ConsultasReinspeccion();
            int idHojaPrueba = crei.obtenerIdHojaPruebas(Reinspeccion.getId(), cn);
            this.idHojaPrueba=idHojaPrueba;
            HojaPruebasJpaController hpc = new HojaPruebasJpaController();
            this.ctxHojaPrueba = hpc.find(idHojaPrueba);
            CdaJpaController cdaControler = cdaControler = new CdaJpaController();
            ctxCDA = cdaControler.find(1);
            configurarPermisibles();
            List<Integer> listaPruebasReinspeccion = crei.obtenerPruebasReinspeccion(idHojaPrueba, cn);
            ReporteJpaController rc = new ReporteJpaController();
            List<Prueba> listaPruebas = new ArrayList();
            for (Integer id : listaPruebasReinspeccion) 
            {
                listaPruebas.add(rc.findPruebas(id));
            }

            //aqui meter seriales
            cargarMedidas(listaPruebas);
            cargarDilucion("I");
            cargarInfo();
            cargarSeriales(this.ctxHojaPrueba.getId(), cn);
//            parametros.put("Comentarios", cargarComentariosFUR(this.ctxHojaPrueba, "I", listaPruebas));
            parametros.put("Comentarios", cargarObservacionesDB(this.ctxHojaPrueba, listaPruebas));
            parametros.put("consecutivoRUNT", this.ctxHojaPrueba.getConsecutivoRunt());
            parametros.put("numeroIntento", Reinspeccion.getIntento());
            parametros.put("isReinspeccion", "NO");
            reinspecionActual = new ReinspeccionJpaController().findReinspeccionByHoja(idHojaPrueba);
            parametros.put("fecha", new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(reinspecionActual.getFechaAnterior()));
            cargarImagen(true, parametros, 0,true);
            if (Reinspeccion.getAprobada().equals("Y")) {
                parametros.put("Aprobado", "X");
            } else {
                parametros.put("Reprobado", "X");
            }
            JasperPrint fillReport = JasperFillManager.fillReport(report, parametros, cn);
            //String destFileNamePdf="./certificado"+hojaPruebas+".pdf";
            File carpetaReportes = new File("C:\\Reportes");

            if (!carpetaReportes.exists()) {
                carpetaReportes.mkdir();
            }

            String destFileNamePdf = "C:\\Reportes\\reporte" + idHojaPrueba + ".pdf";

            if (imprimirPdf) {
                JasperExportManager.exportReportToPdfFile(fillReport, destFileNamePdf);
                // JasperRunManager.runReportToPdf(destFileNamePdf, parametros);
                try {
                    File path = new File(destFileNamePdf);
                    Desktop.getDesktop().open(path);
                } catch (IOException ex) {
                    Mensajes.mostrarExcepcion(ex);
                }
            } else {
                JasperViewer jasperViewer = new JasperViewer(fillReport, false); //generas tu visor del reporte
                jasperViewer.setVisible(true); //lo haces visible
            }
        }
    }

    /**
     * Abre una conexion con la base de datos
     *
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Connection cargarConexion() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Conexion conex = Conexion.getInstance();
        Connection conexion = DriverManager.getConnection(conex.getUrl(), conex.getUsuario(), conex.getContrasena());
        return conexion;
    }


    /**
     * Carga las medidas de las ultimas pruebas FINALIZADAS es decir si existe
     * una prueba no finalizada y autorizada el reporte mostrara las medidas de
     * la prueba anterior
     *
     * @param listaIds - La lista de las ultimas pruebas finalizadas
     * @param idHojaPrueba - La hoja de prueba
     */
    public void cargarMedidas(List<Prueba> ctxListPrueba) throws ClassNotFoundException, SQLException 
    {
        parametros.put("ID_HP", (long) this.ctxHojaPrueba.getId());
        System.out.print("La HP a imprimir es la " + this.ctxHojaPrueba.getId());
//      DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
    
        //Quitar las pruebas duplicadas, dejar solamente una prueba para cada tipo

        //y que sea la ultima insertada es decir ordenada, si se ordena de mayor a menor la
        //primera prueba que encuentre va a ser la de id mayor
        //Ordenar la lista por id de pruebas
        //buscar las pruebas de Inspeccion Sensorial y seleccionar la prueba con id mayor 1
        //buscar las pruebas de luces y seleccionar la prueba con id mayor 2
        //buscar las pruebas de desviacin y seleccionar la prueba con id mayor 4
        //buscar las pruebas de freno y seleccionar la prueba con id mayor 5
        //buscar las pruebas de suspension y seleccionar la prueba con id mayor 6
        //busscar las pruebas de ruido y seleccionar la prueba con mayor id 7
        //buscar las pruebas de gases y seleccionar la prueba con mayor id 8
        //buscar la prueba de taximetro y seleccionar la prueba con mayor id 9
        
        for (Prueba p : ctxListPrueba) 
        {
            List<Medida> listaMedidas = p.getMedidaList();

            if (listaMedidas != null)
            {
                int idPrueba=p.getId();
                int idTipoPrueba=p.getTipoPrueba().getId();
                int  idTipoGasolina=p.getHojaPruebas().getVehiculo().getTipoGasolina().getId();
                char  FormaMedTemperatura=p.getHojaPruebas().getFormaMedTemperatura();
                String disenoVehiculo=p.getHojaPruebas().getVehiculo().getDiseno().name();
                int tipoVehiculo=p.getHojaPruebas().getVehiculo().getTipoVehiculo().getId();
                
                if (idTipoPrueba == 1)
                    cargarMedidaPruebaVisual(idPrueba,listaMedidas);
                    
                if (idTipoPrueba == 2)
                {
                    cargarMedidaPruebaLucesMotos(listaMedidas);
                    cargarMedidaPruebaLucesVehiculos(idPrueba, listaMedidas, tipoVehiculo);
                }
                    
                if (int1 && int2) 
                    parametros.put("SumaLuces", 2);
                
                if (idTipoPrueba == 4)
                    cargaMedidaPruebaDesviacion(listaMedidas);
                                
                if (idTipoPrueba == 5) 
                    cargaMedidaPpruebaFrenos(idPrueba,listaMedidas);

                if (idTipoPrueba == 6) 
                    cargaMedidaPruebaSuspension(listaMedidas);

                if (idTipoPrueba== 7) 
                    cargaMedidaPruebaRuido(listaMedidas);
                
                if (idTipoPrueba == 8) 
                    cargarMedidaPruebaGases(listaMedidas,idTipoGasolina,FormaMedTemperatura,disenoVehiculo,tipoVehiculo);

                if (idTipoPrueba == 9) 
                    cargaMedidaPruebaTaximetro(listaMedidas);
            }
            
        }//end of for listaPruebas
    }


    /**
     * @autor ELKIN B
     * 
     * @param idPrueba
     * @param listaMedidas
     * @return 
     */
    private void cargarMedidaPruebaVisual(int idPrueba,List<Medida> listaMedidas) throws ClassNotFoundException
    {
        
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar medidas para la prueba de visual -------");
        System.out.println("----------------------------------------------------");
        
//      validarDefectosPorfundidaLabrado(idPrueba);
//        idPruebaVisual=idPrueba;
        
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        df.applyPattern("#0.00");
        try 
        {
            for (Medida m : listaMedidas)
            {
                switch (m.getTipoMedida().getId())
                {
                    //Labrado eje 1   
                    case 9004:
                        df.applyPattern("#0.00");
                        String LabraIzq1 = df.format(m.getValor());
                        parametros.put("LabraIzq1", ajustarValorMedida(LabraIzq1.trim()));
                        break;

                    case 9046:
                    case 9013:
                        df.applyPattern("#0.00");
                        String LabraDer1 = df.format(m.getValor());
                        parametros.put("LabraDer1", ajustarValorMedida(LabraDer1.trim()));
                        break;

                    //presion eje 1
                    case 9022:
                        df.applyPattern("#0.00");
                        String PresIzq1 = df.format(m.getValor());
                        parametros.put("PresIzq1", ajustarValorMedida(PresIzq1.trim()) + " psi ");
                        break;

                    case 9031:
                    case 9048:
                        df.applyPattern("#0.00");
                        String PresDer1 = df.format(m.getValor());
                        parametros.put("PresDer1", ajustarValorMedida(PresDer1.trim()) + " psi ");
                        break;

                    //labrado eje 2 
                    case 9005:
                        df.applyPattern("#0.00");
                        String LabraIzq1Eje2 = df.format(m.getValor());
                        parametros.put("LabraIzq1Eje2", ajustarValorMedida(LabraIzq1Eje2.trim()));
                        break;

                    case 9009:
                        df.applyPattern("#0.00");
                        String LabraIzq2Eje2 = df.format(m.getValor());
                        parametros.put("LabraIzq2Eje2", ajustarValorMedida(LabraIzq2Eje2.trim()));
                        break;

                    case 9014:
                    case 9047:
                        df.applyPattern("#0.00");
                        String LabrDer1Eje2 = df.format(m.getValor());
                        parametros.put("LabrDer1Eje2", ajustarValorMedida(LabrDer1Eje2.trim()));
                        break;
                    case 9018:
                        df.applyPattern("#0.00");
                        String LabrDer2Eje2 = df.format(m.getValor());
                        parametros.put("LabrDer2Eje2", ajustarValorMedida(LabrDer2Eje2.trim()));
                        break;

                    //Presion eje 2        
                    case 9023:
                        df.applyPattern("#0.00");
                        String PresIzq1Eje2 = df.format(m.getValor());
                        parametros.put("PresIzq1Eje2", ajustarValorMedida(PresIzq1Eje2.trim()) + " psi ");
                        break;

                    case 9027:
                        df.applyPattern("#0.00");
                        String PresIzq2Eje2 = df.format(m.getValor());
                        parametros.put("PresIzq2Eje2", ajustarValorMedida(PresIzq2Eje2.trim())+ " psi " );
                        break;
                    case 9032:
                    case 9049:
                        df.applyPattern("#0.00");
                        String PresDer1Eje2 = df.format(m.getValor());
                        parametros.put("PresDer1Eje2", ajustarValorMedida(PresDer1Eje2.trim())  + " psi ");
                        break;

                    case 9036:
                        df.applyPattern("#0.00");
                        String PresDer2Eje2 = df.format(m.getValor());
                        parametros.put("PresDer2Eje2", ajustarValorMedida(PresDer2Eje2.trim())  + " psi ");
                        break;
                    //Labrado eje 3
                    case 9006:
                        df.applyPattern("#0.00");
                        String LabraIzq1Eje3 = df.format(m.getValor());
                        parametros.put("LabraIzq1Eje3", ajustarValorMedida(LabraIzq1Eje3.trim()));
                        break;

                    case 9010:
                        df.applyPattern("#0.00");
                        String LabraIzq2Eje3 = df.format(m.getValor());
                        parametros.put("LabraIzq2Eje3", ajustarValorMedida(LabraIzq2Eje3.trim()));
                        break;

                    case 9015:
                        df.applyPattern("#0.00");
                        String LabrDer1Eje3 = df.format(m.getValor());
                        parametros.put("LabrDer1Eje3", ajustarValorMedida(LabrDer1Eje3.trim()));
                        break;
                    case 9019:
                        df.applyPattern("#0.00");
                        String LabrDer2Eje3 = df.format(m.getValor());
                        parametros.put("LabrDer2Eje3", ajustarValorMedida(LabrDer2Eje3.trim()));
                        break;

                    //Presion eje 3        
                    case 9024:
                        df.applyPattern("#0.00");
                        String PresIzq1Eje3 = df.format(m.getValor());
                        parametros.put("PresIzq1Eje3", ajustarValorMedida(PresIzq1Eje3.trim()) + " psi ");
                        break;

                    case 9028:
                        df.applyPattern("#0.00");
                        String PresIzq2Eje3 = df.format(m.getValor());
                        parametros.put("PresIzq2Eje3", ajustarValorMedida(PresIzq2Eje3.trim()) + " psi ");
                        break;

                    case 9033:
                        df.applyPattern("#0.00");
                        String PresDer1Eje3 = df.format(m.getValor());
                        parametros.put("PresDer1Eje3", ajustarValorMedida(PresDer1Eje3.trim()) + " psi ");
                        break;

                    case 9037:
                        df.applyPattern("#0.00");
                        String PresDer2Eje3 = df.format(m.getValor());
                        parametros.put("PresDer2Eje3", ajustarValorMedida(PresDer2Eje3.trim())  + " psi ");
                        break;

                    //Labrado eje 4        
                    case 9007:
                        df.applyPattern("#0.00");
                        String LabrIzq1Eje4 = df.format(m.getValor());
                        parametros.put("LabrIzq1Eje4", ajustarValorMedida(LabrIzq1Eje4.trim()));
                        break;

                    case 9011:
                        df.applyPattern("#0.00");
                        String LabrIzq2Eje4 = df.format(m.getValor());
                        parametros.put("LabrIzq2Eje4", ajustarValorMedida(LabrIzq2Eje4.trim()));
                        break;

                    case 9016:
                        df.applyPattern("#0.00");
                        String LabrDer1Eje4 = df.format(m.getValor());
                        parametros.put("LabrDer1Eje4", ajustarValorMedida(LabrDer1Eje4.trim()));
                        break;

                    case 9020:
                        df.applyPattern("#0.00");
                        String LabrDer2Eje4 = df.format(m.getValor());
                        parametros.put("LabrDer2Eje4", ajustarValorMedida(LabrDer2Eje4.trim()));
                        break;

                    //presion eje 4
                    case 9025:
                        df.applyPattern("#0.00");
                        String PresIzq1Eje4 = df.format(m.getValor());
                        parametros.put("PresIzq1Eje4", ajustarValorMedida(PresIzq1Eje4.trim())  + " psi ");
                        break;

                    case 9029:
                        df.applyPattern("#0.00");
                        String PresIzq2Eje4 = df.format(m.getValor());
                        parametros.put("PresIzq2Eje4", ajustarValorMedida(PresIzq2Eje4.trim()) + " psi ");
                        break;

                    case 9034:
                        df.applyPattern("#0.00");
                        String PresDer1Eje4 = df.format(m.getValor());
                        parametros.put("PresDer1Eje4", ajustarValorMedida(PresDer1Eje4.trim())+ " psi ");
                        break;

                    case 9038:
                        df.applyPattern("#0.00");
                        String PresDer2Eje4 = df.format(m.getValor());
                        parametros.put("PresDer2Eje4", ajustarValorMedida(PresDer2Eje4.trim())+ " psi ");
                        break;

                    //Labrado  eje 5         
                    case 9008:
                        df.applyPattern("#0.00");
                        String LabrIzq1Eje5 = df.format(m.getValor());
                        parametros.put("LabrIzq1Eje5", ajustarValorMedida(LabrIzq1Eje5.trim()));
                        break;

                    case 9012:
                        df.applyPattern("#0.00");
                        String LabrIzq2Eje5 = df.format(m.getValor());
                        parametros.put("LabrIzq2Eje5", ajustarValorMedida(LabrIzq2Eje5.trim()));
                        break;

                    case 9017:
                        df.applyPattern("#0.00");
                        String LabrDer1Eje5 = df.format(m.getValor());
                        parametros.put("LabrDer1Eje5", ajustarValorMedida(LabrDer1Eje5.trim()));
                        break;

                    case 9021:
                        df.applyPattern("#0.00");
                        String LabrDer2Eje5 = df.format(m.getValor());
                        parametros.put("LabrDer2Eje5", ajustarValorMedida(LabrDer2Eje5.trim()));
                        break;

                    //presion eje 5
                    case 9026:
                        df.applyPattern("#0.00");
                        String PresIzq1Eje5 = df.format(m.getValor());
                        parametros.put("PresIzq1Eje5", ajustarValorMedida(PresIzq1Eje5.trim())  + " psi ");
                        break;

                    case 9030:
                        df.applyPattern("#0.00");
                        String PresIzq2Eje5 = df.format(m.getValor());
                        parametros.put("PresIzq2Eje5", ajustarValorMedida(PresIzq2Eje5.trim())  + " psi ");
                        break;

                    case 9035:
                        df.applyPattern("#0.00");
                        String PresDer1Eje5 = df.format(m.getValor());
                        parametros.put("PresDer1Eje5", ajustarValorMedida(PresDer1Eje5.trim())  + " psi ");
                        break;

                    case 9039:
                        df.applyPattern("#0.00");
                        String PresDer2Eje5 = df.format(m.getValor());
                        parametros.put("PresDer2Eje5", ajustarValorMedida(PresDer2Eje5.trim())  + " psi ");
                        break;

                    // labrado repuesto
                    case 9040:
                        df.applyPattern("#0.00");
                        String LabrRepuesto1 = df.format(m.getValor());
                        parametros.put("LabrRepuesto1", ajustarValorMedida(LabrRepuesto1.trim()));
                        break;

                    case 9041:
                        df.applyPattern("#0.00");
                        String LabrRepuesto2 = df.format(m.getValor());
                        parametros.put("LabrRepuesto2", ajustarValorMedida(LabrRepuesto2.trim()));
                        break;

                    // Rrespuesto presion      
                    case 9043:
                        df.applyPattern("#0.00");
                        String PresRespuesto1 = df.format(m.getValor());
                        parametros.put("PresRespuesto1", ajustarValorMedida(PresRespuesto1.trim()) + " psi ");
                        break;

                    case 9044:
                        df.applyPattern("#0.00");
                        String PresRespuesto2 = df.format(m.getValor());
                        parametros.put("PresRespuesto2", ajustarValorMedida(PresRespuesto2.trim()) + " psi ");
                        break;
                      
                    case 1006:
                        parametros.put("kilometrajeMedida",cargarKilometraje(idPrueba));
                        break;
                    default://Error con el codigo de la medida pra gases
                        break;
                }
            }
        } catch (Exception e) 
        {
            System.out.println("Error en el metodo : cargarMedidaPruebaVisual()" +e.getMessage());
        }
    }
    
    /**
     * 
     * @param idprueba
     * @return 
     */
    public static String cargarKilometraje(int idprueba) 
    {
        String kilometraje = "-1";
        try {
            Connection cn = UtilConexion.obtenerConexion();
            kilometraje = HojaPruebasJpaController.consultarMedida(idprueba, cn);
            if (kilometraje.equalsIgnoreCase("0")) 
            {
                kilometraje="NO FUNCIONAL";
            }
            
        } catch (Exception e) {
            System.out.println("Error en el metod:cargarKilometraje()" + e);
        }
        return kilometraje;
    }
    
    /**
     * 
     * @param idPrueba
     * @param listaMedidas
     * @param tipoVehiculo 
     */
    private void cargarMedidaPruebaLucesMotos( List<Medida> listaMedidas)
    {
        System.out.println("----------------------------------------------------");
        System.out.println("-Cargar medidas para la prueba de Luces Motos-------");
        System.out.println("----------------------------------------------------");

        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);

        try 
        {
            for (Medida m : listaMedidas) 
            {
                switch (m.getTipoMedida().getId()) 
                {
                    
                    //----------------------------------------------------------
                    //---------------------BAJA MOTOS---------------------------
                    //----------------------------------------------------------   
                    case 2014://INTESIDAD BAJA  1
                        //df.applyPattern("#0.0");//ESTO DA PROBLEMAS AL AJUSTAR EL DATO, SE DEBE 
                        String IntBajaDerechaMotos = df.format(m.getValor());
                        parametros.put("IntBajaDerecha", ajustarValorMedida(IntBajaDerechaMotos) + m.getCondicion());
                        break;
                        
                    case 2013://INCLINACION BAJA  1
                        //df.applyPattern("#0.0");
                        ang = m.getValor();
                        String AngInclDerechaMoto = df.format(m.getValor());
                        parametros.put("AngInclDerecha", ajustarValorMedida(AngInclDerechaMoto) + m.getCondicion());
                        break;
                        
                    case 2015://INTESIDAD BAJA FAROLA 2
                        String IntBajaDerecha2 = df.format(m.getValor());
                        parametros.put("IntencidadBajaIzquierdaF1", ajustarValorMedida(IntBajaDerecha2) + m.getCondicion());
                        break; 
                      

                    case 2000://INTESIDAD BAJA PARA MOTO
                        parametros.put("IntBajaIzq3", ajustarValorMedida(String.valueOf(m.getValor())));
                        break;

                    case 2001://INTESIDADA BAJA PARA MOTO FAROLA 4
                        //parametros.put("SumaLuces",String.valueOf(m.getValor()));
                        break;

                    case 2002://INCLINACION BAJA PARA MOTO FAROLA 2
                        //df.applyPattern("#0.0");
                        String InclinacionInquierdaF1 = df.format(m.getValor());
//                      parametros.put("AngInclIzq", ajustarValorMedida(AngInclIzq) + m.getCondicion());
                        parametros.put("InclinacionInquierdaF1", ajustarValorMedida(InclinacionInquierdaF1) + m.getCondicion());
                        break;

                    case 2022://INCLINACION BAJA FAROLA 3
                        //df.applyPattern("#0.0");
                        String IntBajaIzq3 = df.format(m.getValor());
                        parametros.put("IntBajaIzq3", ajustarValorMedida(IntBajaIzq3.trim()) + m.getCondicion());
                        break;
                        
                    //----------------------------------------------------------
                    //---------------------ALTAS MOTOS---------------------------
                    //----------------------------------------------------------
                        
                    case 2056://INTESIDAD ALTA DERECHA FOROLA 1
                        //df.applyPattern("#0.0");
                        String IntesidadAltaIzqF1 = df.format(m.getValor());
                        parametros.put("IntesidadAltaIzqF1", ajustarValorMedida(IntesidadAltaIzqF1.trim()) + m.getCondicion());
                        break;
                    case 2057://INTESIDAD ALTA DERECHA FOROLA 1
                        //df.applyPattern("#0.0");
                        String IntAltaDer = df.format(m.getValor());
                        parametros.put("IntAltaDer", ajustarValorMedida(IntAltaDer.trim()) + m.getCondicion());
                        break;
                    case 2061://INTESIDAD ALTA DERECHA FOROLA 1
                        //df.applyPattern("#0.0");
                        String IntendidaAltDerechaF2 = df.format(m.getValor());
                        parametros.put("IntendidaAltDerechaF2", ajustarValorMedida(IntendidaAltDerechaF2.trim()) + m.getCondicion());
                        break;
                    case 2062://INTESIDAD ALTA DERECHA FOROLA 1
                        //df.applyPattern("#0.0");
                        String IntensidadAltaIzqF2 = df.format(m.getValor());
                        parametros.put("IntensidadAltaIzqF2", ajustarValorMedida(IntensidadAltaIzqF2.trim()) + m.getCondicion());
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error en el metodo : cargarMedidaPruebaLucesMotos()" + e.getMessage());
        }
    }
    
    
    
    /**
     * @autor ELKIN B
     * 
     * @param idPrueba
     * @param listaMedidas 
     */
    private void cargarMedidaPruebaLucesVehiculos(int idPrueba,List<Medida> listaMedidas, int tipoVehiculo)
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar medidas para la prueba de Luces -------");
        System.out.println("----------------------------------------------------");
        
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        //df.applyPattern("#0.00");
        try 
        {
            String sql = "SELECT m.MEASURETYPE,m.Simult FROM medidas AS m WHERE m.TEST=?";
            ResultSet rs = conexionDBcargarMedida(sql, idPrueba);
            cargandoDatosFurLuces(rs);
            for (Medida m : listaMedidas)
            {
                switch (m.getTipoMedida().getId()) 
                {

                    //----------------------------------------------------------VALOR 1 
                    
                    case 2024: //INTENSIDAD BAJA DERECHA FAROLA 1
                        //df.applyPattern("#0.0");
                        String IntBajaDerechaVehiculo = df.format(m.getValor());
                        parametros.put("IntBajaDerecha", ajustarValorMedida(IntBajaDerechaVehiculo) + m.getCondicion());
                        break;

                    case 2040://INCLINACION DERECHA FAROLA 1
                        //df.applyPattern("#0.0");
                        String AngInclDerecha = df.format(m.getValor());
                        parametros.put("AngInclDerecha", ajustarValorMedida(AngInclDerecha.trim()) + m.getCondicion());
                        break;  

                    case 2031://INTENSIDAD BAJA IZQUIERDA FAROLA 1
                        //df.applyPattern("#0.0");
                        String IntencidadBajaIzquierdaF1 = df.format(m.getValor());
                        parametros.put("IntencidadBajaIzquierdaF1", ajustarValorMedida(IntencidadBajaIzquierdaF1.trim()) + m.getCondicion());
                        break; // nueva     
                        
                    case 2044://INCLINACION IZQUIERDA FAROLA 1
                        //df.applyPattern("#0.0");
                        String InclinacionInquierdaF1 = df.format(m.getValor());
                        parametros.put("InclinacionInquierdaF1", ajustarValorMedida(InclinacionInquierdaF1.trim()) + m.getCondicion());
                        break;
                        
                    case 2032://INTENSIDAD ALTA IZQUIERDA FAROLA 1
                        //df.applyPattern("#0.0");
                        String IntesidadAltaIzqF1 = df.format(m.getValor());
                        parametros.put("IntesidadAltaIzqF1", ajustarValorMedida(IntesidadAltaIzqF1.trim()) + m.getCondicion());
                        break;

                    case 2036://INTENSIDAD ALTA DERECHA FAROLA 1
                        //df.applyPattern("#0.0");
                        String IntAltaIzq_1 = df.format(m.getValor());
                        parametros.put("IntAltaDer", ajustarValorMedida(IntAltaIzq_1.trim()) + m.getCondicion());
                        break;
                        
                    case 2050://INTESIDAD EXPORADORA DERECHA 1
                        //df.applyPattern("#0.0");
                        String IntExpDer_1 = df.format(m.getValor());
                        parametros.put("IntExpDer", ajustarValorMedida(IntExpDer_1.trim()) + m.getCondicion());
                        break;

                    case 2053://INTESIDAD EXPLORADORA IZQUIERDA 1
                        //df.applyPattern("#0.0");
                        String IntExpIzq_1 = df.format(m.getValor());
                        parametros.put("IntExpIzq", ajustarValorMedida(IntExpIzq_1.trim()) + m.getCondicion());
                        break;

                    //----------------------------------------------------------  VALOR 2 

                    case 2025://INTESIDAD BAJA DERECHA FAROLA 2
                        //df.applyPattern("#0.0");
                        String IntBajaDerecha = df.format(m.getValor());
                        parametros.put("IntBajaDerecha2", ajustarValorMedida(IntBajaDerecha.trim()) + m.getCondicion());
                        break;

                    case 2041://INCLINACION DERECHA FAROLA 2
                        //df.applyPattern("#0.0");
                        String InclinacionDereF2 = df.format(m.getValor());
                        parametros.put("InclinacionDereF2", ajustarValorMedida(InclinacionDereF2.trim()) + m.getCondicion());
                        break;
                        
                    case 2030://INTENSIDAD BAJA IZQUIERDA FAROLA 2
                        //df.applyPattern("#0.0");
                        String IntencidadBajaIzquierdaF2 = df.format(m.getValor());
                        parametros.put("IntencidadBajaIzquierdaF2", ajustarValorMedida(IntencidadBajaIzquierdaF2.trim()) + m.getCondicion());
                        break; // nueva 

                    case 2045://INCLINACION IZQUIERDA FAROLA 2
                        //df.applyPattern("#0.0");
                        String InclinacionInquierdaF2 = df.format(m.getValor());
                        parametros.put("InclinacionInquierdaF2", ajustarValorMedida(InclinacionInquierdaF2.trim()) + m.getCondicion());
                        break;
                        
                    case 2037://INTENSIDAD ALTA DERECHA FAROLA 2
                        //df.applyPattern("#0.0");
                        String IntendidaAltDerechaF2 = df.format(m.getValor());
                        parametros.put("IntendidaAltDerechaF2", ajustarValorMedida(IntendidaAltDerechaF2.trim()) + m.getCondicion());
                        break;

                    case 2033://INTENSIDAD ALTA IZQUIERDA FAROLA 2
                        //df.applyPattern("#0.0");
                        String IntensidadAltaIzqF2 = df.format(m.getValor());
                        parametros.put("IntensidadAltaIzqF2", ajustarValorMedida(IntensidadAltaIzqF2.trim()) + m.getCondicion());
                        break;

                    case 2051://INTESIDAD EXPORLADORA DERCHA 2
                        //df.applyPattern("#0.0");
                        String IntExpDer2=df.format(m.getValor());
                        parametros.put("IntExpDer2", ajustarValorMedida(IntExpDer2.trim()) + m.getCondicion());
                        break;

                    case 2054://INTESIDAD EXPLORADORA DERECHA 2
                        //df.applyPattern("#0.0");
                        String IntExpIzq2 = df.format(m.getValor());
                        parametros.put("IntExpIzq2", ajustarValorMedida(IntExpIzq2.trim()) + m.getCondicion());
                        break;
 
                    //----------------------------------------------------------VALOR 3 
                        
                        
                    case 2026://INTENSIDAD BAJA DERECHA FAROLA 3
                        //df.applyPattern("#0.0");
                        String IntBajaDerecha3 = df.format(m.getValor());
                        parametros.put("IntBajaDerecha3", ajustarValorMedida(IntBajaDerecha3.trim()) + m.getCondicion());
                        break;

                    case 2042://INCLINACION DERECHA FAROLA 3
                        //df.applyPattern("#0.0");
                        String InclinacionDereF3 = df.format(m.getValor());
                        parametros.put("InclinacionDereF3", ajustarValorMedida(InclinacionDereF3.trim()) + m.getCondicion());
                        break;

                    case 2029://INTENSIDAD BAJA IZQUIERDA FAROLA 2
                        //df.applyPattern("#0.0");
                        String IntencidadBajaIzquierdaF3 = df.format(m.getValor());
                        parametros.put("IntencidadBajaIzquierdaF3", ajustarValorMedida(IntencidadBajaIzquierdaF3.trim()) + m.getCondicion());
                        break; // nueva 

                    case 2046://INCLINACION IZQUIERDA FAROLA 3
                        //df.applyPattern("#0.0");
                        String InclinacionInquierdaF3 = df.format(m.getValor());
                        parametros.put("InclinacionInquierdaF3", ajustarValorMedida(InclinacionInquierdaF3.trim()) + m.getCondicion());
                        break;

                    case 2038://INTENSIDAD ALTA DERECHA FAROLA 2
                        //df.applyPattern("#0.0");
                        String IntendidaAltDerechaF3 = df.format(m.getValor());
                        parametros.put("IntendidaAltDerechaF3", ajustarValorMedida(IntendidaAltDerechaF3.trim()) + m.getCondicion());
                        break;
                        
                    case 2034://INTENSIDAD ALTA IZQUIERDA FAROLA 3
                        //df.applyPattern("#0.0");
                        String IntensidadAltaIzqF3 = df.format(m.getValor());
                        parametros.put("IntensidadAltaIzqF3", ajustarValorMedida(IntensidadAltaIzqF3.trim()) + m.getCondicion());
                        break;
                  
                    case 2052://INTEDAD EXPLORADORA DERECHA 3
                        //df.applyPattern("#0.0");
                        String IntExpDer3= df.format(m.getValor());
                        parametros.put("IntExpDer3", ajustarValorMedida(IntExpDer3.trim()) + m.getCondicion());
                        //Esta medida no sale en los reportes
                        break; 
                        
                    case 2055://INTESIDAD EXPLORADORA INZQUIERDA 3
                        //df.applyPattern("#0.0");
                        parametros.put("IntExpIzq3", df.format(m.getValor()) + m.getCondicion());
                        break;

                        
                        
                    //----------------------------------------------------------
                    //---------------------SUMATORIA VEHICULOS------------------
                    //----------------------------------------------------------      
                    case 2011: //SUMATORIA DE LAS INTESIDADES 
                        //df.applyPattern("#0.0");
                        String SumaLuces = df.format(m.getValor());
                        parametros.put("SumaLuces", ajustarValorMedida(SumaLuces) + m.getCondicion());
                        break;
                            
                    //----------------------------------------------------------
                    //---------------------ALTA MOTOCARRO----------------------
                    //----------------------------------------------------------          
                    case 2017://INTENSIDAD ALTA DERECHA MOTOCARRO
                        //df.applyPattern("#0.0");
                        String IntAltaIzq_2 = df.format(m.getValor());
                        parametros.put("IntAltaIzq", ajustarValorMedida(IntAltaIzq_2.trim()) + m.getCondicion());
                        break;
                        
                    //----------------------------------------------------------
                    //---------------------BAJAS MOTOCARRO----------------------
                    //----------------------------------------------------------       
                    case 2018://intensidad baja derecha motocarro
                        //df.applyPattern("#0.0");
                        String IntBajaDerechaMotocarro = df.format(m.getValor());
                        parametros.put("IntBajaDerecha", ajustarValorMedida(IntBajaDerechaMotocarro.trim()) + m.getCondicion());
                        break;

                    case 2019://intensidad baja izquierda en motocarro
                        String IntBajaIzqMotocarro = df.format(m.getValor());
                        parametros.put("IntBajaIzq", ajustarValorMedida(IntBajaIzqMotocarro.trim()) + m.getCondicion());
                        break;

                    case 2020://INCLINACION BAJA INQUIERDA MOTOCARRO
                        //df.applyPattern("#0.0");
                        String AngInclIzq_1 = df.format(m.getValor());
                        parametros.put("AngInclIzq", ajustarValorMedida(AngInclIzq_1.trim()) + m.getCondicion());
                        break;

                    case 2021://Inclinacion baja derecha moto carro
                        //df.applyPattern("#0.0");
                        String AngInclDerechaMC = df.format(m.getValor());
                        parametros.put("AngInclDerecha", ajustarValorMedida(AngInclDerechaMC.trim()) + m.getCondicion());
                        break;
                           
                    default:
                        break;
                }//end of switch
            }//end of for ista de medidas

        } catch (Exception e) 
        {
             System.out.println("Error en el metodo : cargarMedidaPruebaLuces()" +e.getMessage());
        }
    }
    
    /**
     * @autor ELKIN B
     * 
     * @param rs 
     */
    private void cargandoDatosFurLuces(ResultSet rs) 
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar cargandoDatosFurLuces---------- -------");
        System.out.println("----------------------------------------------------");
        
        try 
        {
            while (rs.next()) 
            {
                String tipoMedida= rs.getString("MEASURETYPE");

                if (tipoMedida.equals("2050") || tipoMedida.equals("2051") || tipoMedida.equals("2052"))
                {
                    if (rs.getString("Simult").equalsIgnoreCase("Y")) 
                    {
                        parametros.put("SimExpDer", "SI");
                    } else {
                        parametros.put("SimExpDer", "NO");
                    }
                }

                if (tipoMedida.equals("2053") || tipoMedida.equals("2054") || tipoMedida.equals("2055"))
                {
                    if (rs.getString("Simult").equalsIgnoreCase("Y")) {
                        parametros.put("SimExpIzq", "SI");
                    } else {
                        parametros.put("SimExpIzq", "NO");
                    }
                }

                if (tipoMedida.equals("2024") || tipoMedida.equals("2025") || tipoMedida.equals("2026") || tipoMedida.equals("2027")) 
                {

                    if (rs.getString("Simult").equals("Y")) 
                    {
                        parametros.put("SimBajasDer", "SI");
                    } else {
                        parametros.put("SimBajasDer", "NO");
                    }
                }
                if (tipoMedida.equals("2028") || tipoMedida.equals("2029") || tipoMedida.equals("2030") || tipoMedida.equals("2031")) 
                {
                    if (rs.getString("Simult").equals("Y")) 
                    {
                        parametros.put("SimBajasIzq", "SI");
                    } else {
                        parametros.put("SimBajasIzq", "NO");
                    }
                }
                if (tipoMedida.equals("2036") || tipoMedida.equals("2037") || tipoMedida.equals("2038") || tipoMedida.equals("2039"))
                {
                    if (rs.getString("Simult").equals("Y")) 
                    {
                        parametros.put("SimAltasDer", "SI");
                    } else {
                        parametros.put("SimAltasDer", "NO");
                    }
                }
                if (tipoMedida.equals("2032") || tipoMedida.equals("2033") || tipoMedida.equals("2034") || tipoMedida.equals("2035")) 
                {
                    if (rs.getString("Simult").equals("Y")) 
                    {
                        parametros.put("SimAltasIzq", "SI");
                    } else {
                        parametros.put("SimAltasIzq", "NO");
                    }
                }
            }

        } catch (Exception e)
        {
            System.out.println("Error en el metodo : cargandoDatosFurLuces()" +e.getMessage() + e.getLocalizedMessage());
        }

    }
    
    /**
     * @autor ELKIN B
     * 
     * @param listaMedidas
     * @param idTipoGasolina 
     */
    private void cargarMedidaPruebaGases(List<Medida> listaMedidas,int idTipoGasolina,char FormaMedTemperatura, String disenoVehiculo,int tipoVehiculo)
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar medidas para la prueba de Gases -------");
        System.out.println("----------------------------------------------------");
        
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        
        try 
        {
            for (Medida m : listaMedidas) 
            {
                switch (m.getTipoMedida().getId()) 
                {
                    //----------------------------------------------------------
                    //---------------VEHICULOS CICLO OTTO 4T -------------------
                    //----------------------------------------------------------
                    
                    case 8001://medida de HC Ralenti Cuatro Tiempos
                        String HCRalenti=df.format(m.getValor());
                        parametros.put("HCRalenti",ajustarValorMedida(HCRalenti) + m.getCondicion());//Esta no se da nunca con decimales garantizado desde prueba
                        break;
                            
                    case 8002://medida de CO Ralenti Cuatro tiempos
                        String CORalenti = df.format(m.getValor());
                        parametros.put("CORalenti", ajustarValorMedida(CORalenti.trim()) + m.getCondicion());
                        break;
                    case 8003:
                        if (idTipoGasolina == 1) 
                        {
                            String CO2Ralenti = df.format(m.getValor());
                            parametros.put("CO2Ralenti", ajustarValorMedida(CO2Ralenti.trim()) + m.getCondicion());
                        } else {
                            String CO2Ralenti = df.format(m.getValor());
                            parametros.put("CO2Ralenti", ajustarValorMedida(CO2Ralenti.trim()) + m.getCondicion());
                        }
                        break;                  
                    case 8004://medida de O2 Ralenti Cuatro Tiempos
                            if (idTipoGasolina == 1) 
                            {
                                String O2Ralenti = df.format(m.getValor());
                                parametros.put("O2Ralenti", ajustarValorMedida(O2Ralenti.trim()));
                            } else {

                                String O2Ralenti = df.format(m.getValor());
                                parametros.put("O2Ralenti", ajustarValorMedida(O2Ralenti.trim()));
                            }
                            break;
                            
                    case 8005:
                        String RevGasoRal = df.format(m.getValor());
                        parametros.put("RevGasoRal",ajustarValorMedida(RevGasoRal));
                        break; //Temperatura de Aceite 

                            
                            
                    case 8007://medida de HC Crucero Cuatro tiempos
                        String HCCrucero = df.format(m.getValor());
                        parametros.put("HCCrucero", ajustarValorMedida(HCCrucero) + m.getCondicion());//No decimales
                        break;
                        
                    case 8008: // medida de CO Crucero Cuatro tiempos
                        String COCrucero = df.format(m.getValor());
                        parametros.put("COCrucero", ajustarValorMedida(COCrucero.trim()) + m.getCondicion());
                    break;
                    
                    case 8009://medida de CO2  Crucero cuatro tiempos
                        if (idTipoGasolina == 1) 
                        {
                            String CO2Crucero = df.format(m.getValor());
                            parametros.put("CO2Crucero", ajustarValorMedida(CO2Crucero.trim()) + m.getCondicion());
                        } else {
                            parametros.put("CO2Crucero", ajustarValorMedida(df.format(m.getValor())));
                        }
                        break;
                            
                    case 8010://medida de O2 Crucero cuatro tiempos                               
                        if (idTipoGasolina == 1)
                        {
                            String O2Crucero = df.format(m.getValor());
                            parametros.put("O2Crucero", ajustarValorMedida(O2Crucero.trim()) + m.getCondicion());
                        } else {
                            String O2Crucero = df.format(m.getValor());
                            parametros.put("O2Crucero", ajustarValorMedida(O2Crucero.trim()) + m.getCondicion());
                        }
                        break;
  
                    case 8011://Revoluciones
                        String RevGasoCruc = df.format(m.getValor());
                        parametros.put("RevGasoCruc", ajustarValorMedida(RevGasoCruc));
                        break;

                    //----------------------------------------------------------
                    //---------------VEHICULOS CICLO OTTO 2T -------------------
                    //----------------------------------------------------------
                   
                    case 8019:// medida de CO Ralenti dos tiempos
                        if (idTipoGasolina == 1) 
                        {
                            String CO2Ralenti = df.format(m.getValor()) + m.getCondicion();
                            parametros.put("CO2Ralenti", ajustarValorMedida(CO2Ralenti.trim()));
                        } else {
                            String CO2Ralenti = df.format(m.getValor()) + m.getCondicion();
                            parametros.put("CO2Ralenti", ajustarValorMedida(CO2Ralenti.trim()));
                        }
                        break;
                    case 8020://medida de CO2 Ralenti dos tiempos
                        String CORalenti2T = df.format(m.getValor());
                        parametros.put("CORalenti", ajustarValorMedida(CORalenti2T.trim()) + m.getCondicion());
                        break;

                    case 8028://medida de O2 Ralenti dos tiempos
                        String RevGasoRal2T = df.format(m.getValor());
                        parametros.put("RevGasoRal", ajustarValorMedida(RevGasoRal2T) + m.getCondicion());
                        break;

                    case 8021://medida de O2 Ralenti dos tiempos
                        if (idTipoGasolina == 1) 
                        {
                            String O2Ralenti2T = df.format(m.getValor()) + m.getCondicion();
                            parametros.put("O2Ralenti", ajustarValorMedida(O2Ralenti2T.trim()));
                        } else {
                            String O2Ralenti2T = df.format(m.getValor()) + m.getCondicion();
                            parametros.put("O2Ralenti", ajustarValorMedida(O2Ralenti2T.trim()));
                        }
                        break;

                    case 8018://medida de HC Ralenti Dos tiempos
                        String HCRalenti2T= df.format(m.getValor());
                        parametros.put("HCRalenti",ajustarValorMedida(HCRalenti2T) + m.getCondicion());
                        break;

                    //----------------------------------------------------------
                    //---------------VEHICULOS CICLO OTTO(CATALIZADOR) ---------
                    //----------------------------------------------------------    
                     
                    case 8006://Revoluciones
                        char temp = FormaMedTemperatura;
                        if (temp == 'C') {
                            parametros.put("TempGasoRal", "");
                            parametros.put("Catalizador", "S");
                        } else {
                            if (temp == 'B') 
                            {
                                String TempGasoRal=df.format(m.getValor());
                                parametros.put("TempGasoRal",ajustarValorMedida(TempGasoRal));
                                parametros.put("Catalizador", "N");
                            } else
                            {
                                String TempGasoRal=df.format(m.getValor());
                                
                                if (disenoVehiculo.equalsIgnoreCase("Scooter")) 
                                {
                                    if (tipoVehiculo == 4) {
                                        parametros.put("TempGasoRal", "\"0\"");
                                    } else {
                                        parametros.put("TempGasoRal", TempGasoRal);
                                    }
                                }else{
                                    parametros.put("TempGasoRal",ajustarValorMedida(TempGasoRal));
                                }
                                                                 
                                
                                parametros.put("Catalizador", "NA");

                            }
                        }
                        break;

                    //----------------------------------------------------------
                    //---------------VEHICULOS CICLO OTTO(TEMPERATURA) ---------
                    //----------------------------------------------------------     
                     
                    case 8022://medida de O2 Ralenti dos tiempos
                        String TempGasoRal=df.format(m.getValor()).equals("0") ? "" : df.format(m.getValor());
                        parametros.put("TempGasoRal",ajustarValorMedida(TempGasoRal));

                        
                    //----------------------------------------------------------
                    //---------VEHICULOS CICLO OTTO(TEMPERATURA AMBIENTE) ------
                    //----------------------------------------------------------      
                    case 8031:
                        if (idTipoGasolina != 3) {
                            String TempAmbGas = df.format(m.getValor());
                            parametros.put("TempAmbGas", ajustarValorMedida(TempAmbGas.trim()));

                        } else {
                            String TempAmbDis = df.format(m.getValor());
                            parametros.put("TempAmbDis", ajustarValorMedida(TempAmbDis.trim()));
                        }
                        break;
                    
                    //----------------------------------------------------------
                    //---------VEHICULOS CICLO OTTO(HUMEDAD RELATIVA) ----------
                    //---------------------------------------------------------- 
                    case 8032:
                        if (idTipoGasolina != 3) 
                        {
                            String HRGas = df.format(m.getValor());
                            parametros.put("HRGas", ajustarValorMedida(HRGas.trim()));
                        } else {
                            String HRDis = df.format(m.getValor());
                            parametros.put("HRDis", ajustarValorMedida(HRDis.trim()));
                        }
                        break;  
                       
                        
                    //----------------------------------------------------------
                    //--VEHICULOS CICLO DIESEL (MEDIDAS DE OPACIMETRO) ---------
                    //----------------------------------------------------------    
                    case 8033://OPACIDAD CICLO1 
                        String OpCiclo1 = df.format(m.getValor());
                        parametros.put("OpCiclo1", ajustarValorMedida(OpCiclo1.trim()) + m.getCondicion());
                        parametros.put("unidadCiclo1", m.getTipoMedida().getUnidad());
                        break;

                    case 8013://OPACIDAD CICLO2  
                        String OpCiclo2 = df.format(m.getValor());
                        parametros.put("OpCiclo2", ajustarValorMedida(OpCiclo2.trim()) + m.getCondicion());
                        parametros.put("unidadCiclo2", m.getTipoMedida().getUnidad());
                        break;
                        
                    case 8014://OPACIDAD CICLO3 
                        String OpCiclo3 = df.format(m.getValor());
                        parametros.put("OpCiclo3", ajustarValorMedida(OpCiclo3.trim()) + m.getCondicion());
                        parametros.put("unidadCiclo3", m.getTipoMedida().getUnidad());
                        break;       

                    case 8015://OPACIDAD CICLO4 
                        String OpCiclo4 = df.format(m.getValor());
                        parametros.put("OpCiclo4", ajustarValorMedida(OpCiclo4.trim()) + m.getCondicion());
                        parametros.put("unidadCiclo4", m.getTipoMedida().getUnidad());
                        break;  

                    //----------------------------------------------------------
                    //--VEHICULOS CICLO DIESEL (MEDIDAS DE GOBERNADA) ----------
                    //----------------------------------------------------------      
                     case 8038: //GOBERNADA CICLO1
                        String GobCic1= df.format(m.getValor());
                        parametros.put("GobCic1",ajustarValorMedida(GobCic1));
                        break;

                    case 8039://GOBERNADA CICLO2
                        String GobCic2=df.format(m.getValor());
                        parametros.put("GobCic2", ajustarValorMedida(GobCic2));
                        break;

                    case 8040: //GOBERNADA CICLO3
                        String GobCic3=df.format(m.getValor());
                        parametros.put("GobCic3",ajustarValorMedida(GobCic3));
                        break;
                        
                    case 8041: //GOBERNADA CICLO4
                        String GobCic4=df.format(m.getValor());
                        parametros.put("GobCic4",ajustarValorMedida(GobCic4));
                        break;

                        
                    //----------------------------------------------------------
                    //------VEHICULOS CICLO DIESEL (RESULTADO) -----------------
                    //----------------------------------------------------------   
                    case 8017:
                        String ResultadoOp = df.format(m.getValor());
                        parametros.put("ResultadoOp", ajustarValorMedida(ResultadoOp.trim()) + m.getCondicion());
                        break;

                    //----------------------------------------------------------
                    //------VEHICULOS CICLO DIESEL (RPM RALENTI) ---------------
                    //----------------------------------------------------------      
                    case 8035: //RPM RALENTI
                        parametros.put("RevDiesel", Validaciones.redondear(Double.parseDouble(df.format(m.getValor()))) + m.getCondicion());
                        break;

                    //----------------------------------------------------------
                    //------VEHICULOS CICLO DIESEL (TERPERATURA INICIAL) -------
                    //----------------------------------------------------------       
                    case 8034: //Temperatura Inicial Diesel
                        String RevDiesel= df.format(m.getValor()).equals("0") ? "" : df.format(m.getValor()) + m.getCondicion();
                        parametros.put("TempIniD",ajustarValorMedida(RevDiesel));
                        break;

                    //----------------------------------------------------------
                    //------VEHICULOS CICLO DIESEL (TERPERATURA FINAL) -------
                    //----------------------------------------------------------     
                    case 8037:
                        String TempFinD= df.format(m.getValor()).equals("0") ? "" : df.format(m.getValor()) + m.getCondicion();
                        parametros.put("TempFinD",ajustarValorMedida(TempFinD));
                        break;
                        


                    case 8012://Revoluciones
                        String valor = df.format(m.getValor());
                        char tempC = FormaMedTemperatura;
                        if (tempC == 'C') {
                            parametros.put("TempGasoCruc", "");
                        } else {
                            parametros.put("TempGasoCruc",ajustarValorMedida(valor));
                        }
                        break;

                    default://Error con el codigo de la medida pra gases
                        break;
                }//end of switch
            }//end of for

        } catch (Exception e) 
        {
            System.out.println("Error en el metodo :cargarMedidaPruebaGases() " +e.getMessage());
        }
    }
    
    
    /**
     * @autor ELKIN B
     * 
     * @param idPrueba
     * @param listaMedidas
     */
    private void cargaMedidaPpruebaFrenos(int idPrueba,List<Medida> listaMedidas)
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar medidas para la prueba de Frenos -------");
        System.out.println("----------------------------------------------------");
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        DecimalFormat dfEntero = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        dfEntero.applyPattern("#");
        
        try 
        {
            //pruebas de frenos sin decimales
//            df.applyPattern("#");
            Float PsEje1Der = 0.0F;
            Float PsEje2Der = 0.0F;
            Float PsEje3Der = 0.0F;
            Float PsEje4Der = 0.0F;
            Float PsEje5Der = 0.0F;
            Float PsEje1Izq = 0.0F;
            Float PsEje2Izq = 0.0F;
            Float PsEje3Izq = 0.0F;
            Float PsEje4Izq = 0.0F;
            Float PsEje5Izq = 0.0F;

            Float FrzEje1Der = 0.0F;
            Float FrzEje2Der = 0.0F;
            Float FrzEje3Der = 0.0F;
            Float FrzEje4Der = 0.0F;
            Float FrzEje5Der = 0.0F;
            Float FrzEje1Izq = 0.0F;
            Float FrzEje2Izq = 0.0F;
            Float FrzEje3Izq = 0.0F;
            Float FrzEje4Izq = 0.0F;
            Float FrzEje5Izq = 0.0F;
            int sumpesos = 0, sumfuerzas = 0;
            int sumapd = 0, sumapi = 0, sumafd = 0, sumafi = 0;
            String efi = "";
            
            int defecto_profundidad=validarDefectosPorfundidaLabrado();
            
            for (Medida m : listaMedidas)
            {
                if (defecto_profundidad <= 0)
                {
                    switch (m.getTipoMedida().getId())
                    {
                        case 5000://peso derecho eje 1
                            parametros.put("PsEje1Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            sumapd += m.getValor();
                            PsEje1Der = m.getValor();
                            break;
                        case 5001://peso derecho eje 2
                            parametros.put("PsEje2Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje2Der = m.getValor();
                            sumapd += m.getValor();
                            break;
                        case 5002:
                            parametros.put("PsEje3Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje3Der = m.getValor();
                            sumapd += m.getValor();
                            break;
                        case 5003:
                            parametros.put("PsEje4Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje4Der = m.getValor();
                            sumapd += m.getValor();
                            break;
                        case 5025:
                            parametros.put("PsEje5Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje5Der = m.getValor();
                            sumapd += m.getValor();
                            break;
                        case 5004:
                            parametros.put("PsEje1Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje1Izq = m.getValor();
                            sumapi += m.getValor();
                            break;
                        case 5005:
                            parametros.put("PsEje2Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje2Izq = m.getValor();
                            sumapi += m.getValor();
                            break;
                        case 5006:
                            parametros.put("PsEje3Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje3Izq = m.getValor();
                            sumapi += m.getValor();
                            break;
                        case 5007:
                            parametros.put("PsEje4Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje4Izq = m.getValor();
                            sumapi += m.getValor();
                            break;
                        case 5026:
                            parametros.put("PsEje5Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            PsEje5Izq = m.getValor();
                            sumapi += m.getValor();
                            break;
                        case 5008://fuerza de frenado eje 1
                            parametros.put("FrzEje1Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje1Der = m.getValor();

                            break;
                        case 5009://fuerza de frenado eje2
                            parametros.put("FrzEje2Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje2Der = m.getValor();
                            break;
                        case 5010://fuerza de frenado eje2
                            parametros.put("FrzEje3Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje3Der = m.getValor();
                            break;
                        case 5011://fuerza de frenado eje2
                            parametros.put("FrzEje4Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje4Der = m.getValor();
                            break;
                        case 5027:
                            parametros.put("FrzEje5Der", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje5Der = m.getValor();
                            break;
                        case 5012://fuerza de frenado eje2
                            parametros.put("FrzEje1Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje1Izq = m.getValor();
                            break;
                        case 5013://fuerza de frenado eje2
                            parametros.put("FrzEje2Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje2Izq = m.getValor();
                            break;

                        case 5014://fuerza de frenado eje2
                            parametros.put("FrzEje3Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje3Izq = m.getValor();
                            break;
                        case 5015://fuerza de frenado eje2
                            parametros.put("FrzEje4Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje4Izq = m.getValor();
                            break;
                        case 5016: //frenado emergencia eje 1 derecho
                            sumafd += m.getValor();
                            break;
                        case 5017: //frenado emergencia eje 2 derecho
                            sumafd += m.getValor();
                            break;
                        case 5018: //frenado emergencia eje 3 derecho
                            sumafd += m.getValor();
                            break;
                        case 5019: //frenado emergencia eje 4 derecho
                            sumafd += m.getValor();
                            break;

                        case 5020: //frenado emergencia eje 1 izquierda
                            sumafi += m.getValor();
                            break;
                        case 5021: //frenado emergencia eje 2 izquierda
                            sumafi += m.getValor();
                            break;
                        case 5022: //frenado emergencia eje 3 izquierda
                            sumafi += m.getValor();
                            break;
                        case 5023: //frenado emergencia eje 4 izquierda
                            sumafi += m.getValor();
                            break;

                        case 5028://fuerza de frenado eje2
                            parametros.put("FrzEje5Izq", dfEntero.format(m.getValor()) + m.getCondicion());
                            FrzEje5Izq = m.getValor();
                            break;
                        case 5024://Eficacia de frenado
                            String EficTotal=String.valueOf(m.getValor());
                            parametros.put("EficTotal",ajustarValorMedida(EficTotal) + m.getCondicion());
                            break;
                        case 5032:
                            String DesEje1=df.format(m.getValor());
                            parametros.put("DesEje1", ajustarValorMedida(DesEje1)+ m.getCondicion());
                            break;
                        case 5033:
                            String DesEje2=df.format(m.getValor());
                            parametros.put("DesEje2",ajustarValorMedida(DesEje2) + m.getCondicion());
                            break;
                        case 5034:
                            String DesEje3=df.format(m.getValor());
                            parametros.put("DesEje3", ajustarValorMedida(DesEje3) + m.getCondicion());
                            break;
                        case 5035:
                            String DesEje4=df.format(m.getValor());
                            parametros.put("DesEje4",ajustarValorMedida(DesEje4) + m.getCondicion());
                            break;
                        case 5031:
                            String DesEje5=df.format(m.getValor());
                            parametros.put("DesEje5",ajustarValorMedida(DesEje5) + m.getCondicion());
                            break;
                        case 5036:
//                            df.applyPattern("#0.00");
                            df.setMaximumFractionDigits(2);
                            String EficAux=df.format(m.getValor());
                            parametros.put("EficAux", ajustarValorMedida(EficAux) + m.getCondicion());//un decimal
                        default:
                            break;

                    }//end of switch
                    if (sumafi > 0 && sumafd > 0) 
                    {
                        parametros.put("FrzSumIzq", Integer.toString(sumafi));
                        parametros.put("FrzSumDer", Integer.toString(sumafd));
                        parametros.put("PsSumIzq", Integer.toString(sumapi));
                        parametros.put("PsSumDer", Integer.toString(sumapd));
                    }

                } else {

                    switch (m.getTipoMedida().getId()) 
                    {
                        case 5000://peso derecho eje 1
                            sumpesos += m.getValor();
                            break;
                        case 5001://peso derecho eje 2
                            sumpesos += m.getValor();

                            break;
                        case 5002:
                            sumpesos += m.getValor();
                            break;
                        case 5003:
                            sumpesos += m.getValor();
                            break;
                        case 5025:
                            sumpesos += m.getValor();
                            break;
                        case 5004:
                            sumpesos += m.getValor();
                            break;
                        case 5005:
                            sumpesos += m.getValor();
                            break;
                        case 5006:
                            sumpesos += m.getValor();
                            break;
                        case 5007:
                            sumpesos += m.getValor();
                            break;
                        case 5026:
                            sumpesos += m.getValor();
                            break;
                        case 5008://fuerza de frenado eje 1
                            sumfuerzas += m.getValor();
                            break;
                        case 5009://fuerza de frenado eje2
                            sumfuerzas += m.getValor();
                            break;
                        case 5010://fuerza de frenado eje2
                            sumfuerzas += m.getValor();
                            break;
                        case 5011://fuerza de frenado eje2
                            sumfuerzas += m.getValor();
                            break;
                        case 5027:
                            sumfuerzas += m.getValor();
                            break;
                        case 5012://fuerza de frenado eje2
                            sumfuerzas += m.getValor();
                            break;
                        case 5013://fuerza de frenado eje2
                            sumfuerzas += m.getValor();

                            break;
                        case 5014://fuerza de frenado eje2
                            sumfuerzas += m.getValor();
                            break;
                        case 5015://fuerza de frenado eje2
                            sumfuerzas += m.getValor();

                            break;
                        case 5028://fuerza de frenado eje2
                            sumfuerzas += m.getValor();
                            break;
                        case 5024://Eficacia de frenado
                            //df.applyPattern("#0.00");
                            String valor=df.format(m.getValor());
                            efi = ajustarValorMedida(valor);//no decimal
                            break;
                        case 5032:
                            //df.applyPattern("#0.00");
                            //parametros.put("DesEje1", df.format(m.getValor()) + m.getCondicion());//no decimal
                            break;
                        case 5033:
                            //df.applyPattern("#0.00");
                            //parametros.put("DesEje2", df.format(m.getValor()) + m.getCondicion());//no decimal
                            break;
                        case 5034:
                            //df.applyPattern("#0.00");
                            //parametros.put("DesEje3", df.format(m.getValor()) + m.getCondicion());//no decimal
                            break;
                        case 5035:
                            //df.applyPattern("#0.00");
                            //parametros.put("DesEje4", df.format(m.getValor()) + m.getCondicion());//no decimal
                            break;
                        case 5031:
                            //df.applyPattern("#0.00");
                            //parametros.put("DesEje5", df.format(m.getValor()) + m.getCondicion());//no decimal
                            break;
                        case 5036:
                            //df.applyPattern("#0.00");
                            //df.setMaximumFractionDigits(2);
                            String efiaux = df.format(m.getValor());//un decimal
                        default:
                            break;

                    }
                }
            }
               
            if (defecto_profundidad > 0)
            {
                String observamedfre = "";
                observamedfre = observamedfre.concat(" ").concat("EfiTotal: ").concat(efi).concat(" SumFuerzas: ").concat(Integer.toString(sumfuerzas)).concat(" SumPesos: ").concat(Integer.toString(sumpesos));
                System.out.println("Voy a guardar las observaciones de la prueba " + observamedfre + idPrueba);

                Connection cn = cargarConexion();
                String strTotal = "UPDATE pruebas\n"
                        + "Set pruebas.Aprobada= 'N', pruebas.observaciones =? \n"
                        + "WHERE pruebas.Id_pruebas=?";

                PreparedStatement consultaTotal = cn.prepareStatement(strTotal);
                consultaTotal.setString(1, observamedfre);
                consultaTotal.setLong(2,idPrueba);
                int up = consultaTotal.executeUpdate();
            }
        } catch (Exception e) 
        {
            System.out.println("Error en el metodo : cargaMedidaPpruebaFrenos()" +e.getMessage());
        }   
    }
    
    
    /**
     * 
     * @param idPrueba
     * @return
     * @throws ClassNotFoundException 
     */
    private int validarDefectosPorfundidaLabrado() throws ClassNotFoundException 
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------validarDefectosPorfundidaLabrado--------------");
        System.out.println("----------------------------------------------------");
        int defecto_profundidad =0;
        
        try
        {
            String sql = "SELECT dp.id_defecto FROM defxprueba AS dp WHERE dp.id_prueba=?";
//            String sql="SELECT MAX(p.Id_Pruebas) AS id_defecto FROM pruebas p WHERE p.Tipo_prueba_for=1 AND p.hoja_pruebas_for=? ORDER BY Id_Pruebas desc";
            int idPrueba=consultarPruebaVisual(idHojaPrueba);
            ResultSet rs = conexionDBcargarMedida(sql,idPrueba);
            
            while (rs.next()) 
            {
                String id_defecto = rs.getString("id_defecto");
                if (id_defecto != null) 
                {
                    if (id_defecto.equals("10094") || id_defecto.equals("10095") || id_defecto.equals("14016")) 
                    {
                        defecto_profundidad = defecto_profundidad + 1;
                    }
                }
            }

        } catch (SQLException e) 
        {
           System.out.println("ERROR EN EL METODO : validarDefectosPorfundidaLabrado() " + e.getErrorCode() + e.getMessage() );
        }
        return defecto_profundidad;
    }
    

    /**
     * @autor ELKIN B
     * 
     * @param listaMedidas 
     */
    private void cargaMedidaPruebaRuido(List<Medida> listaMedidas)
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar medidas para la prueba de Ruido -------");
        System.out.println("----------------------------------------------------");
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        df.applyPattern("#0.00");
        try 
        {
            double promedioRuidoPito = 0;
            double promedioRuidoMotor = 0;
            int tipoMedida;
            for (Medida m : listaMedidas) 
            {
                tipoMedida = m.getTipoMedida().getId();
                if (tipoMedida == 7002) 
                {
                    promedioRuidoPito = m.getValor();
                }
                if (tipoMedida == 7005) 
                {
                    promedioRuidoMotor = m.getValor();
                }

                df.applyPattern("#.0#");
                df.setMaximumFractionDigits(1);
                parametros.put("RuidoPito", df.format(promedioRuidoPito) + m.getCondicion());//un solo decimal
                parametros.put("RuidoMotor", df.format(promedioRuidoMotor) + m.getCondicion());//un solo decimal
            }

        } catch (Exception e) 
        {
            System.out.println("Error en el metodo : cargaMedidaPpruebaRuido() " + e.getMessage());
        }
        
    }
    
    /**
     * @autor ELKIN B
     * 
     * @param listaMedidas 
     */
    private void cargaMedidaPruebaSuspension(List<Medida> listaMedidas)
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar medidas para la prueba de Suspension ---");
        System.out.println("----------------------------------------------------");
//        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
            DecimalFormat df = new DecimalFormat();
//        df.applyPattern("#0.00");
            df.setRoundingMode(RoundingMode.DOWN);
        try 
        {
//            df.applyPattern("#0.00");//un solo digito no creo no
            for (Medida m : listaMedidas) 
            {
                switch (m.getTipoMedida().getId()) 
                {
                    case 6016:
                        String DelanteraDer = df.format(m.getValor());
                        parametros.put("DelanteraDer", ajustarValorMedida(DelanteraDer.trim()) + m.getCondicion());
                        break;
                    case 6020:
                        String DelanteraIzq = df.format(m.getValor());
                        parametros.put("DelanteraIzq", ajustarValorMedida(DelanteraIzq.trim()) + m.getCondicion());
                        break;
                    case 6017:
                        String TraseraDerecha=df.format(m.getValor());
                        parametros.put("TraseraDerecha",ajustarValorMedida(TraseraDerecha) + m.getCondicion());//sin decimales
                        break;
                    case 6021:
                        String TraseraIzq = df.format(m.getValor());
                        parametros.put("TraseraIzq", ajustarValorMedida(TraseraIzq.trim()) + m.getCondicion());
                        break;
                    default:
                        break;
                }//end of switch
            }//end of for ista de medidas

            
        } catch (Exception e) 
        {
            System.out.println("Error en el metodo : cargaMedidaPruebaSuspension() " + e.getMessage());
        }
    }
    
    /**
     * @autor ELKIN B
     * 
     * @param listaMedidas 
     */
    private  void cargaMedidaPruebaDesviacion(List<Medida> listaMedidas)
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar medidas para la prueba de Desviacion ---");
        System.out.println("----------------------------------------------------");
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        df.applyPattern("#0.00");
        try 
        {
            //prueba de desviacion ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¿cuantos decimales?
            df.applyPattern("#0.0#");
            df.setMaximumFractionDigits(1);
            for (Medida m : listaMedidas) 
            {
                switch (m.getTipoMedida().getId())
                {
                    case 4000:
                        String DvcnEje1 = df.format(m.getValor());
                        parametros.put("DvcnEje1", ajustarValorMedida(DvcnEje1.trim())  + m.getCondicion());
                        break;
                    case 4001:
                        String DvcnEje2 = df.format(m.getValor());
                        parametros.put("DvcnEje2", ajustarValorMedida(DvcnEje2.trim()) + m.getCondicion());
                        break;
                    case 4002:
                        String DvcnEje3 = df.format(m.getValor());
                        parametros.put("DvcnEje3", ajustarValorMedida(DvcnEje3.trim()) + m.getCondicion());
                        break;
                    case 4003:
                        String DvcnEje4 = df.format(m.getValor());
                        parametros.put("DvcnEje4", ajustarValorMedida(DvcnEje4.trim()) + m.getCondicion());
                        break;
                    case 4004:
                        String DvcnEje5 = df.format(m.getValor());
                        parametros.put("DvcnEje5", ajustarValorMedida(DvcnEje5.trim()) + m.getCondicion());
                        break;
                    default:
                        break;
                }//end of switch
            }//end of for ista de medidas
            
        } catch (Exception e) 
        {
            System.out.println("Error en el metodo :cargaMedidaPruebaDesviacion() "+ e.getMessage());
        }
    }
    
    
    /**
     * @autor ELKIN B
     * 
     * @param listaMedidas 
     */
    private  void cargaMedidaPruebaTaximetro(List<Medida> listaMedidas)
    {
        System.out.println("----------------------------------------------------");
        System.out.println("------Cargar medidas para la prueba de Taximetro ---");
        System.out.println("----------------------------------------------------");
        
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        df.applyPattern("#0.00");
        
        try 
        {
            //prueba de taximetro cuantos decimales
            df.applyPattern("#0.0");
            df.setMaximumFractionDigits(1);
            for (Medida m : listaMedidas) 
            {
                switch (m.getTipoMedida().getId()) 
                {
                    case 9002:
                        String ErrorDistancia = df.format(m.getValor());
                        parametros.put("ErrorDistancia", ajustarValorMedida(ErrorDistancia.trim()) + m.getCondicion());
                        break;
                    case 9003:
                        String ErrorTiempo = df.format(m.getValor());
                        parametros.put("ErrorTiempo", ajustarValorMedida(ErrorTiempo.trim()) + m.getCondicion());
                        break;
                    default:
                        break;
                }
            }
            parametros.put("PerTaxi", "+/-2");//pone el permisible del taximetro
            //pone la referencia comercial de la llanta
            if (this.ctxHojaPrueba != null) 
            {
                String refLlanta = this.ctxHojaPrueba.getVehiculo().getLlantas().getNombre();
                parametros.put("RefLlanta", refLlanta);
            }
        } catch (Exception e)
        {
            System.out.println("Error en el metodo : cargaMedidaPruebaTaximetro()" +e.getMessage());
        }
    }
    
    /**
     * @autor ELKIN B
     * 
     * @param sql
     * @param id
     * @return 
     */
    private ResultSet conexionDBcargarMedida(String sql,int id) throws ClassNotFoundException 
    {
         System.out.println("QUERY : " + sql);
        try 
        {
            Connection cn = cargarConexion();
            PreparedStatement consultaTotal = cn.prepareStatement(sql);
            consultaTotal.setLong(1, id);
            ResultSet rs = consultaTotal.executeQuery();
            System.out.println("Exitoso en conexionDBcargarMedida()  para el query : " + sql);
            return rs;
        } catch (SQLException e) 
        {
            System.err.println("Error en el metodo : conexionDBcargarMedida()" +e.getMessage() + e.getLocalizedMessage());
            System.err.println("Error en el metodo : conexionDBcargarMedida()" +e.getSQLState());
        }
        return null;
    }

    
    /**
     * 
     * @param sql
     * @param id
     * @return
     * @throws ClassNotFoundException 
     */
    private int consultarPruebaVisual(int id)
    {
//        String sql="SELECT p.Id_Pruebas FROM pruebas p WHERE p.Tipo_prueba_for=1 AND p.hoja_pruebas_for=?";
        String sql="";
        if (isReinspecion) 
        {
            sql="SELECT MIN(p.Id_Pruebas) AS id_defecto FROM pruebas p WHERE p.Tipo_prueba_for=1 AND p.hoja_pruebas_for=? ORDER BY Id_Pruebas desc";
        }else{
            sql="SELECT MAX(p.Id_Pruebas) AS id_defecto FROM pruebas p WHERE p.Tipo_prueba_for=1 AND p.hoja_pruebas_for=? ORDER BY Id_Pruebas desc";
        }
        
        try 
        {
            Connection cn = cargarConexion();
            PreparedStatement consultaTotal = cn.prepareStatement(sql);
            consultaTotal.setInt(1, id);
            ResultSet rs = consultaTotal.executeQuery();
            while (rs.next()) 
            {
                return Integer.parseInt(rs.getString("id_defecto"));
            }
        } catch (Exception e) {
            System.err.println("Error en el metodo : consultarPruebaVisual()" + e.getMessage() + e.getLocalizedMessage());
        }
        return 0;
    }
    
    
    
    /**
     * los permisibles dependen del tipo del vehiculo, del modelo del vehiculo
     * de los tiempos y del tipo de combustible, los permisibles de las pruebas
     * estan harcoded en cada hilo de la prueba
     *
     * los permisibles dependen del tipo del vehiculo, del modelo del vehiculo
     * de los tiempos y del tipo de combustible
     * 
     * @param cn
     * @param hojaPruebas
     * @throws SQLException
     */
    private void configurarPermisibles() throws SQLException {

            parametros.put("diametro", "");
        if (this.ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() == 5) {//Motocarro
            //parametros.put("PerO2","[0-11]");
            parametros.put("PerCO2", "---");
            //Desviacion
            parametros.put("PerDesv", "---");
            //Taximetro
            parametros.put("PerTaxi", "---");
            //Suspension
            parametros.put("PerSusp", "---");
            //Frenos
            parametros.put("PerEficTotal", "30");//30%
            parametros.put("PerEficAux", "18");//no hay desequilibrio
            parametros.put("PerDeseqB", "20-30");
            parametros.put("PerDeseq", ">30");
            parametros.put("PerSumaLuces", "---");
            //Desviacion : el permisible es solamente uno
            //Dispositivos de cobro: el permisible es solamente uno
            //Gases :

            if (this.ctxHojaPrueba.getVehiculo().getTiemposMotor() == 4) {
                parametros.put("PerO2", "[0-6]");
            }
            if (this.ctxHojaPrueba.getVehiculo().getTiemposMotor() == 2 && this.ctxHojaPrueba.getVehiculo().getModelo() < 2010) {
                parametros.put("PerO2", "[0-11]");
            } else {
                parametros.put("PerO2", "[0-6]");
            }

            if (this.ctxHojaPrueba.getVehiculo().getTiemposMotor() == 2 && this.ctxHojaPrueba.getVehiculo().getModelo() <= 2009) {
                parametros.put("PerHCRal", "[0-10000]");
            } else {
                parametros.put("PerHCRal", "[0-2000]");
            }
            parametros.put("PerHCCruc", "---");
            parametros.put("PerCOCruc", "---");
            parametros.put("PerCORal", "[0-4.5]");
            //no mostrar permisibles de Opacidad porque ninguna moto es diesel
            parametros.put("PerOpac", "---");
        }

        if (this.ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() == 4) {//Motocicletas
            //parametros.put("PerO2","[0-11]");
            parametros.put("PerCO2", "---");
            //Desviacion
            parametros.put("PerDesv", "---");
            //Taximetro
            parametros.put("PerTaxi", "---");
            //Suspension
            parametros.put("PerSusp", "---");
            //Frenos
            parametros.put("PerEficTotal", "30");//30%
            parametros.put("PerEficAux", "---");//no hay desequilibrio
            parametros.put("PerDeseq", "---");
            parametros.put("PerDeseqB", "---");
            parametros.put("PerSumaLuces", "---");
            //Desviacion : el permisible es solamente uno
            //Dispositivos de cobro: el permisible es solamente uno
            //Gases :
            if (this.ctxHojaPrueba.getVehiculo().getTiemposMotor() == 4) {
                parametros.put("PerO2", "[0-6]");
            }
            if (this.ctxHojaPrueba.getVehiculo().getTiemposMotor() == 2 && this.ctxHojaPrueba.getVehiculo().getModelo() < 2010) {
                parametros.put("PerO2", "[0-11]");
            } else {
                parametros.put("PerO2", "[0-6]");
            }
            if (this.ctxHojaPrueba.getVehiculo().getTiemposMotor() == 2 && this.ctxHojaPrueba.getVehiculo().getModelo() <= 2009) {
                parametros.put("PerHCRal", "[0-10000]");
            } else {
                parametros.put("PerHCRal", "[0-2000]");
            }
            parametros.put("PerHCCruc", "---");
            parametros.put("PerCOCruc", "---");
            parametros.put("PerCORal", "[0-4.5]");
            //no mostrar permisibles de Opacidad porque ninguna moto es diesel
            parametros.put("PerOpac", "---");
        } else if (this.ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() == 1 || this.ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() == 3 || this.ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() == 2 || this.ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() == 109 || this.ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() == 110) {// 1 -> Livianos ,  3-> pesados, 2-> 4x4 109 or 110-> aplica a taxi 
            if (this.ctxHojaPrueba.getVehiculo().getTipoGasolina().getId() == 1 || this.ctxHojaPrueba.getVehiculo().getTipoGasolina().getId() == 4) {
                parametros.put("PerO2", "[0-5]");
                parametros.put("PerCO2", "[ <7 ]");
            } else {
                parametros.put("PerO2", " ");
                parametros.put("PerCO2", " ");
            }
            //Desviacion
            parametros.put("PerDesv", "10");
            //Taximetro
            if (this.ctxHojaPrueba.getVehiculo().getServicios().getId() == 2) {
                parametros.put("PerTaxi", "+/-2");
            } else {
                parametros.put("PerTaxi", "---");
            }
            //Suspension: se realiza suspension a todos los vehiculos livianos
            //if(rs.getInt("CARTYPE")== 1)
            parametros.put("PerSusp", "40");
            //else
            //parametros.put("PerSusp","---");//a pesados no se realiza suspension
            //Frenos:
            parametros.put("PerEficTotal", "50");
            parametros.put("PerEficAux", "18");
            parametros.put("PerDeseqB", "20-30");
            parametros.put("PerDeseq", ">30");//ojo de 20 a 30 es defecto tipo B
            parametros.put("PerSumaLuces", "225");

            //Gases
            if (this.ctxHojaPrueba.getVehiculo().getTipoGasolina().getId() == 1) {//si es gasolina
                if (this.ctxHojaPrueba.getVehiculo().getModelo() <= 1970) {
                    parametros.put("PerHCRal", "[0-800]");
                    parametros.put("PerCORal", "[0-5.0]");
                    parametros.put("PerHCCruc", "[0-800]");
                    parametros.put("PerCOCruc", "[0-5.0]");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1970 && this.ctxHojaPrueba.getVehiculo().getModelo() <= 1984) {
                    parametros.put("PerHCRal", "[0-650]");
                    parametros.put("PerCORal", "[0-4.0]");
                    parametros.put("PerHCCruc", "[0-650]");
                    parametros.put("PerCOCruc", "[0-4.0]");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1984 && this.ctxHojaPrueba.getVehiculo().getModelo() <= 1997) {
                    parametros.put("PerHCRal", "[0-400]");
                    parametros.put("PerCORal", "[0-3.0]");
                    parametros.put("PerHCCruc", "[0-400]");
                    parametros.put("PerCOCruc", "[0-3.0]");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1997) {
                    parametros.put("PerHCRal", "[0-200]");
                    parametros.put("PerCORal", "[0-1.0]");
                    parametros.put("PerHCCruc", "[0-200]");
                    parametros.put("PerCOCruc", "[0-1.0]");
                }
                parametros.put("PerOpac", "---");
            } else if (this.ctxHojaPrueba.getVehiculo().getTipoGasolina().getId() == 4) {//si es gas gasolina o es gas natural vehicular
                if (this.ctxHojaPrueba.getVehiculo().getModelo() <= 1970) {
                    parametros.put("PerHCRal", "[0-800]");
                    parametros.put("PerCORal", "[0-5.0]");
                    parametros.put("PerHCCruc", "[0-800]");
                    parametros.put("PerCOCruc", "[0-5.0]");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1970 && this.ctxHojaPrueba.getVehiculo().getModelo() <= 1984) {
                    parametros.put("PerHCRal", "[0-650]");
                    parametros.put("PerCORal", "[0-4.0]");
                    parametros.put("PerHCCruc", "[0-650]");
                    parametros.put("PerCOCruc", "[0-4.0]");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1984 && this.ctxHojaPrueba.getVehiculo().getModelo() <= 1997) {
                    parametros.put("PerHCRal", "[0-400]");
                    parametros.put("PerCORal", "[0-3.0]");
                    parametros.put("PerHCCruc", "[0-400]");
                    parametros.put("PerCOCruc", "[0-3.0]");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1997) {
                    parametros.put("PerHCRal", "[0-200]");
                    parametros.put("PerCORal", "[0-1.0]");
                    parametros.put("PerHCCruc", "[0-200]");
                    parametros.put("PerCOCruc", "[0-1.0]");
                }
            } else if (this.ctxHojaPrueba.getVehiculo().getTipoGasolina().getId() == 3) {//si es diesel
                parametros.put("PerHCRal", "---");
                parametros.put("PerCORal", "---");
                parametros.put("PerHCCruc", "---");
                parametros.put("PerCOCruc", "---");

                if (this.ctxHojaPrueba.getVehiculo().getModelo() <= 1970) {
                    parametros.put("PerOpac", "50");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1970 && this.ctxHojaPrueba.getVehiculo().getModelo() <= 1984) {
                    parametros.put("PerOpac", "45");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1984 && this.ctxHojaPrueba.getVehiculo().getModelo() <= 1997) {
                    parametros.put("PerOpac", "40");
                } else if (this.ctxHojaPrueba.getVehiculo().getModelo() > 1997) {
                    parametros.put("PerOpac", "35");
                }
                int diametro = this.ctxHojaPrueba.getVehiculo().getDiametro();
                parametros.put("diametro", String.valueOf(diametro));
            }//end of diesel
        }
    }//end of methoc configurarPermisibles

    /**
     * Llena la tabla de resumen de los defectos por grupo Evalua la prueba
     * colocando el parametro aprobado o no aprobado Los defectos se cargan de
     * la ultima prueba Finalizada LOGICA DE EVALUACION DE LA REVISON TECNICO
     * MECANICA EN ESTE METODO
     *
     * @param cn
     * @param hojaPruebas
     * @throws SQLException
     */
    private void cargarDefectos(Connection cn, long hojaPruebas, String placa, Integer numeroPruebasAutorizadas) throws SQLException {
        ResultSet rs;

        String strTotal = "SELECT COUNT(d.Tipo_defecto)\n"
                + "FROM pruebas AS pr\n"
                + "INNER JOIN defxprueba AS dp ON pr.id_pruebas=dp.id_prueba\n"
                + "INNER JOIN defectos AS d ON dp.id_defecto = d.CARDEFAULT\n"
                + "INNER JOIN sub_grupos AS sg ON sg.SCDEFGROUPSUB = d.DEFGROUPSSUB\n"
                + "WHERE pr.id_pruebas IN \n"
                + " (\n"
                + "SELECT MAX(id_pruebas)\n"
                + "FROM pruebas AS p\n"
                + "WHERE p.hoja_pruebas_for = ? AND p.Finalizada = 'Y' AND p.abortada='N'\n"
                + "GROUP BY p.Tipo_prueba_for) AND d.Tipo_defecto=? AND pr.hoja_pruebas_for = ? AND pr.Finalizada = 'Y' AND pr.abortada='N'";
        
        PreparedStatement consultaTotal = cn.prepareStatement(strTotal);
        
        //CUENTA LOS TIPOS DE DEFECTOS TIPO A
        consultaTotal.setLong(1, hojaPruebas);
        consultaTotal.setString(2, "A");
        consultaTotal.setLong(3, hojaPruebas);
        rs = consultaTotal.executeQuery();
        rs.first();
        int totalDefA = rs.getInt(1);
        parametros.put("TotalDefA", String.valueOf(totalDefA));
        consultaTotal.clearParameters();
        
         //CUENTA LOS TIPOS DE DEFECTOS TIPO B
        consultaTotal.setLong(1, hojaPruebas);
        consultaTotal.setString(2, "B");
        consultaTotal.setLong(3, hojaPruebas);
        rs = consultaTotal.executeQuery();
        rs.first();
        int totalDefB = rs.getInt(1);
        parametros.put("TotalDefB", String.valueOf(totalDefB));

        String ensenanza = "select count(*)  FROM vehiculos as v WHERE v.esEnsenaza=1  AND v.CARPLATE= ?";
        PreparedStatement validaTipo = cn.prepareStatement(ensenanza);
        validaTipo.setString(1, placa);
        rs = validaTipo.executeQuery();
        rs.next();
        int valida = rs.getInt(1);

        
        //CUNETA LOS DEFECTOS PARA ENSENANZA
        String strTotalDefEnsenanza = "SELECT COUNT(*)\n"
                + "FROM hoja_pruebas AS hp\n"
                + "INNER JOIN pruebas AS p ON hp.TESTSHEET=p.hoja_pruebas_for\n"
                + "INNER JOIN defxprueba AS dp ON p.Id_Pruebas=dp.id_prueba\n"
                + "INNER JOIN defectos AS d ON d.CARDEFAULT=dp.id_defecto\n"
                + "INNER JOIN grupos_sub_grupos gsg on gsg.SCDEFGROUPSUB = d.DEFGROUPSSUB\n"
                + "WHERE hp.TESTSHEET = ? AND p.Tipo_prueba_for = 1 AND gsg.DEFGROUP=21;";

        PreparedStatement psDefectosVisualEnse = cn.prepareStatement(strTotalDefEnsenanza);
        psDefectosVisualEnse.setLong(1, hojaPruebas);
        rs = psDefectosVisualEnse.executeQuery();
        rs.next();
        int defEnse = rs.getInt(1);

        
        
        if (valida > 0) 
        {
            if (defEnse > 0) {
                parametros.put("ReprobadoEnse", "X");
            } else {
                parametros.put("AprobadoEnse", "X");
            }
        }

        //EVALUACION DE LA PRUEBA DE Inspeccion Sensorial
        //Si existe un defecto tipo A entonces rechazar la prueba
        String strIVTipoA = "select count(d.Tipo_defecto) from pruebas as pr "
                + "inner join defxprueba as dp on pr.id_pruebas=dp.id_prueba "
                + "inner join defectos as d on dp.id_defecto = d.CARDEFAULT "
                + "where pr.id_pruebas = "
                + "(select max(id_pruebas) from pruebas as p where p.hoja_pruebas_for = ? and p.Tipo_prueba_for = 1 and p.Finalizada = 'Y' AND p.abortada='N') "
                + " and d.Tipo_defecto=? and pr.hoja_pruebas_for = ? and pr.Tipo_prueba_for = 1 and pr.Finalizada = 'Y' AND pr.Abortada='N' ";
        PreparedStatement psDefectosVisual = cn.prepareStatement(strIVTipoA);
        psDefectosVisual.setLong(1, hojaPruebas);
        psDefectosVisual.setString(2, "A");
        psDefectosVisual.setLong(3, hojaPruebas);
        rs = psDefectosVisual.executeQuery();
        rs.next();
        int defectosA = rs.getInt(1);
        psDefectosVisual.clearParameters();
        
        psDefectosVisual.setLong(1, hojaPruebas);
        psDefectosVisual.setString(2, "B");
        psDefectosVisual.setLong(3, hojaPruebas);
        rs = psDefectosVisual.executeQuery();
        rs.next();
        int defectosB = rs.getInt(1);
        
        //JOptionPane.showMessageDialog(null,"Defectos A: " + defectosA +"\n Defectos B:" + defectosB);
        //Consultar el numero de la prueba
        String strNumPrueba = "select max(id_pruebas) from pruebas as p where p.hoja_pruebas_for = ? and p.Tipo_prueba_for =1 and p.Finalizada = 'Y' AND p.Abortada='N'";
        PreparedStatement psNumPrueba = cn.prepareStatement(strNumPrueba);
        psNumPrueba.setLong(1, hojaPruebas);
        rs = psNumPrueba.executeQuery();
        rs.next();
        long numeroPruebaIV = rs.getLong(1);
        
        String strIVEval = "UPDATE pruebas SET Aprobada = ? where Id_Pruebas = ?";
        PreparedStatement psEvalIV = cn.prepareStatement(strIVEval);

        if (defectosA > 0) {//solo desaprueba la Inspeccion Sensorial si encuentra defectos tipo A o en el peor caso de defectos tipo B
            psEvalIV.setString(1, "N");
        } else {
            psEvalIV.setString(1, "Y");
        }

        psEvalIV.setLong(2, numeroPruebaIV);
        int pruebasEjecutadas = psEvalIV.executeUpdate();
        System.out.println("Pruebas de IV Actualizadas: " + pruebasEjecutadas);
        
        //Deben estar terminadas todas las pruebas y aprobadas todas
        //porque se da el caso que las pruebas estan terminadas pero existen algunas
        //no aprobadas o que no registraban el defecto lo cual
        //conduce a inconsistencias
        //si no estan terminadas entonces nada no se da rechazado ni aprobado
        //para motos: iv,gases,sonometro,frenos,luces una consulta
        //independientemente del tipo del vehiculo... si el numero de defectos tipo A es mayor a 0 es rechazado
        //deben estar aprobadas todas las pruebas autorizadas

        //como saber cuantas pruebas estan autorizadas para que todas esten finalizadas y
        //aprobadas sin importar el detalle del sonometro
        //Cuenta las pruebas finalizadas y aprobadas numero que debe ser igual al numero de pruebas
        //autorizadas
        String sqlPruebasFinalizadas = "select count(*) from ( "
                + "select max(id_pruebas) from pruebas as p "
                + "inner join hoja_pruebas as hp on p.hoja_pruebas_for = hp.TESTSHEET "
                + "where hp.TESTSHEET = ? and p.Finalizada = 'Y' AND p.Abortada='N' group by p.tipo_prueba_for) as tmp";

        //Cuenta las pruebas finalizadas y aprobadas numero que debe ser igual al numero de pruebas
        //autorizadas
        PreparedStatement consultaPruebasFinalizadas = cn.prepareStatement(sqlPruebasFinalizadas);
        consultaPruebasFinalizadas.setLong(1, hojaPruebas);
        rs = consultaPruebasFinalizadas.executeQuery();
        rs.next();
        int numeroPruebasFinalizadas = rs.getInt(1);//numero de pruebas finalizadas y aprobadas no es exacto en el caso cuando
        //se autoriza de nuevo una prueba de foto porque cuenta la prueba anterior.
        //Contar las pruebas autorizadas es decir contar los tipos de pruebas autorizadas

        /*String pruebasAutorizadas = "select count(*) from ( "
         + "select max(id_pruebas) from pruebas as p "
         + "inner join hoja_pruebas as hp on p.hoja_pruebas_for = hp.TESTSHEET "
         + "where hp.TESTSHEET = ? group by p.tipo_prueba_for) as tmp";
         PreparedStatement consultaPruebasAutorizadas = cn.prepareStatement(pruebasAutorizadas);
         consultaPruebasAutorizadas.setLong(1, hojaPruebas);
         rs = consultaPruebasAutorizadas.executeQuery();
         rs.next();
         int numeroPruebasAutorizadas = rs.getInt(1);//trae el numero de pruebas autorizadas las cuales*/
        //deben ser iguales a el numero de pruebas finalizadas y aprobadas
        String strPruebasNoFinalizadas = "select count(*) from ("
                + "select max(id_pruebas) from pruebas as p inner join tipo_prueba as tp "
                + "on p.Tipo_prueba_for = tp.TESTTYPE where p.hoja_pruebas_for = ? and p.Finalizada = 'Y' AND p.Abortada='N' AND p.Aprobada = 'N' group by p.Tipo_prueba_for) as tmp";
        PreparedStatement psNoFinalizadas = cn.prepareStatement(strPruebasNoFinalizadas);
        psNoFinalizadas.setLong(1, hojaPruebas);
        rs = psNoFinalizadas.executeQuery();
        int numeroPruebasNoFinalizadas = 0;
        if (rs.next()) {
            numeroPruebasNoFinalizadas = rs.getInt(1);
        }

        //Numero de pruebas finalizadas = numero de pruebas finalizadas - numero de pruebas pendientes;
        //numeroPruebasFinalizadas = numeroPruebasFinalizadas + numeroPruebasNoFinalizadas;
        //Los parametros del vehiculo
        String sql = "SELECT CARTYPE,Modelo,Tiempos_motor,FUELTYPE,SERVICE,CLASS,SPSERVICE from vehiculos as v inner join hoja_pruebas as hp on v.CAR=hp.Vehiculo_for where hp.TESTSHEET = ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setLong(1, hojaPruebas);
        ResultSet rs1 = ps.executeQuery();
        rs1.next();//primer registroy
        switch (rs1.getInt("CARTYPE")) {
            case 4:
                //Motos
                //iv,gases,frenos,luces,sonometro,foto
                //debe hacer mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡s de 5 pruebas teniendo en cuenta que el sonometro se realize asi no se tomen medidas
                //debe haber aprobado el mismo numero de pruebas que se autorizaron
                if (numeroPruebasFinalizadas >= 5 && (numeroPruebasFinalizadas == numeroPruebasAutorizadas)) {
                    if (totalDefA > 0) {
                        parametros.put("Reprobado", "X");
                    } else//si no tiene defectos tipo A
                    {
                        if (totalDefB < 5) {
                            parametros.put("Aprobado", "X");
                        } else {
                            parametros.put("ComentDefectos", "Vehiculo Reprobado Defectos tipo B es Mayor a 5");
                            parametros.put("Reprobado", "X");
                        }//fin si no tiene defectos tipo A
                    }
                } else {//si no ha terminado todas las pruebas
                    parametros.put("Reprobado", "");
                }//fin else mas de un defecto tipo A
                break;
            case 3:
                //Pesados
                //Pesado no hace suspension minimo 6 pruebas obligatorias
                //sinembargo debe haber terminado y aprobado todas las pruebas
                if (numeroPruebasFinalizadas >= 6 && numeroPruebasFinalizadas == numeroPruebasAutorizadas) {
                    if (totalDefA > 0) {//si tiene un defecto tipo A no aprueba independientemente del tipo de servicio
                        parametros.put("Reprobado", "X");
                    } else {//si es de servicio publico
                        int servicio = rs1.getInt("SERVICE");
                        if (servicio == 3 || servicio == 1 || servicio == 4) {//particular u oficial o diplomatico
                            if (totalDefB < 10) {
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        } else if (servicio == 2 || servicio == 5) {//publico y ensenanza
                            if (totalDefB < 5) {//con 5 o mas se reprueba
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        }//end servicio publico
//                    int ensenanza = rs1.getInt("CLASS");

                    }//fin else defectos tipoA
                } else {//si no ha terminado todas las pruebas
                    parametros.put("Reprobado", "");
                }//fin else de no terminar un minimo de pruebas
                break;
            case 2:
                //4x4 debe hacer minimo 6 pruebas
                //1.Inspeccion Sensorial 2.Gases 3.Frenos 4.Luces 5. Suspension es opcional
                //     6.Foto //7.sonometro 7 pruebas como minimo de ahi hay que mirar si faltan pruebas
                if (numeroPruebasFinalizadas >= 6 && numeroPruebasAutorizadas == numeroPruebasFinalizadas) {
                    if (totalDefA > 0) {
                        parametros.put("Reprobado", "X");
                    } else {//si no hay defectos tipo A verficar los defectos tipo B
                        int servicio = rs1.getInt("SERVICE");
                        if (servicio == 3 || servicio == 1 || servicio == 4) {//si es particular, oficial o diplomatico 9 defectos
                            if (totalDefB < 10) {
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        } else if (servicio == 2 || servicio == 5) {//publico
                            if (totalDefB < 5) {//con 5 o mas se reprueba
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        }
//                    int ensenanza = rs1.getInt("CLASS");
                        if (rs1.getInt("CLASS") == 27) {//si es de ensenanza
                            if (totalDefB < 5) {//con 5 o mas se reprueba
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        }//fin ensenanza
                    }//fin else defectos tipoA
                } else {//si no ha terminado todas las pruebas
                    parametros.put("Reprobado", "");
                }//fn else de no terminar un minimo de pruebas
                break;
            //fin de livianos
            case 1:
                //Livianos
                //1.Inspeccion Sensorial 2.Gases 3.Frenos 4.Luces 5. Suspension 6. Desviacion
                //7.Foto //8.sonometro 7 no a todos los livianos se les hace suspension
                if (numeroPruebasFinalizadas >= 7 && numeroPruebasFinalizadas == numeroPruebasAutorizadas) {
                    //si es de servicio publico
                    if (totalDefA > 0) {
                        parametros.put("Reprobado", "X");
                    } else {//si no hay defectos tipo A
                        int servicio = rs1.getInt("SERVICE");
                        if (servicio == 3 || servicio == 1 || servicio == 4) {//particular diplomatico u oficial
                            if (totalDefB < 10) {
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        } else if (servicio == 2 || servicio == 5) {//publico o servicio especial
                            if (totalDefB < 5) {//con 5 o mas se reprueba
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        }//end servicio publico
//                    

                    }//fin else defectos tipoA
                } else {//si no ha terminado todas las pruebas
                    parametros.put("Reprobado", "");
                }//fn else de no terminar un minimo de pruebas pero terminar todas si es mayor el numero de pruebas al minimo
                break;
            //fin de motocarros
            case 5:
                //MotoCarro
                //1.Inspeccion Sensorial 2.Gases 3.Frenos 4.Luces 5. Foto suspension seria opcional.
                if (numeroPruebasFinalizadas >= 5 && numeroPruebasFinalizadas == numeroPruebasAutorizadas) {
                    //si es de servicio publico
                    if (totalDefA > 0) {
                        parametros.put("Reprobado", "X");
                    } else {//si no hay defectos tipo A
                        int servicio = rs1.getInt("SERVICE");
                        if (servicio == 3 || servicio == 1 || servicio == 4) {//particular diplomatico u oficial
                            if (totalDefB < 7) {
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        } else if (servicio == 2 || servicio == 5) {//publico o servicio ensenanza
                            if (totalDefB < 5) {//con 5 o mas se reprueba
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        }//end servicio publico
//                    int ensenanza = rs1.getInt("CLASS");

                    }//fin else defectos tipoA
                } else {//si no ha terminado todas las pruebas
                    parametros.put("Reprobado", "");
                }//fn else de no terminar un minimo de pruebas pero terminar todas si es mayor el numero de pruebas al minimo
                break;
            //fin de livianos
            case 7:
                //Remolques
                //1.Inspeccion Sensorial 2.Frenos  3.Foto
                if (numeroPruebasFinalizadas >= 1 && numeroPruebasFinalizadas == numeroPruebasAutorizadas) {
                    //si es de servicio publico
                    if (totalDefA > 0) {
                        parametros.put("Reprobado", "X");
                    } else {//si no hay defectos tipo A
                        int servicio = rs1.getInt("SERVICE");
                        if (servicio == 3 || servicio == 1 || servicio == 4) {//particular diplomatico u oficial
                            if (totalDefB < 10) {
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        } else if (servicio == 2 || servicio == 5) {//publico o servicio especial
                            if (totalDefB < 5) {//con 5 o mas se reprueba
                                parametros.put("Aprobado", "X");
                            } else {
                                parametros.put("Reprobado", "X");
                            }
                        }//end servicio publico
//                    int ensenanza = rs1.getInt("CLASS");

                    }//fin else defectos tipoA
                } else {//si no ha terminado todas las pruebas
                    parametros.put("Reprobado", "");
                }//fn else de no terminar un minimo de pruebas pero terminar todas si es mayor el numero de pruebas al minimo
                break;
            case 110:
                //Aplica prueba Taximetro
                //1.Inspeccion Sensorial 2.Gases 3.Frenos 4.Luces 5. Suspension 6. Desviacion
                //7.Foto //8.sonometro 7 no a todos los livianos se les hace suspension
                if (numeroPruebasFinalizadas >= 7 && numeroPruebasFinalizadas == numeroPruebasAutorizadas) {
                    //si es de servicio publico
                    if (totalDefA > 0) {
                        parametros.put("Reprobado", "X");
                    } else {//si no hay defectos tipo A                        
                        if (totalDefB < 10) {
                            parametros.put("Aprobado", "X");
                        } else {
                            parametros.put("Reprobado", "X");
                        }
                    }//end servicio publico/                  
                } else {//si no ha terminado todas las pruebas
                    parametros.put("Reprobado", "");
                }//fn else de no terminar un minimo de pruebas pero terminar todas si es mayor el numero de pruebas al minimo
                break;
            //fin de motocarros
            default:
                break;
        }
    }//fin del metodo cargarDefectos

    /**
     * Pone el tipo de documento.. el comentario del CDA y los datos del CDA
     * antes colocaba las pruebas no finalizadas
     *
     * @param hojaPruebas
     * @param cn
     */
    private void cargarInfo() {

        HojaPruebasJpaController hojaPruebasController = new HojaPruebasJpaController();
        HojaPruebas tempHojaPruebas = hojaPruebasController.find((int) this.ctxHojaPrueba.getId());
        ConsultasReinspeccion crei = new ConsultasReinspeccion();
        ResultSet rs;

        String tipoDoc = this.ctxHojaPrueba.getPropietario().getTipoIdentificacion().name();
        //Colocar la expresion

        StringBuilder sb = new StringBuilder();
        sb.append("CC.(");
        String tmpString = tipoDoc.equals("CC") ? "X" : " ";
        sb.append(tmpString);
        sb.append(") NIT.(");
//        tmpString = tipoDoc.equals("NIT") ? "X" : " ";
//        sb.append(tmpString);
//        sb.append(") CE.( ");
        tmpString = tipoDoc.equals("CE") ? "X" : " ";
        sb.append(tmpString);
        sb.append(")");
        parametros.put("exprTipoDoc", sb.toString());

        String numeroIntento = String.valueOf(this.ctxHojaPrueba.getIntentos());
        String consecutivoRunt = this.ctxHojaPrueba.getConsecutivoRunt();
        parametros.put("numeroIntento", numeroIntento);
        //FUR ASOCIADO
        if (Integer.valueOf(numeroIntento) == 2) {
            parametros.put("intentoasoc", "1");
        }
        parametros.put("consecutivoRUNT", consecutivoRunt);
        //Cargar la informacion del CDA

        parametros.put("NITCDA", this.ctxCDA.getNit());
        parametros.put("NombreCDA", this.ctxCDA.getNombre());
        parametros.put("DireccionCDA", this.ctxCDA.getDireccion());
        parametros.put("TelefonoCDA", this.ctxCDA.getTelefono());
        parametros.put("ciudadCDA", this.ctxCDA.getCiudad());

        parametros.put("nombreResp", this.ctxHojaPrueba.getResponsable().getNombre());
        System.out.println("Pase por aqui " + this.ctxHojaPrueba.getResponsable().getNombre());
        if (this.ctxHojaPrueba.getVehiculo().getDiseno().name().equalsIgnoreCase("Scooter")) {
            parametros.put("TempGasoCruc", "0");
            parametros.put("TempGasoRal", "0");
        }
    }//end of method cargarInfo

    //Carga las ultimas ids de las pruebas que esten finalizadas
    //Deberia cargar solamente las ultimas ids de las pruebas asi no esten finalizadas????
    /**
     * Metodo que retorna una lista de ids de las ultimas pruebas finalizadas
     *
     * @param cn
     * @param hojaPruebas
     * @return la ultima prueba finalizada
     */
    private List<Prueba> cargarIdPruebasNoDuplicadas(HojaPruebas ctxHojaPrueba, String estado) {
        //ultimas pruebas de cada tipo finalizadas para ahorrar tiempo de consulta

        List<Prueba> ctxListPrueba = new ArrayList();
        if (estado.equalsIgnoreCase("I")) {
            ctxListPrueba = ctxHojaPrueba.getListPruebas();
        } else {
            Reinspeccion r = ctxHojaPrueba.getReinspeccionList().get(0);
            List<Prueba> tempListPrueba = ctxHojaPrueba.getListPruebas();
            Boolean encontrado = false;
            for (Prueba ctxpru : tempListPrueba) {
                for (Prueba prReins : r.getPruebaList()) {
                    if (ctxpru.getTipoPrueba().getId() == prReins.getTipoPrueba().getId()) {
                        encontrado = true;
                        break;
                    }
                }
                if (encontrado == false) {
                    if (!ctxpru.getAbortado().equalsIgnoreCase("A") && !ctxpru.getFinalizada().equalsIgnoreCase("x")) {
                        ctxListPrueba.add(ctxpru);
                    }
                }
                encontrado = false;
            }
            for (Prueba prReins : r.getPruebaList()) {
                if (!prReins.getAbortado().equalsIgnoreCase("A") && !prReins.getFinalizada().equalsIgnoreCase("x")) {
                    ctxListPrueba.add(prReins);
                }

            }
        }

        List<Prueba> listaIdsPruebas = new ArrayList();
        for (Prueba pr : ctxListPrueba) {
            if (!pr.getAbortado().equalsIgnoreCase("A") && !pr.getFinalizada().equalsIgnoreCase("x")) {
                listaIdsPruebas.add(pr);
            }
        }
        return listaIdsPruebas;
    }//end of cargarIdPruebasNoDuplicadas

    /**
     * Verifica si el certificado ya fue impreso.. si en efecto entonces pide
     * las razones para sacar el duplicado y llama a imprimirDuplicado Si no ha
     * sido impreso entonces verifica el consecutivo y llama al metodo imprimir
     *
     * @param idHojaPrueba
     * @param cn
     */
    public void imprimirCertificado(long idHojaPrueba, long consecutivoRUNT, Connection cn) {

        try {
            //traer los parametros para el certificado
            Certificado certificado = consultasCertificados.obtenerCertificadoPorHojaPrueba((int) idHojaPrueba, cn);

            if (certificado != null) {//Imprimir duplicado
                int anularCertificado = preguntarAnularCertificado(certificado);
                long consecutivoRUNTAnterior = consultas.consultarCertificadoRUNTAnteriorCertificado(certificado.getId(), cn);
                Date fechaExpedicionAnterior = consultasCertificados.obtenerFechaExpedicionCertificado(certificado.getId(), cn);
                Date fechaVecimientoCertificadoAnteror = consultasCertificados.obtenerFechaVencimientoCertificado(certificado.getId(), cn);
                if (anularCertificado == JOptionPane.YES_OPTION) {

                    int cambiarConsecutivo = preguntarCambiarConsecutivo(certificado);
                    if (cambiarConsecutivo == JOptionPane.YES_OPTION) {
                        boolean cambiarConsecutivoRunt = cambiarConsecutivoRunt(certificado, cn);
                        if (!cambiarConsecutivoRunt) {//Si ocurrio un error en el cambio del consecutivo no proceder mas
                            return;
                        }
                    }//end of if cambiarConsecutivo   
                    // CDAVcali2018
                    ListenerDuplicado listenerDuplicado = new ListenerDuplicado(idHojaPrueba, certificado.getId(), new Timestamp(fechaExpedicionAnterior.getTime()), new Timestamp(fechaVecimientoCertificadoAnteror.getTime()));
                    JOptionPane.showMessageDialog(null, " He Generado un Nuevo Consecutivo Runt de Manera Exitosa ..!");
                    /* JasperPrint fillReport = generarReporte(cn, idHojaPrueba, consecutivoRUNTAnterior, fechaExpedicionAnterior, fechaVecimientoCertificadoAnteror);
                     mostrarCertificadoViewer(fillReport, listenerDuplicado); */
                } else {
                    String motivoAnulacion = UtilDuplicado.preguntarMotivoAnulacion();
                    int consecutivoPreimpreso = UtilDuplicado.preguntarConsecutivo();
                    if (motivoAnulacion != null && !motivoAnulacion.isEmpty() && consecutivoPreimpreso > 0) {
                        cn.setAutoCommit(false);

                        consultasCertificados.anularCertificado(certificado.getId(), motivoAnulacion, cn);
                        long consecutivoRUNTCertificadoAnterior = consultas.consultarCertificadoRUNTAnteriorCertificado(certificado.getId(), cn);
                        consultasCertificados.registrarNuevoCertificado(consecutivoPreimpreso, idHojaPrueba, fechaExpedicionAnterior, fechaVecimientoCertificadoAnteror, consecutivoRUNTCertificadoAnterior, cn);
                        consultas.cerrarRevision(consecutivoRUNTCertificadoAnterior, idHojaPrueba, true, cn);//se supone que la revision ya debe hacer cerrado
                        JasperPrint fillReport = generarReporte(cn, idHojaPrueba, consecutivoRUNTAnterior, fechaExpedicionAnterior, fechaVecimientoCertificadoAnteror);
                        mostrarCertificado_o_DuplicadoPdf(fillReport, (int) idHojaPrueba);
                        cn.commit();
                        cn.setAutoCommit(true);
                        JOptionPane.showMessageDialog(null, " He Generado un Nuevo Certificado de Manera Exitosa ..!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Por Favor Introduzca valores validos para el  motivo de anulacion y/o consecutivo preimpreso");
                    }
                }

            }//fin de imprimir duplicado
            else {// si no es un duplicado
                //Esta hoja de prueba ha sido aprobada desea imprimir certificado.               

            }//end else no es un certificado
        } catch (JRException | SQLException | IOException ex) {
            Mensajes.mostrarExcepcion(ex);
            rollbackOperacion(cn);
        }
    }

    /**
     * Genera el jasperPrint de un certificado o duplicado
     *
     * @param cn
     * @param idHojaPrueba
     * @throws JRException
     * @throws SQLException
     * @throws HeadlessException
     */
    private JasperPrint generarReporte(Connection cn, long idHojaPrueba, long consecutivoRUNTCertificados, Date fechaExpedicion, Date fechaVencimiento) throws JRException, SQLException, HeadlessException {
        String nombreReporte = "certificado.jasper";
        Map parametrosCertificado = new HashMap();
        Cda infoCda = consultasCertificados.obtenerInfoCDA(cn);
        ponerInfoCDAParametros(parametrosCertificado, infoCda);
        //poner los parametros para el certificado
        parametrosCertificado.put("ID_HP", (long) idHojaPrueba); //poner la hoja de prueba
        parametrosCertificado.put("ConsecutivoRUNT", consecutivoRUNTCertificados);
        //traer la fecha desde el servidor y ponerla

        //Pasar la fecha de hoy como parametro
        Calendar calHoy = Calendar.getInstance();
        calHoy.setTime(fechaExpedicion);
        ponerInfoFechaExpedicionParametros(calHoy, parametrosCertificado);

        Calendar calVencimiento = Calendar.getInstance();
        calVencimiento.setTime(fechaVencimiento);//le suma un anio a la fecha de expedicion
        Timestamp timeStampVencimiento = ponerParametrosFechaVencimiento(calVencimiento, parametrosCertificado);
        listenerPrimerCertificado = new ListenerPrimerCertificado(idHojaPrueba, new java.sql.Timestamp(fechaExpedicion.getTime()), timeStampVencimiento, consecutivoRUNTCertificados);
        //Ajuste que corresponde a la migracion a maven
        JasperReport certificado = (JasperReport) JRLoader.loadObject(CargarArchivos.cargarArchivo(nombreReporte));
//        JasperPrint fillReport = JasperFillManager.fillReport(nombreReporte, parametrosCertificado, cn);
        JasperPrint fillReport = JasperFillManager.fillReport(certificado, parametrosCertificado, cn);
        return fillReport;
    }

    public void setImprimirPdf(boolean imprimirPdf) {
        this.imprimirPdf = imprimirPdf;
    }

    private void llamarProcedimientoMedidas(Connection cn, long hojaPruebas) {
        try {
            CallableStatement llamarProcedimiento = cn.prepareCall("{call PonerAsteriscosMedidas(?)}");
            llamarProcedimiento.setLong(1, hojaPruebas);
            llamarProcedimiento.execute();
        } catch (SQLException exc) {
            Mensajes.mostrarExcepcion(exc);
        }
    }

    /**
     * Metodo que chequea la dilucion en la prueba de gases.
     *
     */
    private void cargarDilucion(String estado) {
        List<Prueba> ctxListPrueba = null;
        if (estado.equalsIgnoreCase("I")) {
            ctxListPrueba = ctxHojaPrueba.getListPruebas();
        } else {
            Reinspeccion r = ctxHojaPrueba.getReinspeccionList().get(0);
            ctxListPrueba = r.getPruebaList();
        }
        for (Prueba pr : ctxListPrueba) {
            if (pr.getTipoPrueba().getId() == 8 && pr.getFinalizada().equalsIgnoreCase("Y") && !pr.getAbortado().equalsIgnoreCase("A")) {
                if (pr.getComentario() != null && pr.getComentario().equalsIgnoreCase("DILUCION DE MUESTRA")) {
                    parametros.put("dilusion", "X");
                }
            }
        }
    }

    private String cargarComentariosFUR(HojaPruebas ctxHojaPrueba, String estado, List<Prueba> ctxListPrueba) throws SQLException 
    {
        System.out.println("---------------------------------------------------");
        System.out.println("------------- CargarComentariosFUR-----------------");
        System.out.println("---------------------------------------------------");
        
        String comentario = "";
//        String strConsulta = null;
        String cmLabr = "";
        String infLabr;
//        String serial_equipos = "";
        
        for (Prueba pr : ctxListPrueba) 
        {
            comentario = pr.getObservaciones();
            if (pr.getTipoPrueba().getId() == 5) 
            {
                comentario = consultarObservaciones(pr.getId(),pr.getTipoPrueba().getId());
            }

            if (comentario != null) 
            {
                if (pr.getTipoPrueba().getId() == 1 && comentario.length() > 2) 
                {
                    if (cmLabr.length() > 3)
                    {
                        cmLabr = cmLabr.concat("\n Profundidad labrado: ");
                    } else {
                        cmLabr = "Profundidad labrado: ";
                    }
                    String[] lstObs = comentario.split("obs");
                    String[] lstTrama = lstObs[0].split("@@");
                    String[] lstEjes = lstTrama[0].split("&");
                    for (int i = 0; i < lstEjes.length; i++) 
                    {
                        infLabr = lstEjes[i].replace("$", "-");
                        cmLabr = cmLabr.concat("Eje" + String.valueOf(i + 1)).concat(" ").concat(infLabr).concat("mm; ");
                    }
                    if (lstTrama.length > 1) 
                    {
                        if (ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() != 4 && ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId() != 5) {
                            String[] llantaRespuesto = lstTrama[1].split("%");
                            cmLabr = cmLabr.concat("LLanta de Repuesto:" + llantaRespuesto[0]).concat("mm; ");
                            if (llantaRespuesto.length > 1) {
                                cmLabr = cmLabr.concat("LLanta de Repuesto(2):" + llantaRespuesto[1]).concat("mm; ");
                            }
                        }
                    }
                    if (lstObs.length > 1) {
                        cmLabr = cmLabr.concat("\n").concat(" ").concat(lstObs[1]);
                    }
                }
                if (pr.getTipoPrueba().getId() == 2 && comentario.length() > 2) {
                    cmLabr = cmLabr.concat("\n Luces: ").concat(comentario);

                }
                if (pr.getTipoPrueba().getId() == 8 && comentario.length() > 2) 
                {
                    cmLabr = cmLabr.concat("\n Gases: ").concat(comentario);
                }
               
                if (pr.getTipoPrueba().getId() == 5 && comentario.length() > 2) 
                {
                    cmLabr = cmLabr.concat("Se presenta el defecto de profundidad de labrado, se repetirá la prueba de frenos en la re inspección: \n  Frenos: ").concat(comentario);

                }
            }
        }
        String sb = ModeloTablaImpresionFecha.organizarMayores(ctxHojaPrueba.getId(), reinspeccion);      
        return cmLabr;
    }//end method

    /**
     * 
     * @param ctxHojaPrueba
     * @param ctxListPrueba
     * @return 
     */
    private String  cargarObservacionesDB(HojaPruebas ctxHojaPrueba, List<Prueba> ctxListPrueba)
    {
        System.out.println("---------------------------------------------------");
        System.out.println("----------- Carga Observaciones FUR-----------------");
        System.out.println("---------------------------------------------------");
        String observacionesPrueba="";
        
        
        
        for (Prueba pr : ctxListPrueba) 
        {
            int idPrueba=pr.getId();
            int tipoPrueba=pr.getTipoPrueba().getId();
            int tipoVehiculo=ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId();
            String observacioVisual="";
            String observacionesLuces="";
            String observacionesGases="";
            String obsercvacionesFrenos="";
            
            System.out.println("---------------------------------------------");
            System.out.println("-------------  observacioVisual  ------------");
            System.out.println("---------------------------------------------");
            if (tipoPrueba == 1)
            {
                observacioVisual=consultarObservaciones(idPrueba,tipoPrueba);
                if (observacioVisual == null || observacioVisual.length() < 3) 
                {
                    observacioVisual = "";
                } else 
                {
                    observacioVisual= observacioVisual  + ("\n Visual : " + observacioVisual);
                    System.out.println(" observacionesLuces  "  + observacioVisual);
                }
            }

            if (tipoPrueba == 2) 
            {
                observacionesLuces=consultarObservaciones(idPrueba,tipoPrueba); 
                
                if(observacionesLuces==null || observacionesLuces.length()<3)
                {
                    observacionesLuces="";
                }else 
                {
                    observacionesPrueba= observacionesPrueba  + ("\n Luces : " + observacionesLuces);
                    System.out.println(" observacionesLuces  "  + observacionesLuces);
                }
            }
            if (tipoPrueba == 5 && obsercvacionesFrenos.length() > 2) 
            {
                obsercvacionesFrenos = consultarObservaciones(idPrueba, tipoPrueba);
                if (obsercvacionesFrenos == null || obsercvacionesFrenos.length() < 3) 
                {
                    obsercvacionesFrenos = "";
                }else 
                {
                    observacionesPrueba= observacionesPrueba  + ("\n Frenos : " + obsercvacionesFrenos);
                    System.out.println(" obsercvacionesFrenos  "  + obsercvacionesFrenos);
                }
            }    
            if (tipoPrueba == 8)
            {
                observacionesGases=consultarObservaciones(idPrueba,tipoPrueba); 
                if (observacionesGases == null || observacionesGases.length() < 3)
                {
                    observacionesGases = "";
                }else{
                    observacionesPrueba = observacionesPrueba + (" \n Gases : " + observacionesGases);
                    System.out.println(" observacionesGases  " + observacionesGases);
                }

            }
        }
        System.out.println(" Tdoas las observaciones rescatasas : \n " + observacionesPrueba);
        return observacionesPrueba;
    }
    
    /**
     * 
     * @param idPrueba
     * @return 
     */
    private String consultarObservaciones(int idPrueba,int TipoPrueba)
    {

        String sql="SELECT  p.observaciones FROM pruebas p WHERE p.Tipo_prueba_for=? AND p.Id_Pruebas=?";
        try 
        {
            Connection cn = cargarConexion();
            PreparedStatement consultaTotal = cn.prepareStatement(sql);
            consultaTotal.setInt(1,TipoPrueba);
            consultaTotal.setInt(2, idPrueba);
            ResultSet rs = consultaTotal.executeQuery();
            while (rs.next()) 
            {
                return rs.getString("observaciones");
            }
        } catch (Exception e) {
            System.err.println("Error en el metodo : consultarObservacionesFrenos()" + e.getMessage() + e.getLocalizedMessage());
        }
        return null;
    }
    
    private int preguntarAnularCertificado(Certificado certificado) throws HeadlessException {
        //Duplicado
        //Preguntar si se desea anular el certificado
        int seleccion = JOptionPane.showOptionDialog(null, "El certificado ya existe bajo el consecutivo PREIMPRESO:" + certificado.getConsecutivo() + "\n"
                + "consecutivo RUNT:" + certificado.getConsecutivoRunt() + "Que Operacion desea hacer  Ã‚Â¿Cambiar Nro Runt o ANULARLO ? ",
                "Seleccione una opcion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        return seleccion;
    }

    private int preguntarCambiarConsecutivo(Certificado certificado) throws HeadlessException {
        int opcion = JOptionPane.showOptionDialog(null, "Desea cambiar el CONSECUTIVO RUNT de este certificado: " + certificado.getConsecutivoRunt(),
                "Seleccione una opcion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        return opcion;
    }

    private boolean cambiarConsecutivoRunt(Certificado certificado, Connection cn) throws SQLException {

        String strNuevoConsecutivoRUNT = JOptionPane.showInputDialog("Digite nuevoConsecutivo RUNT");
        long nuevoConsecutivoRUNT;
        try {
            nuevoConsecutivoRUNT = Long.parseLong(strNuevoConsecutivoRUNT);//ingresa un consecutivo nuevo
        } catch (NumberFormatException ne) {
            Mensajes.mensajeAdvertencia("Error ha digitado un consecutivo invalido");
            return false;
        }//end of try catch

        if (nuevoConsecutivoRUNT <= 0) {
            return false;
        }

        consultas = new Consultas();
        consultas.actualizarConsecuivoRUNTdeCertificado(certificado.getId(), nuevoConsecutivoRUNT, cn);
        consultas.actualizarConsecutivoRUNTCertificados(nuevoConsecutivoRUNT, cn);

        return true;
    }

    /**
     * Obtiene la fecha actual del servidor mediante una funcion de sql del
     * servidor
     *
     * @param cn
     * @return la fecha del servidor en el momento de la invocacion
     */
    private Date obtenerFechaHoy(Connection cn) throws SQLException {

        String strFecha = "SELECT NOW()"; //insertar el certificado y el consecutivo
        PreparedStatement consultaFecha = cn.prepareStatement(strFecha);
        ResultSet rsFecha = consultaFecha.executeQuery();
        rsFecha.next();
        java.sql.Timestamp mysqlTimestamp = rsFecha.getTimestamp(1);
        Date fechaHoy = new Date(mysqlTimestamp.getTime());

        return fechaHoy;
    }

    private void ponerInfoCDAParametros(Map parametrosCertificado, Cda infoCda) {
        //poner informacion dle cda en los paramentros
        parametrosCertificado.put("NombreCDA", infoCda.getNombre());
        parametrosCertificado.put("NITCDA", infoCda.getNit());
        parametrosCertificado.put("Responsable", infoCda.getNombreResponsable());//Bug, pero debido a que el Runt lo deja en blanco no parece importante
        parametrosCertificado.put("CertificadosConformidad", infoCda.getCertificado());
    }

    private void ponerInfoFechaExpedicionParametros(Calendar calHoy, Map parametrosCertificado) {
        //Sacar el ano el mes y el dia en Cadenas
        int anoFechaExpedicion = calHoy.get(Calendar.YEAR);
        int mesFechaExpedicion = calHoy.get(Calendar.MONTH) + 1; //sumarle uno al mes cuestion de presentacion
        int diaFechaExpedicion = calHoy.get(Calendar.DAY_OF_MONTH);
        //Pasar esos parametros
        parametrosCertificado.put("AÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±oExpedicion", String.valueOf(anoFechaExpedicion));
        parametrosCertificado.put("MesExpedicion", String.valueOf(mesFechaExpedicion));
        parametrosCertificado.put("DiaExpedicion", String.valueOf(diaFechaExpedicion));
    }

    private Timestamp ponerParametrosFechaVencimiento(Calendar calHoy, Map parametrosCertificado) {

        int anoFechaVencimiento = calHoy.get(Calendar.YEAR);
        int mesFechaVencimiento = calHoy.get(Calendar.MONTH) + 1; //sumarle uno al mes
        int diaFechaVencimiento = calHoy.get(Calendar.DAY_OF_MONTH);
        java.sql.Timestamp timeStampVencimiento = new java.sql.Timestamp(calHoy.getTimeInMillis());
        parametrosCertificado.put("AÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±oVencimiento", String.valueOf(anoFechaVencimiento));
        parametrosCertificado.put("MesVencimiento", String.valueOf(mesFechaVencimiento));
        parametrosCertificado.put("DiaVencimiento", String.valueOf(diaFechaVencimiento));
        return timeStampVencimiento;

    }

    private void mostrarCertificadoViewer(JasperPrint fillReport, ActionListener listener) throws HeadlessException, JRException {
        JFrame app = new JFrame("Certificado");
        JRViewerModificado jvModificado = new JRViewerModificado(fillReport);
        jvModificado.setListenerImprimir(listener);
        app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        app.setExtendedState(JFrame.MAXIMIZED_BOTH);
        app.setContentPane(jvModificado);
        app.setVisible(true);
    }

    public long preguntarConsecutivoPreimpreso(Connection cn) throws SQLException {
        long consecutivoPreimpreso = consultas.obtenerSiguienteConsecutivoPreimpreso(cn);

        int opcion = JOptionPane.showOptionDialog(null, "El siguiente consecutivo es correcto? " + (++consecutivoPreimpreso), "Seleccione una opcion",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (opcion == JOptionPane.NO_OPTION) {
            String strConsecutivoPreimpreso = JOptionPane.showInputDialog("Digite Consecutivo");
            long longConsecutivoPreimpreso;
            try {
                longConsecutivoPreimpreso = Long.parseLong(strConsecutivoPreimpreso);
                consecutivoPreimpreso = longConsecutivoPreimpreso;
            } catch (NumberFormatException ne) {
                Mensajes.mensajeError("Error ha digitado un consecutivo invalido");
                System.exit(1);//no deja continuar
            }
        }

        return consecutivoPreimpreso;
    }

    public void mostrarCertificado_o_DuplicadoPdf(JasperPrint fillReport, int idHojaPrueba) throws JRException, IOException {
        /*String destFileNamePdf = "C:\\Certificados\\certificado" + idHojaPrueba + ".pdf";
         JasperExportManager.exportReportToPdfFile(fillReport, destFileNamePdf);

         File path = new File(destFileNamePdf);
         Desktop.getDesktop().open(path);*/
    }

    private void imprimir(Connection cn, long idHojaPrueba) throws JRException, SQLException, HeadlessException {

        //Esta hoja de prueba ha sido aprobada desea imprimir certificado.
        int seleccion = JOptionPane.showOptionDialog(null, "Esta hoja de prueba ha sido aprobada ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¿DESEA IMPRIMIR CERTIFICADO?",
                "Seleccione una opcion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (seleccion == JOptionPane.NO_OPTION) {
            return;
        }

        consultas = new Consultas();
        long consecutivoRUNTCertificados;

        //traer el siguiente consecutivo que sugiere el sistema
        consecutivoRUNTCertificados = consultas.consultarConsecutivoRUNTCertificados(cn);
        int opcion = JOptionPane.showOptionDialog(null, "El consecutivo  RUNT para certificados sugerido es " + (consecutivoRUNTCertificados + 1) + " ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¿Desea digitar uno distinto?", "Seleccione una opcion",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (opcion == JOptionPane.YES_OPTION) {//digitar otro consecutivo distinto al sugerido
            String strConsecutivo = JOptionPane.showInputDialog("Digite Consecutivo");
            try {
                consecutivoRUNTCertificados = Long.parseLong(strConsecutivo);
            } catch (NumberFormatException ne) {
                JOptionPane.showMessageDialog(null, "Error ha digitado un consecutivo invalido");
                return;
            }
        } else if (opcion == JOptionPane.NO_OPTION) {
            consecutivoRUNTCertificados++;
        }

        String nombreReporte = "certificado.jasper";
        Map parametrosCertificado = new HashMap();
        String str = "SELECT * FROM cda WHERE id_cda = 1";
        PreparedStatement consultaInfoCDA = cn.prepareStatement(str);
        ResultSet rsInfoCDA = consultaInfoCDA.executeQuery();
        rsInfoCDA.next();
        parametrosCertificado.put("NombreCDA", rsInfoCDA.getString("nombre"));
        parametrosCertificado.put("NITCDA", rsInfoCDA.getString("NIT"));
        parametrosCertificado.put("Responsable", rsInfoCDA.getString("nom_resp_certificados"));
        parametrosCertificado.put("CertificadosConformidad", rsInfoCDA.getString("Certificado_conformidad"));
        //poner los parametros para el certificado
        parametrosCertificado.put("ID_HP", (long) idHojaPrueba); //poner la hoja de prueba
        parametrosCertificado.put("ConsecutivoRUNT", consecutivoRUNTCertificados);
        //traer la fecha desde el servidor y ponerla
        String strFecha = "SELECT NOW()"; //insertar el certificado y el consecutivo
        PreparedStatement consultaFecha = cn.prepareStatement(strFecha);
        ResultSet rsFecha = consultaFecha.executeQuery();
        rsFecha.next();
        java.sql.Timestamp mysqlTimestamp = rsFecha.getTimestamp(1);
        Date fechaHoy = new Date(mysqlTimestamp.getTime());
        //Pasar la fecha de hoy como parametro
        Calendar calHoy = Calendar.getInstance();
        calHoy.setTime(fechaHoy);
        //Sacar el ano el mes y el dia en Cadenas
        int anoFechaExpedicion = calHoy.get(Calendar.YEAR);
        int mesFechaExpedicion = calHoy.get(Calendar.MONTH) + 1; //sumarle uno al mes cuestion de presentacion
        int diaFechaExpedicion = calHoy.get(Calendar.DAY_OF_MONTH);
        //Pasar esos parametros
        parametrosCertificado.put("AÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±oExpedicion", String.valueOf(anoFechaExpedicion));
        parametrosCertificado.put("MesExpedicion", String.valueOf(mesFechaExpedicion));
        parametrosCertificado.put("DiaExpedicion", String.valueOf(diaFechaExpedicion));
        //dos anos
        String strFechaMatricula = "Select v.Fecha_registro,v.SERVICE,v.CARTYPE,v.SPSERVICE from vehiculos as v inner join hoja_pruebas as hp "
                + "on v.CAR = hp.Vehiculo_for and hp.TESTSHEET = ?";
        PreparedStatement psFechaMatricula = cn.prepareStatement(strFechaMatricula);
        psFechaMatricula.setLong(1, idHojaPrueba);
        ResultSet rsFechaMatricula = psFechaMatricula.executeQuery();
        rsFechaMatricula.next();
        Date dateFechaMatricula = rsFechaMatricula.getDate(1);
        int servicio = rsFechaMatricula.getInt(2);//obtener el servicio
        int tipoVehiculo = rsFechaMatricula.getInt(3);//obtener el tipo de Vehiculo
        int servicioEspecial = rsFechaMatricula.getInt(4);//obtener el servicio especial
        DateFormat df = DateFormat.getDateInstance();
        System.out.println(df.format(dateFechaMatricula));
        //Calendar de la fecha de matricula
        Calendar cFechaRegistro = Calendar.getInstance();
        cFechaRegistro.setTime(dateFechaMatricula);
        //Fecha de Hoy - 6 anos mayor a fecha de Registro
        Calendar cHoyMenosSeis = Calendar.getInstance();
        cHoyMenosSeis.setTime(fechaHoy);
        cHoyMenosSeis.add(Calendar.YEAR, -6);

        System.out.println(df.format(cHoyMenosSeis.getTime()));

        //Comparacion
        boolean imprimirADosAnos = cFechaRegistro.compareTo(cHoyMenosSeis) >= 0;

        if (servicio == 3 && imprimirADosAnos && (tipoVehiculo != 4) && servicioEspecial == 1) {//Que no sea moto que sea particular y no servicio especial
            calHoy.add(Calendar.YEAR, 1);
        } else {
            calHoy.add(Calendar.YEAR, 1);
        }
        //calHoy.add(Calendar.YEAR,1);        //no es calHoy es calFechaVencimiento
        int anoFechaVencimiento = calHoy.get(Calendar.YEAR);
        int mesFechaVencimiento = calHoy.get(Calendar.MONTH) + 1; //sumarle uno al mes
        int diaFechaVencimiento = calHoy.get(Calendar.DAY_OF_MONTH);
        java.sql.Timestamp timeStampVencimiento = new java.sql.Timestamp(calHoy.getTimeInMillis());
        parametrosCertificado.put("AÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±oVencimiento", String.valueOf(anoFechaVencimiento));
        parametrosCertificado.put("MesVencimiento", String.valueOf(mesFechaVencimiento));
        parametrosCertificado.put("DiaVencimiento", String.valueOf(diaFechaVencimiento));

        listenerPrimerCertificado = new ListenerPrimerCertificado(idHojaPrueba, mysqlTimestamp, timeStampVencimiento, consecutivoRUNTCertificados);
        JasperReport certificado = (JasperReport) JRLoader.loadObject(CargarArchivos.cargarArchivo(nombreReporte));
        JasperPrint fillReport = JasperFillManager.fillReport(certificado, parametrosCertificado, cn);
//        JasperPrint fillReport = JasperFillManager.fillReport(nombreReporte, parametrosCertificado, cn);

        if (!imprimirPdf) {
            JFrame app = new JFrame("Certificado");
            JRViewerModificado jvModificado = new JRViewerModificado(fillReport);
            jvModificado.setListenerImprimir(listenerPrimerCertificado);
            app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            app.setExtendedState(JFrame.MAXIMIZED_BOTH);
            app.setContentPane(jvModificado);
            app.setVisible(true);

        } else {
            JOptionPane.showMessageDialog(null, "No se permite imprimir en pdf el CERTIFICADO");
        }
    }

    
    private void rollbackOperacion(Connection cn) {
        try {
            cn.rollback();

        } catch (SQLException ex1) {
            Logger.getLogger(LlamarReporte.class.getName()).log(Level.SEVERE, null, ex1);
        }
    }
    
    /**
     * 
     * @param reinspeccion
     * @param parametros
     * @param numeroImagen 
     */
    private void cargarImagen(boolean reinspeccion, Map parametros, int numeroImagen,boolean  flag) {
            SepararImagenes separarImagenes = new SepararImagenes();
            separarImagenes.obtenerImagen(numeroImagen, this.ctxHojaPrueba, reinspeccion,flag);
            parametros.put("Foto1", separarImagenes.getFoto1());
            parametros.put("Foto2", separarImagenes.getFoto2());
    }

    /**
     * 
     * @param hojaPrueba
     * @param cn
     * @throws SQLException 
     */
    private void cargarSeriales(int hojaPrueba, Connection cn) throws SQLException
    {
        System.out.println("----------------------------------------------------");
        System.out.println("Datos para el quipo de la prueba " + hojaPrueba);
        System.out.println("----------------------------------------------------");

//        parametros.put("InstDesvia", "");
//        parametros.put("MarcaDesvia", "");
//        parametros.put("InstSusp", "");
//        parametros.put("MarcaSusp", "");
        

        try 
        {
            ResultSet rs = consultarSerialDB(hojaPrueba, cn);
            while (rs.next()) {
                int Tipo_prueba_for = rs.getInt(1);
                String marca = rs.getString(2);
                String pefD = rs.getString(3);
                String serialEquipo = rs.getString(4);
                String pef = rs.getString(5);
                
                if (Tipo_prueba_for == 1) 
                {
                    //modificacion para agg equipos
//                    int tipoVehiculo = this.ctxHojaPrueba.getVehiculo().getTipoVehiculo().getId();
//                    String tipoEquipo="";
//                    if (tipoVehiculo == 4)
//                    {
//                        tipoEquipo="Holguras";
//                    } else {
//                        tipoEquipo="Elevador";
//                    }
                    cargarSerialesVisualFur(serialEquipo, marca);

                }


                if (Tipo_prueba_for == 2) {
                    cargandoSerialEquipoLuces(serialEquipo, marca);
                }

                if (Tipo_prueba_for == 4) {
                    cargandoSerialEquipoDesviacion(serialEquipo, marca);
                }

                if (Tipo_prueba_for == 5) {
                    cargandoSerialEquipoFrenos(serialEquipo, marca);
                }

                if (Tipo_prueba_for == 6) {
                    cargandoSerialEquipoSuspencion(serialEquipo, marca);
                }

                if (Tipo_prueba_for == 7) {
                    cargandoSerialEquipoRuido(serialEquipo, marca);
                }

                if (Tipo_prueba_for == 8) {
                    cargandoSerialEquipoGases(marca, pefD, serialEquipo, pef);
                }

                if (Tipo_prueba_for == 9) {
                    cargandoSerialEquipoTaximetro(serialEquipo, marca);
                }
            }
        } catch (SQLException e) 
        {
            System.out.println("Error en el metodo : cargarSeriales "+ e.getMessage() + e.getStackTrace());
        }

    }
    
    /**
     * 
     * @param hojaPrueba
     * @param cn
     * @return 
     */
    private ResultSet consultarSerialDB(int hojaPrueba,Connection cn) 
    {
        try 
        {
            String strConsulta = "SELECT pruebas.Tipo_prueba_for, equipos.marca, equipos.pef/1000,pruebas.serialEquipo,equipos.pef\n"
                    + "FROM equipos\n"
                    + "INNER JOIN pruebas ON pruebas.serialEquipo=equipos.resolucionambiental\n"
                    + "INNER JOIN hoja_pruebas ON hoja_pruebas.TESTSHEET = pruebas.hoja_pruebas_for\n"
                    + "WHERE hoja_pruebas.TESTSHEET=?\n"
                    + "ORDER BY pruebas.Tipo_prueba_for ASC";
            PreparedStatement ps = cn.prepareStatement(strConsulta);
            ps.setInt(1, hojaPrueba);
            ResultSet rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) 
        {
            System.out.println("Error en el metodo : consultarSerialDB()" +e.getMessage());
        }
        return null;
    }
    
    /**
     * 
     * @param serialEquipo
     * @param marca 
     */
    private void cargarSerialesVisualFur(String serialEquipo, String marca) 
    {
        try 
        {
            System.out.println("---------------------------------------------------");
            System.out.println("Cargando datos de PROFUNDIMETRO : Searial: " + serialEquipo + " Marca : " + marca);
            System.out.println("---------------------------------------------------");

            EquiposJpaController1 controller1 = new EquiposJpaController1();
            String[] data = serialEquipo.split(";");

            //equipo Profundimetro    
            parametros.put("InstProfundidad", data[0]);
            parametros.put("MarcaProfundidad", marca);

            //equipo holguras 0 elevador
            Equipo EquipoElevador = controller1.buscarSerialResolucionAmbental(data[1].trim());
            System.out.println("------------FUERA----------------------");
            if (EquipoElevador != null) 
            {
                System.out.println("------------CARGA dATOS----------------------");
                parametros.put("holgurasElevador", EquipoElevador.getNomEquipo());
                parametros.put("holgurasElevadorMarca", EquipoElevador.getMarca());
                parametros.put("holgurasElevadorSerial", EquipoElevador.getResolucionambiental());
            }else{
                System.out.println("Error la entidad equipo es nula");
            }
        } catch (Exception e) 
        {
            System.out.println("Error : " + e.toString());
            System.out.println("Error  :" + e.getMessage() + e.getLocalizedMessage());
            Logger.getLogger(LlamarReporte.class.getName()).log(Level.SEVERE, null,e);
        }
    }

    /**
     * 
     * @param serialEquipo
     * @param marca 
     */
    private void cargandoSerialEquipoLuces(String serialEquipo, String marca)
    {
        System.out.println("Cargando datos equipo de LUCES : Searial: " + serialEquipo + " Marca : " + marca);
        parametros.put("InstLuces",serialEquipo);
        parametros.put("MarcaLuces", marca);
    }
    
    /**
     * 
     * @param serialEquipo
     * @param marca 
     */
    private void cargandoSerialEquipoDesviacion(String serialEquipo, String marca)
    {
        System.out.println("Cargando datos equipo de DESVIACION : Searial: " + serialEquipo + " Marca : " + marca);
        parametros.put("InstDesvia", serialEquipo);
        parametros.put("MarcaDesvia", marca);
    }
    
    /**
     * 
     * @param serialEquipo
     * @param marca 
     */
    private void cargandoSerialEquipoFrenos(String serialEquipo, String marca) 
    {
        System.out.println("Cargando datos equipo de FRENOS : Searial: " + serialEquipo + " Marca : " + marca);
        parametros.put("InstFreno", serialEquipo);
        parametros.put("MarcaFreno",marca);
    }

    /**
     * 
     * @param serialEquipo
     * @param marca 
     */
    private void cargandoSerialEquipoSuspencion(String serialEquipo, String marca) 
    {
        System.out.println("Cargando datos equipo de SUSPENSION : Searial: " + serialEquipo + " Marca : " + marca);
        parametros.put("InstSusp", serialEquipo);
        parametros.put("MarcaSusp",marca);
    }
    
    /**
     * 
     * @param serialEquipo
     * @param marca 
     */
    private void cargandoSerialEquipoRuido(String serialEquipo, String marca) {
        System.out.println("Cargando datos equipo de RUIDO : Searial: " + serialEquipo + " Marca : " + marca);
        parametros.put("InstRuido", serialEquipo);
        parametros.put("MarcaRuido",marca);
    }
    
    /**
     * 
     * @param marca
     * @param pefD
     * @param serialEquipo
     * @param pef 
     */
    private void cargandoSerialEquipoGases(String marca,String pefD,String serialEquipo,String pef) 
    {
        System.out.println("Cargando datos equipo de Gases : Searial: " + serialEquipo + " Marca : " + marca);
        try 
        {
            EquiposJpaController1 controller1 = new EquiposJpaController1();
            String[] data = serialEquipo.split(";");
            if (data.length == 3) 
            {
                if (this.ctxHojaPrueba.getVehiculo().getTipoGasolina().getId() == 3) {//Diesel
                    parametros.put("PefGases", pef);
                } else {
                    parametros.put("PefGases", pefD.substring(0, 5));
                }
                parametros.put("InstGases", data[0]);
                parametros.put("MarcaGases",marca);
                
                Equipo CaptadorRpm = controller1.buscarSerialResolucionAmbental(data[1]);
                if (CaptadorRpm != null) 
                {
                    parametros.put("RpmSondaTemp", CaptadorRpm.getNomEquipo());
                    parametros.put("MarcaRpm", CaptadorRpm.getMarca());
                    parametros.put("InstRpm", CaptadorRpm.getResolucionambiental());
                }
                Equipo termoHigrometro = controller1.buscarSerialResolucionAmbental(data[2]);
                if (termoHigrometro!=null) 
                {
                    parametros.put("InstTermo", termoHigrometro.getResolucionambiental());
                    parametros.put("MarcaTermo", termoHigrometro.getMarca());
                }
            } else {
                Mensajes.mensajeError("Se esperaban 3 datos en el campos \n"
                        + "serialResolucion\n "
                        + " No se cargaran los datos de gases en \n  "
                        + " el FUR : Corregir en la db :  estrutura (xx;xxx;xxx).");
                System.out.println("Se esperaban 3 datos en el campos serialResolucion(2355;44444;4444)");
            }

        } catch (Exception e)
        {
            System.err.println("Error en el metodo : cargandoSerialEquipoGases()" + e.getMessage());
            System.err.println("Datos recibido en este metodo que arroja Error: " 
                    + "Marca "+ marca + "pefD "+ pefD + "serialEquipo "+ serialEquipo + "pef "+ pef);
        }

    }
    
    /**
     * 
     * @param serialEquipo
     * @param marca 
     */
    private void cargandoSerialEquipoTaximetro(String serialEquipo, String marca)
    {
        System.out.println("Cargando datos equipo de TAXIMETRO : Searial: " + serialEquipo + " Marca : " + marca);
        parametros.put("InstTax", serialEquipo);
        parametros.put("MarcaTax",marca);
    }
    
    /**
     * 
     * @return 
     */
    public static String ajustarValorMedida(String numero) 
    {
        
        boolean flag = true;
        int contador = 0;
        numero=numero.trim();
        while (flag) 
        {
            if (contador >= numero.length())
            {
                flag = false;
                continue;
            }

            if (!validarPuntoOcoma(numero))
            {
                int valorEntero=Integer.parseInt(numero);
                if(valorEntero>=0 &&  valorEntero<10)
                {
                    BigDecimal bd = new BigDecimal(numero);
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    decimalFormat.setRoundingMode(RoundingMode.DOWN);
                    numero=decimalFormat.format(bd.doubleValue()).replace(",",".");
                    
                }if (valorEntero>=10 && valorEntero<100) 
                {
                    double valor = Double.parseDouble(numero);
                    numero = "" + valor;  
                }
                flag = false;
                continue;
            }
            
            ///proceso para los enteros llegan con (,) como separador de miles
            if (numero.charAt(contador) == ',')
            {
                String recorte = numero.substring(0, contador);
                if (recorte.length() < 2 && numero.length() > 4)
                {
                    numero = numero.replace(",", "");
                    if (validarPuntoOcoma(numero)) 
                    {
                        continue;
                    }
                }
                
                if (recorte.length()== 2) 
                {
                    numero = formatearMedidas(numero.replace(",", "."));
                    flag = false;
                }
            }

            ///proceso para los  decimales llegan con (.) como separador 
            if (numero.charAt(contador) == '.') 
            {
                numero = formatearMedidas(numero.replace(",", "."));
                flag = false;
                continue;
            }
            contador++;
        }
        return numero;
    }
    
    
    private static boolean  validarPuntoOcoma(String num)
    {
        for (int i = 0; i < num.length(); i++) 
        {
            if (num.charAt(i) == '.' || num.charAt(i) == ',') 
            {
                return true;
            }
        }
        return  false;
    }
    
    private static int  validarPunto(String num)
    {
        int contador=0;
        for (int i = 0; i < num.length(); i++) 
        {
            if (num.charAt(i) == '.') 
            {
                contador++;
            }
        }
        return  contador;
    }
    
    
    /**
     * 
     * @param val
     * @return 
     */
    public static String formatearMedidas(String val)
    {

        System.out.println("------------------------");
        System.out.println("--------MEDIDA ORIGINAL : "+  val);
        System.out.println("-------------------------");
        try 
        { 
            if (val != null || !val.equalsIgnoreCase("")) 
            {
                if (validarPunto(val)==2) 
                {
                    val=val.replace(".",";");
                    String[] data = val.split(";");
                    if (data.length == 3) 
                    {
                        val = data[0] + data[1];
                    }
                }
            }

            BigDecimal bd = new BigDecimal(val);
            if (bd.doubleValue() < 10)
            {    
                DecimalFormat decimalFormat = new DecimalFormat();
                decimalFormat.applyPattern("0.00");
                decimalFormat.setRoundingMode(RoundingMode.DOWN);
                return decimalFormat.format(bd.doubleValue()).replace(",", ".");
                
            } else if (bd.doubleValue() >=10 && bd.doubleValue() < 100)
            {
                DecimalFormat decimalFormat = new DecimalFormat("#.0");
                decimalFormat.setRoundingMode(RoundingMode.DOWN);
                return decimalFormat.format(bd.doubleValue()).replace(",", ".");
            } else {
                DecimalFormat decimalFormat = new DecimalFormat("####");
                decimalFormat.setRoundingMode(RoundingMode.DOWN);
                return decimalFormat.format(bd.doubleValue());
            }
        } catch (Exception e) 
        {
            System.err.println("Error en el metodo ajustarValorMedida(): " + e.getMessage());
            System.err.println("valor recibido : " + val + " "+ e);
            return val;
        }
    }    
 
}
