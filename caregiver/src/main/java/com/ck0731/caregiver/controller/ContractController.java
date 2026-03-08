package com.ck0731.caregiver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ck0731.caregiver.entity.Caregiver;
import com.ck0731.caregiver.entity.Contract;
import com.ck0731.caregiver.entity.Message;
import com.ck0731.caregiver.service.CaregiverService;
import com.ck0731.caregiver.service.ContractService;
import com.ck0731.caregiver.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CaregiverService caregiverService;

    // 客户发起签约请求
    @PostMapping("/request")
    public boolean requestContract(@RequestBody Contract contract) {
        contract.setStatus("pending");
        contract.setRequestTime(LocalDateTime.now());
        boolean success = contractService.save(contract);

        if (success) {
            // 创建消息通知护理员
            Message message = new Message();
            message.setSenderId(contract.getClientId());
            message.setReceiverId(contract.getCaregiverId());
            message.setMessageType("contract_request");
            message.setContractId(contract.getId());
            message.setContent("您收到一个新的签约请求");
            message.setIsRead(0);
            messageService.save(message);
        }

        return success;
    }

    // 护理员接受签约
    @PutMapping("/accept/{id}")
    public boolean acceptContract(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        if (contract != null && "pending".equals(contract.getStatus())) {
            contract.setStatus("in_service");
            contract.setAcceptTime(LocalDateTime.now());
            contract.setStartTime(LocalDateTime.now());
            boolean success = contractService.updateById(contract);

            if (success) {
                // 更新护理员状态为护理中
                Caregiver caregiver = caregiverService.getById(contract.getCaregiverId());
                if (caregiver != null) {
                    caregiver.setStatus("busy");
                    caregiverService.updateById(caregiver);
                }

                // 创建消息通知客户
                Message message = new Message();
                message.setSenderId(contract.getCaregiverId());
                message.setReceiverId(contract.getClientId());
                message.setMessageType("contract_accept");
                message.setContractId(contract.getId());
                message.setContent("护理员已接受您的签约请求");
                message.setIsRead(0);
                messageService.save(message);
            }

            return success;
        }
        return false;
    }

    // 护理员拒绝签约
    @PutMapping("/reject/{id}")
    public boolean rejectContract(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        if (contract != null && "pending".equals(contract.getStatus())) {
            contract.setStatus("rejected");
            boolean success = contractService.updateById(contract);

            if (success) {
                // 创建消息通知客户
                Message message = new Message();
                message.setSenderId(contract.getCaregiverId());
                message.setReceiverId(contract.getClientId());
                message.setMessageType("contract_reject");
                message.setContractId(contract.getId());
                message.setContent("护理员已拒绝您的签约请求");
                message.setIsRead(0);
                messageService.save(message);
            }

            return success;
        }
        return false;
    }

    // 护理员请求结束服务
    @PutMapping("/end-request/{id}")
    public boolean endRequestContract(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        if (contract != null && "in_service".equals(contract.getStatus())) {
            contract.setStatus("end_requested");
            contract.setEndRequestTime(LocalDateTime.now());
            boolean success = contractService.updateById(contract);

            if (success) {
                // 创建消息通知客户
                Message message = new Message();
                message.setSenderId(contract.getCaregiverId());
                message.setReceiverId(contract.getClientId());
                message.setMessageType("service_end_request");
                message.setContractId(contract.getId());
                message.setContent("护理员请求结束服务");
                message.setIsRead(0);
                messageService.save(message);
            }

            return success;
        }
        return false;
    }

    // 客户接受结束服务
    @PutMapping("/end-accept/{id}")
    public boolean endAcceptContract(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        if (contract != null && "end_requested".equals(contract.getStatus())) {
            contract.setStatus("completed");
            contract.setEndTime(LocalDateTime.now());
            boolean success = contractService.updateById(contract);

            if (success) {
                // 更新护理员状态为空闲
                Caregiver caregiver = caregiverService.getById(contract.getCaregiverId());
                if (caregiver != null) {
                    caregiver.setStatus("available");
                    caregiverService.updateById(caregiver);
                }

                // 创建消息通知护理员
                Message message = new Message();
                message.setSenderId(contract.getClientId());
                message.setReceiverId(contract.getCaregiverId());
                message.setMessageType("service_end_accept");
                message.setContractId(contract.getId());
                message.setContent("客户已确认服务结束");
                message.setIsRead(0);
                messageService.save(message);
            }

            return success;
        }
        return false;
    }

    // 客户拒绝结束服务
    @PutMapping("/end-reject/{id}")
    public boolean endRejectContract(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        if (contract != null && "end_requested".equals(contract.getStatus())) {
            contract.setStatus("in_service");
            boolean success = contractService.updateById(contract);

            if (success) {
                // 创建消息通知护理员
                Message message = new Message();
                message.setSenderId(contract.getClientId());
                message.setReceiverId(contract.getCaregiverId());
                message.setMessageType("service_end_reject");
                message.setContractId(contract.getId());
                message.setContent("客户拒绝结束服务");
                message.setIsRead(0);
                messageService.save(message);
            }

            return success;
        }
        return false;
    }

    // 获取护理员的签约列表
    @GetMapping("/caregiver/{caregiverId}")
    public List<Contract> getCaregiverContracts(@PathVariable Long caregiverId) {
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.eq("caregiver_id", caregiverId)
               .orderByDesc("create_time");
        return contractService.list(wrapper);
    }

    // 获取客户的签约列表
    @GetMapping("/client/{clientId}")
    public List<Contract> getClientContracts(@PathVariable Long clientId) {
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.eq("client_id", clientId)
               .orderByDesc("create_time");
        return contractService.list(wrapper);
    }

    // 根据ID获取签约详情
    @GetMapping("/{id}")
    public Contract getById(@PathVariable Long id) {
        return contractService.getById(id);
    }
}
