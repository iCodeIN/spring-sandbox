package at.favre.server.springsandbox.repository;

public interface NameRepository {

    void store(String name);

    boolean contains(String name);

}
