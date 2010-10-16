package tr.gov.e.hizmet.dao;

import java.util.List;

import tr.gov.e.hizmet.model.Person;

public interface HizmetDAO {
	  
	public void createNew(Person person);
	
	public List<Person> findAll();
	
	public List<Person> findByName(String name);
	
	public void update(Person person);
	
	public void remove(Person person);
	
	public Person findById(Long id);

}
