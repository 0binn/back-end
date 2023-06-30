package project.seatsence.src.store.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.seatsence.src.store.dao.StoreChairRepository;
import project.seatsence.src.store.domain.StoreChair;

@Service
@RequiredArgsConstructor
public class StoreChairService {

    private final StoreChairRepository storeChairRepository;

    public void saveAll(List<StoreChair> storeChairList) {
        storeChairRepository.saveAll(storeChairList);
    }
}
