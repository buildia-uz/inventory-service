package uz.buildia.inventory.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class InventoryApi {

    @GetMapping("/dummy")
    public String getDummyData(@RequestParam String scannedId, @RequestParam String data) {
        return String.format("%s %s", scannedId, data);
    }
}
