package com.mongo.mongotraining;

import com.mongo.mongotraining.repository.Customer;
import com.mongo.mongotraining.repository.CustomerRepository;
import com.mongo.mongotraining.repository.User;
import com.mongo.mongotraining.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Optional;


@SpringBootApplication
public class MongotrainingApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MongotrainingApplication.class);

	@Autowired
	private UserRepository repository;

	@Autowired
	private CustomerRepository customerRepository;

	public static void main(String[] args) {
		SpringApplication.run(MongotrainingApplication.class, args);
	}

	public void getRepositoryData() {
		final User testUser = new User("1", "Tasha", "Calderon", "4567 Main St Buffalo, NY 98052");

		LOGGER.info("Saving user: {}", testUser);

		// Save the User class to Azure CosmosDB database.
		final Mono<User> saveUserMono = repository.save(testUser);

		final Flux<User> firstNameUserFlux = repository.findByFirstName("testFirstName");

		//  Nothing happens until we subscribe to these Monos.
		//  findById will not return the user as user is not present.
		final Mono<User> findByIdMono = repository.findById(testUser.getId());
		final User findByIdUser = findByIdMono.block();
		Assert.isNull(findByIdUser, "User must be null");

		final User savedUser = saveUserMono.block();
		Assert.state(savedUser != null, "Saved user must not be null");
		Assert.state(savedUser.getFirstName().equals(testUser.getFirstName()), "Saved user first name doesn't match");

		LOGGER.info("Saved user");

		firstNameUserFlux.collectList().block();

		final Optional<User> optionalUserResult = repository.findById(testUser.getId()).blockOptional();
		Assert.isTrue(optionalUserResult.isPresent(), "Cannot find user.");

		final User result = optionalUserResult.get();
		Assert.state(result.getFirstName().equals(testUser.getFirstName()), "query result firstName doesn't match!");
		Assert.state(result.getLastName().equals(testUser.getLastName()), "query result lastName doesn't match!");

		LOGGER.info("Found user by findById : {}", result);
	}

	public void getCustomerData() {

		// save a couple of customers
		customerRepository.save(new Customer("Alice", "Smith")).block();
		customerRepository.save(new Customer("Bob", "Smith")).block();

		// fetch all customers
		System.out.println("Customers found with findAll():");
		System.out.println("-------------------------------");
		for (Customer customer : customerRepository.findAll().collectList().block()) {
			System.out.println(customer);
		}
		System.out.println();

		// fetch an individual customer
		System.out.println("Customer found with findByFirstName('Alice'):");
		System.out.println("--------------------------------");
		System.out.println(customerRepository.findByFirstName("Alice").blockFirst());

		System.out.println("Customers found with findByLastName('Smith'):");
		System.out.println("--------------------------------");
		for (Customer customer : customerRepository.findByLastName("Smith").collectList().block()) {
			System.out.println(customer);
		}
	}

	@Override
	public void run(String... args) {
		getRepositoryData();
		getCustomerData();
	}


	@PostConstruct
	public void setup() {
		LOGGER.info("Clear the database");
		this.repository.deleteAll().block();
		customerRepository.deleteAll().block();

	}

	@PreDestroy
	public void cleanup() {
		LOGGER.info("Cleaning up users");
		this.repository.deleteAll().block();
	}
}
