/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soltelec.consolaentrada.indra.dto;

/**
 *
 * @author GerenciaDesarrollo
 */
public class DatosGasesDiesel extends DatosUsuarioPrueba {

    private String temperatura = "";
    private String rpmRalenti = "";
    private String velocidadGobernada0 = "";
    private String velocidadGobernada1 = "";
    private String velocidadGobernada2 = "";
    private String velocidadGobernada3 = "";
    private String humedadRelativa = "";
    private String rpmGobernada = "";
    private String aceleracion0 = "";
    private String aceleracion1 = "";
    private String aceleracion2 = "";
    private String aceleracion3 = "";
    private String valorFinal = "";
    private String temperaturaInicial = "";
    private String temperaturaFinal = "";
    private String LTOEStandar = "";

    /**
     * @return the temperatura
     */
    public String getTemperatura() {
        return temperatura;
    }

    /**
     * @param temperatura the temperatura to set
     */
    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    /**
     * @return the rpmGobernada
     */
    public String getRpmGobernada() {
        return rpmGobernada;
    }

    /**
     * @param rpmGobernada the rpmGobernada to set
     */
    public void setRpmGobernada(String rpmGobernada) {
        this.rpmGobernada = rpmGobernada;
    }

    /**
     * @return the aceleracion0
     */
    public String getAceleracion0() {
        return aceleracion0;
    }

    /**
     * @param aceleracion0 the aceleracion0 to set
     */
    public void setAceleracion0(String aceleracion0) {
        this.aceleracion0 = aceleracion0;
    }

    /**
     * @return the aceleracion1
     */
    public String getAceleracion1() {
        return aceleracion1;
    }

    /**
     * @param aceleracion1 the aceleracion1 to set
     */
    public void setAceleracion1(String aceleracion1) {
        this.aceleracion1 = aceleracion1;
    }

    /**
     * @return the aceleracion2
     */
    public String getAceleracion2() {
        return aceleracion2;
    }

    /**
     * @param aceleracion2 the aceleracion2 to set
     */
    public void setAceleracion2(String aceleracion2) {
        this.aceleracion2 = aceleracion2;
    }

    /**
     * @return the aceleracion3
     */
    public String getAceleracion3() {
        return aceleracion3;
    }

    /**
     * @param aceleracion3 the aceleracion3 to set
     */
    public void setAceleracion3(String aceleracion3) {
        this.aceleracion3 = aceleracion3;
    }

    /**
     * @return the valorFinal
     */
    public String getValorFinal() {
        return valorFinal;
    }

    /**
     * @param valorFinal the valorFinal to set
     */
    public void setValorFinal(String valorFinal) {
        this.valorFinal = valorFinal;
    }

    public String getRpmRalenti() {
        return rpmRalenti;
    }

    public void setRpmRalenti(String rpmRalenti) {
        this.rpmRalenti = rpmRalenti;
    }

    public String getVelocidadGobernada0() {
        return velocidadGobernada0;
    }

    public void setVelocidadGobernada0(String velocidadGobernada0) {
        this.velocidadGobernada0 = velocidadGobernada0;
    }

    public String getVelocidadGobernada1() {
        return velocidadGobernada1;
    }

    public void setVelocidadGobernada1(String velocidadGobernada1) {
        this.velocidadGobernada1 = velocidadGobernada1;
    }

    public String getVelocidadGobernada2() {
        return velocidadGobernada2;
    }

    public void setVelocidadGobernada2(String velocidadGobernada2) {
        this.velocidadGobernada2 = velocidadGobernada2;
    }

    public String getVelocidadGobernada3() {
        return velocidadGobernada3;
    }

    public void setVelocidadGobernada3(String velocidadGobernada3) {
        this.velocidadGobernada3 = velocidadGobernada3;
    }

    public String getHumedadRelativa() {
        return humedadRelativa;
    }

    public void setHumedadRelativa(String humedadRelativa) {
        this.humedadRelativa = humedadRelativa;
    }

    public String getTemperaturaInicial() {
        return temperaturaInicial;
    }

    public void setTemperaturaInicial(String temperaturaInicial) {
        this.temperaturaInicial = temperaturaInicial;
    }

    public String getTemperaturaFinal() {
        return temperaturaFinal;
    }

    public void setTemperaturaFinal(String temperaturaFinal) {
        this.temperaturaFinal = temperaturaFinal;
    }

    public String getLTOEStandar() {
        return LTOEStandar;
    }

    public void setLTOEStandar(String LTOEStandar) {
        this.LTOEStandar = LTOEStandar;
    }
    
    


    @Override
    public String toString() {
        return operarioGases
                + ";" + noIdentificacionOpGases
                + ";" + temperatura
                + ";" + rpmRalenti
                + ";" + velocidadGobernada0
                + ";" + velocidadGobernada1
                + ";" + velocidadGobernada2
                + ";" + velocidadGobernada3
                + ";" + aceleracion0
                + ";" + aceleracion1
                + ";" + aceleracion2
                + ";" + aceleracion3
                + ";" + valorFinal
                + ";" + temperaturaInicial
                + ";" + temperaturaFinal
                + ";" + humedadRelativa
                + ";" + LTOEStandar;
    }

        public String toString1() {
        return "DatosGasesDiesel{" + "operarioGases=" + operarioGases + "noIdentificacionOpGases=" + noIdentificacionOpGases + "temperatura=" + temperatura + ", rpmRalenti=" + rpmRalenti + ", rpmGobernada=" + rpmGobernada + ", aceleracion0=" + aceleracion0 + ", aceleracion1=" + aceleracion1 + ", aceleracion2=" + aceleracion2 + ", aceleracion3=" + aceleracion3 + ", velocidadGobernada0=" + velocidadGobernada0 + ", velocidadGobernada1=" + velocidadGobernada1 + ", velocidadGobernada2=" + velocidadGobernada2 + ", velocidadGobernada3=" + velocidadGobernada3 + ", valorFinal=" + valorFinal + ", temperaturaInicial=" + temperaturaInicial + ", temperaturaFinal=" + temperaturaFinal + ", LTOEStandar=" + LTOEStandar  + '}';
    }

}
