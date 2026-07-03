package animals.demo.facility.repository;

import animals.demo.facility.entity.ShelterAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelterAdminRepository extends JpaRepository<ShelterAdmin, Long> {
}
