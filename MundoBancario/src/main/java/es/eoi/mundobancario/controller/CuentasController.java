package es.eoi.mundobancario.controller;

import static es.eoi.mundobancario.utils.DtoConverter.fromCuentaNuevaDto;
import static es.eoi.mundobancario.utils.DtoConverter.fromMovimientoNuevoDto;
import static es.eoi.mundobancario.utils.DtoConverter.toCuentaConClienteDto;
import static es.eoi.mundobancario.utils.DtoConverter.toCuentaConClienteDtoList;
import static es.eoi.mundobancario.utils.DtoConverter.toMovimientoDtoList;
import static es.eoi.mundobancario.utils.DtoConverter.toPrestamoDtoList;
import static es.eoi.mundobancario.utils.Fechas.queDiaEsHoy;
import static es.eoi.mundobancario.utils.Fechas.sumaMesesAHoy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.eoi.mundobancario.dto.CuentaConClienteDto;
import es.eoi.mundobancario.dto.CuentaNuevaDto;
import es.eoi.mundobancario.dto.MovimientoDto;
import es.eoi.mundobancario.dto.MovimientoNuevoDto;
import es.eoi.mundobancario.dto.PrestamoDto;
import es.eoi.mundobancario.dto.PrestamoNuevoDto;
import es.eoi.mundobancario.entity.Amortizacion;
import es.eoi.mundobancario.entity.Cuenta;
import es.eoi.mundobancario.entity.Movimiento;
import es.eoi.mundobancario.entity.Prestamo;
import es.eoi.mundobancario.service.AmortizacionService;
import es.eoi.mundobancario.service.ClienteService;
import es.eoi.mundobancario.service.CuentaService;
import es.eoi.mundobancario.service.MovimientoService;
import es.eoi.mundobancario.service.PrestamoService;
import es.eoi.mundobancario.service.TipoMovimientoService;

@RestController
@RequestMapping(value = "/cuentas")
public class CuentasController {

	@Autowired
	CuentaService cuentaService;

	@Autowired
	MovimientoService movimientoService;

	@Autowired
	TipoMovimientoService tipoMovimientoService;

	@Autowired
	PrestamoService prestamoService;

	@Autowired
	ClienteService clienteService;

	@Autowired
	AmortizacionService amortizacionService;

	@GetMapping
	public List<CuentaConClienteDto> getAll() {
		return toCuentaConClienteDtoList(cuentaService.getAll());
	}

	@GetMapping("/deudoras")
	public List<CuentaConClienteDto> getBySaldo() {
		return toCuentaConClienteDtoList(cuentaService.getDeudoras());
	}

	@GetMapping("/{id}")
	public CuentaConClienteDto getById(@PathVariable Integer id) {
		return toCuentaConClienteDto(cuentaService.getById(id));
	}

	@PostMapping
	public boolean post(@RequestBody CuentaNuevaDto dto, @RequestParam Integer idcliente) {
		Cuenta cuenta = fromCuentaNuevaDto(dto);
		cuenta.setCliente(clienteService.getById(idcliente));
		return cuentaService.post(cuenta);
	}

	@PutMapping("/{id}")
	public boolean updateCuenta(@PathVariable Integer id, @RequestParam String alias) {
		return cuentaService.putAlias(id, alias);
	}

	@GetMapping(value = "/{id}/movimientos")
	public List<MovimientoDto> getMovimientosByCuenta(@PathVariable Integer id) {
		return toMovimientoDtoList(movimientoService.getByCuenta(cuentaService.getById(id)));
	}

	@GetMapping(value = "/{id}/prestamos")
	public List<PrestamoDto> findByCuenta(@PathVariable Integer id) {
		return toPrestamoDtoList(prestamoService.getByCuenta(cuentaService.getById(id)));
	}

