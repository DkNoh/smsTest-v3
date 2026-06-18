package com.example.sms.controller.sms;

import com.example.sms.dto.common.ApiResponse;
import com.example.sms.dto.common.PageResponseDTO;
import com.example.sms.dto.sms.SmsHistorySearchRequestDTO;
import com.example.sms.dto.sms.SmsHistoryUpdateRequestDTO;
import com.example.sms.service.sms.SmsHistoryService;
import com.example.sms.vo.sms.SmsHistoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sms/history")
public class SmsHistoryController {

    private final SmsHistoryService service;

    @GetMapping
    public String page() {
        return "sms/history";
    }

    @ResponseBody
    @GetMapping("/data")
    public ResponseEntity<ApiResponse<PageResponseDTO<SmsHistoryVO>>> getData(
            @ModelAttribute SmsHistorySearchRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(service.search(request)));
    }

    @ResponseBody
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody SmsHistoryUpdateRequestDTO request) {
        service.create(request);
        return ResponseEntity.ok(ApiResponse.success("등록되었습니다.", null));
    }

    @ResponseBody
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<String>> update(@Valid @RequestBody SmsHistoryUpdateRequestDTO request) {
        service.update(request);
        return ResponseEntity.ok(ApiResponse.success("수정되었습니다.", null));
    }

    @ResponseBody
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<String>> delete(@RequestParam Integer smsHistoryId) {
        service.delete(smsHistoryId);
        return ResponseEntity.ok(ApiResponse.success("삭제되었습니다.", null));
    }
}
