package models.controllers;

import models.repositories.RepositorioDeAgentesSectoriales;
import models.repositories.factories.FactoryRepositorioDeAgentesSectoriales;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;

public class AgenteSectorialController {

    RepositorioDeAgentesSectoriales repo = FactoryRepositorioDeAgentesSectoriales.get();

    public ModelAndView mostrar(Request request, Response response){
        HashMap<String,Object> parametros = new HashMap<>();
        parametros.put("agente",this.repo.buscar(request.session().attribute("resource_id")));
        return new ModelAndView(parametros, "agente/agente-menu.hbs");
    }

    public ModelAndView mostrarRecomendaciones(Request request, Response response) {
        return new ModelAndView(new HashMap<String,Object>(),"recomendaciones.hbs"); //dependen del tipo de cuenta?
    }

    public ModelAndView mostrarReportes(Request request, Response response) {
        return new ModelAndView(new HashMap<String,Object>(),"agente/reportes-menu.hbs");
    }
}
