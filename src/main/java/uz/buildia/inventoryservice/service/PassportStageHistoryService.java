package uz.buildia.inventoryservice.service;

import uz.buildia.inventoryservice.dto.common.PassportStageHistoryDto;
import uz.buildia.inventoryservice.dto.request.PassportHistorySetRequest;

public interface PassportStageHistoryService {

    PassportStageHistoryDto save(PassportHistorySetRequest passportHistorySetRequest);

    boolean isClosePassport(String qrId);
}
