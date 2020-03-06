package es.eoi.mundobancario.entity;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AMORTIZACIONES")
public class Amortizacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;

	@Temporal(TemporalType.DATE)
	@Column(name = "FECHA")
	private Calendar fecha;

	@Column(name = "IMPORTE")
	private double importe;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PRESTAMO", referencedColumnName = "ID")
	private Prestamo prestamo;

	public Amortizacion(Calendar fecha2, double importe) {
		super();
		this.fecha = fecha2;
		this.importe = importe;
	}

}