package web.rumers.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.rumers.app.entity.College;

@Repository
public interface AppRepository extends JpaRepository<College, Long> {

}
