package com.mg.pingpong.controller;

import com.mg.pingpong.entity.Equipment;
import com.mg.pingpong.entity.Player;
import com.mg.pingpong.repository.EquipmentRepository;
import com.mg.pingpong.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;
    private final PlayerRepository playerRepository;

    // 목록 조회
    @GetMapping("/equipment")
    public String list(Model model) {
        List<Equipment> equipmentList = equipmentRepository.findAllByOrderByPlayerNameAsc();
        List<Player> players = playerRepository.findAll();

        // 아직 용품 미등록 선수 목록
        List<String> registeredNames = equipmentList.stream()
                .map(Equipment::getPlayerName).toList();
        List<Player> unregisteredPlayers = players.stream()
                .filter(p -> !registeredNames.contains(p.getName()))
                .toList();

        model.addAttribute("equipmentList", equipmentList);
        model.addAttribute("players", players);
        model.addAttribute("unregisteredPlayers", unregisteredPlayers);
        model.addAttribute("newEquipment", new Equipment());
        return "equipment";
    }

    // 추가
    @PostMapping("/equipment/save")
    public String save(@RequestParam String playerName,
                       @RequestParam String blade,
                       @RequestParam String frontRubber,
                       @RequestParam String backRubber,
                       @RequestParam(required = false) String memo) {

        // 이미 등록된 선수면 업데이트
        Equipment equipment = equipmentRepository.findByPlayerName(playerName)
                .orElse(new Equipment());

        equipment.setPlayerName(playerName);
        equipment.setBlade(blade);
        equipment.setFrontRubber(frontRubber);
        equipment.setBackRubber(backRubber);
        equipment.setMemo(memo);
        equipmentRepository.save(equipment);

        return "redirect:/equipment";
    }

    // 삭제
    @PostMapping("/equipment/delete/{id}")
    public String delete(@PathVariable Long id) {
        equipmentRepository.deleteById(id);
        return "redirect:/equipment";
    }

    // 수정 폼
    @GetMapping("/equipment/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow();
        List<Player> players = playerRepository.findAll();
        model.addAttribute("equipment", equipment);
        model.addAttribute("players", players);
        return "equipment-edit";
    }

    // 수정 저장
    @PostMapping("/equipment/edit/{id}")
    public String editSave(@PathVariable Long id,
                           @RequestParam String blade,
                           @RequestParam String frontRubber,
                           @RequestParam String backRubber,
                           @RequestParam(required = false) String memo) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow();
        equipment.setBlade(blade);
        equipment.setFrontRubber(frontRubber);
        equipment.setBackRubber(backRubber);
        equipment.setMemo(memo);
        equipmentRepository.save(equipment);
        return "redirect:/equipment";
    }
}