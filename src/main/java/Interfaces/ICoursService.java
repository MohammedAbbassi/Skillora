package Interfaces;

import entities.Cours;
import services.IService;

import java.sql.SQLException;

/**
 * Contrat CRUD pour les cours : API générique ({@link IService})
 * et méthodes en français ({@link InterfaceCoursChap}).
 */
public interface ICoursService extends IService<Cours>, InterfaceCoursChap<Cours> {

    Cours getById(int id) throws SQLException;
}
