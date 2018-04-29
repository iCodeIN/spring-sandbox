package at.favre.server.springsandbox.web;

import at.favre.server.springsandbox.repository.JpaNameRepository;
import at.favre.server.springsandbox.repository.StoredName;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class HttpHelloWorldEndpoint implements HelloWorldEndpoint {

    @Inject
    private JpaNameRepository repository;

    public HttpHelloWorldEndpoint() {
    }

    @Override
    public String test(@NotNull String name) {
        return String.format("Hello %s", name);
    }

    @Override
    public String add(@NotNull String name) {
        repository.save(new StoredName(name));
        return name + " added";
    }

    @Override
    public String getAll() {
        return repository.findAll().toString();
    }
}
