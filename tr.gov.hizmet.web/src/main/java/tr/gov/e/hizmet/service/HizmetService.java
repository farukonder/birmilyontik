package tr.gov.e.hizmet.service;

import java.util.List;

import tr.gov.e.hizmet.model.Person;

public interface HizmetService {

	public void createNew(Person person);
	
	public List<Person> findAll();
	
	public List<Person> findByName(String name);
	
	public void update(Person person);
	
	public void remove(Person person);
	
	public Person findById(Long id);
}
