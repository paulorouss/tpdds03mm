package models.entities.transporte.publico;

import models.entities.geoDDS.Direccion;
import models.entities.geoDDS.entidades.Distancia;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;

@Entity
@Table(name = "linea")
public class Linea {
    @Id
    @GeneratedValue
    private int id;

    @Column(name = "nombre_linea",nullable = false)
    private String nombreDeLinea;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "linea_id",referencedColumnName = "id")
    private final List<Parada> paradas=new ArrayList<>(); //tomamos que la linea no comparte paradas con otras lineas

    public int getId() {
        return id;
    }

    public Linea() {
    }

    public Linea(String nombreDeLinea, Parada ... variasParadas) {
        this.nombreDeLinea = nombreDeLinea;
        paradas.addAll(Arrays.asList(variasParadas));

    }


    public Distancia calcularDistanciaEntreParadas(Parada unaParada, Parada otraParada) throws Exception {
        int indiceParadaUno = paradas.indexOf(unaParada);
        int indiceParadaDos = paradas.indexOf(otraParada);
        if(indiceParadaUno == -1 || indiceParadaDos == -1){
            throw new Exception("Una de las paradas ingresadas no se encontro");
        }
        if(indiceParadaUno<indiceParadaDos){
            return distanciaEntreParadaAnteriorYSiguiente(unaParada,otraParada);
        }else{
            return distanciaEntreParadaAnteriorYSiguiente(otraParada,unaParada);
        }

    }

    private Distancia distanciaEntreParadaAnteriorYSiguiente(Parada anterior, Parada siguiente){
        Distancia distanciaTotal = new Distancia(0.0,"KM");
        Parada p = anterior;
        while (p != siguiente && p != null){
            distanciaTotal.valor += p.distanciaSiguiente.valor;
            p = p.paradaSiguiente;
        }
        return distanciaTotal;
    }

    private Parada buscarParadaDadaDireccion(Direccion direccion) throws Exception {
        Optional<Parada> posibleParada = this.paradas.stream().filter(p -> p.direccion == direccion).findFirst();
        if(posibleParada.isPresent()){
            return posibleParada.get();
        }else{
            throw new Exception("No existe parada en esa direccion");
        }
    }


    public String getNombreDeLinea() {
        return nombreDeLinea;
    }

    public void setNombreDeLinea(String nombreDeLinea) {
        this.nombreDeLinea = nombreDeLinea;
    }

    public List<Parada> getParadas() {
        return paradas;
    }

    public void setParadas(Parada ... paradas) {
        this.paradas.addAll(Arrays.asList(paradas));
    }

    public String detallePrimerParada() {
        return paradas.get(0).direccion.getCalle() + " " + paradas.get(0).direccion.getAltura();
    }

    public String detalleUltimaParada() {
        return paradas.get(paradas.size()-1).direccion.getCalle() + " " + paradas.get(paradas.size()-1).direccion.getAltura();
    }


    public Distancia calcularDistancia(Direccion origen, Direccion destino) throws Exception {
        Parada paradaInicio = this.buscarParadaDadaDireccion(origen);
        Parada paradaFinal = this.buscarParadaDadaDireccion(destino);
        return this.calcularDistanciaEntreParadas(paradaInicio,paradaFinal);
    }
}
