package test.domain.CargaDeActividades;

import models.entities.CargaDeActividades.adapters.CargaDeActividadesApachePOIAdapter;
import models.entities.CargaDeActividades.entidades.*;
import models.entities.calculoHC.CalculoHC;
import models.entities.calculoHC.UnidadHC;
import models.entities.parametros.ParametroFE;
import models.entities.transporte.privado.TipoVehiculo;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CargaDeActividadesApachePOIAdapterTest {
    CargaDeActividadesApachePOIAdapter adapterTest;
    HSSFSheet hojaLeida;
    Actividad actividadTest, actividadTestCompuesta;



    @BeforeEach
    public void init() throws IOException {

        ParametroFE autoFE = new ParametroFE(TipoVehiculo.AUTO.toString(),0.2);
        ParametroFE gasFE = new ParametroFE(TipoDeConsumo.GAS_NATURAL.toString(),0.6);
        ParametroFE camionCarga = new ParametroFE(TipoDeConsumo.CAMION_CARGA.toString(),2.0);
        List<ParametroFE> parametrosFE = new ArrayList<>();
        parametrosFE.add(autoFE);
        parametrosFE.add(gasFE);
        parametrosFE.add(camionCarga);

        CalculoHC.setFactoresEmisionFE(parametrosFE);
        CalculoHC.setUnidadPorDefecto(UnidadHC.GRAMO_EQ);

        adapterTest = new CargaDeActividadesApachePOIAdapter();
        adapterTest.setFile("src/test/java/test/domain/CargaDeActividades/actividad.xls");
        Periodo periodoTest = new Periodo(4,2022);
        Periodo periodoTestCompuesto = new Periodo(5,2021);
        actividadTest =
                //new ActividadDA(Actividad.COMBUSTION_FIJA, TipoDeConsumo.GAS_NATURAL, Unidad.M3, Periodicidad.MENSUAL,34.0,4,2022);
                new Actividad(TipoActividad.COMBUSTION_FIJA, TipoDeConsumo.GAS_NATURAL, Unidad.M3,
                        periodoTest, Periodicidad.MENSUAL,34.0);
        actividadTestCompuesta =
                //new ActividadDA(Actividad.LOGISTICA_PRODUCTOS_RESIDUOS, TipoDeConsumo.PRODUCTO_TRANSPORTADO, Unidad.U, Periodicidad.MENSUAL,2.0,5,2021);
                new Actividad(TipoActividad.LOGISTICA_PRODUCTOS_RESIDUOS,TipoDeConsumo.PRODUCTO_TRANSPORTADO,Unidad.U,
                        periodoTestCompuesto,Periodicidad.MENSUAL,2.0);
        hojaLeida = adapterTest.obtenerHoja(0);
    }


    @Test
    @DisplayName("Excel: se lee una fila")
    public void excel() {

        CargaDeActividadesApachePOIAdapter.LineaLeida linea = adapterTest.leerFila(hojaLeida,1);
        Assertions.assertEquals("COMBUSTION_FIJA",linea.actividad);
        Assertions.assertEquals("GAS_NATURAL",linea.tipoDeConsumo);
        Assertions.assertEquals("M3",linea.unidad);
        Assertions.assertEquals(34.0,linea.valor);
        Assertions.assertEquals("MENSUAL",linea.periodicidad);
        Assertions.assertEquals("04/2022",linea.periodoImputacion);

    }

    @Test
    @DisplayName("Excel: se lee una fila de campo compuesto") //la de categoria
    public void excelCompuesto() {

        CargaDeActividadesApachePOIAdapter.LineaLeida linea = adapterTest.leerFila(hojaLeida,5);
        Assertions.assertEquals("LOGISTICA_PRODUCTOS_RESIDUOS",linea.actividad);
        Assertions.assertEquals("CATEGORIA",linea.tipoDeConsumo);
        Assertions.assertEquals("-",linea.unidad);
        Assertions.assertEquals("MATERIA_PRIMA",linea.valorString);
        Assertions.assertEquals("MENSUAL",linea.periodicidad);
        Assertions.assertEquals("05/2021",linea.periodoImputacion);

    }

    @Test
    @DisplayName("Se parsea la fecha")
    public void fecha() {

        Assertions.assertEquals(4,adapterTest.obtenerMes("04/2022"));
        Assertions.assertEquals(2022,adapterTest.obtenerAnio("04/2022"));
        Assertions.assertNull(adapterTest.obtenerMes("2025"));
        Assertions.assertEquals(2025,adapterTest.obtenerAnio("2025"));

    }

    @Test
    @DisplayName("Se crea una actividad correctamente")
    public void actividad() {

        CargaDeActividadesApachePOIAdapter.LineaLeida linea = adapterTest.leerEntrada(hojaLeida,1);
        Actividad actividadLeida = adapterTest.crearActividad(linea);
        Assertions.assertEquals(actividadTest.tipoActividad,actividadLeida.tipoActividad);
        Assertions.assertEquals(actividadTest.periodo.getAnio(),actividadLeida.periodo.getAnio());
        Assertions.assertEquals(actividadTest.periodo.getMes(),actividadLeida.periodo.getMes());
        Assertions.assertEquals(actividadTest.valor,actividadLeida.valor);
        Assertions.assertEquals(actividadTest.tipoDeConsumo,actividadLeida.tipoDeConsumo);
        Assertions.assertEquals(actividadTest.periodicidad,actividadLeida.periodicidad);
        Assertions.assertEquals(actividadTest.unidad,actividadLeida.unidad);
    }

    @Test
    @DisplayName("Se crea una actividad compuesta correctamente")
    public void actividadCompuesta() {

        CargaDeActividadesApachePOIAdapter.LineaLeida linea = adapterTest.leerEntrada(hojaLeida,5);
        Actividad actividadLeida = adapterTest.crearActividad(linea);
        Assertions.assertEquals(actividadTestCompuesta.tipoActividad,actividadLeida.tipoActividad);
        Assertions.assertEquals(actividadTestCompuesta.periodo.getAnio(),actividadLeida.periodo.getAnio());
        Assertions.assertEquals(actividadTestCompuesta.periodo.getMes(),actividadLeida.periodo.getMes());
        Assertions.assertEquals(actividadTestCompuesta.valor,actividadLeida.valor);
        Assertions.assertEquals(actividadTestCompuesta.tipoDeConsumo,actividadLeida.tipoDeConsumo);
        Assertions.assertEquals(actividadTestCompuesta.periodicidad,actividadLeida.periodicidad);
        Assertions.assertEquals(actividadTestCompuesta.unidad,actividadLeida.unidad);

    }

}
