package tr.gov.e.hizmet.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import tr.gov.e.hizmet.dao.HizmetDAO;
import tr.gov.e.hizmet.model.Person;

@Repository
public class HizmetDAOImpl implements HizmetDAO {

	@PersistenceContext
	protected EntityManager entityManager;

	@Override
	public void createNew(Person person) {
		entityManager.persist(person);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Person> findAll() {
		return entityManager.createQuery("Select t from " + Person.class.getSimpleName() + " t").getResultList();

	}

	@Override
	public Person findById(Long id) {
		return entityManager.find(Person.class, id);
	}

	@Override
	public List<Person> findByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(Person person) {
		entityManager.remove(person);
	}

	@Override
	public void update(Person person) {
		entityManager.merge(person);
	}

}
