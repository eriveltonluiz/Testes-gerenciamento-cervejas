package project.api.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import project.api.rest.dto.BeerDTO;
import project.api.rest.entity.Beer;

@Mapper
public interface BeerMapper {

    BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

    Beer toModel(BeerDTO beerDTO);

    BeerDTO toDTO(Beer beer);
}
