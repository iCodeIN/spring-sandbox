package at.favre.server.springsandbox.repository;

import org.springframework.data.repository.CrudRepository;

public interface JpaNameRepository extends CrudRepository<StoredName, Long> {

}
