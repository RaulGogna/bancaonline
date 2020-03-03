package es.eoi.mundobancario.Controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.eoi.mundobancario.Service.ClienteService;
import es.eoi.mundobancario.dto.ClienteDto;
import es.eoi.mundobancario.entity.Cliente;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClientesController {
	@Autowired
	ClienteService service;
	@Autowired
	ModelMapper modelMapper;

	private ClienteDto toDto(Cliente cliente) {
		ClienteDto clienteDto = modelMapper.map(cliente, ClienteDto.class);
		return clienteDto;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/clientes")
	public List<ClienteDto> findAll(@PathVariable int id) {
		List<ClienteDto> clientes = new ArrayList<ClienteDto>();
		for (Cliente cliente : service.findAll()) {
			clientes.add(toDto(cliente));
		}
		return clientes;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	public ClienteDto findCliente(@PathVariable int id) {
		return toDto(service.findById(id).get());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/login")
	public ClienteDto login(@PathVariable int id) {
		return toDto(service.login(id));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}/cuentas")
	public ClienteDto findCuentas(@PathVariable int id) {
		return toDto(service.findCuentas(id));
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{id}")
	public ClienteDto updateEmail(@PathVariable int id) {
		return toDto(service.updateEmail(id).get());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/clientes")
	public ClienteDto create(@PathVariable Cliente cliente) {
		return toDto(service.Create(cliente));

	}
}