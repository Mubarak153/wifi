package org.hdev.wifiwpspro;



public class MotdePasse {
    private final String nom_reseau;
    private final String mo_depasse_net;

    public MotdePasse(String nom_reseau, String mo_depasse_net) {
        this.nom_reseau = nom_reseau;
        this.mo_depasse_net = mo_depasse_net;
    }

    public String getNom_reseau() {
        return this.nom_reseau;
    }

    public String getMo_depasse_net() {
        return this.mo_depasse_net;
    }
}