package com.soltelec.consolaentrada.views;

import com.soltelec.consolaentrada.models.controllers.UsuarioJpaController;
import com.soltelec.consolaentrada.models.controllers.conexion.PersistenceController;
import com.soltelec.consolaentrada.models.entities.Usuario;
import com.soltelec.consolaentrada.models.entities.UsuarioLogueado;
import com.soltelec.consolaentrada.models.statics.LoggedUser;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.BusinessBlueSteelSkin;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gerencia Desarrollo de Soluciones Tecnologicas
 */
public class PanelLogin extends javax.swing.JFrame {

    private String pass, nombeUsuario;
    private boolean nombreCorrecto, passCorrecto;
    private List results;

    /**
     * Creates new form Frm_password
     */
    public PanelLogin() {
        initComponents();
        ponerLookAndFeel();
        setLocationRelativeTo(null);
        setTitle("Inicio Sesion");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        camUsuario = new javax.swing.JTextField();
        camContrasena = new javax.swing.JPasswordField();
        btn_ingresar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        btn_change_pswd = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Serif", 1, 19)); // NOI18N
        jLabel1.setText("ACCESO DE USUARIO PARA EL MODULO CLIENTE DEL SOFTWARE");

        jLabel2.setFont(new java.awt.Font("Serif", 1, 18)); // NOI18N
        jLabel2.setText("USUARIO:");

        jLabel3.setFont(new java.awt.Font("Serif", 1, 18)); // NOI18N
        jLabel3.setText("CONTRASEÑA:");

        camUsuario.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        camUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camUsuarioActionPerformed(evt);
            }
        });

        camContrasena.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        camContrasena.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camContrasenaActionPerformed(evt);
            }
        });

        btn_ingresar.setFont(new java.awt.Font("Serif", 1, 14)); // NOI18N
        btn_ingresar.setText("INGRESAR");
        btn_ingresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ingresarActionPerformed(evt);
            }
        });

        jSeparator1.setForeground(new java.awt.Color(0, 102, 255));

        jSeparator2.setForeground(new java.awt.Color(0, 102, 255));

        jButton1.setFont(new java.awt.Font("Serif", 1, 14)); // NOI18N
        jButton1.setText("SALIR");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jSeparator3.setForeground(new java.awt.Color(0, 102, 255));

        btn_change_pswd.setFont(new java.awt.Font("Serif", 1, 14)); // NOI18N
        btn_change_pswd.setText("CAMBIAR PWD");
        btn_change_pswd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_change_pswdActionPerformed(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/soltelec/consolaentrada/images/solt.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3))
                            .addGap(27, 27, 27)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(camContrasena)
                                .addComponent(camUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(156, 156, 156))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator3)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 632, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_ingresar, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_change_pswd, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_ingresar, jButton1});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(camUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(camContrasena, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_ingresar)
                    .addComponent(jButton1)
                    .addComponent(btn_change_pswd))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_ingresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ingresarActionPerformed

        //Captura de lo digitado en el campo de texto de la contrasena
        char p[] = camContrasena.getPassword();
        pass = new String(p);
        nombeUsuario = camUsuario.getText();
        //JOptionPane.showMessageDialog(null, "El nombre de usuario es: " +nombeUsuario +"\n La contrasena es: " +pass);
        EntityManager em = PersistenceController.getEntityManager();
        //em.getTransaction().begin(); 
        Usuario user = null;
        //Try catch para capturar nombre de usuario.
         nombreCorrecto = true; //El nombre de usuario coincide
        //Try catch para capturar la contrasena.
        try {
            Query queryPass = em.createQuery("SELECT u FROM Usuario u WHERE u.contrasena = :contrasena");
            queryPass.setParameter("contrasena", pass);
            results = queryPass.getResultList();
            user = (Usuario) results.get(0);
            LoggedUser.setIdUsuario(user.getUsuario());
            LoggedUser.setNick(user.getNick());
            LoggedUser.setAdministrador(user.getAdministrador().equals("Y"));
            if (user.getContrasena() == null ? pass == null : user.getContrasena().equals(pass)) {               
                passCorrecto = true;
                //02 12 2011 Verificacion de la fecha de validación de la contrasena
                Date fechaValidacion = user.getFechaValidacion();
                Query queryFechaHoy = em.createNativeQuery("SELECT NOW()");//traer la fecha desde el servidor
                Date fechaHoy = (Date) queryFechaHoy.getSingleResult();
                //Validar la Fecha
                if (contrasenaExpirada(fechaValidacion, fechaHoy)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    JOptionPane.showMessageDialog(null, "Debe Cambiar su contrasena:"
                            + "\n Fecha Expiracion : " + sdf.format(fechaValidacion));
                    return;
                }
                String nomTemp;
                boolean pos= user.getNick().startsWith(".");
                if (pos==true) {
                    nomTemp = user.getNick().substring(3, user.getNick().length());
                } else {
                    nomTemp = user.getNick();
                }
                if (nombeUsuario.equalsIgnoreCase(nomTemp)) {
                    nombreCorrecto = true;
                }else{
                    JOptionPane.showMessageDialog(null, "Disculpe Su Nombre de usuario  es INCORRECTO, por Favor vuelva a ingresarlo", "ERROR", JOptionPane.ERROR_MESSAGE);
                    nombreCorrecto = false;
                    camUsuario.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Su contrasena es incorrecta, vuelva a ingresarla Por Favor", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IndexOutOfBoundsException ex) {
            
            //esCorrecto = "incorrectos";
            camContrasena.setText("");
            passCorrecto = false; //Reiniciar variable en caso de que se vuelvan a pedir datos
        }

        if (nombreCorrecto && passCorrecto) {
            //TODO: Enviar a la base de datos la hora y la fecha a la que ingreso el usuario que se ha validado
            //No permitir el ingreso si la contrasena esta vencida
            //Abrir la siguiente ventana e indicando que PanelLogin es el padre.
            this.setVisible(false);
            this.dispose();
            ViewManager.getInstance().iniciar();
            if (user.getAdministrador().equalsIgnoreCase("A")) {
                ViewManager.aplicYellow = true;
            }
            UsuarioLogueado.setIdUsuario(user.getUsuario());
            UsuarioLogueado.setNick(user.getNick());
        }
    }//GEN-LAST:event_btn_ingresarActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void camUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camUsuarioActionPerformed
        btn_ingresarActionPerformed(evt);
    }//GEN-LAST:event_camUsuarioActionPerformed

    private void btn_change_pswdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_change_pswdActionPerformed
        //Validar que el usuario y la contrasena correspondan y despues mostrar un 
        char p[] = camContrasena.getPassword();
        pass = new String(p);
        nombeUsuario = camUsuario.getText();
        UsuarioJpaController ujc = new UsuarioJpaController();

        if (!validarUsuarioContrasena(nombeUsuario, pass, ujc)) {
            JOptionPane.showMessageDialog(null, "Para cambiar su contrasena ingrese "
                    + "\n su usuario y contrasena actual y presione CAMBIAR PWD");
            camUsuario.setText("");
            camContrasena.setText("");

        } else {
            PanelCambiarPassword dlg = new PanelCambiarPassword(this, true);
            dlg.setUjc(ujc);
            dlg.setNickUsuario(nombeUsuario);
            dlg.setVisible(true);
            //Dialogo para que ingrese la nueva contrasena
        }
    }//GEN-LAST:event_btn_change_pswdActionPerformed

    private void camContrasenaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camContrasenaActionPerformed
        btn_ingresarActionPerformed(evt);
    }//GEN-LAST:event_camContrasenaActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new PanelLogin().setVisible(true);
            }
        });
 }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_change_pswd;
    private javax.swing.JButton btn_ingresar;
    private javax.swing.JPasswordField camContrasena;
    private javax.swing.JTextField camUsuario;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables

    private void ponerLookAndFeel() {
        try {

            UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
            SubstanceLookAndFeel.setSkin(new BusinessBlueSteelSkin());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {

            System.out.println("Substance Nebula failed to initialize");

        }
    }

    /**
     *
     * @param fechaValidacion
     * @param fechaHoy fecha de hoy desde el servidor
     * @return
     */
    private boolean contrasenaExpirada(Date fechaValidacion, Date fechaHoy) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("Fecha de validacion: " + sdf.format(fechaValidacion));
        System.out.println("Fecha de Hoy: " + sdf.format(fechaHoy));
        Calendar calendarValidacion = Calendar.getInstance();
        calendarValidacion.setTime(fechaValidacion);
        Calendar calendarHoy = Calendar.getInstance();
        calendarHoy.setTime(fechaHoy);
        return calendarValidacion.before(calendarHoy);

    }

    private boolean validarUsuarioContrasena(String nombeUsuario, String pass, UsuarioJpaController ujc) {
        Usuario usuario = ujc.getUsuarioByNick(nombeUsuario);
        if (usuario != null) {
            return usuario.getContrasena().equals(pass);
        } else {
            return false;
        }
    }

}
