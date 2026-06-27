package com.colegio.entity;

import com.colegio.entity.enums.EscalaCalificacion;
import com.colegio.entity.enums.NotaLetra;
import com.colegio.entity.enums.NotaLogro;
import com.colegio.util.ConversionNotas;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "evaluacion_nota")
public class EvaluacionNota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Integer idNota;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;
    
    @Column(name = "tipo_evaluacion")
    private String tipoEvaluacion;

    @Column(name = "bimestre", nullable = false)
    private Integer bimestre;

    @Column(name = "nota", nullable = false, precision = 4, scale = 2)
    private BigDecimal nota;

    @Column(name = "nota_letra", length = 2)
    private String notaLiteral;

    @Column(name = "nota_logro", length = 30)
    private String notaLogro;

    @Column(name = "escala", length = 10, nullable = false)
    private String escala = "DECIMAL";

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    public EvaluacionNota() {}

    public EvaluacionNota(Alumno alumno, Curso curso, Docente docente, Integer bimestre, BigDecimal nota, LocalDate fechaRegistro) {
        this.alumno = alumno;
        this.curso = curso;
        this.docente = docente;
        this.bimestre = bimestre;
        this.nota = nota;
        this.fechaRegistro = fechaRegistro;
        this.escala = "DECIMAL";
        this.notaLiteral = ConversionNotas.notaLiteral(nota);
        this.notaLogro = ConversionNotas.notaLogro(nota);
    }

    public Integer getIdNota() { return idNota; }
    public void setIdNota(Integer idNota) { this.idNota = idNota; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
    
    public String getTipoEvaluacion() { return tipoEvaluacion; }
    public void setTipoEvaluacion(String tipoEvaluacion) { this.tipoEvaluacion = tipoEvaluacion; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public Integer getBimestre() { return bimestre; }
    public void setBimestre(Integer bimestre) { this.bimestre = bimestre; }

    public BigDecimal getNota() { return nota; }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
        if (nota != null) {
            this.notaLiteral = ConversionNotas.notaLiteral(nota);
            this.notaLogro = ConversionNotas.notaLogro(nota);
        }
    }

    public String getNotaLiteral() { return notaLiteral; }
    public void setNotaLiteral(String notaLiteral) { this.notaLiteral = notaLiteral; }

    public String getNotaLogro() { return notaLogro; }
    public void setNotaLogro(String notaLogro) { this.notaLogro = notaLogro; }

    public String getEscala() { return escala; }
    public void setEscala(String escala) { this.escala = escala; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Transient
    public String getTipo() {
        return "Bimestre " + (bimestre != null ? bimestre : "?");
    }

    @Transient
    public Integer getPeso() {
        return 100;
    }

    @Transient
    public String getValor() {
        return nota != null ? nota.stripTrailingZeros().toPlainString() : "N/A";
    }

    @Transient
    public String getEstadoColor() {
        if (nota == null) return "gray";
        if (nota.compareTo(new BigDecimal("14")) >= 0) return "green";
        if (nota.compareTo(new BigDecimal("11")) >= 0) return "yellow";
        return "red";
    }

    @Transient
    public String getEstadoTexto() {
        if (nota == null) return "Sin nota";
        if (nota.compareTo(new BigDecimal("14")) >= 0) return "Aprobado";
        if (nota.compareTo(new BigDecimal("11")) >= 0) return "En proceso";
        return "Desaprobado";
    }
}
