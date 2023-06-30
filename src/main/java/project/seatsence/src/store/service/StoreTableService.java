package project.seatsence.src.store.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.seatsence.src.store.dao.StoreTableRepository;
import project.seatsence.src.store.domain.StoreTable;

@Service
@RequiredArgsConstructor
public class StoreTableService {

    private final StoreTableRepository storeTableRepository;

    public void saveAll(List<StoreTable> storeTableList) {
        storeTableRepository.saveAll(storeTableList);
    }
}
