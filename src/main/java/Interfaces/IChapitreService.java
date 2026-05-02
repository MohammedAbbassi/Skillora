package Interfaces;

import entities.Chapitre;
import services.IService;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrat CRUD pour les chapitres, avec filtrage par cours.
 */
public interface IChapitreService extends IService<Chapitre>, InterfaceCoursChap<Chapitre> {

    Chapitre getById(int id) throws SQLException;

    List<Chapitre> getByCours(int coursId) throws SQLException;
}
