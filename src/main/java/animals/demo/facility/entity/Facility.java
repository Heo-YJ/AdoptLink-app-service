package animals.demo.facility.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "facilities")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facilityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FacilityType type;

    //데이터 출처
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSource source;

    //공공데이터 고유 식별자
    @Column(length = 100, nullable = false)
    private String externalId;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 300, nullable = false)
    private String address;

    @Column(length = 50)
    private String phone;

    //위도
    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal lat;

    //경도
    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal lng;

    //원본 X좌표
    @Column(precision = 15, scale = 6)
    private BigDecimal rawCoordX;

    //원본 Y좌표
    @Column(precision = 15, scale = 6)
    private BigDecimal rawCoordY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FacilityStatus facilityStatus;

    @Column(length = 200)
    private String openingHours;

    @Column(length = 200)
    private String closedDays;

    @Column(name = "homepage_url", length = 300)
    private String homepageUrl;

    //공공데이터 기준 일자
    private LocalDate dataBaseDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Facility(
            FacilityType type,
            DataSource source,
            String externalId,
            String name,
            String address,
            String phone,
            BigDecimal lat,
            BigDecimal lng,
            BigDecimal rawCoordX,
            BigDecimal rawCoordY,
            FacilityStatus facilityStatus,
            String openingHours,
            String closedDays,
            String homepageUrl,
            LocalDate dataBaseDate
    ) {
        this.type = type;
        this.source = source;
        this.externalId = externalId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.lat = lat;
        this.lng = lng;
        this.rawCoordX = rawCoordX;
        this.rawCoordY = rawCoordY;
        this.facilityStatus = facilityStatus;
        this.openingHours = openingHours;
        this.closedDays = closedDays;
        this.homepageUrl = homepageUrl;
        this.dataBaseDate = dataBaseDate;
    }
}
