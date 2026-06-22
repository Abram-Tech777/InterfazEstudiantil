package com.colegio.service;

import com.colegio.dto.HorarioDTO;
import com.colegio.dto.HorarioMatrizDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HorarioPDFService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Generar PDF del horario en formato matriz
     */
    public byte[] generarPDFHorario(HorarioMatrizDTO horarioMatriz) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        document.open();

        // Título
        Paragraph titulo = new Paragraph(horarioMatriz.getAulaNombre());
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.getFont().setSize(18);
        titulo.getFont().setStyle(Font.BOLD);
        document.add(titulo);

        // Subtítulo con grado
        Paragraph subtitulo = new Paragraph(horarioMatriz.getAulaGrado());
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.getFont().setSize(12);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        // Crear tabla: 6 columnas (Hora + 5 días)
        Table table = new Table(6);
        table.setWidth(100);
        table.setPadding(5);
        table.setSpacing(0);
        table.setBorderColor(new java.awt.Color(0, 0, 0));

        // Encabezado
        agregarCeldaEncabezado(table, "Hora");
        for (String dia : horarioMatriz.getDias()) {
            agregarCeldaEncabezado(table, dia);
        }

        // Filas con horarios
        List<LocalTime> horas = horarioMatriz.getHoras();
        for (LocalTime hora : horas) {
            agregarCeldaHora(table, hora);

            for (String dia : horarioMatriz.getDias()) {
                HorarioDTO horario = horarioMatriz.obtenerHorario(hora, dia);
                if (horario != null) {
                    String contenido = horario.getCursoNombre() + "\n" + horario.getDocenteNombre();
                    agregarCeldaContenido(table, contenido);
                } else {
                    agregarCeldaVacia(table);
                }
            }
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }

    private void agregarCeldaEncabezado(Table table, String texto) throws BadElementException {
        Cell cell = new Cell(new Paragraph(texto));
        cell.setBackgroundColor(new java.awt.Color(211, 211, 211));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void agregarCeldaHora(Table table, LocalTime hora) throws BadElementException {
        String horaTexto = hora.format(TIME_FORMATTER);
        Cell cell = new Cell(new Paragraph(horaTexto));
        cell.setBackgroundColor(new java.awt.Color(211, 211, 211));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void agregarCeldaContenido(Table table, String texto) throws BadElementException {
        Cell cell = new Cell(new Paragraph(texto));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void agregarCeldaVacia(Table table) throws BadElementException {
        Cell cell = new Cell(new Paragraph(""));
        table.addCell(cell);
    }
}
