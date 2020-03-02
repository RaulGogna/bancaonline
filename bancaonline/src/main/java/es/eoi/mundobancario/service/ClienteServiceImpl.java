package es.eoi.mundobancario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.eoi.mundobancario.entity.Cliente;
import es.eoi.mundobancario.repository.ClienteRepository;

@Service
public class ClienteServiceImpl implements ClienteService{
	
	@Autowired
	private ClienteRepository repository;

	@Override
	public void create(Cliente cliente) {
		repository.save(cliente);
	}

	@Override
	public Optional<Cliente> find(int id) {
		Optional<Cliente> client = repository.findById(id);
		if(client.isPresent())
			return client;
		return null;
	}
	
	public List<Cliente> findAll(){
		return repository.findAll();
	}
	
	 @Override
	 public void updateCliente(Cliente cliente) {
	    this.repository.save(cliente);
	 }
	
}