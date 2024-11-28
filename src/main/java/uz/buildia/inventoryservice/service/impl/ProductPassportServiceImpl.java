package uz.buildia.inventoryservice.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.buildia.inventoryservice.constants.status.PassportStatus;
import uz.buildia.inventoryservice.dto.request.PassportHistorySetRequest;
import uz.buildia.inventoryservice.dto.request.ReportRequest;
import uz.buildia.inventoryservice.entity.ProductPassport;
import uz.buildia.inventoryservice.mapper.dto.PassportStageHistoryMapper;
import uz.buildia.inventoryservice.dto.common.ProductPassportDto;
import uz.buildia.inventoryservice.mapper.dto.ProductPassportMapper;
import uz.buildia.inventoryservice.repository.ProductPassportRepository;
import uz.buildia.inventoryservice.service.PassportStageHistoryService;
import uz.buildia.inventoryservice.service.ProductPassportService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPassportServiceImpl implements ProductPassportService {

  private final PassportStageHistoryService passportStageHistoryService;
  private final PassportStageHistoryMapper passportStageHistoryMapper;
  private final ProductPassportMapper productPassportMapper;
  private final ProductPassportRepository productPassportRepository;

  @Override
  public ProductPassportDto report(ReportRequest reportRequest, String scannerId) {
    ProductPassport productPassport;
    if (isPassportOpen(reportRequest.qrId())) {
      if (passportStageHistoryService.isClosePassport(reportRequest.qrId())) {
        productPassport = closePassport(reportRequest, scannerId);
      } else {
        productPassport = updatePassport(reportRequest, scannerId);
      }
    } else {
      productPassport = createNewPassport(reportRequest, scannerId);
    }
    return productPassportMapper.toDto(productPassport);
  }

  // TODO: 7/26/2024 implement
  @Override
  public String generateNewPassportId() {
    return null;
  }

  // TODO: 7/26/2024 implement
  private ProductPassport joinPassports() {
    return null;
  }

  // TODO: 7/26/2024 move to history table
  private ProductPassport closePassport(ReportRequest reportRequest, String scannerId) {
    ProductPassport productPassport = updatePassport(reportRequest, scannerId);
    productPassport.setPassportStatus(PassportStatus.CLOSED);
    productPassport.setHandOveredAt(LocalDateTime.now());

    return productPassportRepository.save(productPassport);
  }

  private ProductPassport createNewPassport(ReportRequest reportRequest, String scannerId) {
    ProductPassport productPassport =
        productPassportRepository.save(
            ProductPassport.builder()
                .passportStatus(PassportStatus.OPEN)
                .qrId(reportRequest.qrId())
                .createdAt(LocalDateTime.now())
                .model(reportRequest.model())
                .isMain(reportRequest.isMain())
                .build());

    PassportHistorySetRequest passportHistorySetRequest =
        PassportHistorySetRequest.builder()
            .productPassportDto(productPassportMapper.toDto(productPassport))
            .scannerId(scannerId)
            .build();

    passportStageHistoryMapper.toEntity(
        passportStageHistoryService.save(passportHistorySetRequest));

    return productPassportRepository.save(productPassport);
  }

  private ProductPassport updatePassport(ReportRequest reportRequest, String scannerId) {
    return productPassportRepository
        .findById(reportRequest.qrId())
        .map(
            productPassport -> {
              passportStageHistoryService.save(
                  PassportHistorySetRequest.builder()
                      .productPassportDto(productPassportMapper.toDto(productPassport))
                      .scannerId(scannerId)
                      .build());
              productPassport.setUpdatedAt(LocalDateTime.now());
              return productPassportRepository.save(productPassport);
            })
        .orElseThrow(() -> new RuntimeException("Product passport not found"));
  }

  private boolean isPassportOpen(String qrId) {
    return productPassportRepository.existsByQrIdAndPassportStatus(qrId, PassportStatus.OPEN);
  }
}
