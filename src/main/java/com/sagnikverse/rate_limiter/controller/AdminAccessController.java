package com.sagnikverse.rate_limiter.controller;

import com.sagnikverse.rate_limiter.entity.AccessControlEntry;
import com.sagnikverse.rate_limiter.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/access")
@RequiredArgsConstructor
public class AdminAccessController {

    private final AccessControlService accessService;

    @PostMapping
    public AccessControlEntry add(@RequestBody AccessControlEntry entry) {
        return accessService.addEntry(entry);
    }

    @DeleteMapping("/{identifier}")
    public String remove(@PathVariable String identifier) {
        accessService.removeEntry(identifier);
        return "Removed " + identifier;
    }
}