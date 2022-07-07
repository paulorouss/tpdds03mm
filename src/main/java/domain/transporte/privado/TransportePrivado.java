package domain.transporte.privado;

import domain.geoDDS.Direccion;
import domain.geoDDS.ServicioCalcularDistancia;
import domain.geoDDS.entidades.Distancia;
import domain.organizaciones.TramoCompartido;
import domain.organizaciones.Trabajador;
import domain.transporte.CalcularHCTransporte;
import domain.transporte.MedioTransporte;
import domain.transporte.TipoCombustible;

import java.io.IOException;

public class TransportePrivado implements MedioTransporte {
    TipoVehiculo tipo;
    TipoCombustible tipoCombustible;

    TramoCompartido tramoCompartido = new TramoCompartido();

    public TransportePrivado(TipoVehiculo tipo, TipoCombustible tipoCombustible) {
        this.tipo = tipo;
        this.tipoCombustible = tipoCombustible;
    }


    public String detalle() {
        return "Tipo de vehiculo:" + tipo.toString() + " - " + "Tipo de combustible: " + tipoCombustible.toString();
    }


    public Distancia calcularDistancia(Direccion origen, Direccion destino) throws IOException {
        return ServicioCalcularDistancia.getInstance().distanciaEntre(origen, destino);
    }

    public Double getConsumoPorKM(){
        return CalcularHCTransporte.getConsumosPorKm().get(this.tipo);
    }

    public Double calcularHC(Distancia distancia) {
        return this.getConsumoPorKM() * distancia.valor / tramoCompartido.cantidadDeTrabajadores();
    }

    public void agregarTrabajadorATramoCompartido(Trabajador trabajador) throws Exception {
        tramoCompartido.validarTrabajador(trabajador);
    }

}
