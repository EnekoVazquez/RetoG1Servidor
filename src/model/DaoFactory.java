/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Eneko
 */
public class DaoFactory {

    /**
     * Devuelve una instancia de la implementación del Dao.
     *
     * @return Una instancia de la implementación del Dao.
     */
    public Sign getDao() {
        Sign sign;
        sign = (Sign) new DaoImplementacion();
        return sign;
    }

}
