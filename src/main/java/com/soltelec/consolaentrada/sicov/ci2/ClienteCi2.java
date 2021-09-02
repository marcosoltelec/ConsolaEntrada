/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soltelec.consolaentrada.sicov.ci2;

import com.soltelec.consolaentrada.models.controllers.HojaPruebasJpaController;
import com.soltelec.consolaentrada.models.entities.AuditoriaSicov;
import com.soltelec.consolaentrada.models.entities.RespuestaDTO;
import com.soltelec.consolaentrada.reporte.ListenerEnvioFUR;
import com.soltelec.consolaentrada.utilities.GenerarArchivoTramaCI2;
import java.rmi.RemoteException;
import java.util.List;
import javax.xml.rpc.ServiceException;

/**
 *
 * @author GerenciaDesarrollo
 */
public class ClienteCi2 {

    private String urlSicov2;
    private String urlSicovEncript;
    private String urlSicov;
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ClienteCi2.class);

    public ClienteCi2(String urlSicov) {
        try {
            this.urlSicov = urlSicov;
        } catch (Exception e) {
        }
    }

    public String getUrlSicov2() {
        return urlSicov2;
    }

    public void setUrlSicov2(String urlSicov2) {
        this.urlSicov2 = urlSicov2;
    }

    public String getUrlSicovEncript() {
        return urlSicovEncript;
    }

    public void setUrlSicovEncript(String urlSicovEncript) {
        this.urlSicovEncript = urlSicovEncript;
    }

    public RespuestaDTO enviarFur(Formulario_v2 formulario) 
    {
        System.out.println("--------------------------------------------------");
        System.out.println("---------------  Enviar Fur     ------------------");
        
        try 
        { 
            GenerarArchivoTramaCI2.setFur(formulario.toString());
            System.out.println("TRAMA DEL FUR PARA CI2 \n" + formulario.toString());
            DatosSoap stub = new DatosLocator(urlSicov).getdatosSoap();
            Resultado resultado = stub.ingresar_fur_v2(formulario);
            logger.info("Fur enviado ° " + formulario.getP_fur_aso());
        System.out.println("--------------------------------------------------");
            return new RespuestaDTO(resultado.getCodigoRespuesta(), resultado.getMensajeRespuesta());
        } catch (RemoteException | ServiceException e) {
            logger.error("Error al enviar el fur", e);
            //throw new SartComunicadorException(500, e.getMessage());
        }
        return null;
    }

    public RespuestaDTO utilizarPin(Pin pin) {
        try {
            logger.info("Metodo utilizar pin url de servicio: " + urlSicov);
            DatosSoap stub = new DatosLocator(urlSicov).getdatosSoap();
            Resultado resultado = stub.utilizar_pin(pin);
            logger.info("Resultado de ejecutar pin: " + resultado);
            return new RespuestaDTO(resultado.getCodigoRespuesta(), resultado.getMensajeRespuesta());
        } catch (RemoteException | ServiceException e) {
            logger.error("Error al iniciar el pin ", e);
            return null;
        }

    }

    public RespuestaDTO consultarPinPlaca(Pin pin) {
        try {
            DatosSoap stub = new DatosLocator(this.urlSicov).getdatosSoap();
            Resultado resultado = stub.consulta_pin_placa(pin);
            return new RespuestaDTO(resultado.getCodigoRespuesta(), resultado.getMensajeRespuesta());
        } catch (ServiceException | RemoteException e) {
            logger.error("Error al consultar el pin", e);
            return null;
        }
    }

    public String getUrlSicov() {
        return urlSicov;
    }

    public static void main(String[] args) {
        HojaPruebasJpaController hpControler = new HojaPruebasJpaController();
        List<AuditoriaSicov> lstTramas = hpControler.recogerTramasExist(hpControler.find(233015));
        ListenerEnvioFUR listenerEnvioFUR = new ListenerEnvioFUR(233015L, lstTramas);
        listenerEnvioFUR.actionPerformed(null);
//        Pin pin = new Pin();
//        pin.setClave("f46839");
//        pin.setP_pin("1606202000140");
//        pin.setP_placa("HUX222");
//        pin.setUsuario("soltelecpruebas");
//        new ClienteCi2("http://186.116.8.156:9090/ci2_cda_ws/sincrofur.asmx?WSDL").utilizarPin(pin);
    }

}
