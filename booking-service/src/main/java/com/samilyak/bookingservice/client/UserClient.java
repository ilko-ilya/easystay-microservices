package com.samilyak.bookingservice.client;

import com.samilyak.bookingservice.config.FeignTracingConfig;
import com.samilyak.bookingservice.dto.client.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service",
        path = "/api/auth",
        contextId = "userClient")
public interface UserClient {

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable ("id") Long id);

    @GetMapping("/find-user-id")
    Long getUserIdByEmail(@RequestParam("email") String email);

}
