package animals.demo.facility.entity;

import animals.demo.admin.entity.Admin;
import animals.demo.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "shelter_admin_requests")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShelterAdminRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facilityId", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewedByAdminId")
    // Status : PENDING 상태 일때는 admin이 null
    private Admin admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(length = 300)
    private String rejectReason;

    @Column(name = "proof_image_url", length = 300, nullable = false)
    private String proofImageUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //처리 날짜: admin에 의해 요청이 반려될 경우(Status : REJECTED), user가 새로 신청해야 함. 하나의 요청 결과는 변하지 않음
    @Column(updatable = false)
    private LocalDateTime reviewedAt;

    @Builder
    private ShelterAdminRequest(
            User user,
            Facility facility,
            Status status,
            String proofImageUrl
    ) {
        this.user = user;
        this.facility = facility;
        this.status = Status.PENDING;
        this.proofImageUrl = proofImageUrl;
    }

    //요청 수락
    public void approve(Admin admin) {
        if(this.status != Status.PENDING) { return; }

        this.admin = admin;
        this.status = Status.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }

    //요청 반려
    public void reject(Admin admin, String rejectReason) {
        if(this.status != Status.PENDING) { return; }

        this.admin = admin;
        this.status = Status.REJECTED;
        this.rejectReason = rejectReason;
        this.reviewedAt = LocalDateTime.now();
    }
}
