package es.eoi.mundobancario.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.eoi.mundobancario.Repository.CuentasRepository;
import es.eoi.mundobancario.Repository.MovimientoRepository;
import es.eoi.mundobancario.Repository.PrestamoRepository;
import es.eoi.mundobancario.entity.Amortizacion;
import es.eoi.mundobancario.entity.Cuenta;
import es.eoi.mundobancario.entity.Movimiento;
import es.eoi.mundobancario.entity.Prestamo;
import es.eoi.mundobancario.enums.TiposMovimiento;
import es.eoi.mundobancario.excepcion.NotMoneyEnoughtException;

@Service
public class CuentaServiceImpl implements CuentaService {
	@Autowired
	CuentasRepository cuentasRepository;
	@Autowired
	MovimientoRepository movimientoRepository;
	@Autowired
	PrestamoRepository prestamoRepository;

	TiposMovimiento tipo;

	// TODO añadir excepcion
	private Cuenta checkNull(Optional<Cuenta> cuenta) {
		if (cuenta.isPresent()) {
			return cuenta.get();
		} else {
			return null;
		}
	}

	public Cuenta create(Cuenta cuenta) {
		return cuentasRepository.save(cuenta);
	}

	public Cuenta findById(int numCuenta) {
		return checkNull(cuentasRepository.findById(numCuenta));
	}

	public Cuenta update(Cuenta cuenta) {
		return cuentasRepository.save(cuenta);
	}

	public void remove(int numCuenta) {
		cuentasRepository.deleteById(numCuenta);
	}

	public Movimiento createPagos(Movimiento movimiento, int id) {
		try {
		Cuenta cuenta = checkNull(cuentasRepository.findById(id));
		double saldo = cuenta.getSaldo();
		if(saldo <= movimiento.getImporte())
			throw new NotMoneyEnoughtException();
		cuenta.setSaldo(saldo - movimiento.getImporte());
		movimiento.setTipoMovimiento(tipo.PAGO);
		update(cuenta);
		return movimientoRepository.save(movimiento);
		}catch(Exception e) {
			e.getMessage();
			return null;
		}
	}

	public Movimiento createIngresos(Movimiento movimiento, int id) {
		Cuenta cuenta = checkNull(cuentasRepository.findById(id));
		double saldo = cuenta.getSaldo();
		cuenta.setSaldo(saldo + movimiento.getImporte());
		movimiento.setTipoMovimiento(tipo.INGRESO);
		update(cuenta);
		return movimientoRepository.save(movimiento);
	}

	// TODO arreglar
	public Prestamo createPrestamos(Prestamo prestamo, Movimiento movimiento, int id) {
		Date fecha = new Date();
		int mes = fecha.getMonth();
		Cuenta cuenta = checkNull(cuentasRepository.findById(id));
		List<Amortizacion> amortizaciones = new ArrayList<Amortizacion>();
		List<Prestamo> prestamos = cuenta.getPrestamo();
		List<Movimiento> movimientos = cuenta.getMovimiento();
		double saldo = cuenta.getSaldo();
		for (int i = 0; i < prestamo.getPlazo(); i++) {
			fecha.setMonth(mes + i);
			amortizaciones.add(new Amortizacion(fecha, (prestamo.getImporte() / 4)));
		}
		prestamo.setAmortizacion(amortizaciones);
		if (prestamoRepository.save(prestamo) != null) {
			movimiento.setTipoMovimiento(tipo.PRESTAMO);
			movimientoRepository.save(movimiento);
			
			prestamos.add(prestamo);
			movimientos.add(movimiento);
			
			cuenta.setMovimiento(movimientos);
			cuenta.setPrestamo(prestamos);
			cuenta.setSaldo(saldo + prestamo.getImporte());
			
			update(cuenta);
			return prestamo;
		} else
			return null;
	}

	// TODO arreglar
	public Cuenta findPrestamosAmortizados(int id) {
		Cuenta cuenta = checkNull(cuentasRepository.findById(id));
		for (Prestamo prestamo : cuenta.getPrestamo()) {
			for (Amortizacion amortizacion : prestamo.getAmortizacion()) {
				if (amortizacion.getFecha().compareTo(new Date()) <= 0)
					return cuenta;
			}
		}
		return null;
	}

	// TODO arreglar
	public Cuenta findPrestamosVivos(int id) {
		Cuenta cuenta = checkNull(cuentasRepository.findById(id));
		for (Prestamo prestamo : cuenta.getPrestamo()) {
			for (Amortizacion amortizacion : prestamo.getAmortizacion()) {
				if (amortizacion.getFecha().compareTo(new Date()) > 0)
					return cuenta;
			}
		}
		return null;
	}

	public List<Cuenta> findAllDeudora() {
		return cuentasRepository.findAllBySaldoLessThan(0);
	}

	public List<Cuenta> findAll() {
		return cuentasRepository.findAll();
	}

	public void ejecutarAmortizacionsDiarias() {
		// TODO Auto-generated method stub
	}
}
