package com.colegio.service;

import com.colegio.entity.Alumno;
import com.colegio.entity.Conducta;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ConductaPDFService {

    public byte[] generarPDFReporte(Alumno alumno, List<Conducta> conductas, int bimestre) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new java.awt.Color(0x7d, 0x1f, 0x1f));
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, new java.awt.Color(0x66, 0x66, 0x66));
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new java.awt.Color(0xff, 0xff, 0xff));
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new java.awt.Color(0x33, 0x33, 0x33));
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new java.awt.Color(0x66, 0x66, 0x66));
        Font greenFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new java.awt.Color(0x16, 0xa3, 0x4a));
        Font redFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new java.awt.Color(0xdc, 0x26, 0x26));

        Paragraph titulo = new Paragraph("Cuaderno de Control", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Reporte de Anotaciones", subtitleFont);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        Paragraph info = new Paragraph();
        info.setFont(contentFont);
        info.add(new Chunk("Estudiante: ", labelFont));
        info.add(new Chunk(alumno.getNombreCompleto(), contentFont));
        info.add(Chunk.NEWLINE);
        info.add(new Chunk("Aula: ", labelFont));
        info.add(new Chunk(alumno.getAula() != null ? alumno.getAula().getGrado() + " - " + alumno.getAula().getSeccion() : "Sin aula", contentFont));
        info.add(Chunk.NEWLINE);
        info.add(new Chunk("Fecha: ", labelFont));
        info.add(new Chunk(java.time.LocalDate.now().toString(), contentFont));
        if (bimestre > 0) {
            info.add(Chunk.NEWLINE);
            info.add(new Chunk("Bimestre: ", labelFont));
            info.add(new Chunk(String.valueOf(bimestre), contentFont));
        }
        info.setSpacingAfter(20);
        document.add(info);

        if (conductas.isEmpty()) {
            Paragraph empty = new Paragraph("No se registraron anotaciones para este per\u00edodo.", contentFont);
            empty.setAlignment(Element.ALIGN_CENTER);
            empty.setSpacingAfter(20);
            document.add(empty);
        } else {
            long positivas = conductas.stream().filter(c -> "POSITIVA".equals(c.getTipo())).count();
            long negativas = conductas.stream().filter(c -> "NEGATIVA".equals(c.getTipo())).count();

            Paragraph resumen = new Paragraph();
            resumen.setSpacingAfter(16);
            resumen.add(new Chunk("Positivas: ", labelFont));
            resumen.add(new Chunk(String.valueOf(positivas), greenFont));
            resumen.add(new Chunk("    Negativas: ", labelFont));
            resumen.add(new Chunk(String.valueOf(negativas), redFont));
            document.add(resumen);

            java.awt.Color headerBg = new java.awt.Color(0x7d, 0x1f, 0x1f);
            java.awt.Color positiveBg = new java.awt.Color(0xf0, 0xfd, 0xf4);
            java.awt.Color negativeBg = new java.awt.Color(0xfe, 0xf2, 0xf2);
            java.awt.Color borderColor = new java.awt.Color(0xe5, 0xe7, 0xeb);

            for (Conducta c : conductas) {
                boolean isPositive = "POSITIVA".equals(c.getTipo());

                Table card = new Table(1);
                card.setWidth(100);
                card.setPadding(4);
                card.setSpacing(4);
                card.setBorderColor(borderColor);

                Paragraph headerText = new Paragraph("[" + c.getTipo() + "]  " + c.getTitulo(), headerFont);
                Cell headerCell = new Cell(headerText);
                headerCell.setBackgroundColor(headerBg);
                headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerCell.setBorderColor(borderColor);
                card.addCell(headerCell);

                StringBuilder bodyContent = new StringBuilder();
                if (c.getDescripcion() != null && !c.getDescripcion().isEmpty()) {
                    bodyContent.append(c.getDescripcion()).append("\n\n");
                }
                bodyContent.append("Bimestre: ").append(c.getBimestre()).append("   ");
                bodyContent.append("Fecha: ").append(c.getFechaRegistro() != null ? c.getFechaRegistro().toString() : "-");
                if (c.getDocente() != null) {
                    bodyContent.append("\nRegistrado por: ").append(c.getDocente().getNombreCompleto());
                }

                Paragraph bodyPara = new Paragraph(bodyContent.toString(), contentFont);
                Cell bodyCell = new Cell(bodyPara);
                bodyCell.setBackgroundColor(isPositive ? positiveBg : negativeBg);
                bodyCell.setBorderColor(borderColor);
                bodyCell.setVerticalAlignment(Element.ALIGN_TOP);
                card.addCell(bodyCell);

                document.add(card);
            }
        }

        document.close();
        return baos.toByteArray();
    }
}
