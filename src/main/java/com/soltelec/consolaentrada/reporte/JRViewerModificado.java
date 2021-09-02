/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soltelec.consolaentrada.reporte;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;

import java.awt.event.ActionListener;

/**
 *
 * @author GerenciaDesarrollo
 */
public class JRViewerModificado extends JRViewer {   
    
    public JRViewerModificado(JasperPrint jrPrint) {
        super(jrPrint);        
    }  
    
    public void setListenerImprimir(ActionListener listener){
        btnPrint.addActionListener(listener);
    }
        
}
