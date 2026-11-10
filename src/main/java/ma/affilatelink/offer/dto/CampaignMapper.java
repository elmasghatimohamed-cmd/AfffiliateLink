package ma.affilatelink.offer.dto;

import ma.affilatelink.offer.dto.CampaignResponse;
import ma.affilatelink.offer.entities.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    @Mapping(target = "offerId", source = "offer.id")
    CampaignResponse toResponse(Campaign campaign);
}
