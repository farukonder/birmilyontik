package tr.gov.e.hizmet.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tr.gov.e.hizmet.model.Person;
import tr.gov.e.hizmet.service.HizmetService;

@Component("hizmetController")
@Scope("session")
public class HizmetController {

	private HizmetService hizmetService;
	private Person person = new Person();

	String name;

	public HizmetController() {
	}

	public List<String> getPersonByName(String title) {
		List<String> titles = new ArrayList<String>();

		titles.add("" + System.currentTimeMillis());
		titles.add("" + System.currentTimeMillis());
		titles.add("" + System.currentTimeMillis());

		return titles;
	}

	@Autowired
	public HizmetController(HizmetService hizmetService) {
		this.hizmetService = hizmetService;
	}

	public void save(ActionEvent actionEvent) {
		hizmetService.createNew(person);
	}

	@PostConstruct
	public void onLoad() {

	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HizmetService getHizmetService() {
		return hizmetService;
	}

	public void setHizmetService(HizmetService hizmetService) {
		this.hizmetService = hizmetService;
	}

}
