package uz.buildia.inventoryservice.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.buildia.inventoryservice.constants.status.PassportHistoryStatus;
import uz.buildia.inventoryservice.dto.common.PassportStageHistoryDto;
import uz.buildia.inventoryservice.dto.common.StageDto;
import uz.buildia.inventoryservice.dto.request.PassportHistorySetRequest;
import uz.buildia.inventoryservice.entity.PassportStageHistory;
import uz.buildia.inventoryservice.entity.ProductPassport;
import uz.buildia.inventoryservice.mapper.dto.PassportStageHistoryMapper;
import uz.buildia.inventoryservice.mapper.dto.ProductPassportMapper;
import uz.buildia.inventoryservice.mapper.dto.ScannerMapper;
import uz.buildia.inventoryservice.mapper.dto.StageMapper;
import uz.buildia.inventoryservice.repository.PassportStageHistoryRepository;
import uz.buildia.inventoryservice.service.PassportStageHistoryService;
import uz.buildia.inventoryservice.service.ScannerService;
import uz.buildia.inventoryservice.service.StageService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassportStageHistoryServiceImpl implements PassportStageHistoryService {

  private final ScannerService scannerService;
  private final StageService stageService;
  private final ScannerMapper scannerMapper;
  private final StageMapper stageMapper;
  private final ProductPassportMapper productPassportMapper;
  private final PassportStageHistoryMapper passportStageHistoryMapper;
  private final PassportStageHistoryRepository passportStageHistoryRepository;

  @Override
  public PassportStageHistoryDto save(PassportHistorySetRequest passportHistorySetRequest) {
    ProductPassport productPassport =
        productPassportMapper.toEntity(passportHistorySetRequest.productPassportDto());
    if (Boolean.TRUE.equals(isHistoryOpenForQrId(productPassport.getQrId()))) {
      return passportStageHistoryMapper.toDto(
          closeHistory(getCurrentHistory(productPassport.getQrId())));
    } else {
      return passportStageHistoryMapper.toDto(startNewHistory(passportHistorySetRequest));
    }
  }

  @Override
  public boolean isClosePassport(String qrId) {
    PassportStageHistory history = getCurrentHistory(qrId);
    if (history == null) {
      log.warn("No PassportStageHistory found for QR ID: {}", qrId);
      return true;
    }
    var stage = history.getStage();
    if (stage == null) {
      log.warn("Stage is null for PassportStageHistory with QR ID: {}", qrId);
      return true;
    }
    return stage.getNextStage() == null;
  }

  private PassportStageHistory getCurrentHistory(String qrId) {
    return passportStageHistoryRepository
        .findByProductPassportQrIdAndPassportHistoryStatus(qrId, PassportHistoryStatus.OPEN)
        .orElse(
            passportStageHistoryRepository.findFirstByProductPassportQrIdOrderByFinishedAtDesc(
                qrId));
  }

  private Boolean isHistoryOpenForQrId(String qrId) {
    return passportStageHistoryRepository.existsByPassportHistoryStatusIsAndProductPassportQrIdIs(
        PassportHistoryStatus.OPEN, qrId);
  }

  private PassportStageHistory startNewHistory(
      PassportHistorySetRequest passportHistorySetRequest) {

    PassportStageHistory currentHistory =
        getCurrentHistory(passportHistorySetRequest.productPassportDto().qrId());
    StageDto stageDto =
        Objects.isNull(currentHistory)
            ? stageService.getFirstStageByModel(
                passportHistorySetRequest.productPassportDto().model())
            : stageService.getNextStageById(currentHistory.getStage().getId());

    ProductPassport productPassport =
        productPassportMapper.toEntity(passportHistorySetRequest.productPassportDto());
    PassportStageHistory passportStageHistory =
        PassportStageHistory.builder()
            .stage(stageMapper.toEntity(stageDto))
            .productPassport(productPassport)
            .scanner(
                scannerMapper.toEntity(
                    scannerService.getScannerByHrId(passportHistorySetRequest.scannerId())))
            .model(productPassport.getModel())
            .startedAt(LocalDateTime.now())
            .defectsQuantity(0L)
            .passportHistoryStatus(PassportHistoryStatus.OPEN)
            .build();
    return passportStageHistoryRepository.save(passportStageHistory);
  }

  private PassportStageHistory closeHistory(PassportStageHistory passportStageHistory) {
    passportStageHistory.setFinishedAt(LocalDateTime.now());
    passportStageHistory.setPassportHistoryStatus(PassportHistoryStatus.CLOSED);
    return passportStageHistoryRepository.save(passportStageHistory);
  }
}
