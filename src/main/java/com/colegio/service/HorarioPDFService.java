package com.colegio.service;

import com.colegio.dto.FilaHorarioDTO;
import com.colegio.dto.HorarioDTO;
import com.colegio.dto.HorarioMatrizDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HorarioPDFService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] generarPDFHorario(HorarioMatrizDTO horarioMatriz) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        document.open();

        Paragraph titulo = new Paragraph(horarioMatriz.getAulaNombre());
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.getFont().setSize(18);
        titulo.getFont().setStyle(Font.BOLD);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph(horarioMatriz.getAulaGrado());
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.getFont().setSize(12);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        int numDias = horarioMatriz.getDias().size();
        int totalColumnas = 1 + numDias; // time + days

        Table table = new Table(totalColumnas);
        table.setWidth(100);
        table.setPadding(5);
        table.setSpacing(0);
        table.setBorderColor(new java.awt.Color(0, 0, 0));

        agregarCeldaEncabezado(table, "Horario");
        for (String dia : horarioMatriz.getDias()) {
            agregarCeldaEncabezado(table, dia);
        }

        for (FilaHorarioDTO fila : horarioMatriz.getFilas()) {
            if (fila.isRecreo()) {
                String tiempoTexto = fila.getHoraInicio().format(TIME_FORMATTER) + " - " + fila.getHoraFin().format(TIME_FORMATTER);
                agregarCeldaHora(table, tiempoTexto);
                for (int i = 0; i < numDias; i++) {
                    agregarCeldaRecreo(table);
                }
            } else {
                String tiempoTexto = fila.getHoraInicio().format(TIME_FORMATTER) + " - " + fila.getHoraFin().format(TIME_FORMATTER);
                agregarCeldaHora(table, tiempoTexto);

                for (String dia : horarioMatriz.getDias()) {
                    HorarioDTO horario = fila.getHorario(dia);
                    if (horario != null) {
                        String contenido = horario.getCursoNombre() + "\n" + horario.getDocenteNombre();
                        agregarCeldaContenido(table, contenido);
                    } else {
                        agregarCeldaVacia(table);
                    }
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

    private void agregarCeldaHora(Table table, String texto) throws BadElementException {
        Cell cell = new Cell(new Paragraph(texto));
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

    private void agregarCeldaRecreo(Table table) throws BadElementException {
        Paragraph p = new Paragraph("RECREO");
        p.setAlignment(Element.ALIGN_CENTER);
        Cell cell = new Cell(p);
        cell.setBackgroundColor(new java.awt.Color(255, 243, 205));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}
