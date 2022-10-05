package db;

import models.entities.geoDDS.Direccion;
import models.entities.geoDDS.entidades.Localidad;
import models.entities.geoDDS.entidades.Municipio;
import models.entities.geoDDS.entidades.Pais;
import models.entities.geoDDS.entidades.Provincia;
import models.entities.organizaciones.entidades.*;
import models.entities.organizaciones.solicitudes.Solicitud;
import models.entities.seguridad.cuentas.Permiso;
import models.entities.seguridad.cuentas.Rol;
import models.entities.seguridad.cuentas.TipoRecurso;
import models.entities.seguridad.cuentas.Usuario;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PersistenciaTest {

    @Test
    @DisplayName("Se persiste una organizacion")


    public void organizacionPersistir(){
        List<Sector> sectores = new ArrayList<>();
        Sector marketing = new Sector();
        marketing.nombreSector = "Marketing";
        sectores.add(marketing);
        List<String> clasificaciones = new ArrayList<>();
        clasificaciones.add("Videojuegos");
        clasificaciones.add("Desarrollador");

        Trabajador juan = new Trabajador("Fernandez","Juan", TipoDoc.DNI, 12345678);

        List<Trabajador> trabajadoresA = new ArrayList<>();
        trabajadoresA.add(juan);

        Pais pais = new Pais(1,"A");
        Provincia provincia = new Provincia(1,"A",pais);
        Municipio municipio = new Municipio(1,"A",provincia);

        Localidad localidad = new Localidad(1,"A",1,municipio);


        Direccion direccion1 = new Direccion(100,"Rivadavia",localidad);

        Organizacion organizacion = new Organizacion(clasificaciones,trabajadoresA,
                "Valve Corporation S.A",sectores, TipoOrganizacion.EMPRESA,direccion1);

        List<Solicitud> listaSolicitudes = new ArrayList<>();
        Solicitud sol = new Solicitud(marketing,juan);
        //setearle
        listaSolicitudes.add(sol);
        organizacion.setListaDeSolicitudes(listaSolicitudes);

        EntityManagerHelper.beginTransaction();
        EntityManagerHelper.getEntityManager().persist(pais);
        EntityManagerHelper.getEntityManager().persist(provincia);
        EntityManagerHelper.getEntityManager().persist(municipio);
        EntityManagerHelper.getEntityManager().persist(localidad);
        EntityManagerHelper.getEntityManager().persist(sol);
        EntityManagerHelper.getEntityManager().persist(marketing);
        EntityManagerHelper.getEntityManager().persist(juan);
        EntityManagerHelper.getEntityManager().persist(organizacion);
        EntityManagerHelper.commit();
    }

    @Test
    @DisplayName("Se recupera una organizacion")
    public void organizacionRecuperar(){
        Organizacion org = (Organizacion) EntityManagerHelper
                .createQuery("FROM Organizacion WHERE razon_social = 'Valve Corporation S.A'").getSingleResult(); //HQL = Hibernate Query Language (pseudo SQL)
                //importa el nombre de la clase y de los atributos, no me importa el nombre de la columna
        Assertions.assertEquals("Valve Corporation S.A", org.getRazonSocial());
    }

    //para listas .getResultList(). count: getMaxResult()
    //find(Servicio.class, 1) -> el id
    //todo parametrizar consultas al entityManager

    @Test
    @DisplayName("Se persiste un usuario")
    public void usuarioPersistir(){

        Usuario user = new Usuario("juancito","123456789",Rol.BASICO,1, TipoRecurso.ORGANIZACION,Permiso.VER_ORGANIZACION);
        EntityManagerHelper.beginTransaction();
        EntityManagerHelper.persist(user);
        EntityManagerHelper.commit();
    }

    @AfterAll
    public static void close(){
        EntityManagerHelper.closeEntityManager();
        EntityManagerHelper.closeEntityManagerFactory();
    }



}
