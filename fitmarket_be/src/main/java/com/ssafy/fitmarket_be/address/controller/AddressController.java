package com.ssafy.fitmarket_be.address.controller;

import com.ssafy.fitmarket_be.address.dto.AddressCreateRequestDto;
import com.ssafy.fitmarket_be.address.dto.AddressResponseDto;
import com.ssafy.fitmarket_be.address.dto.AddressUpdateRequestDto;
import com.ssafy.fitmarket_be.address.service.AddressService;
import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/addresses")
public class AddressController {

  private final AddressService addressService;

  @GetMapping
  public ResponseEntity<List<AddressResponseDto>> findAddresses(
      @AuthenticationPrincipal(expression = "id") Long userId) {
    List<AddressResponseDto> addresses = this.addressService.findAddresses(userId);
    log.debug("address result: {}", addresses);
    return ResponseEntity.status(HttpStatus.OK).body(addresses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<AddressResponseDto> find(@AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable Long id) {
    AddressResponseDto address = this.addressService.find(userId, id);
    return ResponseEntity.status(HttpStatus.OK).body(address);
  }

  @PostMapping
  public ResponseEntity<Void> create(@AuthenticationPrincipal(expression = "id") Long userId,
      @Valid @RequestBody AddressCreateRequestDto request) {
    log.trace("create request: {}", request);
    Long addressId = this.addressService.create(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(URI.create("/addresses/" + addressId))
        .build();
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Void> update(@AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable Long id,
      @Valid @RequestBody AddressUpdateRequestDto request) {
    this.addressService.update(userId, id, request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PatchMapping("/{id}/main")
  public ResponseEntity<Void> setMain(@AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable Long id) {
    this.addressService.setMain(userId, id);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable Long id) {
    this.addressService.delete(userId, id);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
