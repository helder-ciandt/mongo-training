package com.mongo.mongotraining.repository;

import com.microsoft.azure.spring.data.cosmosdb.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CustomerRepository extends ReactiveCosmosRepository<Customer, String> {

    Flux<Customer> findByFirstName(String firstName);
    Flux<Customer> findByLastName(String lastName);
}
