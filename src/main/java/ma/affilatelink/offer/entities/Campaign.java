package ma.affilatelink.offer.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "budget", nullable = false, precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "payout_model", nullable = false, length = 50)
    private String payoutModel;

    @Column(name = "payout_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal payoutValue;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    Campaign(String name,
             LocalDate startDate,
             LocalDate endDate,
             BigDecimal budget,
             String payoutModel,
             BigDecimal payoutValue, String status,
             Offer offer
    ) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.payoutModel = payoutModel;
        this.payoutValue = payoutValue;
        this.status = status;
        this.offer = offer;
    }
}