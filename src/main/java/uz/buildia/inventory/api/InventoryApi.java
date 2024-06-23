package uz.buildia.inventory.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class InventoryApi {

    @GetMapping("/dummy")
    public String getDummyData(@RequestParam String scannerId, @RequestParam String data) {
        System.out.println("Nodir is here");
        return String.format("%s %s", scannerId, data);
    }
}
