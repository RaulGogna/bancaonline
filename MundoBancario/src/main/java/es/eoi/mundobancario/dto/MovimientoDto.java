package es.eoi.mundobancario.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MovimientoDto {

	private int id;
	private String descripcion;
	private Date fecha;
	private double importe;

}
