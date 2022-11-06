package models.controllers;

import models.entities.CargaDeActividades.entidades.Periodo;
import models.entities.calculoHC.CalculoHC;
import models.entities.calculoHC.UnidadHC;
import models.entities.organizaciones.contacto.Contacto;
import models.entities.organizaciones.contacto.MandarMail;
import models.entities.organizaciones.contacto.MandarWhatsapp;
import models.entities.organizaciones.contacto.MedioNotificacion;
import models.entities.organizaciones.entidades.Organizacion;
import models.entities.organizaciones.solicitudes.EstadoSolicitud;
import models.entities.organizaciones.solicitudes.PosibleEstadoSolicitud;
import models.entities.organizaciones.solicitudes.Solicitud;
import models.helpers.PeriodoHelper;
import models.helpers.PersistenciaHelper;
import models.helpers.SessionHelper;
import models.helpers.threads.FileHandlerThread;
import models.repositories.RepositorioDeContactos;
import models.repositories.RepositorioDeOrganizaciones;
import models.repositories.RepositorioDeParametrosFE;
import models.repositories.RepositorioDeSolicitudes;
import models.repositories.factories.FactoryRepositorioDeContactos;
import models.repositories.factories.FactoryRepositorioDeOrganizaciones;
import models.repositories.factories.FactoryRepositorioDeParametrosFE;
import models.repositories.factories.FactoryRepositorioDeSolicitudes;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OrganizacionController {
    //TODO provincias, municipios, orgs, sectores, medios t que llege una lista, que no escriba a mano
    private RepositorioDeOrganizaciones repoOrganizaciones;
    private RepositorioDeParametrosFE repoFE;
    private RepositorioDeSolicitudes repoSolicitudes;
    private RepositorioDeContactos repoContactos;

    public OrganizacionController() {
        this.repoOrganizaciones = FactoryRepositorioDeOrganizaciones.get();
        this.repoFE = FactoryRepositorioDeParametrosFE.get();
        this.repoSolicitudes = FactoryRepositorioDeSolicitudes.get();
        this.repoContactos = FactoryRepositorioDeContactos.get();
    }


    public ModelAndView mostrar(Request request, Response response){
        HashMap<String,Object> parametros = new HashMap<>();
        Organizacion org = this.obtenerOrganizacion(request,response);
        parametros.put("organizacion",org);

        return new ModelAndView(parametros, "organizacion/organizacion-menu.hbs");
    }

    public ModelAndView mostrarVinculaciones(Request request, Response response){
        HashMap<String,Object> parametros = new HashMap<>();
        Organizacion org = this.obtenerOrganizacion(request,response);
        parametros.put("organizacion",org);
        parametros.put("solicitudes",org.getListaDeSolicitudes());
        parametros.put("solicitudesRender",org.getListaDeSolicitudes().stream()
                .map(s -> s.getEstadoSolicitud().getPosibleEstadoSolicitud() == PosibleEstadoSolicitud.PENDIENTE)
                .collect(Collectors.toList()));

        return new ModelAndView(parametros, "organizacion/solicitudes-organizacion-menu.hbs"); //aceptar esa solicitud en concreto
    }

    public ModelAndView mostrarMedicion(Request request, Response response) {
        HashMap<String, Object> parametros = new HashMap<>();
        Organizacion org = this.obtenerOrganizacion(request, response);
        parametros.put("organizacion", org); //todo cargar las mediciones
        parametros.put("mediciones",org.getListaDeActividades());
        return new ModelAndView(parametros, "organizacion/mediciones-menu.hbs");
    }

    private Organizacion obtenerOrganizacion(Request request, Response response){
        if(this.repoOrganizaciones.existe(new Integer(request.session().attribute("resource_id").toString()))){
            return this.repoOrganizaciones.buscar(new Integer(request.session().attribute("resource_id").toString()));
        } else{
            response.redirect("/error");
            Spark.halt();
        }
        return null;
    }

    public ModelAndView mostrarReportes(Request request, Response response) { //esto es lo de cargar excel
        return new ModelAndView(new HashMap<String,Object>(),"organizacion/reportes-menu.hbs"); //mostrar los botones y al tocar dice el valor
    }

    public ModelAndView mostrarHC(Request request, Response response){
        HashMap<String, Object> parametros = new HashMap<>();
        //agregar razon social
        return new ModelAndView(new HashMap<String,Object>(),"organizacion/calculadora-organizacion.hbs");
    }

    public ModelAndView calcularHC(Request request, Response response){ //NO LLEGA ACA SI ES TOTAL
        this.setearCalculadoraHC();
//        if(request.queryParams("mes") == null || request.queryParams("anio") == null){ //todo hacer un boton para calcular total
//            throw new RuntimeException("No se ingreso mes o año");
//        }
        Periodo periodo = PeriodoHelper.nuevoPeriodo(request.queryParams("mes"),request.queryParams("anio"));
        HashMap<String, Object> parametros = new HashMap<>();

        Organizacion org = this.obtenerOrganizacion(request, response);
        parametros.put("consumoActividad",org.calcularHCActividadesEnPeriodo(periodo));
        parametros.put("consumoTrabajador",org.calcularHCEmpleados(periodo));
        parametros.put("factorEmision",CalculoHC.getUnidadPorDefectoString());
        parametros.put("huellaCarbono",org.calcularHCEnPeriodo(periodo));

        return new ModelAndView(parametros,"organizacion/calculadora-organizacion.hbs");
    }

    public ModelAndView calcularCalculadoraHCTotal(Request request, Response response){
        this.setearCalculadoraHC();
        HashMap<String, Object> parametros = new HashMap<>();
        Organizacion org = this.obtenerOrganizacion(request, response);

        parametros.put("consumoActividad",org.calcularHCTotalActividades());
        parametros.put("consumoTrabajador",org.calcularHCTotalTrabajadores());
        parametros.put("factorEmision",CalculoHC.getUnidadPorDefectoString());
        parametros.put("huellaCarbono",org.calcularHCTotal());

        return new ModelAndView(parametros,"organizacion/calculadora-organizacion.hbs");
    }

    private void setearCalculadoraHC(){
        CalculoHC.setFactoresEmisionFE(this.repoFE.buscarTodos());
        CalculoHC.setUnidadPorDefecto(UnidadHC.GRAMO_EQ);
    }

    public ModelAndView mostrarRecomendaciones(Request request, Response response) {
        return new ModelAndView(new HashMap<String,Object>(),"recomendaciones.hbs"); //dependen del tipo de cuenta?
    }

    public ModelAndView mostrarNuevaMedicion(Request request, Response response) {
        return new ModelAndView(new HashMap<String,Object>(),"organizacion/subir-archivo-menu.hbs");
    }

    public Response registrarNuevaMedicion(Request request, Response response) {

        //VIENE DEL EXCEL, todo ajax/jquery
        Organizacion org = this.obtenerOrganizacion(request,response);
        FileHandlerThread thread = new FileHandlerThread("ruta/pendiente",org.getId());
        thread.start();

        response.redirect("/organizacion/mediciones");
        return response;
    }

    public Response aceptarVinculacion(Request request, Response response){
        Solicitud solicitud = this.obtenerSolicitud(request,response);
        solicitud.aceptarSolicitud();
        PersistenciaHelper.persistir(solicitud);
        response.redirect("/organizacion/vinculaciones");
        return response;
    }
    public Response rechazarVinculacion(Request request, Response response){
        Solicitud solicitud = this.obtenerSolicitud(request,response);
        solicitud.rechazarSolicitud();
        PersistenciaHelper.persistir(solicitud);
        response.redirect("/organizacion/vinculaciones");
        return response;
    }

    private Solicitud obtenerSolicitud(Request request, Response response){
//        Organizacion organizacion = this.obtenerOrganizacion(request,response);
//        Sector sector = organizacion.obtenerSectorPorNombre(request.queryParams("nombreSector")); //TODO que le pase la sol Id de una -> repoSolicitudes
//        return sector.getSolicitudes().stream().filter(sol -> sol.getId() == new Integer(request.queryParams("solicitudId"))).collect(Collectors.toList()).get(0);
        return this.repoSolicitudes.buscar(new Integer(request.queryParams("solicitudId")));
    }

    public ModelAndView mostrarContactos(Request request, Response response){
        HashMap<String, Object> parametros = new HashMap<>();
        Organizacion organizacion = this.obtenerOrganizacion(request,response);
        parametros.put("contactos",organizacion.getContactos());
        return new ModelAndView(parametros,"organizacion/contactos-menu.hbs");
    }

    public ModelAndView mostrarNuevoContacto(Request request, Response response){
        return new ModelAndView(null,"organizacion/contacto-nuevo-menu.hbs");
    }

    public Response registrarNuevoContacto(Request request, Response response){
        if(SessionHelper.atributosNoSonNull(request,"nroTelefono","email","medioNotificacion")){
            Contacto nuevoContacto = new Contacto(request.queryParams("nroTelefono"),request.queryParams("email"), this.getMedioDeNotificacionDeRequest(request));
            Organizacion org = this.obtenerOrganizacion(request,response);
            org.agregarContacto(nuevoContacto);
            PersistenciaHelper.persistir(org);
        }
        return response;
    }

    public ModelAndView mostrarEditarContacto(Request request, Response response){
        return new ModelAndView(null,"organizacion/contacto-editar-menu.hbs");
    }

    public Response editarContacto(Request request, Response response){
        Contacto contactoAEditar = this.repoContactos.buscar(new Integer(request.queryParams("contactoId")));
        if(SessionHelper.atributosNoSonNull(request,"nroTelefono")){
            contactoAEditar.setNroTelefono(request.queryParams("nroTelefono"));
        }
        if(SessionHelper.atributosNoSonNull(request,"email")){
            contactoAEditar.setEmail(request.queryParams("email"));
        }
        contactoAEditar.setListaDeMedios(this.getMedioDeNotificacionDeRequest(request)); //es un desplegable
        return response;
    }

    private List<MedioNotificacion> getMedioDeNotificacionDeRequest(Request request){
        List<MedioNotificacion> medios = new ArrayList<>();
        switch (request.queryParams("medioNotificacion")){
            case "MAIL":
                medios.add(new MandarMail());
                break;
            case "WHATSAPP":
                medios.add(new MandarWhatsapp());
                break;
            case "AMBOS":
                medios.add(new MandarWhatsapp());
                medios.add(new MandarMail());
                break;
            default:
                break;
        }
        return medios;
    }

    public Response eliminarContacto(Request request, Response response) {
        Contacto contactoAEliminar = this.repoContactos.buscar(new Integer(request.queryParams("contactoId")));
        Organizacion organizacion = this.obtenerOrganizacion(request,response);
        organizacion.eliminarContacto(contactoAEliminar);
        PersistenciaHelper.persistir(organizacion);
        response.redirect("/contactos");
        return response;
    }
}
