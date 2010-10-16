package tr.gov.e.hizmet.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.gov.e.hizmet.dao.HizmetDAO;
import tr.gov.e.hizmet.model.Person;
import tr.gov.e.hizmet.service.HizmetService;

@Service("hizmetService")
public class HizmetServiceImpl implements HizmetService {

	private HizmetDAO hizmetDAO;

	@Autowired
	public HizmetServiceImpl(HizmetDAO hizmetDAO) {
		this.hizmetDAO = hizmetDAO;
	}

	@Transactional
	@Override
	public void createNew(Person person) {
		hizmetDAO.createNew(person);
	}

	@Transactional
	@Override
	public List<Person> findAll() {
		return hizmetDAO.findAll();
	}

	@Transactional
	@Override
	public Person findById(Long id) {
		return hizmetDAO.findById(id);
	}

	@Transactional
	@Override
	public List<Person> findByName(String name) {
		return hizmetDAO.findByName(name);
	}

	@Transactional
	@Override
	public void remove(Person person) {
		hizmetDAO.remove(person);
	}

	@Transactional
	@Override
	public void update(Person person) {
		hizmetDAO.update(person);
	}

}