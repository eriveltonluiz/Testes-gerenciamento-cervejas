package project.api.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import project.api.rest.builder.BeerDTOBuilder;
import project.api.rest.dto.BeerDTO;
import project.api.rest.entity.Beer;
import project.api.rest.exception.BeerAlreadyRegisteredException;
import project.api.rest.exception.BeerNotFoundException;
import project.api.rest.exception.BeerStockExceededException;
import project.api.rest.mapper.BeerMapper;
import project.api.rest.repository.BeerRepository;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    @SuppressWarnings("unused")
	private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;
    
    @Test
    void whenBeerInformedThenItShuldBeCreated() throws BeerAlreadyRegisteredException {
    	BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    	Beer expectedSavedBeer = beerMapper.toModel(beerDTO);
    	
    	Mockito.when(beerRepository.findByName(beerDTO.getName())).thenReturn(Optional.empty());
    	Mockito.when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);
    	
    	BeerDTO createdBeerDTO = beerService.createBeer(beerDTO);
    	
    	MatcherAssert.assertThat(createdBeerDTO.getId(), Matchers.is(Matchers.equalTo(beerDTO.getId())));
    	MatcherAssert.assertThat(createdBeerDTO.getName(), Matchers.is(equalTo(beerDTO.getName())));
    	MatcherAssert.assertThat(createdBeerDTO.getQuantity(), Matchers.is(equalTo(beerDTO.getQuantity())));
    	
    	//Verifica se o 1ºparâmetro createdBeerDTO.getQuantity() é maior que o 2º(3)
    	MatcherAssert.assertThat(createdBeerDTO.getQuantity(), Matchers.is(Matchers.greaterThan(3)));
    	
    	assertEquals(beerDTO.getId(), createdBeerDTO.getId());
    }
    
    @Test
    void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {
    	BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    	Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

    	Mockito.when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));
    	
    	assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
    }

    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException{
    	BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
        
        Mockito.when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));
        
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
        
        MatcherAssert.assertThat(foundBeerDTO, Matchers.is(Matchers.equalTo(expectedFoundBeerDTO)));
    }
    
    @Test
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        
        Mockito.when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());
        
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
    }

    @Test
    void whenListBeerIsCalledThenReturnAListOfBeers() {
    	BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
        
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));
        
        List<BeerDTO> foundBeerDTO = beerService.listAll();
        
        assertThat(foundBeerDTO, is(Matchers.not(Matchers.empty())));
        assertThat(foundBeerDTO.get(0), is(Matchers.equalTo(expectedFoundBeerDTO)));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
        
        Mockito.when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        
        List<BeerDTO> foundListBeersDTO = beerService.listAll();
        
        assertThat(foundListBeersDTO, Matchers.is(Matchers.empty()));
    }
    
    @Test
    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException{
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);

        Mockito.when(beerRepository.findById(expectedDeletedBeer.getId())).thenReturn(Optional.of(expectedDeletedBeer));
        Mockito.doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());
        
        beerService.deleteById(expectedDeletedBeerDTO.getId());
        
        //irá verificar se os métodos foram chamados apenas uma vez 
        Mockito.verify(beerRepository, Mockito.times(1)).findById(expectedDeletedBeerDTO.getId());
        Mockito.verify(beerRepository, Mockito.times(1)).deleteById(expectedDeletedBeerDTO.getId());
    }

    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // then
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        
        //expectedQuantityAfterIncrement deverá ser menor que expectedBeerDTO.getMax() caso contrário o teste não passa
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
    }

    @Test
    void whenIncrementIsGreatherThanMaxThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 80;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 45;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

//    @Test
//    void whenIncrementIsCalledWithInvalidIdThenThrowException() {
//        int quantityToIncrement = 10;
//
//        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
//
//        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
//    }
    
    //Outro bloco de comentários
    
//
//    @Test
//    void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
//        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
//
//        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
//        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
//
//        int quantityToDecrement = 5;
//        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
//        BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);
//
//        assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
//        assertThat(expectedQuantityAfterDecrement, greaterThan(0));
//    }
//
//    @Test
//    void whenDecrementIsCalledToEmptyStockThenEmptyBeerStock() throws BeerNotFoundException, BeerStockExceededException {
//        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
//
//        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
//        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
//
//        int quantityToDecrement = 10;
//        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
//        BeerDTO incrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);
//
//        assertThat(expectedQuantityAfterDecrement, equalTo(0));
//        assertThat(expectedQuantityAfterDecrement, equalTo(incrementedBeerDTO.getQuantity()));
//    }
//
//    @Test
//    void whenDecrementIsLowerThanZeroThenThrowException() {
//        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
//
//        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
//
//        int quantityToDecrement = 80;
//        assertThrows(BeerStockExceededException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
//    }
//
//    @Test
//    void whenDecrementIsCalledWithInvalidIdThenThrowException() {
//        int quantityToDecrement = 10;
//
//        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
//
//        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
//    }
}
