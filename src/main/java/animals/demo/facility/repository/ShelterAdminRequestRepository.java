package animals.demo.facility.repository;

import animals.demo.facility.entity.Facility;
import animals.demo.facility.entity.ShelterAdmin;
import animals.demo.facility.entity.ShelterAdminRequest;
import animals.demo.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShelterAdminRequestRepository extends JpaRepository<ShelterAdminRequest, Long> {
    boolean existsByUser_UserIdAndFacility_FacilityId(Long userId, Long facilityId);
    Optional<ShelterAdminRequest> findByUser_UserId(Long userId);
    List<ShelterAdminRequest> findByRequestId(Long requestId);
}
