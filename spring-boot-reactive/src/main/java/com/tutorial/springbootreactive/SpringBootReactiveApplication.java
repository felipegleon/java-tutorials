package com.tutorial.springbootreactive;

import com.tutorial.springbootreactive.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.Locale;

@SpringBootApplication
public class SpringBootReactiveApplication implements CommandLineRunner {

	private final static Logger LOG = LoggerFactory.getLogger(SpringBootReactiveApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactiveApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		/*
		LOG.info("####### firstFlux #########");
		firstFlux();
		LOG.info("####### methodSubscribe #########");
		methodSubscribe();
		LOG.info("####### eventOnComplete #########");
		eventOnComplete();
		LOG.info("####### methodMapInFlux #########");
		methodMapInFlux();
		 */
		LOG.info("####### filterOperatorInFlux #########");
		filterOperatorInFlux();
	}

	/**
	 * It is a data stream immutable where we have an observable for each item in the stream. When an element in the
	 * stream change the is when the observable can see the change of the object. Then we must subscribe to the flux for
	 * can see the changes
	 */
	public void firstFlux () {
		Flux<String> names = Flux.just("Mike" , "Alban" , "Manou" , "Wafu")
				.doOnNext(System.out::println);

		names.subscribe();
	}

	/**
	 * In this section can see that the subscribe method  is observing the elements in the Flux.
	 */
	public void methodSubscribe () {
		Flux<String> names = Flux.just("Mike" , "Alban" , "", "Manou" , "Wafu")
				.doOnNext(element ->{
					if (element.isEmpty()){
						throw  new RuntimeException("The name can't be empty.");
					}
					System.out.println(element);
				});

		names.subscribe(element -> LOG.info("Name: " + element),
				error -> LOG.error(error.getMessage()));
	}

	/**
	 * Is a event that is call when the Flux do finished.
	 */
	public void	eventOnComplete () {
		Flux<String> names = Flux.just("Mike" , "Alban" , "Manou" , "Wafu")
				.doOnNext(element ->{
					if (element.isEmpty()){
						throw  new RuntimeException("The name can't be empty.");
					}
					System.out.println(element);
				});

		names.subscribe(element -> LOG.info("Name: " + element),
				error -> LOG.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						LOG.info("The Flux do finished");
					}
				});
	}

	/**
	 * is a method that allow transform the elements in a Flux and  return other Flux with the new objects transformed.
	 */
	public void methodMapInFlux () {
		Flux<Person> flux = Flux.just("Mike" , "Alban" , "Manou" , "Wafu")
				.map(name -> new Person(name.toUpperCase(), null))
				.doOnNext(person ->{
					if (person == null){
						throw  new RuntimeException("The name can't be empty.");
					}
					System.out.println(person.getFirstName());
				})
				.map(person -> {
					String name = person.getFirstName();
					person.setFirstName(name.toLowerCase());
					return person;
				});

		flux.subscribe(person -> LOG.info("Name: " + person.getFirstName()),
				error -> LOG.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						LOG.info("The Flux do finished");
					}
				});
	}

	/**
	 * This operator allow filter the elements in a Flux that comply  a condition, the return is a new Flux with the
	 * objects filtered.
	 */
	public void filterOperatorInFlux () {
		Flux<Person> flux = Flux.just("Mike Towers" , "Alban Charles" , "Manou Lafone" , "Wafu Per")
				.map(name -> new Person(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
				.filter(person -> person.getFirstName().equalsIgnoreCase("Manou"))
				.doOnNext(person ->{
					if (person == null){
						throw  new RuntimeException("The name can't be empty.");
					}
					System.out.println(person.getFirstName());
				})
				.map(person -> {
					String name = person.getFirstName();
					person.setFirstName(name.toLowerCase());
					return person;
				});

		flux.subscribe(person -> LOG.info("Name: " + person.getFirstName() + " " + person.getLastName()),
				error -> LOG.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						LOG.info("The Flux do finished");
					}
				});
	}
}
