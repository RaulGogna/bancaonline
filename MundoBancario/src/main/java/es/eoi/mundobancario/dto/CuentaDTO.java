package es.eoi.mundobancario.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CuentaDTO {

	private int num_cuenta;
	private String alias;
	private float saldo;

}
