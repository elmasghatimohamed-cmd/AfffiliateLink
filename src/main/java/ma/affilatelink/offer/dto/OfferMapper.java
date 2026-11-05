package ma.affilatelink.offer.dto;

import ma.affilatelink.offer.entities.Offer;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = CampaignMapper.class)
public interface OfferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "campaigns", ignore = true)
    Offer toEntity(CreateOfferRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "advertiserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "campaigns", ignore = true)
    void updateEntity(UpdateOfferRequest request, @MappingTarget Offer offer);

    @Mapping(
            target = "tags",
            expression = "java(offer.getTags().stream().map(tag -> tag.getName()).collect(java.util.stream.Collectors.toSet()))"
    )
    @Mapping(target = "campaigns", source = "campaigns")
    OfferResponse toResponse(Offer offer);

    @AfterMapping
    default void applyUpdate(
            UpdateOfferRequest request,
            @MappingTarget Offer offer
    ) {
        offer.updateOffer(
                request.title(),
                request.description(),
                request.landingUrl(),
                request.niche()
        );
    }
}