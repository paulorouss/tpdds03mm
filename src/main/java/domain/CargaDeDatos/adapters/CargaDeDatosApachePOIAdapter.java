package domain.CargaDeDatos.adapters;

import domain.CargaDeDatos.entidades.*;
import domain.calculoHC.CalculoHC;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CargaDeDatosApachePOIAdapter implements CargaDeDatosAdapter{

    public class LineaLeida{
        public String actividad,tipoDeConsumo,periodicidad,valorString,periodoImputacion,unidad;
        public double valor;
    }

    public CargaDeDatosApachePOIAdapter(String path){
        this.file=path;
    }

    List<ActividadDA> formulariosDa = new ArrayList<>();

    private final String file;


    public List<ActividadDA> leerArchivo() throws IOException {

        HSSFSheet hojaALeer = obtenerHoja(0);

        int rowStart = Math.min(1, hojaALeer.getFirstRowNum());
        int rowEnd = Math.max(30, hojaALeer.getLastRowNum());
        for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
            LineaLeida linea = leerEntrada(hojaALeer,rowNum);
            if(Objects.equals(linea.tipoDeConsumo, "CATEGORIA")){
                rowNum+=3;
            }
            formulariosDa.add(crearActividad(linea));

        }

        return formulariosDa;
    }

    public Integer obtenerMes(String periodoImputacion){
        String[] parse = periodoImputacion.split("/");
        if(parse.length == 1){
            return null;
        }
        else{
            return Integer.valueOf(parse[0]);
        }
    }
    public Integer obtenerAnio(String periodoImputacion){
        String[] parse = periodoImputacion.split("/");
        if(parse.length == 1){
            return Integer.valueOf(parse[0]);
        }
        else{
            return Integer.valueOf(parse[1]);
        }
    }

    public ActividadDA crearActividad(LineaLeida linea){
        return new ActividadDA(
                Actividad.valueOf(linea.actividad),
                TipoDeConsumo.valueOf(linea.tipoDeConsumo),
                Unidad.valueOf(linea.unidad),Periodicidad.valueOf(linea.periodicidad),
                linea.valor,
                obtenerMes(linea.periodoImputacion),
                obtenerAnio(linea.periodoImputacion));
    }

    public LineaLeida leerEntrada(HSSFSheet hojaALeer, int rowNum){
        LineaLeida linea = leerFila(hojaALeer,rowNum);
        if(Objects.equals(linea.tipoDeConsumo, "CATEGORIA")){
            LineaLeida medio = leerFila(hojaALeer,rowNum+1);
            LineaLeida distancia = leerFila(hojaALeer,rowNum+2);
            LineaLeida peso = leerFila(hojaALeer,rowNum+3);
            linea.valor = distancia.valor * peso.valor * CalculoHC.getFactorEmision(TipoDeConsumo.valueOf(medio.valorString));
            linea.tipoDeConsumo = String.valueOf(TipoDeConsumo.PRODUCTO_TRANSPORTADO);
            linea.unidad = String.valueOf(Unidad.U);
        }
        return linea;
    }

    public HSSFSheet obtenerHoja(int nroHoja) throws IOException {
        InputStream myFile = Files.newInputStream(new File(file).toPath());
        HSSFWorkbook wb = new HSSFWorkbook(myFile);
        return wb.getSheetAt(nroHoja);
    }

    public LineaLeida leerFila(HSSFSheet hoja, int rowNum){

        LineaLeida lineaLeida = new LineaLeida();
        Row r = hoja.getRow(rowNum);
        if (r == null) {
            return null;
        }

        Cell c = r.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        lineaLeida.actividad = c.getStringCellValue();
        c = r.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        lineaLeida.tipoDeConsumo = c.getStringCellValue();
        c = r.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        lineaLeida.unidad = c.getStringCellValue();
        c = r.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if(Objects.equals(lineaLeida.tipoDeConsumo, "CATEGORIA") ||
            Objects.equals(lineaLeida.tipoDeConsumo, "MEDIO_TRANSPORTE")){
            lineaLeida.valorString = c.getStringCellValue(); //si hay un numero tira una excepcion
        }else{
            lineaLeida.valor = c.getNumericCellValue();
        }
        c = r.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        lineaLeida.periodicidad = c.getStringCellValue();
        c = r.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        lineaLeida.periodoImputacion = c.getStringCellValue();

        return lineaLeida;
    }

}