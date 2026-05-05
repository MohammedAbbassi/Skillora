package com.example.interfaces;

import java.util.List;

/**
 * Interface générique définissant les opérations CRUD
 * partagées par les services Cours et Chapitre.
 *
 * @param <T>  Type de l'entité (Cours ou Chapitre)
 * @param <ID> Type de l'identifiant (Integer)
 */
public interface ICoursChapitreService<T, ID> {

    /**
     * Ajoute une nouvelle entité.
     *
     * @param entity L'entité à persister
     * @return L'entité persistée (avec son id généré)
     */
    T ajouter(T entity);

    /**
     * Met à jour une entité existante.
     *
     * @param entity L'entité avec les nouvelles valeurs
     * @return L'entité mise à jour
     */
    T modifier(T entity);

    /**
     * Supprime une entité par son identifiant.
     *
     * @param id Identifiant de l'entité à supprimer
     */
    void supprimer(ID id);

    /**
     * Recherche une entité par son identifiant.
     *
     * @param id Identifiant recherché
     * @return L'entité trouvée, ou {@code null} si absente
     */
    T trouverParId(ID id);

    /**
     * Retourne la liste complète des entités.
     *
     * @return Liste de toutes les entités
     */
    List<T> trouverTous();
}
