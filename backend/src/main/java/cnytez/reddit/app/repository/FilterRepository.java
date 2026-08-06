package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Integer> {
}