	@PostMapping("/{id}/ingresos")
	public boolean postIngreso(@PathVariable Integer id, @RequestBody MovimientoNuevoDto dto) {
		Cuenta cuenta = cuentaService.getById(id);
		Movimiento movimiento = fromMovimientoNuevoDto(dto);
		movimiento.setCuenta(cuentaService.getById(id));
		movimiento.setFecha(queDiaEsHoy());
		movimiento.setTipo(tipoMovimientoService.getByTipo("INGRESO"));
		cuenta.setSaldo(cuenta.getSaldo()+dto.getImporte());
		cuentaService.post(cuenta);
		return movimientoService.post(movimiento);
	}

	@PostMapping("/{id}/pagos")
	public boolean postPagos(@PathVariable Integer id, @RequestBody MovimientoNuevoDto dto) {
		Cuenta cuenta = cuentaService.getById(id);
		if(cuentaService.getById(id).getSaldo()-dto.getImporte()>0) {
			Movimiento movimiento = fromMovimientoNuevoDto(dto);
			movimiento.setCuenta(cuentaService.getById(id));
			movimiento.setFecha(queDiaEsHoy());
			movimiento.setTipo(tipoMovimientoService.getByTipo("PAGO"));
			cuenta.setSaldo(cuenta.getSaldo()-movimiento.getImporte());
			return movimientoService.post(movimiento);
		}
		return false;
	}

	@PostMapping("/{id}/prestamos")
	public boolean postPrestamo(@PathVariable Integer id, @RequestBody PrestamoNuevoDto dto) {
		if(prestamoService.getByCuentaAndPagado(cuentaService.getById(id), "PENDIENTE").isPresent())
			return false;
		Movimiento movimiento = new Movimiento();
		movimiento.setCuenta(cuentaService.getById(id));
		movimiento.setTipo(tipoMovimientoService.getByTipo("PRÉSTAMO"));
		movimiento.setDescripcion("Prestamo: " + dto.getDescripcion());
		movimiento.setFecha(queDiaEsHoy());
		movimiento.setImporte(dto.getImporte());
		movimientoService.post(movimiento);
		Prestamo prestamo = new Prestamo();
		prestamo.setDescripcion(dto.getDescripcion());
		prestamo.setFecha(queDiaEsHoy());
		prestamo.setImporte(dto.getImporte());
		prestamo.setPlazos(dto.getPlazos());
		prestamo.setCuenta(cuentaService.getById(id));
		prestamo.setPagado("PENDIENTE");
		prestamoService.post(prestamo);
		for (int i = 1; i <= dto.getPlazos(); i++) {
			Amortizacion amortizacion = new Amortizacion();
			amortizacion.setImporte(dto.getImporte() / dto.getPlazos());
			amortizacion.setFecha(sumaMesesAHoy(i));
			amortizacion.setPrestamo(prestamo);
			amortizacion.setPagado("PENDIENTE");
			prestamo.addAmortizacion(amortizacion);
			amortizacionService.post(amortizacion);
		}
		Cuenta cuenta = cuentaService.getById(id);
		cuenta.setSaldo(cuenta.getSaldo() + dto.getImporte());
		cuentaService.post(cuenta);
		return true;
	}

	@GetMapping(value = "/{id}/prestamosVivos")
	public List<PrestamoDto> getPrestamosVivos(@PathVariable Integer id) {
		return toPrestamoDtoList(prestamoService.getPrestamosVivosByCuentaId(id));
	}

	@GetMapping(value = "/{id}/prestamosAmortizados")
	public List<PrestamoDto> getPrestamosAmortizados(@PathVariable Integer id) {
		return toPrestamoDtoList(prestamoService.getPrestamosAmortizados(id));
	}

	
	@PostMapping("/ejecutarAmortizacionesDiarias")
	@Scheduled(fixedDelay = 1000)
	public void ejecutarAmortizacionesDiarias() {
		amortizacionService.ejecutarAmortizacionesDiarias();
	}
}