package uz.buildia.inventoryservice;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbScriptRunner implements CommandLineRunner {

    private final EntityManager entityManager;

    public DbScriptRunner(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        entityManager.createNativeQuery("INSERT INTO inventory_service.scanners (hr_id) VALUES ('SC_001')").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO inventory_service.scanners (hr_id) VALUES ('SC_002')").executeUpdate();

        entityManager.createNativeQuery("INSERT INTO inventory_service.stages (id, model, stage_name) VALUES (1, 'model_of_product', 'first')").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO inventory_service.stages (id, model, stage_name) VALUES (2, 'model_of_product', 'second')").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO inventory_service.stages (id, model, stage_name) VALUES (3, 'model_of_product', 'third')").executeUpdate();

        entityManager.createNativeQuery("UPDATE inventory_service.stages SET next_stage = 2 WHERE id = 1").executeUpdate();
        entityManager.createNativeQuery("UPDATE inventory_service.stages SET previous_stage = 1, next_stage = 3 WHERE id = 2").executeUpdate();
        entityManager.createNativeQuery("UPDATE inventory_service.stages SET previous_stage = 2 WHERE id = 3").executeUpdate();
    }
}
