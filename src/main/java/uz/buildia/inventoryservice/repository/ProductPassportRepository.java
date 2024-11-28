package uz.buildia.inventoryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.buildia.inventoryservice.constants.status.PassportStatus;
import uz.buildia.inventoryservice.entity.ProductPassport;

@Repository
public interface ProductPassportRepository extends JpaRepository<ProductPassport, String> {
	boolean existsByQrIdAndPassportStatus(String qrId, PassportStatus passportStatus);
}