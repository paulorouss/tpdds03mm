package models.repositories;

import db.EntityManagerHelper;
import models.repositories.daos.DAO;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

public class Repositorio<T>{
    protected DAO<T> dao;

    public Repositorio(DAO<T> dao) {
        this.dao = dao;
    }

    public void setDao(DAO<T> dao) {
        this.dao = dao;
    }

    public void agregar(Object unObjeto){
        this.dao.agregar(unObjeto);
    }

    public void modificar(Object unObjeto){
        this.dao.modificar(unObjeto);
    }

    public void eliminar(Object unObjeto){
        this.dao.eliminar(unObjeto);
    }

    public List<T> buscarTodos(){
        return this.dao.buscarTodos();
    }

    public T buscar(int id){
        return this.dao.buscar(id);
    }

    public Boolean existe(int id){
        return this.dao.buscar(id) != null;
    }

    public CriteriaBuilder criteriaBuilder(){
        return EntityManagerHelper.getEntityManager().getCriteriaBuilder();
    }
}
